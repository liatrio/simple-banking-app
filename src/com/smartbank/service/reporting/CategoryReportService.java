package com.smartbank.service.reporting;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for category-based transaction reporting.
 */
public interface CategoryReportService {
    
    /**
     * Get spending by category for a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return Map of category IDs to total spending
     */
    Map<Long, CategorySpending> getSpendingByCategory(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get income by category for a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return Map of category IDs to total income
     */
    Map<Long, CategorySpending> getIncomeByCategory(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get transactions by category for a date range.
     * 
     * @param accountNumber The account number
     * @param categoryId The category ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of transactions in the category
     */
    List<Transaction> getTransactionsByCategory(long accountNumber, long categoryId, Date startDate, Date endDate);
    
    /**
     * Get uncategorized transactions for a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return List of uncategorized transactions
     */
    List<Transaction> getUncategorizedTransactions(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get spending trends by category over multiple periods.
     * 
     * @param accountNumber The account number
     * @param periods The number of periods (e.g., months)
     * @param periodType The type of period (e.g., "month", "week", "day")
     * @return Map of periods to category spending
     */
    Map<String, Map<Long, CategorySpending>> getSpendingTrendsByCategory(
            long accountNumber, int periods, String periodType);
    
    /**
     * Get budget vs. actual spending for categories.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return Map of category IDs to budget vs. actual spending
     */
    Map<Long, BudgetVsActual> getBudgetVsActual(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get recommended categories for uncategorized transactions.
     * 
     * @param accountNumber The account number
     * @return Map of transaction IDs to recommended category IDs
     */
    Map<Long, List<CategoryRecommendation>> getRecommendedCategories(long accountNumber);
    
    /**
     * Generate a spending report by category.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @param format The report format (e.g., "csv", "pdf", "html")
     * @return The report content as a byte array
     */
    byte[] generateSpendingReport(long accountNumber, Date startDate, Date endDate, String format);
    
    /**
     * Get recurring transactions by category.
     * 
     * @param accountNumber The account number
     * @return Map of category IDs to lists of recurring transactions
     */
    Map<Long, List<Transaction>> getRecurringTransactionsByCategory(long accountNumber);
    
    /**
     * Class representing spending in a category.
     */
    class CategorySpending {
        private final long categoryId;
        private final String categoryName;
        private final double amount;
        private final int transactionCount;
        private final double percentage;
        
        public CategorySpending(long categoryId, String categoryName, double amount, 
                               int transactionCount, double percentage) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.amount = amount;
            this.transactionCount = transactionCount;
            this.percentage = percentage;
        }

        public long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getAmount() {
            return amount;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public double getPercentage() {
            return percentage;
        }
    }
    
    /**
     * Class representing budget vs. actual spending.
     */
    class BudgetVsActual {
        private final long categoryId;
        private final String categoryName;
        private final double budgetAmount;
        private final double actualAmount;
        private final double difference;
        private final double percentageUsed;
        
        public BudgetVsActual(long categoryId, String categoryName, double budgetAmount, 
                             double actualAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.budgetAmount = budgetAmount;
            this.actualAmount = actualAmount;
            this.difference = budgetAmount - actualAmount;
            this.percentageUsed = budgetAmount > 0 ? (actualAmount / budgetAmount) * 100 : 0;
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

        public double getActualAmount() {
            return actualAmount;
        }

        public double getDifference() {
            return difference;
        }

        public double getPercentageUsed() {
            return percentageUsed;
        }
        
        public boolean isOverBudget() {
            return actualAmount > budgetAmount;
        }
    }
    
    /**
     * Class representing a category recommendation.
     */
    class CategoryRecommendation {
        private final long categoryId;
        private final String categoryName;
        private final double confidence;
        
        public CategoryRecommendation(long categoryId, String categoryName, double confidence) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.confidence = confidence;
        }

        public long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}