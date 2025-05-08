package com.smartbank.service.recurring;

import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for recurring transaction operations.
 */
public interface RecurringTransactionService {
    
    /**
     * Create a new recurring transaction.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number (can be 0 for non-transfer transactions)
     * @param amount The transaction amount
     * @param type The transaction type
     * @param description The transaction description
     * @param frequency The frequency of recurrence
     * @param startDate The start date for the recurring transaction
     * @param endDate The optional end date (null for indefinite)
     * @param occurrenceLimit The optional occurrence limit (null for indefinite)
     * @return The created recurring transaction
     * @throws Exception If the transaction cannot be created
     */
    RecurringTransaction createRecurringTransaction(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            Transaction.Type type, String description, RecurringTransaction.Frequency frequency,
            LocalDate startDate, LocalDate endDate, Integer occurrenceLimit) throws Exception;
    
    /**
     * Create a new recurring transaction with daily frequency.
     * 
     * @param sourceAccountNumber The source account number
     * @param amount The transaction amount
     * @param type The transaction type
     * @param description The transaction description
     * @param startDate The start date for the recurring transaction
     * @return The created recurring transaction
     * @throws Exception If the transaction cannot be created
     */
    RecurringTransaction createDailyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate) throws Exception;
    
    /**
     * Create a new recurring transaction with weekly frequency.
     * 
     * @param sourceAccountNumber The source account number
     * @param amount The transaction amount
     * @param type The transaction type
     * @param description The transaction description
     * @param startDate The start date for the recurring transaction
     * @param dayOfWeek The day of week (1=Monday, 7=Sunday)
     * @return The created recurring transaction
     * @throws Exception If the transaction cannot be created
     */
    RecurringTransaction createWeeklyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate, int dayOfWeek) throws Exception;
    
    /**
     * Create a new recurring transaction with monthly frequency.
     * 
     * @param sourceAccountNumber The source account number
     * @param amount The transaction amount
     * @param type The transaction type
     * @param description The transaction description
     * @param startDate The start date for the recurring transaction
     * @param dayOfMonth The day of month (1-31)
     * @return The created recurring transaction
     * @throws Exception If the transaction cannot be created
     */
    RecurringTransaction createMonthlyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate, int dayOfMonth) throws Exception;
    
    /**
     * Create a new recurring transfer between accounts.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The transfer amount
     * @param description The transfer description
     * @param frequency The frequency of recurrence
     * @param startDate The start date for the recurring transfer
     * @return The created recurring transaction
     * @throws Exception If the transfer cannot be created
     */
    RecurringTransaction createRecurringTransfer(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            String description, RecurringTransaction.Frequency frequency,
            LocalDate startDate) throws Exception;
    
    /**
     * Get a recurring transaction by ID.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return An Optional containing the recurring transaction if found, or empty if not found
     */
    Optional<RecurringTransaction> getRecurringTransactionById(long recurringTransactionId);
    
    /**
     * Get all recurring transactions.
     * 
     * @return A list of all recurring transactions
     */
    List<RecurringTransaction> getAllRecurringTransactions();
    
    /**
     * Get recurring transactions for a specific account.
     * 
     * @param accountNumber The account number
     * @return A list of recurring transactions for the specified account
     */
    List<RecurringTransaction> getRecurringTransactionsByAccount(long accountNumber);
    
    /**
     * Get recurring transactions by status.
     * 
     * @param status The recurring transaction status
     * @return A list of recurring transactions with the specified status
     */
    List<RecurringTransaction> getRecurringTransactionsByStatus(RecurringTransaction.Status status);
    
    /**
     * Get recurring transactions due for execution.
     * 
     * @param date The date to check for due transactions
     * @return A list of active recurring transactions due for execution
     */
    List<RecurringTransaction> getDueRecurringTransactions(LocalDate date);
    
    /**
     * Update a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @param amount The new amount (null to keep current value)
     * @param description The new description (null to keep current value)
     * @param frequency The new frequency (null to keep current value)
     * @param endDate The new end date (null to keep current value, use LocalDate.MAX to remove an existing end date)
     * @param occurrenceLimit The new occurrence limit (null to keep current value, use 0 to remove an existing limit)
     * @return The updated recurring transaction
     * @throws Exception If the transaction cannot be updated
     */
    RecurringTransaction updateRecurringTransaction(
            long recurringTransactionId, Double amount, String description,
            RecurringTransaction.Frequency frequency, LocalDate endDate, 
            Integer occurrenceLimit) throws Exception;
    
    /**
     * Pause a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return The updated recurring transaction
     * @throws Exception If the transaction cannot be paused
     */
    RecurringTransaction pauseRecurringTransaction(long recurringTransactionId) throws Exception;
    
    /**
     * Resume a paused recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return The updated recurring transaction
     * @throws Exception If the transaction cannot be resumed
     */
    RecurringTransaction resumeRecurringTransaction(long recurringTransactionId) throws Exception;
    
    /**
     * Cancel a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return The updated recurring transaction
     * @throws Exception If the transaction cannot be cancelled
     */
    RecurringTransaction cancelRecurringTransaction(long recurringTransactionId) throws Exception;
    
    /**
     * Delete a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return true if the recurring transaction was deleted, false if it did not exist
     */
    boolean deleteRecurringTransaction(long recurringTransactionId);
    
    /**
     * Execute a recurring transaction now.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return The transaction result
     * @throws Exception If the transaction cannot be executed
     */
    RecurringTransactionResult executeRecurringTransaction(long recurringTransactionId) throws Exception;
    
    /**
     * Process all due recurring transactions.
     * 
     * @return The number of transactions processed
     */
    int processDueRecurringTransactions();
    
    /**
     * Get the execution history for a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return A list of execution history records
     */
    List<RecurringTransactionExecution> getExecutionHistory(long recurringTransactionId);
    
    /**
     * Validate a recurring transaction.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The transaction amount
     * @param type The transaction type
     * @param frequency The frequency of recurrence
     * @return A ValidationResult indicating if the transaction is valid and any validation messages
     */
    ValidationResult validateRecurringTransaction(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            Transaction.Type type, RecurringTransaction.Frequency frequency);
}