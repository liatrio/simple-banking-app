package com.smartbank.service.recurring;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Record of a recurring transaction execution.
 */
@Entity
@Table(name = "recurring_transaction_executions")
public class RecurringTransactionExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long executionId;
    
    @Column(nullable = false)
    private long recurringTransactionId;
    
    @Column
    private long transactionId; // ID of the created transaction (if successful)
    
    @Column(nullable = false)
    private LocalDateTime executionTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringTransactionResult.Status status;
    
    @Column
    private String message;
    
    /**
     * Default constructor for JPA.
     */
    protected RecurringTransactionExecution() {
    }
    
    /**
     * Create a new RecurringTransactionExecution.
     * 
     * @param executionId The execution ID
     * @param recurringTransactionId The recurring transaction ID
     * @param transactionId The ID of the created transaction (0 if not successful)
     * @param executionTime The execution time
     * @param status The execution status
     * @param message The execution message
     */
    public RecurringTransactionExecution(long executionId, long recurringTransactionId,
                                       long transactionId, LocalDateTime executionTime,
                                       RecurringTransactionResult.Status status, String message) {
        this.executionId = executionId;
        this.recurringTransactionId = recurringTransactionId;
        this.transactionId = transactionId;
        this.executionTime = executionTime;
        this.status = status;
        this.message = message;
    }
    
    /**
     * Create a RecurringTransactionExecution from a RecurringTransactionResult.
     * 
     * @param executionId The execution ID
     * @param result The execution result
     * @return A new RecurringTransactionExecution
     */
    public static RecurringTransactionExecution fromResult(long executionId, RecurringTransactionResult result) {
        long transactionId = result.getTransaction() != null ? result.getTransaction().getTransactionId() : 0;
        return new RecurringTransactionExecution(
                executionId,
                result.getRecurringTransactionId(),
                transactionId,
                result.getExecutionTime(),
                result.getStatus(),
                result.getMessage());
    }
    
    /**
     * Get the execution ID.
     * 
     * @return The execution ID
     */
    public long getExecutionId() {
        return executionId;
    }
    
    /**
     * Set the execution ID.
     * 
     * @param executionId The execution ID
     */
    public void setExecutionId(long executionId) {
        this.executionId = executionId;
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
     * Set the recurring transaction ID.
     * 
     * @param recurringTransactionId The recurring transaction ID
     */
    public void setRecurringTransactionId(long recurringTransactionId) {
        this.recurringTransactionId = recurringTransactionId;
    }
    
    /**
     * Get the transaction ID.
     * 
     * @return The transaction ID (0 if not successful)
     */
    public long getTransactionId() {
        return transactionId;
    }
    
    /**
     * Set the transaction ID.
     * 
     * @param transactionId The transaction ID
     */
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
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
     * Set the execution time.
     * 
     * @param executionTime The execution time
     */
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
    
    /**
     * Get the execution status.
     * 
     * @return The execution status
     */
    public RecurringTransactionResult.Status getStatus() {
        return status;
    }
    
    /**
     * Set the execution status.
     * 
     * @param status The execution status
     */
    public void setStatus(RecurringTransactionResult.Status status) {
        this.status = status;
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
     * Set the execution message.
     * 
     * @param message The execution message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Check if the execution was successful.
     * 
     * @return true if the execution was successful
     */
    public boolean isSuccessful() {
        return status == RecurringTransactionResult.Status.SUCCESS;
    }
}