package com.smartbank.service.recurring;

import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Result of executing a recurring transaction.
 */
public class RecurringTransactionResult {
    
    /**
     * Status of the recurring transaction execution.
     */
    public enum Status {
        SUCCESS,            // Transaction executed successfully
        INSUFFICIENT_FUNDS, // Not enough funds in source account
        ACCOUNT_NOT_FOUND,  // Source or target account not found
        INVALID_AMOUNT,     // Amount is invalid (e.g., negative)
        ACCOUNT_RESTRICTED, // Account is restricted from transactions
        SYSTEM_ERROR,       // System error during execution
        VALIDATION_ERROR    // Transaction failed validation
    }
    
    private final long recurringTransactionId;
    private final Status status;
    private final String message;
    private final LocalDateTime executionTime;
    private final Transaction transaction;  // The transaction created by this execution (if successful)
    private final LocalDate nextExecutionDate; // The next scheduled execution date
    private final int executionCount; // The total execution count (including this one) if successful
    
    private RecurringTransactionResult(Builder builder) {
        this.recurringTransactionId = builder.recurringTransactionId;
        this.status = builder.status;
        this.message = builder.message;
        this.executionTime = builder.executionTime;
        this.transaction = builder.transaction;
        this.nextExecutionDate = builder.nextExecutionDate;
        this.executionCount = builder.executionCount;
    }
    
    /**
     * Check if the execution was successful.
     * 
     * @return true if the execution was successful
     */
    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }
    
    /**
     * Get the recurring transaction ID.
     * 
     * @return The recurring transaction ID
     */
    public long getRecurringTransactionId() {
        return recurringTransactionId;
    }
    
    /**
     * Get the execution status.
     * 
     * @return The execution status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Get the execution message.
     * 
     * @return The execution message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the execution time.
     * 
     * @return The execution time
     */
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    /**
     * Get the transaction created by this execution (if successful).
     * 
     * @return The transaction, or null if the execution was not successful
     */
    public Transaction getTransaction() {
        return transaction;
    }
    
    /**
     * Get the next scheduled execution date.
     * 
     * @return The next scheduled execution date
     */
    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }
    
    /**
     * Get the total execution count (including this one) if successful.
     * 
     * @return The execution count
     */
    public int getExecutionCount() {
        return executionCount;
    }
    
    /**
     * Builder for RecurringTransactionResult.
     */
    public static class Builder {
        private final long recurringTransactionId;
        private final Status status;
        private String message;
        private LocalDateTime executionTime = LocalDateTime.now();
        private Transaction transaction;
        private LocalDate nextExecutionDate;
        private int executionCount;
        
        /**
         * Create a new Builder for a successful execution result.
         * 
         * @param recurringTransactionId The recurring transaction ID
         * @param transaction The created transaction
         * @param nextExecutionDate The next scheduled execution date
         * @param executionCount The total execution count
         * @return A Builder for a successful result
         */
        public static Builder success(long recurringTransactionId, Transaction transaction, 
                                    LocalDate nextExecutionDate, int executionCount) {
            Builder builder = new Builder(recurringTransactionId, Status.SUCCESS);
            builder.transaction = transaction;
            builder.nextExecutionDate = nextExecutionDate;
            builder.executionCount = executionCount;
            builder.message = "Transaction executed successfully";
            return builder;
        }
        
        /**
         * Create a new Builder for a failed execution result.
         * 
         * @param recurringTransactionId The recurring transaction ID
         * @param status The failure status
         * @param message The failure message
         * @return A Builder for a failed result
         */
        public static Builder failure(long recurringTransactionId, Status status, String message) {
            Builder builder = new Builder(recurringTransactionId, status);
            builder.message = message;
            return builder;
        }
        
        /**
         * Create a new Builder.
         * 
         * @param recurringTransactionId The recurring transaction ID
         * @param status The execution status
         */
        public Builder(long recurringTransactionId, Status status) {
            this.recurringTransactionId = recurringTransactionId;
            this.status = status;
        }
        
        /**
         * Set the execution message.
         * 
         * @param message The execution message
         * @return This Builder instance
         */
        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }
        
        /**
         * Set the execution time.
         * 
         * @param executionTime The execution time
         * @return This Builder instance
         */
        public Builder withExecutionTime(LocalDateTime executionTime) {
            this.executionTime = executionTime;
            return this;
        }
        
        /**
         * Set the transaction.
         * 
         * @param transaction The transaction
         * @return This Builder instance
         */
        public Builder withTransaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }
        
        /**
         * Set the next scheduled execution date.
         * 
         * @param nextExecutionDate The next scheduled execution date
         * @return This Builder instance
         */
        public Builder withNextExecutionDate(LocalDate nextExecutionDate) {
            this.nextExecutionDate = nextExecutionDate;
            return this;
        }
        
        /**
         * Set the execution count.
         * 
         * @param executionCount The execution count
         * @return This Builder instance
         */
        public Builder withExecutionCount(int executionCount) {
            this.executionCount = executionCount;
            return this;
        }
        
        /**
         * Build the RecurringTransactionResult.
         * 
         * @return The built RecurringTransactionResult
         */
        public RecurringTransactionResult build() {
            return new RecurringTransactionResult(this);
        }
    }
}