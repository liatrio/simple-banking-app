package com.smartbank.service.transfer;

import com.smartbank.model.Transaction;

import java.util.Date;

/**
 * Result of a transfer operation.
 */
public class TransferResult {
    private final long sourceAccountNumber;
    private final long targetAccountNumber;
    private final double transferAmount;
    private final double feeAmount;
    private final double sourceAccountBalanceAfter;
    private final double targetAccountBalanceAfter;
    private final Date timestamp;
    private final String description;
    private final Transaction sourceTransaction;
    private final Transaction targetTransaction;
    private final long transferId;
    
    /**
     * Create a new TransferResult.
     */
    public TransferResult(Builder builder) {
        this.sourceAccountNumber = builder.sourceAccountNumber;
        this.targetAccountNumber = builder.targetAccountNumber;
        this.transferAmount = builder.transferAmount;
        this.feeAmount = builder.feeAmount;
        this.sourceAccountBalanceAfter = builder.sourceAccountBalanceAfter;
        this.targetAccountBalanceAfter = builder.targetAccountBalanceAfter;
        this.timestamp = builder.timestamp;
        this.description = builder.description;
        this.sourceTransaction = builder.sourceTransaction;
        this.targetTransaction = builder.targetTransaction;
        this.transferId = builder.transferId;
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
    public double getTransferAmount() {
        return transferAmount;
    }
    
    /**
     * Get the fee amount.
     * 
     * @return The fee amount
     */
    public double getFeeAmount() {
        return feeAmount;
    }
    
    /**
     * Get the total amount (transfer + fee).
     * 
     * @return The total amount
     */
    public double getTotalAmount() {
        return transferAmount + feeAmount;
    }
    
    /**
     * Get the source account balance after the transfer.
     * 
     * @return The source account balance after the transfer
     */
    public double getSourceAccountBalanceAfter() {
        return sourceAccountBalanceAfter;
    }
    
    /**
     * Get the target account balance after the transfer.
     * 
     * @return The target account balance after the transfer
     */
    public double getTargetAccountBalanceAfter() {
        return targetAccountBalanceAfter;
    }
    
    /**
     * Get the timestamp of the transfer.
     * 
     * @return The timestamp
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }
    
    /**
     * Get the transfer description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the source transaction.
     * 
     * @return The source transaction
     */
    public Transaction getSourceTransaction() {
        return sourceTransaction;
    }
    
    /**
     * Get the target transaction.
     * 
     * @return The target transaction
     */
    public Transaction getTargetTransaction() {
        return targetTransaction;
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
     * Builder for TransferResult.
     */
    public static class Builder {
        private long sourceAccountNumber;
        private long targetAccountNumber;
        private double transferAmount;
        private double feeAmount;
        private double sourceAccountBalanceAfter;
        private double targetAccountBalanceAfter;
        private Date timestamp;
        private String description;
        private Transaction sourceTransaction;
        private Transaction targetTransaction;
        private long transferId;
        
        public Builder() {
            this.timestamp = new Date();
            this.feeAmount = 0.0;
            this.description = "";
        }
        
        public Builder sourceAccountNumber(long sourceAccountNumber) {
            this.sourceAccountNumber = sourceAccountNumber;
            return this;
        }
        
        public Builder targetAccountNumber(long targetAccountNumber) {
            this.targetAccountNumber = targetAccountNumber;
            return this;
        }
        
        public Builder transferAmount(double transferAmount) {
            this.transferAmount = transferAmount;
            return this;
        }
        
        public Builder feeAmount(double feeAmount) {
            this.feeAmount = feeAmount;
            return this;
        }
        
        public Builder sourceAccountBalanceAfter(double sourceAccountBalanceAfter) {
            this.sourceAccountBalanceAfter = sourceAccountBalanceAfter;
            return this;
        }
        
        public Builder targetAccountBalanceAfter(double targetAccountBalanceAfter) {
            this.targetAccountBalanceAfter = targetAccountBalanceAfter;
            return this;
        }
        
        public Builder timestamp(Date timestamp) {
            this.timestamp = new Date(timestamp.getTime());
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder sourceTransaction(Transaction sourceTransaction) {
            this.sourceTransaction = sourceTransaction;
            return this;
        }
        
        public Builder targetTransaction(Transaction targetTransaction) {
            this.targetTransaction = targetTransaction;
            return this;
        }
        
        public Builder transferId(long transferId) {
            this.transferId = transferId;
            return this;
        }
        
        public TransferResult build() {
            return new TransferResult(this);
        }
    }
}