package com.smartbank.service;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.InvestmentAccount;
import com.smartbank.model.User;

import java.util.List;
import java.util.Map;
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
     * Create a new checking account.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @return The created checking account
     */
    CheckingAccount createCheckingAccount(User accountHolder, double initialBalance);
    
    /**
     * Create a new checking account with custom parameters.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @param monthlyMaintenanceFee The monthly maintenance fee
     * @param minimumBalanceRequired The minimum balance required to avoid fees
     * @param overdraftProtection Whether overdraft protection is enabled
     * @param overdraftLimit The overdraft limit if protection is enabled
     * @return The created checking account
     */
    CheckingAccount createCheckingAccount(User accountHolder, double initialBalance, 
                                        double monthlyMaintenanceFee, double minimumBalanceRequired,
                                        boolean overdraftProtection, double overdraftLimit);
    
    /**
     * Create a new investment account.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @return The created investment account
     */
    InvestmentAccount createInvestmentAccount(User accountHolder, double initialBalance);
    
    /**
     * Create a new investment account with custom parameters.
     * @param accountHolder The account holder
     * @param initialBalance The initial balance
     * @param riskProfile The risk profile for the investment
     * @param investmentStrategy The investment strategy
     * @return The created investment account
     */
    InvestmentAccount createInvestmentAccount(User accountHolder, double initialBalance,
                                            InvestmentAccount.RiskProfile riskProfile,
                                            InvestmentAccount.InvestmentStrategy investmentStrategy);
    
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
     * Get accounts by user ID of account holder.
     * @param userId The user ID of the account holder
     * @return A list of accounts owned by the user with the given ID
     */
    List<Account> getAccountsByUser(String userId);
    
    /**
     * Get accounts by username of account holder.
     * @param username The username of the account holder
     * @return A list of accounts owned by the user with the given username
     */
    List<Account> getAccountsByUsername(String username);
    
    /**
     * Get account by account number.
     * @param accountNumber The account number
     * @return The account with the given number
     */
    Optional<Account> getAccount(long accountNumber);
    
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
     * Apply maintenance fee to a checking account.
     * @param accountNumber The account number
     * @return The amount of fee applied
     * @throws Exception If the account does not exist or is not a checking account
     */
    double applyMaintenanceFee(long accountNumber) throws Exception;
    
    /**
     * Apply transaction fees to a checking account.
     * @param accountNumber The account number
     * @return The amount of fees applied
     * @throws Exception If the account does not exist or is not a checking account
     */
    double applyTransactionFees(long accountNumber) throws Exception;
    
    /**
     * Update overdraft settings for a checking account.
     * @param accountNumber The account number
     * @param overdraftProtection Whether overdraft protection is enabled
     * @param overdraftLimit The overdraft limit if protection is enabled
     * @return The updated checking account
     * @throws Exception If the account does not exist or is not a checking account
     */
    CheckingAccount updateOverdraftSettings(long accountNumber, boolean overdraftProtection, double overdraftLimit) throws Exception;
    
    /**
     * Apply management fee to an investment account.
     * @param accountNumber The account number
     * @return The amount of fee applied
     * @throws Exception If the account does not exist or is not an investment account
     */
    double applyManagementFee(long accountNumber) throws Exception;
    
    /**
     * Rebalance the portfolio of an investment account.
     * @param accountNumber The account number
     * @return true if rebalancing was successful
     * @throws Exception If the account does not exist or is not an investment account
     */
    boolean rebalancePortfolio(long accountNumber) throws Exception;
    
    /**
     * Simulate market change for an investment account.
     * @param accountNumber The account number
     * @param marketChangePercent The percentage change in the market
     * @return The new account balance
     * @throws Exception If the account does not exist or is not an investment account
     */
    double simulateMarketChange(long accountNumber, double marketChangePercent) throws Exception;
    
    /**
     * Convert an account to a different type of account.
     * @param accountNumber The account number to convert
     * @param targetType The type to convert to ("Savings", "Credit", "Checking", "Investment")
     * @param additionalParams Additional parameters needed for the conversion
     * @return The converted account
     * @throws Exception If the account does not exist or if conversion is not supported
     */
    Account convertAccountType(long accountNumber, String targetType, Map<String, Object> additionalParams) throws Exception;
    
    /**
     * Delete an account by its account number.
     * @param accountNumber The account number
     * @return true if the account was deleted, false if it did not exist
     */
    boolean deleteAccount(long accountNumber);
}