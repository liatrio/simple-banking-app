package com.smartbank.service.interest;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.InterestCalculationRecordRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the InterestCalculationService interface.
 */
public class InterestCalculationServiceImpl implements InterestCalculationService {
    private static final Logger LOGGER = Logger.getLogger(InterestCalculationServiceImpl.class.getName());
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final InterestCalculationRecordRepository interestCalculationRecordRepository;
    
    // Singleton instance
    private static InterestCalculationServiceImpl instance;
    
    /**
     * Get the singleton instance of this service.
     * 
     * @return The InterestCalculationServiceImpl instance
     */
    public static synchronized InterestCalculationServiceImpl getInstance() {
        if (instance == null) {
            instance = new InterestCalculationServiceImpl();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private InterestCalculationServiceImpl() {
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.interestCalculationRecordRepository = RepositoryFactory.getInterestCalculationRecordRepository();
    }
    
    @Override
    public double accrueInterest(long accountNumber) throws Exception {
        // Get the account
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        
        // Check if accrual has already been done today
        LocalDate today = LocalDate.now();
        LocalDate lastAccrualLocalDate = savingsAccount.getLastInterestAccrualLocalDate();
        if (lastAccrualLocalDate != null && today.equals(lastAccrualLocalDate)) {
            LOGGER.info("Interest already accrued today for account: " + accountNumber);
            return 0.0;
        }
        
        // Calculate and accrue interest
        double dailyInterest = savingsAccount.accrueInterest();
        
        // Save the updated account
        accountRepository.update(savingsAccount);
        
        // Record the interest accrual
        if (dailyInterest > 0) {
            InterestCalculationRecord record = new InterestCalculationRecord(
                    accountNumber,
                    today,
                    savingsAccount.getBalance(),
                    savingsAccount.getInterestRate(),
                    dailyInterest,
                    InterestCalculationRecord.OperationType.ACCRUAL,
                    "Daily interest accrual"
            );
            interestCalculationRecordRepository.save(record);
        }
        
        LOGGER.info(String.format("Accrued daily interest of %.2f for account: %d", dailyInterest, accountNumber));
        return dailyInterest;
    }
    
    @Override
    public Map<Long, Double> accrueInterestForAllAccounts() {
        Map<Long, Double> results = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Get all eligible accounts
        List<SavingsAccount> eligibleAccounts = getAccountsEligibleForInterestAccrual(today);
        
        for (SavingsAccount account : eligibleAccounts) {
            try {
                double dailyInterest = accrueInterest(account.getAccountNumber());
                results.put(account.getAccountNumber(), dailyInterest);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error accruing interest for account: " + account.getAccountNumber(), e);
                results.put(account.getAccountNumber(), 0.0);
            }
        }
        
        LOGGER.info("Completed interest accrual for " + eligibleAccounts.size() + " accounts");
        return results;
    }
    
    @Override
    public double postInterest(long accountNumber) throws Exception {
        // Get the account
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        
        // Post interest to the account
        double postedInterest = savingsAccount.postInterest();
        
        if (postedInterest <= 0) {
            LOGGER.info("No interest to post for account: " + accountNumber);
            return 0.0;
        }
        
        // Save the updated account
        accountRepository.update(savingsAccount);
        
        // Record the interest transaction
        Transaction transaction = new Transaction(
                accountNumber,
                postedInterest,
                Transaction.Type.INTEREST,
                new Date(),
                "Interest payment"
        );
        transactionRepository.save(transaction);
        
        // Record the interest posting
        InterestCalculationRecord record = new InterestCalculationRecord(
                accountNumber,
                LocalDate.now(),
                savingsAccount.getBalance(),
                savingsAccount.getInterestRate(),
                postedInterest,
                InterestCalculationRecord.OperationType.POSTING,
                "Monthly interest posting"
        );
        interestCalculationRecordRepository.save(record);
        
        LOGGER.info(String.format("Posted interest of %.2f to account: %d", postedInterest, accountNumber));
        return postedInterest;
    }
    
    @Override
    public Map<Long, Double> postInterestForAllAccounts() {
        Map<Long, Double> results = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Get all eligible accounts
        List<SavingsAccount> eligibleAccounts = getAccountsEligibleForInterestPosting(today);
        
        for (SavingsAccount account : eligibleAccounts) {
            try {
                double postedInterest = postInterest(account.getAccountNumber());
                results.put(account.getAccountNumber(), postedInterest);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error posting interest for account: " + account.getAccountNumber(), e);
                results.put(account.getAccountNumber(), 0.0);
            }
        }
        
        LOGGER.info("Completed interest posting for " + eligibleAccounts.size() + " accounts");
        return results;
    }
    
    @Override
    public List<SavingsAccount> getAccountsEligibleForInterestAccrual(LocalDate date) {
        List<Account> allAccounts = accountRepository.findAll();
        
        return allAccounts.stream()
                .filter(account -> account instanceof SavingsAccount)
                .map(account -> (SavingsAccount) account)
                .filter(account -> {
                    // Accounts that haven't had interest accrued today
                    LocalDate lastAccrualDate = account.getLastInterestAccrualLocalDate();
                    return lastAccrualDate == null || !lastAccrualDate.equals(date);
                })
                .filter(account -> account.getBalance() >= account.getMinimumBalanceForInterest())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SavingsAccount> getAccountsEligibleForInterestPosting(LocalDate date) {
        List<Account> allAccounts = accountRepository.findAll();
        
        // Check if it's the last day of the month or first day of the month
        boolean isLastDayOfMonth = date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
        boolean isFirstDayOfMonth = date.getDayOfMonth() == 1;
        
        if (!isLastDayOfMonth && !isFirstDayOfMonth) {
            // Not a posting day
            return Collections.emptyList();
        }
        
        return allAccounts.stream()
                .filter(account -> account instanceof SavingsAccount)
                .map(account -> (SavingsAccount) account)
                .filter(account -> {
                    // Check if interest has already been posted this month
                    LocalDate lastPostingDate = account.getLastInterestPostingLocalDate();
                    if (lastPostingDate == null) {
                        return true;
                    }
                    return lastPostingDate.getMonth() != date.getMonth() || 
                           lastPostingDate.getYear() != date.getYear();
                })
                .filter(account -> account.getAccruedInterest() > 0)
                .collect(Collectors.toList());
    }
    
    @Override
    public SavingsAccount updateInterestRate(long accountNumber, double newInterestRate) throws Exception {
        if (newInterestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        double oldRate = savingsAccount.getInterestRate();
        savingsAccount.setInterestRate(newInterestRate);
        
        // Save the updated account
        savingsAccount = (SavingsAccount) accountRepository.update(savingsAccount);
        
        // Record the interest rate change
        InterestCalculationRecord record = new InterestCalculationRecord(
                accountNumber,
                LocalDate.now(),
                savingsAccount.getBalance(),
                newInterestRate,
                0.0,
                InterestCalculationRecord.OperationType.RATE_CHANGE,
                String.format("Interest rate changed from %.4f to %.4f", oldRate, newInterestRate)
        );
        interestCalculationRecordRepository.save(record);
        
        LOGGER.info(String.format("Updated interest rate to %.4f for account: %d", newInterestRate, accountNumber));
        return savingsAccount;
    }
    
    @Override
    public SavingsAccount updateCompoundingMethod(long accountNumber, SavingsAccount.CompoundingMethod compoundingMethod) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        savingsAccount.setCompoundingMethod(compoundingMethod);
        
        // Save the updated account
        savingsAccount = (SavingsAccount) accountRepository.update(savingsAccount);
        
        LOGGER.info(String.format("Updated compounding method to %s for account: %d", compoundingMethod, accountNumber));
        return savingsAccount;
    }
    
    @Override
    public SavingsAccount setMinimumBalanceForInterest(long accountNumber, double minimumBalance) throws Exception {
        if (minimumBalance < 0) {
            throw new IllegalArgumentException("Minimum balance cannot be negative");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        savingsAccount.setMinimumBalanceForInterest(minimumBalance);
        
        // Save the updated account
        savingsAccount = (SavingsAccount) accountRepository.update(savingsAccount);
        
        LOGGER.info(String.format("Updated minimum balance for interest to %.2f for account: %d", minimumBalance, accountNumber));
        return savingsAccount;
    }
    
    @Override
    public List<InterestCalculationRecord> getInterestCalculationHistory(long accountNumber, LocalDate startDate, LocalDate endDate) throws Exception {
        // Validate that the account exists and is a savings account
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        return interestCalculationRecordRepository.findByAccountNumberAndDateRange(accountNumber, startDate, endDate);
    }
    
    @Override
    public double calculateProjectedInterest(long accountNumber, int numDays) throws Exception {
        if (numDays <= 0) {
            throw new IllegalArgumentException("Number of days must be positive");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        
        double balance = savingsAccount.getBalance();
        if (balance < savingsAccount.getMinimumBalanceForInterest()) {
            return 0.0; // Balance doesn't meet minimum requirement
        }
        
        double dailyRate = savingsAccount.getDailyInterestRate();
        double projectedInterest = 0.0;
        
        // Simple projection (no compounding)
        if (savingsAccount.getCompoundingMethod() == SavingsAccount.CompoundingMethod.DAILY) {
            // Compound daily
            double runningBalance = balance;
            for (int i = 0; i < numDays; i++) {
                double dailyInterest = runningBalance * dailyRate;
                runningBalance += dailyInterest;
                projectedInterest += dailyInterest;
            }
        } else {
            // Non-compounded projection (simple interest)
            projectedInterest = balance * dailyRate * numDays;
        }
        
        return projectedInterest;
    }
    
    @Override
    public int runEndOfDayProcessing() {
        Map<Long, Double> results = accrueInterestForAllAccounts();
        return results.size();
    }
    
    @Override
    public int runEndOfMonthProcessing() {
        LocalDate today = LocalDate.now();
        boolean isLastDayOfMonth = today.equals(today.with(TemporalAdjusters.lastDayOfMonth()));
        
        if (!isLastDayOfMonth) {
            LOGGER.info("Not the last day of the month, skipping end-of-month processing");
            return 0;
        }
        
        Map<Long, Double> results = postInterestForAllAccounts();
        return results.size();
    }
}