package com.smartbank.service.impl;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.AccountService;

import java.util.Date;
import java.util.List;
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
    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByUsername(username);
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
    public boolean deleteAccount(long accountNumber) {
        return accountRepository.deleteById(accountNumber);
    }
}