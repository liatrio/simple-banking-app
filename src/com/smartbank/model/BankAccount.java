package com.smartbank.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all bank accounts
 */
public class BankAccount {
    // Static variable for generating unique account numbers
    private static int nextAccountNumber = 1000;
    
    // Account properties
    private final int accountNumber;
    protected double balance;
    private String holderName;
    private List<Transaction> transactionHistory;
    
    /**
     * Constructor for creating a new bank account
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     */
    public BankAccount(String holderName, double initialDeposit) {
        this.accountNumber = generateAccountNumber();
        this.holderName = holderName;
        this.balance = initialDeposit;
        this.transactionHistory = new ArrayList<>();
        
        // Record initial deposit as a transaction
        if (initialDeposit > 0) {
            addTransaction(initialDeposit, TransactionType.DEPOSIT);
        }
    }
    
    /**
     * Generates a unique account number
     * 
     * @return A unique account number
     */
    private static int generateAccountNumber() {
        return nextAccountNumber++;
    }
    
    /**
     * Deposits money into the account
     * 
     * @param amount The amount to deposit
     * @return true if the deposit was successful
     * @throws IllegalArgumentException if the amount is negative
     */
    public boolean deposit(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        balance += amount;
        addTransaction(amount, TransactionType.DEPOSIT);
        return true;
    }
    
    /**
     * Withdraws money from the account
     * 
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful
     * @throws IllegalArgumentException if the amount is negative
     * @throws InsufficientBalanceException if there are insufficient funds
     */
    public boolean withdraw(double amount) throws IllegalArgumentException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient funds for withdrawal");
        }
        
        balance -= amount;
        addTransaction(amount, TransactionType.WITHDRAWAL);
        return true;
    }
    
    /**
     * Adds a transaction to the account's history
     * 
     * @param amount The transaction amount
     * @param type The transaction type
     */
    protected void addTransaction(double amount, TransactionType type) {
        Transaction transaction = new Transaction(amount, type, LocalDateTime.now());
        transactionHistory.add(transaction);
    }
    
    /**
     * Displays the account details
     * 
     * @return A string containing the account details
     */
    public String displayAccountDetails() {
        return String.format("Account Number: %d\nHolder: %s\nBalance: $%.2f\nAccount Type: %s",
                accountNumber, holderName, balance, getClass().getSimpleName());
    }
    
    /**
     * Gets the transaction history
     * 
     * @return The list of transactions
     */
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
    
    // Getters and setters
    public int getAccountNumber() {
        return accountNumber;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public String getHolderName() {
        return holderName;
    }
    
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}
