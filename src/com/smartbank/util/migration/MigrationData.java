package com.smartbank.util.migration;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container class for data being migrated.
 * Stores references to all entities and maintains mappings between them.
 */
public class MigrationData {
    // Lists of entities
    private final List<User> users = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    
    // Maps for quick lookups
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<Long, Account> accountsByNumber = new HashMap<>();
    private final Map<Long, List<Transaction>> transactionsByAccount = new HashMap<>();
    
    /**
     * Add a user to the migration data.
     * @param user The user to add
     */
    public void addUser(User user) {
        users.add(user);
        usersByUsername.put(user.getUsername(), user);
    }
    
    /**
     * Add an account to the migration data.
     * @param account The account to add
     */
    public void addAccount(Account account) {
        accounts.add(account);
        accountsByNumber.put(account.getAccountNumber(), account);
        // Add the account to its user's account list
        User owner = account.getAccountHolder();
        if (owner != null) {
            // Ensure we're using the user instance from our migration data
            User mappedUser = usersByUsername.get(owner.getUsername());
            if (mappedUser != null) {
                mappedUser.addAccount(account);
            }
        }
    }
    
    /**
     * Add a transaction to the migration data.
     * @param transaction The transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        
        // Add to account transaction map
        long accountNumber = transaction.getAccountNumber();
        transactionsByAccount.computeIfAbsent(accountNumber, k -> new ArrayList<>())
                .add(transaction);
    }
    
    // Getters
    public List<User> getUsers() {
        return users;
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public User getUserByUsername(String username) {
        return usersByUsername.get(username);
    }
    
    public Account getAccountByNumber(long accountNumber) {
        return accountsByNumber.get(accountNumber);
    }
    
    public List<Transaction> getTransactionsByAccount(long accountNumber) {
        return transactionsByAccount.getOrDefault(accountNumber, new ArrayList<>());
    }
    
    // Statistics
    public int getUserCount() {
        return users.size();
    }
    
    public int getAccountCount() {
        return accounts.size();
    }
    
    public int getTransactionCount() {
        return transactions.size();
    }
}