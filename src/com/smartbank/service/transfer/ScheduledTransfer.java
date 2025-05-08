package com.smartbank.service.transfer;

import java.util.Date;

/**
 * A scheduled future transfer between accounts.
 */
public class ScheduledTransfer {
    private final long scheduledTransferId;
    private final long sourceAccountNumber;
    private final long targetAccountNumber;
    private final double amount;
    private final String description;
    private final Date scheduledDate;
    private final Date creationDate;
    private ScheduledTransferStatus status;
    
    /**
     * Create a new ScheduledTransfer.
     * 
     * @param scheduledTransferId The scheduled transfer ID
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The transfer amount
     * @param description The description
     * @param scheduledDate The date when the transfer should be executed
     * @param creationDate The date when the scheduled transfer was created
     * @param status The current status of the scheduled transfer
     */
    public ScheduledTransfer(long scheduledTransferId, long sourceAccountNumber, long targetAccountNumber,
                            double amount, String description, Date scheduledDate, Date creationDate,
                            ScheduledTransferStatus status) {
        this.scheduledTransferId = scheduledTransferId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.description = description;
        this.scheduledDate = new Date(scheduledDate.getTime());
        this.creationDate = new Date(creationDate.getTime());
        this.status = status;
    }
    
    /**
     * Get the scheduled transfer ID.
     * 
     * @return The scheduled transfer ID
     */
    public long getScheduledTransferId() {
        return scheduledTransferId;
    }
    
    /**
     * Get the source account number.
     * 
     * @return The source account number
     */
    public long getSourceAccountNumber() {
        return sourceAccountNumber;
    }
    
    /**
     * Get the target account number.
     * 
     * @return The target account number
     */
    public long getTargetAccountNumber() {
        return targetAccountNumber;
    }
    
    /**
     * Get the transfer amount.
     * 
     * @return The transfer amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Get the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the scheduled date.
     * 
     * @return The scheduled date
     */
    public Date getScheduledDate() {
        return new Date(scheduledDate.getTime());
    }
    
    /**
     * Get the creation date.
     * 
     * @return The creation date
     */
    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }
    
    /**
     * Get the status.
     * 
     * @return The status
     */
    public ScheduledTransferStatus getStatus() {
        return status;
    }
    
    /**
     * Set the status.
     * 
     * @param status The new status
     */
    public void setStatus(ScheduledTransferStatus status) {
        this.status = status;
    }
    
    /**
     * Check if the scheduled transfer is pending.
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return status == ScheduledTransferStatus.PENDING;
    }
    
    /**
     * Check if the scheduled transfer is completed.
     * 
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return status == ScheduledTransferStatus.COMPLETED;
    }
    
    /**
     * Check if the scheduled transfer is cancelled.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return status == ScheduledTransferStatus.CANCELLED;
    }
    
    /**
     * Check if the scheduled transfer failed.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return status == ScheduledTransferStatus.FAILED;
    }
    
    /**
     * Check if the scheduled transfer is due for execution.
     * 
     * @return true if due, false otherwise
     */
    public boolean isDue() {
        return isPending() && scheduledDate.before(new Date());
    }
    
    /**
     * Enumeration of scheduled transfer statuses.
     */
    public enum ScheduledTransferStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED
    }
}