package com.smartbank.service;

import com.smartbank.model.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for transaction-related business logic.
 */
public interface TransactionService {
    
    /**
     * Create and record a new transaction.
     * @param accountNumber The account number
     * @param amount The transaction amount
     * @param type The transaction type (DEPOSIT or WITHDRAWAL)
     * @param description The transaction description
     * @return The created transaction
     */
    Transaction createTransaction(long accountNumber, double amount, Transaction.Type type, String description);
    
    /**
     * Update an existing transaction
     * @param transaction The transaction to update
     * @return The updated transaction
     */
    Transaction updateTransaction(Transaction transaction);
    
    /**
     * Get a transaction by its ID.
     * @param transactionId The transaction ID
     * @return An Optional containing the transaction if found, or empty if not found
     */
    Optional<Transaction> getTransactionById(long transactionId);
    
    /**
     * Get all transactions in the system.
     * @return A list of all transactions
     */
    List<Transaction> getAllTransactions();
    
    /**
     * Get transactions for a specific account.
     * @param accountNumber The account number
     * @return A list of transactions for the specified account
     */
    List<Transaction> getTransactionsByAccount(long accountNumber);
    
    /**
     * Get transactions of a specific type.
     * @param type The transaction type
     * @return A list of transactions of the specified type
     */
    List<Transaction> getTransactionsByType(Transaction.Type type);
    
    /**
     * Get transactions within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of transactions within the specified date range
     */
    List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate);
    
    /**
     * Get transactions for a specific account within a date range.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of transactions for the specified account within the specified date range
     */
    List<Transaction> getTransactionsByAccountAndDateRange(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get transactions for a specific account by type.
     * @param accountNumber The account number
     * @param type The transaction type
     * @return A list of transactions for the specified account of the specified type
     */
    List<Transaction> getTransactionsByAccountAndType(long accountNumber, Transaction.Type type);
    
    /**
     * Calculate the total deposits for a specific account.
     * @param accountNumber The account number
     * @return The total amount of deposits
     */
    double calculateTotalDeposits(long accountNumber);
    
    /**
     * Calculate the total withdrawals for a specific account.
     * @param accountNumber The account number
     * @return The total amount of withdrawals
     */
    double calculateTotalWithdrawals(long accountNumber);
}