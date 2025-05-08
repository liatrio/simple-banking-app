package com.smartbank.repository;

import com.smartbank.model.Account;
import com.smartbank.model.User;

import java.util.List;

/**
 * Repository interface for Account entity operations.
 */
public interface AccountRepository extends Repository<Account, Long> {
    
    /**
     * Find accounts by account holder.
     * @param user The account holder
     * @return A list of accounts belonging to the user
     */
    List<Account> findByAccountHolder(User user);
    
    /**
     * Find accounts by user object.
     * @param user The user object
     * @return A list of accounts belonging to the user
     */
    List<Account> findByUser(User user);
    
    /**
     * Find accounts by account holder's username.
     * @param username The username of the account holder
     * @return A list of accounts belonging to the user with the given username
     */
    List<Account> findByUsername(String username);
    
    /**
     * Find accounts with a balance greater than the specified amount.
     * @param minBalance The minimum balance
     * @return A list of accounts with a balance greater than the specified amount
     */
    List<Account> findByBalanceGreaterThan(double minBalance);
    
    /**
     * Find accounts with a balance less than the specified amount.
     * @param maxBalance The maximum balance
     * @return A list of accounts with a balance less than the specified amount
     */
    List<Account> findByBalanceLessThan(double maxBalance);
    
    /**
     * Find accounts by type.
     * @param accountType The account type
     * @return A list of accounts of the specified type
     */
    List<Account> findByType(String accountType);
    
    /**
     * Find an account by its account number.
     * @param accountNumber The account number
     * @return The account with the given number, or null if not found
     */
    Account findByAccountNumber(long accountNumber);
}