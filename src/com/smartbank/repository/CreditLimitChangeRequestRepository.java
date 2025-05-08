package com.smartbank.repository;

import com.smartbank.model.CreditLimitChangeRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for CreditLimitChangeRequest entity operations.
 */
public interface CreditLimitChangeRequestRepository extends Repository<CreditLimitChangeRequest, Long> {
    
    /**
     * Find credit limit change requests by account number.
     * @param accountNumber The account number
     * @return A list of change requests for the specified account
     */
    List<CreditLimitChangeRequest> findByAccountNumber(long accountNumber);
    
    /**
     * Find credit limit change requests by status.
     * @param status The status of the change requests
     * @return A list of change requests with the specified status
     */
    List<CreditLimitChangeRequest> findByStatus(CreditLimitChangeRequest.Status status);
    
    /**
     * Find credit limit change requests by account number and status.
     * @param accountNumber The account number
     * @param status The status of the change requests
     * @return A list of change requests for the specified account with the specified status
     */
    List<CreditLimitChangeRequest> findByAccountNumberAndStatus(long accountNumber, CreditLimitChangeRequest.Status status);
    
    /**
     * Find credit limit change requests created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of change requests created within the specified date range
     */
    List<CreditLimitChangeRequest> findByRequestDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find credit limit change requests by requested user.
     * @param userId The ID of the user who requested the changes
     * @return A list of change requests requested by the specified user
     */
    List<CreditLimitChangeRequest> findByRequestedBy(String userId);
    
    /**
     * Find credit limit change requests by approved user.
     * @param userId The ID of the user who approved the changes
     * @return A list of change requests approved by the specified user
     */
    List<CreditLimitChangeRequest> findByApprovedBy(String userId);
    
    /**
     * Find credit limit change requests by rejected user.
     * @param userId The ID of the user who rejected the changes
     * @return A list of change requests rejected by the specified user
     */
    List<CreditLimitChangeRequest> findByRejectedBy(String userId);
    
    /**
     * Find credit limit change requests by source.
     * @param source The source of the change requests
     * @return A list of change requests from the specified source
     */
    List<CreditLimitChangeRequest> findBySource(CreditLimitChangeRequest.Source source);
    
    /**
     * Find credit limit change requests by source and status.
     * @param source The source of the change requests
     * @param status The status of the change requests
     * @return A list of change requests from the specified source with the specified status
     */
    List<CreditLimitChangeRequest> findBySourceAndStatus(CreditLimitChangeRequest.Source source, CreditLimitChangeRequest.Status status);
    
    /**
     * Find pending credit limit change requests that are older than a specified date.
     * @param date The date to compare against
     * @return A list of pending change requests created before the specified date
     */
    List<CreditLimitChangeRequest> findPendingRequestsOlderThan(LocalDateTime date);
    
    /**
     * Count credit limit change requests by account number and status.
     * @param accountNumber The account number
     * @param status The status of the change requests
     * @return The count of change requests for the specified account with the specified status
     */
    int countByAccountNumberAndStatus(long accountNumber, CreditLimitChangeRequest.Status status);
}