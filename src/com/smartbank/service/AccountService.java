package com.smartbank.service;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for account-related business logic.
 */
public interface AccountService {
    
    /**
     * Create a new savings account.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @param interestRate The interest rate
     * @return The created savings account
     */
    SavingsAccount createSavingsAccount(User accountHolder, double initialBalance, double interestRate);
    
    /**
     * Create a new credit account.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @param creditLimit The credit limit
     * @return The created credit account
     */
    CreditAccount createCreditAccount(User accountHolder, double initialBalance, double creditLimit);
    
    /**
     * Get an account by its account number.
     * @param accountNumber The account number
     * @return An Optional containing the account if found, or empty if not found
     */
    Optional<Account> getAccountByNumber(long accountNumber);
    
    /**
     * Get all accounts in the system.
     * @return A list of all accounts
     */
    List<Account> getAllAccounts();
    
    /**
     * Get accounts owned by a specific user.
     * @param user The account holder
     * @return A list of accounts owned by the user
     */
    List<Account> getAccountsByUser(User user);
    
    /**
     * Get accounts by username of account holder.
     * @param username The username of the account holder
     * @return A list of accounts owned by the user with the given username
     */
    List<Account> getAccountsByUsername(String username);
    
    /**
     * Deposit money into an account.
     * @param accountNumber The account number
     * @param amount The amount to deposit
     * @param description Transaction description
     * @return The updated account
     * @throws Exception If the account does not exist or the amount is invalid
     */
    Account deposit(long accountNumber, double amount, String description) throws Exception;
    
    /**
     * Withdraw money from an account.
     * @param accountNumber The account number
     * @param amount The amount to withdraw
     * @param description Transaction description
     * @return The updated account
     * @throws Exception If the account does not exist, the amount is invalid, or there are insufficient funds
     */
    Account withdraw(long accountNumber, double amount, String description) throws Exception;
    
    /**
     * Apply interest to a savings account.
     * @param accountNumber The account number
     * @return The updated savings account
     * @throws Exception If the account does not exist or is not a savings account
     */
    SavingsAccount applyInterest(long accountNumber) throws Exception;
    
    /**
     * Update credit limit for a credit account.
     * @param accountNumber The account number
     * @param newCreditLimit The new credit limit
     * @return The updated credit account
     * @throws Exception If the account does not exist or is not a credit account
     */
    CreditAccount updateCreditLimit(long accountNumber, double newCreditLimit) throws Exception;
    
    /**
     * Delete an account by its account number.
     * @param accountNumber The account number
     * @return true if the account was deleted, false if it did not exist
     */
    boolean deleteAccount(long accountNumber);
}