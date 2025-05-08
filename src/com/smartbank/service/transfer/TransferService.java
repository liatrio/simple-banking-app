package com.smartbank.service.transfer;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;

import java.util.Date;
import java.util.List;

/**
 * Service interface for account-to-account transfers.
 */
public interface TransferService {
    
    /**
     * Transfer money between two accounts.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The amount to transfer
     * @param description The transfer description
     * @return A TransferResult containing details of the completed transfer
     * @throws TransferException If the transfer fails for any reason
     */
    TransferResult transfer(long sourceAccountNumber, long targetAccountNumber, double amount, String description) 
            throws TransferException;
    
    /**
     * Check if a transfer is possible between two accounts.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The amount to transfer
     * @return A ValidationResult indicating if the transfer is valid and any validation messages
     */
    ValidationResult validateTransfer(long sourceAccountNumber, long targetAccountNumber, double amount);
    
    /**
     * Calculate the fee for a transfer.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The amount to transfer
     * @return The fee amount
     */
    double calculateTransferFee(long sourceAccountNumber, long targetAccountNumber, double amount);
    
    /**
     * Get the daily transfer limit for an account.
     * 
     * @param accountNumber The account number
     * @return The daily transfer limit amount
     */
    double getDailyTransferLimit(long accountNumber);
    
    /**
     * Get the remaining daily transfer amount for an account.
     * 
     * @param accountNumber The account number
     * @return The remaining amount that can be transferred today
     */
    double getRemainingDailyTransferAmount(long accountNumber);
    
    /**
     * Get transfer history for a specific account.
     * 
     * @param accountNumber The account number
     * @return A list of transfer records involving the account
     */
    List<TransferRecord> getTransferHistory(long accountNumber);
    
    /**
     * Get transfer history for a specific account within a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of transfer records within the specified date range
     */
    List<TransferRecord> getTransferHistory(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Schedule a future transfer.
     * 
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The amount to transfer
     * @param description The transfer description
     * @param scheduledDate The date to execute the transfer
     * @return A ScheduledTransfer object representing the scheduled transfer
     * @throws TransferException If the scheduled transfer cannot be created
     */
    ScheduledTransfer scheduleTransfer(long sourceAccountNumber, long targetAccountNumber, 
                                        double amount, String description, Date scheduledDate)
            throws TransferException;
    
    /**
     * Get scheduled transfers for an account.
     * 
     * @param accountNumber The account number
     * @return A list of scheduled transfers for the account
     */
    List<ScheduledTransfer> getScheduledTransfers(long accountNumber);
    
    /**
     * Cancel a scheduled transfer.
     * 
     * @param transferId The ID of the scheduled transfer to cancel
     * @return true if the transfer was canceled, false otherwise
     */
    boolean cancelScheduledTransfer(long transferId);
}