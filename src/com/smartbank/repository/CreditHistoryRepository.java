package com.smartbank.repository;

import com.smartbank.model.CreditHistory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for CreditHistory entity operations.
 */
public interface CreditHistoryRepository extends Repository<CreditHistory, Long> {
    
    /**
     * Find credit history entries by account number.
     * @param accountNumber The account number
     * @return A list of credit history entries for the specified account
     */
    List<CreditHistory> findByAccountNumber(long accountNumber);
    
    /**
     * Find credit history entries by event type.
     * @param eventType The credit history event type
     * @return A list of credit history entries of the specified event type
     */
    List<CreditHistory> findByEventType(CreditHistory.EventType eventType);
    
    /**
     * Find credit history entries within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of credit history entries within the specified date range
     */
    List<CreditHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find credit history entries by account number and event type.
     * @param accountNumber The account number
     * @param eventType The credit history event type
     * @return A list of credit history entries for the specified account and of the specified event type
     */
    List<CreditHistory> findByAccountNumberAndEventType(long accountNumber, CreditHistory.EventType eventType);
    
    /**
     * Find credit history entries for an account within a date range.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of credit history entries for the specified account within the specified date range
     */
    List<CreditHistory> findByAccountNumberAndDateRange(long accountNumber, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find the most recent credit history entries for an account.
     * @param accountNumber The account number
     * @param limit The maximum number of entries to return
     * @return A list of recent credit history entries for the specified account
     */
    List<CreditHistory> findRecentByAccount(long accountNumber, int limit);
    
    /**
     * Find the most recent credit history entries for an account of a specific event type.
     * @param accountNumber The account number
     * @param eventType The credit history event type
     * @param limit The maximum number of entries to return
     * @return A list of recent credit history entries for the specified account and event type
     */
    List<CreditHistory> findRecentByAccountAndEventType(long accountNumber, CreditHistory.EventType eventType, int limit);
    
    /**
     * Count credit history entries by account number and event type.
     * @param accountNumber The account number
     * @param eventType The credit history event type
     * @return The count of credit history entries for the specified account and event type
     */
    int countByAccountNumberAndEventType(long accountNumber, CreditHistory.EventType eventType);
}