package com.smartbank.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class representing a bank that manages multiple accounts
 */
public class Bank {
    private final List<BankAccount> accounts;
    private static Bank instance;
    
    /**
     * Private constructor for singleton pattern
     */
    private Bank() {
        accounts = new ArrayList<>();
    }
    
    /**
     * Gets the singleton instance of the bank
     * 
     * @return The bank instance
     */
    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }
    
    /**
     * Creates a new savings account
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     * @return The created savings account
     * @throws IllegalArgumentException if the initial deposit is less than the minimum balance
     */
    public SavingsAccount createSavingsAccount(String holderName, double initialDeposit) throws IllegalArgumentException {
        SavingsAccount account = new SavingsAccount(holderName, initialDeposit);
        accounts.add(account);
        return account;
    }
    
    /**
     * Creates a new credit account
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     * @return The created credit account
     */
    public CreditAccount createCreditAccount(String holderName, double initialDeposit) {
        CreditAccount account = new CreditAccount(holderName, initialDeposit);
        accounts.add(account);
        return account;
    }
    
    /**
     * Gets an account by its account number
     * 
     * @param accountNumber The account number
     * @return An Optional containing the account if found, or empty if not found
     */
    public Optional<BankAccount> getAccount(int accountNumber) {
        return accounts.stream()
                .filter(account -> account.getAccountNumber() == accountNumber)
                .findFirst();
    }
    
    /**
     * Gets all accounts
     * 
     * @return A list of all accounts
     */
    public List<BankAccount> getAllAccounts() {
        return new ArrayList<>(accounts);
    }
    
    /**
     * Gets all accounts of a specific type
     * 
     * @param accountClass The account class
     * @return A list of accounts of the specified type
     */
    public <T extends BankAccount> List<T> getAccountsByType(Class<T> accountClass) {
        List<T> result = new ArrayList<>();
        for (BankAccount account : accounts) {
            if (accountClass.isInstance(account)) {
                result.add(accountClass.cast(account));
            }
        }
        return result;
    }
}
