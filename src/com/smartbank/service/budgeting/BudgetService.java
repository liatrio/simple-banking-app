package com.smartbank.service.budgeting;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for budget management and tracking.
 */
public interface BudgetService {
    
    /**
     * Set a budget amount for a category.
     * 
     * @param categoryId The category ID
     * @param amount The budget amount
     * @return true if the budget was set successfully, false otherwise
     * @throws BudgetException if there's an issue setting the budget
     */
    boolean setBudgetAmount(long categoryId, double amount) throws BudgetException;
    
    /**
     * Get current budget status for all categories.
     * 
     * @param accountNumber The account number
     * @param period The budget period (e.g., "month", "week")
     * @return Map of category IDs to budget status
     */
    Map<Long, BudgetStatus> getBudgetStatus(long accountNumber, String period);
    
    /**
     * Get budget status for a specific category.
     * 
     * @param accountNumber The account number
     * @param categoryId The category ID
     * @param period The budget period (e.g., "month", "week")
     * @return The budget status
     */
    BudgetStatus getCategoryBudgetStatus(long accountNumber, long categoryId, String period);
    
    /**
     * Get categories that are over budget.
     * 
     * @param accountNumber The account number
     * @param period The budget period (e.g., "month", "week")
     * @return List of budget status for categories that are over budget
     */
    List<BudgetStatus> getOverBudgetCategories(long accountNumber, String period);
    
    /**
     * Get spending trends over time for a category.
     * 
     * @param accountNumber The account number
     * @param categoryId The category ID
     * @param periods The number of periods to include
     * @param periodType The type of period (e.g., "month", "week")
     * @return Map of period labels to spending amounts
     */
    Map<String, Double> getCategorySpendingTrend(long accountNumber, long categoryId, 
                                               int periods, String periodType);
    
    /**
     * Get spending forecasts for categories based on historical data.
     * 
     * @param accountNumber The account number
     * @param period The prediction period (e.g., "month", "week")
     * @return Map of category IDs to spending forecasts
     */
    Map<Long, BudgetForecast> getSpendingForecasts(long accountNumber, String period);
    
    /**
     * Generate a budget report.
     * 
     * @param accountNumber The account number
     * @param period The budget period (e.g., "month", "week")
     * @param format The report format (e.g., "csv", "pdf", "html")
     * @return The report content as a byte array
     */
    byte[] generateBudgetReport(long accountNumber, String period, String format);
    
    /**
     * Calculate daily spending allowance for remaining budget.
     * 
     * @param accountNumber The account number
     * @param categoryId The category ID
     * @param period The budget period (e.g., "month", "week")
     * @return The daily allowance
     */
    double calculateDailyAllowance(long accountNumber, long categoryId, String period);
    
    /**
     * Get budget period date range.
     * 
     * @param period The budget period (e.g., "month", "week")
     * @param referenceDate The reference date (defaults to current date if null)
     * @return A DateRange object with start and end dates
     */
    DateRange getBudgetPeriodRange(String period, Date referenceDate);
    
    /**
     * Class representing a budget period date range.
     */
    class DateRange {
        private final Date startDate;
        private final Date endDate;
        private final String label;
        private final int daysInPeriod;
        private final int daysRemaining;
        
        public DateRange(Date startDate, Date endDate, String label, int daysInPeriod, int daysRemaining) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.label = label;
            this.daysInPeriod = daysInPeriod;
            this.daysRemaining = daysRemaining;
        }
        
        public Date getStartDate() {
            return startDate;
        }
        
        public Date getEndDate() {
            return endDate;
        }
        
        public String getLabel() {
            return label;
        }
        
        public int getDaysInPeriod() {
            return daysInPeriod;
        }
        
        public int getDaysRemaining() {
            return daysRemaining;
        }
    }
    
    /**
     * Class representing budget status for a category.
     */
    class BudgetStatus {
        private final long categoryId;
        private final String categoryName;
        private final double budgetAmount;
        private final double spentAmount;
        private final double remainingAmount;
        private final double percentageUsed;
        private final boolean isOverBudget;
        
        public BudgetStatus(long categoryId, String categoryName, double budgetAmount, 
                           double spentAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.budgetAmount = budgetAmount;
            this.spentAmount = spentAmount;
            this.remainingAmount = budgetAmount - spentAmount;
            this.percentageUsed = budgetAmount > 0 ? (spentAmount / budgetAmount) * 100 : 0;
            this.isOverBudget = spentAmount > budgetAmount;
        }
        
        public long getCategoryId() {
            return categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public double getBudgetAmount() {
            return budgetAmount;
        }
        
        public double getSpentAmount() {
            return spentAmount;
        }
        
        public double getRemainingAmount() {
            return remainingAmount;
        }
        
        public double getPercentageUsed() {
            return percentageUsed;
        }
        
        public boolean isOverBudget() {
            return isOverBudget;
        }
    }
    
    /**
     * Class representing a spending forecast for a category.
     */
    class BudgetForecast {
        private final long categoryId;
        private final String categoryName;
        private final double budgetAmount;
        private final double forecastAmount;
        private final double confidenceLevel;
        private final boolean willExceedBudget;
        
        public BudgetForecast(long categoryId, String categoryName, double budgetAmount, 
                             double forecastAmount, double confidenceLevel) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.budgetAmount = budgetAmount;
            this.forecastAmount = forecastAmount;
            this.confidenceLevel = confidenceLevel;
            this.willExceedBudget = forecastAmount > budgetAmount;
        }
        
        public long getCategoryId() {
            return categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public double getBudgetAmount() {
            return budgetAmount;
        }
        
        public double getForecastAmount() {
            return forecastAmount;
        }
        
        public double getConfidenceLevel() {
            return confidenceLevel;
        }
        
        public boolean willExceedBudget() {
            return willExceedBudget;
        }
    }
}