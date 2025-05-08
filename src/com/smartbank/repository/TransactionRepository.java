package com.smartbank.repository;

import com.smartbank.model.Transaction;
import java.util.Date;
import java.util.List;

/**
 * Repository interface for Transaction entity operations.
 */
public interface TransactionRepository extends Repository<Transaction, Long> {
    
    /**
     * Find transactions by account number.
     * @param accountNumber The account number
     * @return A list of transactions for the specified account
     */
    List<Transaction> findByAccountNumber(long accountNumber);
    
    /**
     * Find transactions by transaction type.
     * @param type The transaction type
     * @return A list of transactions of the specified type
     */
    List<Transaction> findByType(Transaction.Type type);
    
    /**
     * Find transactions within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of transactions within the specified date range
     */
    List<Transaction> findByDateRange(Date startDate, Date endDate);
    
    /**
     * Find transactions by account number and transaction type.
     * @param accountNumber The account number
     * @param type The transaction type
     * @return A list of transactions for the specified account and of the specified type
     */
    List<Transaction> findByAccountNumberAndType(long accountNumber, Transaction.Type type);
    
    /**
     * Find transactions for an account within a date range.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of transactions for the specified account within the specified date range
     */
    List<Transaction> findByAccountNumberAndDateRange(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Find the most recent transactions for an account of a specific type.
     * @param accountNumber The account number
     * @param type The transaction type
     * @param limit The maximum number of transactions to return
     * @return A list of recent transactions for the specified account and type
     */
    List<Transaction> findRecentByAccountAndType(long accountNumber, Transaction.Type type, int limit);
}