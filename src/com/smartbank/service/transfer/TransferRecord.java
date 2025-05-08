package com.smartbank.service.transfer;

import java.util.Date;

/**
 * Record of a transfer between accounts.
 */
public class TransferRecord {
    private final long transferId;
    private final long sourceAccountNumber;
    private final long targetAccountNumber;
    private final double amount;
    private final double fee;
    private final Date timestamp;
    private final String description;
    private final boolean isOutgoing;
    
    /**
     * Create a new TransferRecord.
     * 
     * @param transferId The transfer ID
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The transfer amount
     * @param fee The fee amount
     * @param timestamp The timestamp
     * @param description The description
     * @param isOutgoing Whether this is an outgoing transfer (from the perspective of the viewer)
     */
    public TransferRecord(long transferId, long sourceAccountNumber, long targetAccountNumber, 
                        double amount, double fee, Date timestamp, String description, boolean isOutgoing) {
        this.transferId = transferId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.fee = fee;
        this.timestamp = new Date(timestamp.getTime());
        this.description = description;
        this.isOutgoing = isOutgoing;
    }
    
    /**
     * Get the transfer ID.
     * 
     * @return The transfer ID
     */
    public long getTransferId() {
        return transferId;
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
     * Get the fee amount.
     * 
     * @return The fee amount
     */
    public double getFee() {
        return fee;
    }
    
    /**
     * Get the total amount (transfer + fee).
     * 
     * @return The total amount
     */
    public double getTotalAmount() {
        return amount + fee;
    }
    
    /**
     * Get the timestamp.
     * 
     * @return The timestamp
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
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
     * Check if this is an outgoing transfer.
     * 
     * @return true if outgoing, false if incoming
     */
    public boolean isOutgoing() {
        return isOutgoing;
    }
    
    /**
     * Get the counterparty account number.
     * 
     * @return The account number of the other party in the transfer
     */
    public long getCounterpartyAccountNumber() {
        return isOutgoing ? targetAccountNumber : sourceAccountNumber;
    }
}