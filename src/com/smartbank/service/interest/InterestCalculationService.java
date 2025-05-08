package com.smartbank.service.interest;

import com.smartbank.model.SavingsAccount;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling interest calculations on savings accounts.
 */
public interface InterestCalculationService {
    
    /**
     * Accrue daily interest for a single savings account.
     * This updates the accrued interest amount but does not modify the account balance.
     * 
     * @param accountNumber The account number of the savings account
     * @return The amount of interest accrued
     * @throws Exception If an error occurs during interest accrual
     */
    double accrueInterest(long accountNumber) throws Exception;
    
    /**
     * Accrue daily interest for all eligible savings accounts.
     * This updates the accrued interest amounts but does not modify account balances.
     * 
     * @return A map of account numbers to accrued interest amounts
     */
    Map<Long, Double> accrueInterestForAllAccounts();
    
    /**
     * Post accrued interest to a savings account balance.
     * This adds the accrued interest to the account balance and creates a transaction record.
     * 
     * @param accountNumber The account number of the savings account
     * @return The amount of interest posted
     * @throws Exception If an error occurs during interest posting
     */
    double postInterest(long accountNumber) throws Exception;
    
    /**
     * Post accrued interest to all eligible savings accounts.
     * This adds the accrued interest to account balances and creates transaction records.
     * 
     * @return A map of account numbers to posted interest amounts
     */
    Map<Long, Double> postInterestForAllAccounts();
    
    /**
     * Get savings accounts eligible for interest accrual on the specified date.
     * 
     * @param date The date to check for eligibility
     * @return A list of eligible savings accounts
     */
    List<SavingsAccount> getAccountsEligibleForInterestAccrual(LocalDate date);
    
    /**
     * Get savings accounts eligible for interest posting on the specified date.
     * 
     * @param date The date to check for eligibility
     * @return A list of eligible savings accounts
     */
    List<SavingsAccount> getAccountsEligibleForInterestPosting(LocalDate date);
    
    /**
     * Update the interest rate for a savings account.
     * 
     * @param accountNumber The account number of the savings account
     * @param newInterestRate The new interest rate (e.g., 0.05 for 5% APY)
     * @return The updated savings account
     * @throws Exception If an error occurs updating the interest rate
     */
    SavingsAccount updateInterestRate(long accountNumber, double newInterestRate) throws Exception;
    
    /**
     * Update the compounding method for a savings account.
     * 
     * @param accountNumber The account number of the savings account
     * @param compoundingMethod The new compounding method
     * @return The updated savings account
     * @throws Exception If an error occurs updating the compounding method
     */
    SavingsAccount updateCompoundingMethod(long accountNumber, SavingsAccount.CompoundingMethod compoundingMethod) throws Exception;
    
    /**
     * Set the minimum balance required to earn interest for a savings account.
     * 
     * @param accountNumber The account number of the savings account
     * @param minimumBalance The minimum balance required
     * @return The updated savings account
     * @throws Exception If an error occurs updating the minimum balance
     */
    SavingsAccount setMinimumBalanceForInterest(long accountNumber, double minimumBalance) throws Exception;
    
    /**
     * Get the interest calculation history for a savings account.
     * 
     * @param accountNumber The account number of the savings account
     * @param startDate The start date for history retrieval
     * @param endDate The end date for history retrieval
     * @return A list of interest calculation history records
     * @throws Exception If an error occurs retrieving the history
     */
    List<InterestCalculationRecord> getInterestCalculationHistory(long accountNumber, LocalDate startDate, LocalDate endDate) throws Exception;
    
    /**
     * Calculate the projected interest for a savings account.
     * 
     * @param accountNumber The account number of the savings account
     * @param numDays The number of days to project
     * @return The projected interest amount
     * @throws Exception If an error occurs calculating the projection
     */
    double calculateProjectedInterest(long accountNumber, int numDays) throws Exception;
    
    /**
     * Run end-of-day processing for interest calculations.
     * This typically accrues interest for all eligible accounts.
     * 
     * @return The number of accounts processed
     */
    int runEndOfDayProcessing();
    
    /**
     * Run end-of-month processing for interest calculations.
     * This typically posts interest for all eligible accounts.
     * 
     * @return The number of accounts processed
     */
    int runEndOfMonthProcessing();
}