package com.smartbank.repository;

import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for recurring transactions.
 */
public interface RecurringTransactionRepository {
    
    /**
     * Save a new recurring transaction.
     * 
     * @param recurringTransaction The recurring transaction to save
     * @return The saved recurring transaction
     */
    RecurringTransaction save(RecurringTransaction recurringTransaction);
    
    /**
     * Update an existing recurring transaction.
     * 
     * @param recurringTransaction The recurring transaction to update
     * @return The updated recurring transaction
     */
    RecurringTransaction update(RecurringTransaction recurringTransaction);
    
    /**
     * Find a recurring transaction by its ID.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return An Optional containing the recurring transaction if found, or empty if not found
     */
    Optional<RecurringTransaction> findById(long recurringTransactionId);
    
    /**
     * Get all recurring transactions.
     * 
     * @return A list of all recurring transactions
     */
    List<RecurringTransaction> findAll();
    
    /**
     * Find recurring transactions by source account number.
     * 
     * @param sourceAccountNumber The source account number
     * @return A list of recurring transactions for the specified source account
     */
    List<RecurringTransaction> findBySourceAccountNumber(long sourceAccountNumber);
    
    /**
     * Find recurring transactions by target account number.
     * 
     * @param targetAccountNumber The target account number
     * @return A list of recurring transactions for the specified target account
     */
    List<RecurringTransaction> findByTargetAccountNumber(long targetAccountNumber);
    
    /**
     * Find recurring transactions involving a specific account (as source or target).
     * 
     * @param accountNumber The account number
     * @return A list of recurring transactions involving the specified account
     */
    List<RecurringTransaction> findByAccountNumber(long accountNumber);
    
    /**
     * Find recurring transactions by type.
     * 
     * @param type The transaction type
     * @return A list of recurring transactions of the specified type
     */
    List<RecurringTransaction> findByType(Transaction.Type type);
    
    /**
     * Find recurring transactions by status.
     * 
     * @param status The recurring transaction status
     * @return A list of recurring transactions with the specified status
     */
    List<RecurringTransaction> findByStatus(RecurringTransaction.Status status);
    
    /**
     * Find recurring transactions due for execution.
     * 
     * @param date The date to check for due transactions
     * @return A list of active recurring transactions due for execution
     */
    List<RecurringTransaction> findDueTransactions(LocalDate date);
    
    /**
     * Find recurring transactions by frequency.
     * 
     * @param frequency The frequency
     * @return A list of recurring transactions with the specified frequency
     */
    List<RecurringTransaction> findByFrequency(RecurringTransaction.Frequency frequency);
    
    /**
     * Delete a recurring transaction by its ID.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return true if the recurring transaction was deleted, false if it did not exist
     */
    boolean deleteById(long recurringTransactionId);
}