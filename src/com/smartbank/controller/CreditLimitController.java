package com.smartbank.controller;

import com.smartbank.model.CreditAccount;
import com.smartbank.model.CreditHistory;
import com.smartbank.model.CreditLimitChangeRequest;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.credit.CreditLimitEvaluationResult;
import com.smartbank.service.credit.CreditLimitService;
import com.smartbank.auth.SecurityContext;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing credit limit operations.
 */
public class CreditLimitController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(CreditLimitController.class.getName());
    
    private final CreditLimitService creditLimitService;
    
    /**
     * Constructor.
     */
    public CreditLimitController() {
        this.creditLimitService = ServiceFactory.getCreditLimitService();
    }
    
    /**
     * Evaluate account for potential credit limit adjustment.
     * 
     * @param accountNumber The account number
     * @return The evaluation result or null if an error occurs
     */
    public CreditLimitEvaluationResult evaluateCreditLimit(long accountNumber) {
        try {
            return creditLimitService.evaluateForCreditLimitAdjustment(accountNumber);
        } catch (Exception e) {
            handleException("Error evaluating credit limit for account " + accountNumber, e);
            return null;
        }
    }
    
    /**
     * Request a credit limit change.
     * 
     * @param accountNumber The account number
     * @param newCreditLimit The requested new credit limit
     * @param reason The reason for the change request
     * @return The ID of the created change request, or -1 if an error occurs
     */
    public long requestCreditLimitChange(long accountNumber, double newCreditLimit, String reason) {
        try {
            String userId = SecurityContext.getCurrentUser().getUsername();
            return creditLimitService.requestCreditLimitChange(accountNumber, newCreditLimit, userId, reason);
        } catch (Exception e) {
            handleException("Error requesting credit limit change for account " + accountNumber, e);
            return -1;
        }
    }
    
    /**
     * Approve a credit limit change request.
     * 
     * @param requestId The ID of the change request
     * @param comments Any comments related to the approval
     * @return The updated credit account, or null if an error occurs
     */
    public CreditAccount approveCreditLimitChange(long requestId, String comments) {
        try {
            String userId = SecurityContext.getCurrentUser().getUsername();
            return creditLimitService.approveCreditLimitChange(requestId, userId, comments);
        } catch (Exception e) {
            handleException("Error approving credit limit change request " + requestId, e);
            return null;
        }
    }
    
    /**
     * Reject a credit limit change request.
     * 
     * @param requestId The ID of the change request
     * @param reason The reason for rejection
     * @return True if the rejection was successful, false otherwise
     */
    public boolean rejectCreditLimitChange(long requestId, String reason) {
        try {
            String userId = SecurityContext.getCurrentUser().getUsername();
            return creditLimitService.rejectCreditLimitChange(requestId, userId, reason);
        } catch (Exception e) {
            handleException("Error rejecting credit limit change request " + requestId, e);
            return false;
        }
    }
    
    /**
     * Get all pending credit limit change requests.
     * 
     * @return A list of pending credit limit change requests
     */
    public List<CreditLimitChangeRequest> getPendingCreditLimitChangeRequests() {
        return creditLimitService.getPendingCreditLimitChangeRequests();
    }
    
    /**
     * Get credit limit change requests for a specific account.
     * 
     * @param accountNumber The account number
     * @return A list of credit limit change requests for the specified account
     */
    public List<CreditLimitChangeRequest> getCreditLimitChangeRequestsByAccount(long accountNumber) {
        return creditLimitService.getCreditLimitChangeRequestsByAccount(accountNumber);
    }
    
    /**
     * Get credit history entries for a specific account.
     * 
     * @param accountNumber The account number
     * @return A list of credit history entries for the specified account
     */
    public List<CreditHistory> getCreditHistoryByAccount(long accountNumber) {
        return creditLimitService.getCreditHistoryByAccount(accountNumber);
    }
    
    /**
     * Enable automatic credit limit reviews for an account.
     * 
     * @param accountNumber The account number
     * @return True if automatic reviews were successfully enabled, false otherwise
     */
    public boolean enableAutomaticCreditLimitReviews(long accountNumber) {
        try {
            return creditLimitService.enableAutomaticCreditLimitReviews(accountNumber);
        } catch (Exception e) {
            handleException("Error enabling automatic credit limit reviews for account " + accountNumber, e);
            return false;
        }
    }
    
    /**
     * Disable automatic credit limit reviews for an account.
     * 
     * @param accountNumber The account number
     * @return True if automatic reviews were successfully disabled, false otherwise
     */
    public boolean disableAutomaticCreditLimitReviews(long accountNumber) {
        try {
            return creditLimitService.disableAutomaticCreditLimitReviews(accountNumber);
        } catch (Exception e) {
            handleException("Error disabling automatic credit limit reviews for account " + accountNumber, e);
            return false;
        }
    }
    
    /**
     * Check if automatic credit limit reviews are enabled for an account.
     * 
     * @param accountNumber The account number
     * @return True if automatic reviews are enabled, false otherwise
     */
    public boolean isAutomaticCreditLimitReviewsEnabled(long accountNumber) {
        try {
            return creditLimitService.isAutomaticCreditLimitReviewsEnabled(accountNumber);
        } catch (Exception e) {
            handleException("Error checking automatic credit limit reviews status for account " + accountNumber, e);
            return false;
        }
    }
    
    /**
     * Run automatic credit limit reviews for all eligible accounts.
     * 
     * @return Number of accounts that had their credit limits adjusted
     */
    public int runAutomaticCreditLimitReviews() {
        return creditLimitService.runAutomaticCreditLimitReviews();
    }
    
    /**
     * Handle exceptions by logging them.
     * 
     * @param message The error message
     * @param e The exception
     */
    private void handleException(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
        showErrorAlert("Credit Limit Operation Error", message, e.getMessage());
    }
}