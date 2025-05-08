package com.smartbank.repository;

import com.smartbank.service.statement.StatementRecord;
import com.smartbank.service.statement.StatementType;

import java.util.Date;
import java.util.List;

/**
 * Repository interface for managing statement records.
 */
public interface StatementRepository extends Repository<StatementRecord, Long> {
    
    /**
     * Find statements by account number.
     * 
     * @param accountNumber The account number
     * @return List of statement records for the account
     */
    List<StatementRecord> findByAccountNumber(long accountNumber);
    
    /**
     * Find statements by account number and date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return List of statement records matching the criteria
     */
    List<StatementRecord> findByAccountNumberAndDateRange(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Find statements by user ID.
     * 
     * @param userId The user ID
     * @return List of statement records for the user
     */
    List<StatementRecord> findByUserId(String userId);
    
    /**
     * Find statements by type.
     * 
     * @param statementType The statement type
     * @return List of statement records of the specified type
     */
    List<StatementRecord> findByStatementType(StatementType statementType);
    
    /**
     * Find statements for which email delivery is scheduled but not yet completed.
     * 
     * @return List of statement records that need email delivery
     */
    List<StatementRecord> findPendingEmailDelivery();
    
    /**
     * Count statements by account number.
     * 
     * @param accountNumber The account number
     * @return The number of statements for the account
     */
    int countByAccountNumber(long accountNumber);
    
    /**
     * Find the most recent statement for an account.
     * 
     * @param accountNumber The account number
     * @return The most recent statement, or null if none exists
     */
    StatementRecord findMostRecentByAccountNumber(long accountNumber);
    
    /**
     * Delete old statements for an account, keeping only the most recent ones.
     * 
     * @param accountNumber The account number
     * @param keepCount The number of recent statements to keep
     * @return The number of statements deleted
     */
    int pruneOldStatements(long accountNumber, int keepCount);
}