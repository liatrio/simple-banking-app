package com.smartbank.service.impl;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.InvestmentAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.AccountService;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the AccountService interface.
 */
public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = Logger.getLogger(AccountServiceImpl.class.getName());
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    public AccountServiceImpl() {
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    @Override
    public SavingsAccount createSavingsAccount(User accountHolder, double initialBalance, double interestRate) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        SavingsAccount account = new SavingsAccount(accountHolder, initialBalance, interestRate);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created savings account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public CreditAccount createCreditAccount(User accountHolder, double initialBalance, double creditLimit) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        if (creditLimit < 0) {
            throw new IllegalArgumentException("Credit limit cannot be negative");
        }
        
        CreditAccount account = new CreditAccount(accountHolder, initialBalance, creditLimit);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created credit account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public CheckingAccount createCheckingAccount(User accountHolder, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        CheckingAccount account = new CheckingAccount(accountHolder, initialBalance);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created checking account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public CheckingAccount createCheckingAccount(User accountHolder, double initialBalance, 
                                               double monthlyMaintenanceFee, double minimumBalanceRequired, 
                                               boolean overdraftProtection, double overdraftLimit) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        if (monthlyMaintenanceFee < 0) {
            throw new IllegalArgumentException("Monthly maintenance fee cannot be negative");
        }
        
        if (minimumBalanceRequired < 0) {
            throw new IllegalArgumentException("Minimum balance required cannot be negative");
        }
        
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative");
        }
        
        CheckingAccount account = new CheckingAccount(accountHolder, initialBalance, 
                                                    monthlyMaintenanceFee, minimumBalanceRequired, 
                                                    overdraftProtection, overdraftLimit);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created custom checking account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public InvestmentAccount createInvestmentAccount(User accountHolder, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        InvestmentAccount account = new InvestmentAccount(accountHolder, initialBalance);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created investment account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public InvestmentAccount createInvestmentAccount(User accountHolder, double initialBalance,
                                                   InvestmentAccount.RiskProfile riskProfile,
                                                   InvestmentAccount.InvestmentStrategy investmentStrategy) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        InvestmentAccount account = new InvestmentAccount(accountHolder, initialBalance, riskProfile, investmentStrategy);
        accountRepository.save(account);
        
        // Record initial deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    account.getAccountNumber(),
                    initialBalance,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    "Initial deposit"));
        }
        
        LOGGER.info("Created custom investment account #" + account.getAccountNumber() + " for " + accountHolder.getUsername());
        return account;
    }
    
    @Override
    public Optional<Account> getAccountByNumber(long accountNumber) {
        return accountRepository.findById(accountNumber);
    }
    
    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    @Override
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByAccountHolder(user);
    }
    
    @Override
    public List<Account> getAccountsByUser(String userId) {
        return accountRepository.findByUsername(userId);
    }
    
    @Override
    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    
    @Override
    public Optional<Account> getAccount(long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        return Optional.ofNullable(account);
    }
    
    @Override
    public Account deposit(long accountNumber, double amount, String description) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        account.deposit(amount);
        
        // If it's a checking account, increment the number of monthly transactions
        if (account instanceof CheckingAccount) {
            ((CheckingAccount) account).incrementMonthlyTransactions();
        }
        
        account = accountRepository.update(account);
        
        transactionRepository.save(new Transaction(
                0, // Will be auto-generated
                accountNumber,
                amount,
                Transaction.Type.DEPOSIT,
                new Date(),
                description != null ? description : "Deposit"));
        
        LOGGER.info(String.format("Deposited %.2f to account #%d", amount, accountNumber));
        return account;
    }
    
    @Override
    public Account withdraw(long accountNumber, double amount, String description) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        account.withdraw(amount);
        
        // If it's a checking account, increment the number of monthly transactions
        if (account instanceof CheckingAccount) {
            ((CheckingAccount) account).incrementMonthlyTransactions();
        }
        
        account = accountRepository.update(account);
        
        transactionRepository.save(new Transaction(
                0, // Will be auto-generated
                accountNumber,
                amount,
                Transaction.Type.WITHDRAWAL,
                new Date(),
                description != null ? description : "Withdrawal"));
        
        LOGGER.info(String.format("Withdrew %.2f from account #%d", amount, accountNumber));
        return account;
    }
    
    @Override
    public SavingsAccount applyInterest(long accountNumber) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof SavingsAccount)) {
            throw new Exception("Account is not a savings account: " + accountNumber);
        }
        
        SavingsAccount savingsAccount = (SavingsAccount) account;
        double oldBalance = savingsAccount.getBalance();
        savingsAccount.applyInterest();
        double interestAmount = savingsAccount.getBalance() - oldBalance;
        
        savingsAccount = (SavingsAccount) accountRepository.update(savingsAccount);
        
        // Record interest transaction
        transactionRepository.save(new Transaction(
                0, // Will be auto-generated
                accountNumber,
                interestAmount,
                Transaction.Type.DEPOSIT,
                new Date(),
                "Interest applied"));
        
        LOGGER.info(String.format("Applied interest of %.2f to savings account #%d", interestAmount, accountNumber));
        return savingsAccount;
    }
    
    @Override
    public CreditAccount updateCreditLimit(long accountNumber, double newCreditLimit) throws Exception {
        if (newCreditLimit < 0) {
            throw new IllegalArgumentException("Credit limit cannot be negative");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof CreditAccount)) {
            throw new Exception("Account is not a credit account: " + accountNumber);
        }
        
        CreditAccount creditAccount = (CreditAccount) account;
        creditAccount.setCreditLimit(newCreditLimit);
        creditAccount = (CreditAccount) accountRepository.update(creditAccount);
        
        LOGGER.info(String.format("Updated credit limit to %.2f for credit account #%d", newCreditLimit, accountNumber));
        return creditAccount;
    }
    
    @Override
    public double applyMaintenanceFee(long accountNumber) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof CheckingAccount)) {
            throw new Exception("Account is not a checking account: " + accountNumber);
        }
        
        CheckingAccount checkingAccount = (CheckingAccount) account;
        double feeAmount = checkingAccount.applyMaintenanceFee();
        checkingAccount = (CheckingAccount) accountRepository.update(checkingAccount);
        
        if (feeAmount > 0) {
            // Record maintenance fee transaction
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    accountNumber,
                    feeAmount,
                    Transaction.Type.FEE,
                    new Date(),
                    "Monthly maintenance fee"));
            
            LOGGER.info(String.format("Applied maintenance fee of %.2f to checking account #%d", feeAmount, accountNumber));
        }
        
        return feeAmount;
    }
    
    @Override
    public double applyTransactionFees(long accountNumber) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof CheckingAccount)) {
            throw new Exception("Account is not a checking account: " + accountNumber);
        }
        
        CheckingAccount checkingAccount = (CheckingAccount) account;
        double feeAmount = checkingAccount.calculateTransactionFees();
        if (feeAmount > 0) {
            checkingAccount.withdraw(feeAmount);
            checkingAccount = (CheckingAccount) accountRepository.update(checkingAccount);
            
            // Record transaction fee transaction
            transactionRepository.save(new Transaction(
                    0, // Will be auto-generated
                    accountNumber,
                    feeAmount,
                    Transaction.Type.FEE,
                    new Date(),
                    "Transaction fees"));
            
            LOGGER.info(String.format("Applied transaction fees of %.2f to checking account #%d", feeAmount, accountNumber));
        }
        
        return feeAmount;
    }
    
    @Override
    public CheckingAccount updateOverdraftSettings(long accountNumber, boolean overdraftProtection, double overdraftLimit) throws Exception {
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative");
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof CheckingAccount)) {
            throw new Exception("Account is not a checking account: " + accountNumber);
        }
        
        CheckingAccount checkingAccount = (CheckingAccount) account;
        checkingAccount.setOverdraftProtection(overdraftProtection);
        checkingAccount.setOverdraftLimit(overdraftLimit);
        checkingAccount = (CheckingAccount) accountRepository.update(checkingAccount);
        
        LOGGER.info(String.format("Updated overdraft settings for checking account #%d: protection=%b, limit=%.2f", 
                                accountNumber, overdraftProtection, overdraftLimit));
        return checkingAccount;
    }
    
    @Override
    public double applyManagementFee(long accountNumber) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof InvestmentAccount)) {
            throw new Exception("Account is not an investment account: " + accountNumber);
        }
        
        InvestmentAccount investmentAccount = (InvestmentAccount) account;
        double feeAmount = investmentAccount.applyManagementFee();
        investmentAccount = (InvestmentAccount) accountRepository.update(investmentAccount);
        
        // Record management fee transaction
        transactionRepository.save(new Transaction(
                0, // Will be auto-generated
                accountNumber,
                feeAmount,
                Transaction.Type.FEE,
                new Date(),
                "Investment management fee"));
        
        LOGGER.info(String.format("Applied management fee of %.2f to investment account #%d", feeAmount, accountNumber));
        return feeAmount;
    }
    
    @Override
    public boolean rebalancePortfolio(long accountNumber) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof InvestmentAccount)) {
            throw new Exception("Account is not an investment account: " + accountNumber);
        }
        
        InvestmentAccount investmentAccount = (InvestmentAccount) account;
        boolean result = investmentAccount.rebalancePortfolio();
        investmentAccount = (InvestmentAccount) accountRepository.update(investmentAccount);
        
        if (result) {
            LOGGER.info(String.format("Rebalanced portfolio for investment account #%d", accountNumber));
        } else {
            LOGGER.warning(String.format("Failed to rebalance portfolio for investment account #%d", accountNumber));
        }
        
        return result;
    }
    
    @Override
    public double simulateMarketChange(long accountNumber, double marketChangePercent) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        if (!(account instanceof InvestmentAccount)) {
            throw new Exception("Account is not an investment account: " + accountNumber);
        }
        
        InvestmentAccount investmentAccount = (InvestmentAccount) account;
        double oldBalance = investmentAccount.getBalance();
        double newBalance = investmentAccount.simulateMarketChange(marketChangePercent);
        investmentAccount = (InvestmentAccount) accountRepository.update(investmentAccount);
        
        double changeAmount = newBalance - oldBalance;
        Transaction.Type transactionType = changeAmount >= 0 ? Transaction.Type.MARKET_GAIN : Transaction.Type.MARKET_LOSS;
        
        // Record market change transaction
        transactionRepository.save(new Transaction(
                accountNumber,
                Math.abs(changeAmount),
                transactionType,
                new Date(),
                String.format("Market change of %.2f%%", marketChangePercent)));
        
        LOGGER.info(String.format("Simulated market change of %.2f%% for investment account #%d, balance change: %.2f", 
                                marketChangePercent, accountNumber, changeAmount));
        
        return newBalance;
    }
    
    @Override
    public Account convertAccountType(long accountNumber, String targetType, Map<String, Object> additionalParams) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountNumber);
        if (!accountOpt.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account sourceAccount = accountOpt.get();
        User accountHolder = sourceAccount.getAccountHolder();
        double balance = sourceAccount.getBalance();
        Account targetAccount = null;
        
        switch (targetType) {
            case "Savings":
                double interestRate = additionalParams != null && additionalParams.containsKey("interestRate") 
                        ? (double) additionalParams.get("interestRate") : 0.01;
                targetAccount = new SavingsAccount(accountHolder, balance, interestRate);
                break;
                
            case "Credit":
                double creditLimit = additionalParams != null && additionalParams.containsKey("creditLimit") 
                        ? (double) additionalParams.get("creditLimit") : 1000.0;
                targetAccount = new CreditAccount(accountHolder, balance, creditLimit);
                break;
                
            case "Checking":
                if (additionalParams != null && additionalParams.containsKey("monthlyMaintenanceFee") &&
                        additionalParams.containsKey("minimumBalanceRequired") &&
                        additionalParams.containsKey("overdraftProtection") &&
                        additionalParams.containsKey("overdraftLimit")) {
                    
                    double monthlyMaintenanceFee = (double) additionalParams.get("monthlyMaintenanceFee");
                    double minimumBalanceRequired = (double) additionalParams.get("minimumBalanceRequired");
                    boolean overdraftProtection = (boolean) additionalParams.get("overdraftProtection");
                    double overdraftLimit = (double) additionalParams.get("overdraftLimit");
                    
                    targetAccount = new CheckingAccount(accountHolder, balance, monthlyMaintenanceFee,
                                                      minimumBalanceRequired, overdraftProtection, overdraftLimit);
                } else {
                    targetAccount = new CheckingAccount(accountHolder, balance);
                }
                break;
                
            case "Investment":
                if (additionalParams != null && additionalParams.containsKey("riskProfile") &&
                        additionalParams.containsKey("investmentStrategy")) {
                    
                    InvestmentAccount.RiskProfile riskProfile = (InvestmentAccount.RiskProfile) additionalParams.get("riskProfile");
                    InvestmentAccount.InvestmentStrategy investmentStrategy = (InvestmentAccount.InvestmentStrategy) additionalParams.get("investmentStrategy");
                    
                    targetAccount = new InvestmentAccount(accountHolder, balance, riskProfile, investmentStrategy);
                } else {
                    targetAccount = new InvestmentAccount(accountHolder, balance);
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported account type: " + targetType);
        }
        
        // Save the new account
        accountRepository.save(targetAccount);
        
        // Record conversion transaction in the new account
        transactionRepository.save(new Transaction(
                0, // Will be auto-generated
                targetAccount.getAccountNumber(),
                balance,
                Transaction.Type.ACCOUNT_CONVERSION,
                new Date(),
                "Converted from account #" + accountNumber));
        
        // Optionally delete the old account
        if (additionalParams != null && additionalParams.containsKey("deleteSource") &&
                (boolean) additionalParams.get("deleteSource")) {
            accountRepository.deleteById(accountNumber);
        }
        
        LOGGER.info(String.format("Converted account #%d to %s account #%d", accountNumber, targetType, targetAccount.getAccountNumber()));
        return targetAccount;
    }
    
    @Override
    public boolean deleteAccount(long accountNumber) {
        return accountRepository.deleteById(accountNumber);
    }
}