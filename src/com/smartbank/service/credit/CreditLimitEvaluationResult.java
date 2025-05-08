package com.smartbank.service.credit;

/**
 * Contains the results of a credit limit evaluation.
 */
public class CreditLimitEvaluationResult {
    
    // Recommendation result of credit limit evaluation
    public enum Recommendation {
        INCREASE,
        DECREASE,
        NO_CHANGE
    }
    
    private final long accountNumber;
    private final double currentCreditLimit;
    private final double recommendedCreditLimit;
    private final Recommendation recommendation;
    private final int creditScore;
    private final String reason;
    
    // Additional factors used in evaluation
    private final int monthsWithAccount;
    private final int onTimePaymentsCount;
    private final int latePaymentsCount;
    private final double averageMonthlyBalance;
    private final double utilizationRatio;
    
    /**
     * Create a new credit limit evaluation result.
     * 
     * @param accountNumber The account number
     * @param currentCreditLimit The current credit limit
     * @param recommendedCreditLimit The recommended credit limit
     * @param recommendation The recommendation (increase, decrease, or no change)
     * @param creditScore The current credit score
     * @param reason The reason for the recommendation
     * @param monthsWithAccount The number of months the account has been open
     * @param onTimePaymentsCount The number of on-time payments
     * @param latePaymentsCount The number of late payments
     * @param averageMonthlyBalance The average monthly balance
     * @param utilizationRatio The credit utilization ratio
     */
    public CreditLimitEvaluationResult(
            long accountNumber,
            double currentCreditLimit,
            double recommendedCreditLimit,
            Recommendation recommendation,
            int creditScore,
            String reason,
            int monthsWithAccount,
            int onTimePaymentsCount,
            int latePaymentsCount,
            double averageMonthlyBalance,
            double utilizationRatio) {
        this.accountNumber = accountNumber;
        this.currentCreditLimit = currentCreditLimit;
        this.recommendedCreditLimit = recommendedCreditLimit;
        this.recommendation = recommendation;
        this.creditScore = creditScore;
        this.reason = reason;
        this.monthsWithAccount = monthsWithAccount;
        this.onTimePaymentsCount = onTimePaymentsCount;
        this.latePaymentsCount = latePaymentsCount;
        this.averageMonthlyBalance = averageMonthlyBalance;
        this.utilizationRatio = utilizationRatio;
    }
    
    /**
     * Create a "no change" evaluation result.
     * 
     * @param accountNumber The account number
     * @param currentCreditLimit The current credit limit
     * @param creditScore The current credit score
     * @param reason The reason for no change
     * @return A CreditLimitEvaluationResult indicating no change
     */
    public static CreditLimitEvaluationResult noChange(
            long accountNumber, 
            double currentCreditLimit, 
            int creditScore, 
            String reason) {
        return new CreditLimitEvaluationResult(
                accountNumber,
                currentCreditLimit,
                currentCreditLimit,
                Recommendation.NO_CHANGE,
                creditScore,
                reason,
                0, 0, 0, 0, 0);
    }
    
    // Getters
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public double getCurrentCreditLimit() {
        return currentCreditLimit;
    }
    
    public double getRecommendedCreditLimit() {
        return recommendedCreditLimit;
    }
    
    public Recommendation getRecommendation() {
        return recommendation;
    }
    
    public int getCreditScore() {
        return creditScore;
    }
    
    public String getReason() {
        return reason;
    }
    
    public int getMonthsWithAccount() {
        return monthsWithAccount;
    }
    
    public int getOnTimePaymentsCount() {
        return onTimePaymentsCount;
    }
    
    public int getLatePaymentsCount() {
        return latePaymentsCount;
    }
    
    public double getAverageMonthlyBalance() {
        return averageMonthlyBalance;
    }
    
    public double getUtilizationRatio() {
        return utilizationRatio;
    }
    
    /**
     * Calculate the difference between recommended and current credit limits.
     * @return The difference (positive for increase, negative for decrease)
     */
    public double getCreditLimitDifference() {
        return recommendedCreditLimit - currentCreditLimit;
    }
    
    /**
     * Calculate the percentage change from current to recommended credit limit.
     * @return The percentage change (positive for increase, negative for decrease)
     */
    public double getPercentageChange() {
        return (recommendedCreditLimit - currentCreditLimit) / currentCreditLimit * 100;
    }
    
    @Override
    public String toString() {
        return "CreditLimitEvaluationResult{" +
                "accountNumber=" + accountNumber +
                ", currentCreditLimit=" + currentCreditLimit +
                ", recommendedCreditLimit=" + recommendedCreditLimit +
                ", recommendation=" + recommendation +
                ", creditScore=" + creditScore +
                ", reason='" + reason + '\'' +
                '}';
    }
}