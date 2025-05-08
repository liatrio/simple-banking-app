package com.smartbank.repository;

import com.smartbank.service.recurring.RecurringTransactionExecution;
import com.smartbank.service.recurring.RecurringTransactionResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for recurring transaction executions.
 */
public interface RecurringTransactionExecutionRepository {
    
    /**
     * Save a new execution record.
     * 
     * @param execution The execution record to save
     * @return The saved execution record
     */
    RecurringTransactionExecution save(RecurringTransactionExecution execution);
    
    /**
     * Create a new execution record from an execution result.
     * 
     * @param result The execution result
     * @return The saved execution record
     */
    RecurringTransactionExecution createFromResult(RecurringTransactionResult result);
    
    /**
     * Find execution records for a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return A list of execution records
     */
    List<RecurringTransactionExecution> findByRecurringTransactionId(long recurringTransactionId);
    
    /**
     * Find execution records for a recurring transaction within a date range.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @param startDateTime The start date and time
     * @param endDateTime The end date and time
     * @return A list of execution records
     */
    List<RecurringTransactionExecution> findByRecurringTransactionIdAndDateRange(
            long recurringTransactionId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find execution records by status.
     * 
     * @param status The execution status
     * @return A list of execution records
     */
    List<RecurringTransactionExecution> findByStatus(RecurringTransactionResult.Status status);
    
    /**
     * Find execution records by transaction ID.
     * 
     * @param transactionId The transaction ID
     * @return A list of execution records
     */
    List<RecurringTransactionExecution> findByTransactionId(long transactionId);
    
    /**
     * Get execution counts by date and status.
     * 
     * @param date The date
     * @param status The execution status
     * @return The number of executions
     */
    int getExecutionCountByDateAndStatus(LocalDate date, RecurringTransactionResult.Status status);
    
    /**
     * Get execution counts by recurring transaction ID and status.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @param status The execution status
     * @return The number of executions
     */
    int getExecutionCountByRecurringTransactionIdAndStatus(
            long recurringTransactionId, RecurringTransactionResult.Status status);
    
    /**
     * Delete execution records for a recurring transaction.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return The number of records deleted
     */
    int deleteByRecurringTransactionId(long recurringTransactionId);
}