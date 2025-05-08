package com.smartbank.service.credit;

import com.smartbank.model.Account;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.CreditHistory;
import com.smartbank.model.CreditLimitChangeRequest;
import com.smartbank.model.Transaction;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.CreditHistoryRepository;
import com.smartbank.repository.CreditLimitChangeRequestRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the CreditLimitService interface.
 */
public class CreditLimitServiceImpl implements CreditLimitService {
    private static final Logger LOGGER = Logger.getLogger(CreditLimitServiceImpl.class.getName());
    
    // Credit score factors and weights
    private static final double PAYMENT_HISTORY_WEIGHT = 0.35;
    private static final double UTILIZATION_WEIGHT = 0.30;
    private static final double ACCOUNT_AGE_WEIGHT = 0.15;
    private static final double PAYMENT_BEHAVIOR_WEIGHT = 0.20;
    
    // Credit limit adjustment constants
    private static final int MIN_MONTHS_FOR_INCREASE = 6;
    private static final int MIN_SCORE_FOR_INCREASE = 700;
    private static final double MAX_INCREASE_PERCENTAGE = 25.0;
    private static final double MIN_INCREASE_AMOUNT = 500.0;
    private static final double MAX_INCREASE_AMOUNT = 10000.0;
    private static final int AUTO_REVIEW_PERIOD_MONTHS = 3;
    private static final int CREDIT_SCORE_UPDATE_PERIOD_DAYS = 30;
    
    // Repositories
    private final AccountRepository accountRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final CreditLimitChangeRequestRepository creditLimitChangeRequestRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Constructor with repository injection.
     */
    public CreditLimitServiceImpl() {
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.creditHistoryRepository = RepositoryFactory.getCreditHistoryRepository();
        this.creditLimitChangeRequestRepository = RepositoryFactory.getCreditLimitChangeRequestRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    /**
     * Constructor for dependency injection (primarily for testing).
     */
    public CreditLimitServiceImpl(
            AccountRepository accountRepository,
            CreditHistoryRepository creditHistoryRepository,
            CreditLimitChangeRequestRepository creditLimitChangeRequestRepository,
            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.creditLimitChangeRequestRepository = creditLimitChangeRequestRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public int calculateCreditScore(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        // Get account creation date
        LocalDateTime accountCreationDate = account.getCreationDateTime();
        long accountAgeInMonths = ChronoUnit.MONTHS.between(accountCreationDate, LocalDateTime.now());
        
        // Get payment history
        int onTimePayments = account.getNumberOfOnTimePayments();
        int latePayments = account.getNumberOfLatePayments();
        int totalPayments = onTimePayments + latePayments;
        
        // Calculate payment history score (range 0-100)
        double paymentHistoryScore;
        if (totalPayments == 0) {
            paymentHistoryScore = 70; // Neutral starting point if no payments
        } else {
            paymentHistoryScore = (double) onTimePayments / totalPayments * 100;
        }
        
        // Calculate utilization ratio (how much of the credit limit is being used)
        double balance = account.getBalance();
        double creditLimit = account.getCreditLimit();
        double utilizationRatio = balance < 0 ? Math.abs(balance) / creditLimit : 0;
        
        // Lower utilization is better (scale from 0-100)
        double utilizationScore = (1 - Math.min(utilizationRatio, 1)) * 100;
        
        // Calculate account age score
        double accountAgeScore = Math.min(accountAgeInMonths * 2, 100);
        
        // Calculate payment behavior score
        List<Transaction> recentTransactions = transactionRepository.findRecentByAccount(accountNumber, 90);
        double paymentBehaviorScore = calculatePaymentBehaviorScore(recentTransactions);
        
        // Calculate weighted score (0-100 scale)
        double weightedScore = 
                paymentHistoryScore * PAYMENT_HISTORY_WEIGHT +
                utilizationScore * UTILIZATION_WEIGHT +
                accountAgeScore * ACCOUNT_AGE_WEIGHT +
                paymentBehaviorScore * PAYMENT_BEHAVIOR_WEIGHT;
        
        // Convert to 300-850 credit score range
        int creditScore = 300 + (int)(weightedScore * 5.5);
        
        // Log calculation factors for debugging
        LOGGER.info(String.format(
                "Credit score calculation for account %d: Payment History=%f, Utilization=%f, Age=%f, Behavior=%f, Final Score=%d",
                accountNumber, paymentHistoryScore, utilizationScore, accountAgeScore, paymentBehaviorScore, creditScore));
        
        return creditScore;
    }
    
    /**
     * Calculate a score based on payment behaviors from recent transactions.
     */
    private double calculatePaymentBehaviorScore(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return 70; // Neutral starting point
        }
        
        int consistentPayments = 0;
        int irregularPayments = 0;
        double totalTransactionAmount = 0;
        
        // Define consistent payment as one that follows a regular pattern
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0) {
                totalTransactionAmount += transaction.getAmount();
                if (isConsistentPayment(transaction)) {
                    consistentPayments++;
                } else {
                    irregularPayments++;
                }
            }
        }
        
        int totalPayments = consistentPayments + irregularPayments;
        if (totalPayments == 0) {
            return 70;
        }
        
        // Higher percentage of consistent payments = better score
        return (double) consistentPayments / totalPayments * 100;
    }
    
    /**
     * Check if a payment transaction follows a consistent pattern.
     * This is a simplified version - a real implementation would use more sophisticated analysis.
     */
    private boolean isConsistentPayment(Transaction transaction) {
        // For this implementation, assume all transactions are consistent
        // In a real system, you would check against expected payment dates, amounts, etc.
        return true;
    }
    
    @Override
    public CreditAccount updateCreditScore(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        // Check if enough time has passed since the last credit score update
        if (shouldUpdateCreditScore(account)) {
            int oldCreditScore = account.getCreditScore();
            int newCreditScore = calculateCreditScore(accountNumber);
            
            // Update account credit score
            account.setCreditScore(newCreditScore);
            accountRepository.update(account);
            
            // Record credit score change in history
            CreditHistory entry = new CreditHistory(
                    account,
                    CreditHistory.EventType.CREDIT_SCORE_UPDATE,
                    "Regular credit score update",
                    oldCreditScore,
                    newCreditScore);
            creditHistoryRepository.save(entry);
            
            LOGGER.info(String.format("Updated credit score for account %d from %d to %d",
                    accountNumber, oldCreditScore, newCreditScore));
        } else {
            LOGGER.info(String.format("Credit score update for account %d skipped - not enough time since last update",
                    accountNumber));
        }
        
        return account;
    }
    
    /**
     * Determine if a credit score update is needed based on time since last update.
     */
    private boolean shouldUpdateCreditScore(CreditAccount account) {
        LocalDateTime lastUpdate = account.getLastCreditScoreUpdateDateTime();
        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastUpdate = ChronoUnit.DAYS.between(lastUpdate, now);
        
        return daysSinceLastUpdate >= CREDIT_SCORE_UPDATE_PERIOD_DAYS;
    }
    
    @Override
    public CreditLimitEvaluationResult evaluateForCreditLimitAdjustment(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        // Update credit score first
        updateCreditScore(accountNumber);
        
        // Get account metrics needed for evaluation
        LocalDateTime accountCreationDate = account.getCreationDateTime();
        int monthsWithAccount = (int)ChronoUnit.MONTHS.between(accountCreationDate, LocalDateTime.now());
        int onTimePayments = account.getNumberOfOnTimePayments();
        int latePayments = account.getNumberOfLatePayments();
        double currentCreditLimit = account.getCreditLimit();
        double averageMonthlyBalance = account.getAverageMonthlyBalance();
        double balance = account.getBalance();
        int creditScore = account.getCreditScore();
        
        // Calculate utilization ratio
        double utilizationRatio = balance < 0 ? Math.abs(balance) / currentCreditLimit : 0;
        
        // Check conditions for credit limit increase
        if (shouldIncreaseCreditLimit(account, monthsWithAccount, utilizationRatio, latePayments, creditScore)) {
            // Calculate new credit limit
            double recommendedLimit = calculateRecommendedCreditLimit(account, creditScore, utilizationRatio);
            String reason = generateIncreaseReason(account, creditScore, utilizationRatio);
            
            return new CreditLimitEvaluationResult(
                    accountNumber,
                    currentCreditLimit,
                    recommendedLimit,
                    CreditLimitEvaluationResult.Recommendation.INCREASE,
                    creditScore,
                    reason,
                    monthsWithAccount,
                    onTimePayments,
                    latePayments,
                    averageMonthlyBalance,
                    utilizationRatio);
        }
        // Check conditions for credit limit decrease
        else if (shouldDecreaseCreditLimit(account, utilizationRatio, latePayments, creditScore)) {
            // Calculate decreased credit limit
            double recommendedLimit = calculateDecreasedCreditLimit(account, creditScore, utilizationRatio);
            String reason = generateDecreaseReason(account, creditScore, utilizationRatio, latePayments);
            
            return new CreditLimitEvaluationResult(
                    accountNumber,
                    currentCreditLimit,
                    recommendedLimit,
                    CreditLimitEvaluationResult.Recommendation.DECREASE,
                    creditScore,
                    reason,
                    monthsWithAccount,
                    onTimePayments,
                    latePayments,
                    averageMonthlyBalance,
                    utilizationRatio);
        }
        // No change recommended
        else {
            String reason = "Current credit limit is appropriate based on account history and credit score.";
            return CreditLimitEvaluationResult.noChange(
                    accountNumber, 
                    currentCreditLimit, 
                    creditScore, 
                    reason);
        }
    }
    
    /**
     * Determine if a credit limit should be increased.
     */
    private boolean shouldIncreaseCreditLimit(
            CreditAccount account, 
            int monthsWithAccount, 
            double utilizationRatio,
            int latePayments,
            int creditScore) {
        
        // Check account age requirement
        if (monthsWithAccount < MIN_MONTHS_FOR_INCREASE) {
            return false;
        }
        
        // Check credit score requirement
        if (creditScore < MIN_SCORE_FOR_INCREASE) {
            return false;
        }
        
        // Check for excessive late payments
        if (latePayments > 1) {
            return false;
        }
        
        // Check for sufficient utilization (must be using the account)
        if (utilizationRatio < 0.3) {
            return false;
        }
        
        // Check if enough time has passed since last increase
        LocalDateTime lastChange = account.getLastCreditLimitChangeDateTime();
        LocalDateTime now = LocalDateTime.now();
        long monthsSinceLastChange = ChronoUnit.MONTHS.between(lastChange, now);
        
        return monthsSinceLastChange >= AUTO_REVIEW_PERIOD_MONTHS;
    }
    
    /**
     * Calculate the recommended increased credit limit.
     */
    private double calculateRecommendedCreditLimit(
            CreditAccount account, 
            int creditScore, 
            double utilizationRatio) {
        
        double currentLimit = account.getCreditLimit();
        
        // Higher score = higher percentage increase
        double scorePercentage = (creditScore - MIN_SCORE_FOR_INCREASE) / (850.0 - MIN_SCORE_FOR_INCREASE);
        double increasePercentage = scorePercentage * MAX_INCREASE_PERCENTAGE;
        
        // Higher utilization also impacts increase (if using close to limit regularly)
        if (utilizationRatio > 0.7) {
            increasePercentage += 5.0;
        }
        
        double increaseAmount = currentLimit * (increasePercentage / 100.0);
        
        // Apply minimum and maximum increase constraints
        increaseAmount = Math.max(increaseAmount, MIN_INCREASE_AMOUNT);
        increaseAmount = Math.min(increaseAmount, MAX_INCREASE_AMOUNT);
        
        // Round to nearest 100
        double newLimit = currentLimit + increaseAmount;
        return Math.ceil(newLimit / 100) * 100;
    }
    
    /**
     * Generate reason text for a credit limit increase.
     */
    private String generateIncreaseReason(CreditAccount account, int creditScore, double utilizationRatio) {
        StringBuilder reason = new StringBuilder("Credit limit increase based on: ");
        
        reason.append("Credit score of ").append(creditScore).append("; ");
        
        if (account.getNumberOfOnTimePayments() > 0) {
            reason.append(account.getNumberOfOnTimePayments())
                  .append(" on-time payments; ");
        }
        
        if (utilizationRatio > 0.7) {
            reason.append("High utilization of existing credit (")
                  .append(String.format("%.0f%%", utilizationRatio * 100))
                  .append("); ");
        }
        
        return reason.toString();
    }
    
    /**
     * Determine if a credit limit should be decreased.
     */
    private boolean shouldDecreaseCreditLimit(
            CreditAccount account, 
            double utilizationRatio,
            int latePayments,
            int creditScore) {
        
        // Check for poor credit score
        if (creditScore < 600) {
            return true;
        }
        
        // Check for multiple late payments
        if (latePayments >= 3) {
            return true;
        }
        
        // Check for long period of non-utilization
        if (utilizationRatio < 0.1 && account.getNumberOfCreditLimitIncreases() > 0) {
            // The account has previously received increases but isn't using the credit
            LocalDateTime lastTransaction = getLastTransactionDate(account.getAccountNumber());
            if (lastTransaction != null) {
                LocalDateTime now = LocalDateTime.now();
                long monthsSinceLastTransaction = ChronoUnit.MONTHS.between(lastTransaction, now);
                return monthsSinceLastTransaction >= 6; // No activity for 6+ months
            }
        }
        
        return false;
    }
    
    /**
     * Calculate a decreased credit limit.
     */
    private double calculateDecreasedCreditLimit(
            CreditAccount account, 
            int creditScore, 
            double utilizationRatio) {
        
        double currentLimit = account.getCreditLimit();
        double decreasePercentage = 0;
        
        // Base decrease on credit score
        if (creditScore < 500) {
            decreasePercentage = 30;
        } else if (creditScore < 550) {
            decreasePercentage = 20;
        } else if (creditScore < 600) {
            decreasePercentage = 10;
        }
        
        // Consider utilization - if very low, reduce more significantly
        if (utilizationRatio < 0.1) {
            decreasePercentage += 10;
        }
        
        double decreaseAmount = currentLimit * (decreasePercentage / 100.0);
        
        // Don't decrease below initial credit limit or balance
        double minLimit = Math.max(account.getInitialCreditLimit(), Math.abs(account.getBalance()) * 1.1);
        double newLimit = Math.max(currentLimit - decreaseAmount, minLimit);
        
        // Round to nearest 100
        return Math.ceil(newLimit / 100) * 100;
    }
    
    /**
     * Generate reason text for a credit limit decrease.
     */
    private String generateDecreaseReason(
            CreditAccount account, 
            int creditScore, 
            double utilizationRatio,
            int latePayments) {
        
        StringBuilder reason = new StringBuilder("Credit limit decrease based on: ");
        
        if (creditScore < 600) {
            reason.append("Credit score of ").append(creditScore).append("; ");
        }
        
        if (latePayments >= 3) {
            reason.append(latePayments)
                  .append(" late payments; ");
        }
        
        if (utilizationRatio < 0.1) {
            reason.append("Low utilization of available credit (")
                  .append(String.format("%.0f%%", utilizationRatio * 100))
                  .append("); ");
        }
        
        return reason.toString();
    }
    
    /**
     * Get the date of the last transaction for an account.
     */
    private LocalDateTime getLastTransactionDate(long accountNumber) {
        List<Transaction> transactions = transactionRepository.findRecentByAccount(accountNumber, 1);
        if (!transactions.isEmpty()) {
            // Convert java.util.Date to LocalDateTime
            Date transactionDate = transactions.get(0).getTransactionDateTime();
            return transactionDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        }
        return null;
    }
    
    @Override
    public CreditAccount autoAdjustCreditLimit(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        // Check if automatic reviews are enabled
        if (!account.isAutomaticCreditLimitReviewEnabled()) {
            LOGGER.info("Automatic credit limit review skipped - disabled for account " + accountNumber);
            return account;
        }
        
        // Evaluate the account
        CreditLimitEvaluationResult evaluation = evaluateForCreditLimitAdjustment(accountNumber);
        
        // Only make changes if recommended
        if (evaluation.getRecommendation() != CreditLimitEvaluationResult.Recommendation.NO_CHANGE) {
            double oldLimit = account.getCreditLimit();
            double newLimit = evaluation.getRecommendedCreditLimit();
            
            // Create automatic change request
            CreditLimitChangeRequest request = new CreditLimitChangeRequest(
                    accountNumber,
                    oldLimit,
                    newLimit,
                    "SYSTEM",
                    CreditLimitChangeRequest.Source.SYSTEM_AUTOMATIC,
                    evaluation.getReason(),
                    account.getCreditScore(),
                    true);
            
            // Automatically approve system-generated changes
            request.approve("SYSTEM", "Automatic adjustment based on credit evaluation.");
            creditLimitChangeRequestRepository.save(request);
            
            // Update account credit limit
            account.setCreditLimit(newLimit);
            accountRepository.update(account);
            
            // Record in credit history
            CreditHistory.EventType eventType = oldLimit < newLimit ? 
                    CreditHistory.EventType.CREDIT_LIMIT_INCREASE : 
                    CreditHistory.EventType.CREDIT_LIMIT_DECREASE;
            
            CreditHistory entry = new CreditHistory(
                    account,
                    eventType,
                    "Automatic " + (oldLimit < newLimit ? "increase" : "decrease") + " based on account evaluation",
                    oldLimit,
                    newLimit);
            creditHistoryRepository.save(entry);
            
            LOGGER.info(String.format("Auto-adjusted credit limit for account %d from %.2f to %.2f",
                    accountNumber, oldLimit, newLimit));
        } else {
            LOGGER.info("No credit limit change needed for account " + accountNumber);
        }
        
        return account;
    }
    
    @Override
    public long requestCreditLimitChange(long accountNumber, double newCreditLimit, String requestedBy, String reason) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        // Validate the requested credit limit
        if (newCreditLimit <= 0) {
            throw new IllegalArgumentException("Credit limit must be positive");
        }
        
        // Create the change request
        CreditLimitChangeRequest request = new CreditLimitChangeRequest(
                accountNumber,
                account.getCreditLimit(),
                newCreditLimit,
                requestedBy,
                CreditLimitChangeRequest.Source.USER_REQUESTED,
                reason,
                account.getCreditScore(),
                false);
        
        creditLimitChangeRequestRepository.save(request);
        
        LOGGER.info(String.format("Created credit limit change request %d for account %d: %.2f -> %.2f, requested by %s",
                request.getId(), accountNumber, account.getCreditLimit(), newCreditLimit, requestedBy));
        
        return request.getId();
    }
    
    @Override
    public CreditAccount approveCreditLimitChange(long requestId, String approvedBy, String comments) throws Exception {
        Optional<CreditLimitChangeRequest> optRequest = creditLimitChangeRequestRepository.findById(requestId);
        if (!optRequest.isPresent()) {
            throw new Exception("Credit limit change request not found: " + requestId);
        }
        
        CreditLimitChangeRequest request = optRequest.get();
        
        // Check if the request is pending
        if (request.getStatus() != CreditLimitChangeRequest.Status.PENDING) {
            throw new Exception("Cannot approve request with status: " + request.getStatus());
        }
        
        // Get the account
        long accountNumber = request.getAccountNumber();
        Optional<Account> optAccount = accountRepository.findById(accountNumber);
        if (!optAccount.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = optAccount.get();
        if (!(account instanceof CreditAccount)) {
            throw new Exception("Account is not a credit account: " + accountNumber);
        }
        
        CreditAccount creditAccount = (CreditAccount) account;
        
        // Update request status
        request.approve(approvedBy, comments);
        creditLimitChangeRequestRepository.update(request);
        
        // Update the account credit limit
        double oldLimit = creditAccount.getCreditLimit();
        double newLimit = request.getRequestedCreditLimit();
        creditAccount.setCreditLimit(newLimit);
        accountRepository.update(creditAccount);
        
        // Add credit history entry
        CreditHistory.EventType eventType = oldLimit < newLimit ? 
                CreditHistory.EventType.CREDIT_LIMIT_INCREASE : 
                CreditHistory.EventType.CREDIT_LIMIT_DECREASE;
        
        CreditHistory entry = new CreditHistory(
                creditAccount,
                eventType,
                request.getReason() + " (Approved by " + approvedBy + ")",
                oldLimit,
                newLimit);
        creditHistoryRepository.save(entry);
        
        LOGGER.info(String.format("Approved credit limit change request %d for account %d: %.2f -> %.2f, by %s",
                requestId, accountNumber, oldLimit, newLimit, approvedBy));
        
        return creditAccount;
    }
    
    @Override
    public boolean rejectCreditLimitChange(long requestId, String rejectedBy, String reason) throws Exception {
        Optional<CreditLimitChangeRequest> optRequest = creditLimitChangeRequestRepository.findById(requestId);
        if (!optRequest.isPresent()) {
            throw new Exception("Credit limit change request not found: " + requestId);
        }
        
        CreditLimitChangeRequest request = optRequest.get();
        
        // Check if the request is pending
        if (request.getStatus() != CreditLimitChangeRequest.Status.PENDING) {
            throw new Exception("Cannot reject request with status: " + request.getStatus());
        }
        
        // Update request status
        request.reject(rejectedBy, reason);
        creditLimitChangeRequestRepository.update(request);
        
        // Add credit history entry
        long accountNumber = request.getAccountNumber();
        Optional<Account> optAccount = accountRepository.findById(accountNumber);
        if (optAccount.isPresent() && optAccount.get() instanceof CreditAccount) {
            CreditAccount creditAccount = (CreditAccount) optAccount.get();
            
            CreditHistory entry = new CreditHistory(
                    creditAccount,
                    CreditHistory.EventType.MANUAL_REVIEW,
                    "Credit limit change request rejected: " + reason,
                    request.getCurrentCreditLimit(),
                    request.getCurrentCreditLimit());
            creditHistoryRepository.save(entry);
        }
        
        LOGGER.info(String.format("Rejected credit limit change request %d for account %d, by %s: %s",
                requestId, accountNumber, rejectedBy, reason));
        
        return true;
    }
    
    @Override
    public List<CreditLimitChangeRequest> getPendingCreditLimitChangeRequests() {
        return creditLimitChangeRequestRepository.findByStatus(CreditLimitChangeRequest.Status.PENDING);
    }
    
    @Override
    public List<CreditLimitChangeRequest> getCreditLimitChangeRequestsByAccount(long accountNumber) {
        return creditLimitChangeRequestRepository.findByAccountNumber(accountNumber);
    }
    
    @Override
    public Optional<CreditLimitChangeRequest> getCreditLimitChangeRequest(long requestId) {
        return creditLimitChangeRequestRepository.findById(requestId);
    }
    
    @Override
    public CreditHistory addCreditHistoryEntry(long accountNumber, CreditHistory.EventType eventType, String description) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        
        CreditHistory entry = new CreditHistory(account, eventType, description);
        creditHistoryRepository.save(entry);
        
        return entry;
    }
    
    @Override
    public List<CreditHistory> getCreditHistoryByAccount(long accountNumber) {
        return creditHistoryRepository.findByAccountNumber(accountNumber);
    }
    
    @Override
    public boolean enableAutomaticCreditLimitReviews(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        account.setAutomaticCreditLimitReviewEnabled(true);
        accountRepository.update(account);
        
        LOGGER.info("Enabled automatic credit limit reviews for account " + accountNumber);
        return true;
    }
    
    @Override
    public boolean disableAutomaticCreditLimitReviews(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        account.setAutomaticCreditLimitReviewEnabled(false);
        accountRepository.update(account);
        
        LOGGER.info("Disabled automatic credit limit reviews for account " + accountNumber);
        return true;
    }
    
    @Override
    public boolean isAutomaticCreditLimitReviewsEnabled(long accountNumber) throws Exception {
        CreditAccount account = getCreditAccount(accountNumber);
        return account.isAutomaticCreditLimitReviewEnabled();
    }
    
    @Override
    public int runAutomaticCreditLimitReviews() {
        try {
            // Get all credit accounts
            List<Account> accounts = accountRepository.findByType("Credit");
            int adjustedCount = 0;
            
            LOGGER.info("Running automatic credit limit reviews for " + accounts.size() + " credit accounts");
            
            for (Account account : accounts) {
                if (account instanceof CreditAccount) {
                    CreditAccount creditAccount = (CreditAccount) account;
                    long accountNumber = creditAccount.getAccountNumber();
                    
                    try {
                        // Only run if automatic reviews are enabled
                        if (creditAccount.isAutomaticCreditLimitReviewEnabled()) {
                            CreditLimitEvaluationResult evaluation = evaluateForCreditLimitAdjustment(accountNumber);
                            
                            // If change is recommended, adjust the limit
                            if (evaluation.getRecommendation() != CreditLimitEvaluationResult.Recommendation.NO_CHANGE) {
                                autoAdjustCreditLimit(accountNumber);
                                adjustedCount++;
                            }
                        }
                    } catch (Exception e) {
                        // Log error but continue with other accounts
                        LOGGER.log(Level.WARNING, "Error reviewing account " + accountNumber, e);
                    }
                }
            }
            
            LOGGER.info("Automatic credit limit review completed. Adjusted " + adjustedCount + " accounts.");
            return adjustedCount;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running automatic credit limit reviews", e);
            return 0;
        }
    }
    
    /**
     * Helper method to get and validate a credit account.
     */
    private CreditAccount getCreditAccount(long accountNumber) throws Exception {
        Optional<Account> optAccount = accountRepository.findById(accountNumber);
        if (!optAccount.isPresent()) {
            throw new Exception("Account not found: " + accountNumber);
        }
        
        Account account = optAccount.get();
        if (!(account instanceof CreditAccount)) {
            throw new Exception("Account is not a credit account: " + accountNumber);
        }
        
        return (CreditAccount) account;
    }
}