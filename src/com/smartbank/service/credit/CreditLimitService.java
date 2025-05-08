package com.smartbank.service.credit;

import com.smartbank.model.CreditAccount;
import com.smartbank.model.CreditHistory;
import com.smartbank.model.CreditLimitChangeRequest;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for credit limit and credit history management.
 */
public interface CreditLimitService {
    
    /**
     * Calculate credit score for a credit account based on transaction history and payment behavior.
     * @param accountNumber The account number
     * @return The calculated credit score
     * @throws Exception If the account does not exist or is not a credit account
     */
    int calculateCreditScore(long accountNumber) throws Exception;
    
    /**
     * Update credit score for a credit account.
     * @param accountNumber The account number
     * @return The updated credit account
     * @throws Exception If the account does not exist, is not a credit account, or update fails
     */
    CreditAccount updateCreditScore(long accountNumber) throws Exception;
    
    /**
     * Evaluate account for potential credit limit adjustment.
     * @param accountNumber The account number
     * @return The evaluation result containing recommendation and supporting data
     * @throws Exception If the account does not exist or is not a credit account
     */
    CreditLimitEvaluationResult evaluateForCreditLimitAdjustment(long accountNumber) throws Exception;
    
    /**
     * Automatically adjust credit limit based on account history and credit score.
     * @param accountNumber The account number
     * @return The updated credit account with the new limit, or unchanged if no adjustment is recommended
     * @throws Exception If the account does not exist, is not a credit account, or adjustment fails
     */
    CreditAccount autoAdjustCreditLimit(long accountNumber) throws Exception;
    
    /**
     * Request a manual credit limit change that requires approval.
     * @param accountNumber The account number
     * @param newCreditLimit The requested new credit limit
     * @param requestedBy The ID of the user requesting the change
     * @param reason The reason for the credit limit change
     * @return The ID of the created change request
     * @throws Exception If the account does not exist, is not a credit account, or request creation fails
     */
    long requestCreditLimitChange(long accountNumber, double newCreditLimit, String requestedBy, String reason) throws Exception;
    
    /**
     * Approve a credit limit change request.
     * @param requestId The ID of the change request
     * @param approvedBy The ID of the user approving the request
     * @param comments Any comments related to the approval
     * @return The updated credit account with the new limit
     * @throws Exception If the request does not exist, approval fails, or account update fails
     */
    CreditAccount approveCreditLimitChange(long requestId, String approvedBy, String comments) throws Exception;
    
    /**
     * Reject a credit limit change request.
     * @param requestId The ID of the change request
     * @param rejectedBy The ID of the user rejecting the request
     * @param reason The reason for rejection
     * @return True if the request was successfully rejected
     * @throws Exception If the request does not exist or rejection fails
     */
    boolean rejectCreditLimitChange(long requestId, String rejectedBy, String reason) throws Exception;
    
    /**
     * Get all pending credit limit change requests.
     * @return A list of pending change requests
     */
    List<CreditLimitChangeRequest> getPendingCreditLimitChangeRequests();
    
    /**
     * Get credit limit change requests for a specific account.
     * @param accountNumber The account number
     * @return A list of change requests for the specified account
     */
    List<CreditLimitChangeRequest> getCreditLimitChangeRequestsByAccount(long accountNumber);
    
    /**
     * Get a specific credit limit change request.
     * @param requestId The ID of the change request
     * @return An Optional containing the request if found, or empty if not found
     */
    Optional<CreditLimitChangeRequest> getCreditLimitChangeRequest(long requestId);
    
    /**
     * Add a credit history entry for a credit account.
     * @param accountNumber The account number
     * @param eventType The type of credit history event
     * @param description The description of the event
     * @return The created credit history entry
     * @throws Exception If the account does not exist, is not a credit account, or entry creation fails
     */
    CreditHistory addCreditHistoryEntry(long accountNumber, CreditHistory.EventType eventType, String description) throws Exception;
    
    /**
     * Get credit history entries for a specific account.
     * @param accountNumber The account number
     * @return A list of credit history entries for the specified account
     */
    List<CreditHistory> getCreditHistoryByAccount(long accountNumber);
    
    /**
     * Enable automatic credit limit reviews for an account.
     * @param accountNumber The account number
     * @return True if automatic reviews were successfully enabled
     * @throws Exception If the account does not exist or is not a credit account
     */
    boolean enableAutomaticCreditLimitReviews(long accountNumber) throws Exception;
    
    /**
     * Disable automatic credit limit reviews for an account.
     * @param accountNumber The account number
     * @return True if automatic reviews were successfully disabled
     * @throws Exception If the account does not exist or is not a credit account
     */
    boolean disableAutomaticCreditLimitReviews(long accountNumber) throws Exception;
    
    /**
     * Check if automatic credit limit reviews are enabled for an account.
     * @param accountNumber The account number
     * @return True if automatic reviews are enabled
     * @throws Exception If the account does not exist or is not a credit account
     */
    boolean isAutomaticCreditLimitReviewsEnabled(long accountNumber) throws Exception;
    
    /**
     * Run automatic credit limit reviews for all eligible accounts.
     * This would typically be scheduled to run periodically.
     * @return Number of accounts that had their credit limits adjusted
     */
    int runAutomaticCreditLimitReviews();
}