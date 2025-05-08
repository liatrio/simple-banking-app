package com.smartbank.service.category;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;

import java.util.List;
import java.util.Map;

/**
 * Service interface for transaction categorization operations.
 */
public interface TransactionCategorizationService {
    
    /**
     * Assign a category to a transaction.
     * 
     * @param transactionId The transaction ID
     * @param categoryId The category ID
     * @param isAutomatic Whether this is an automatic assignment
     * @return The categorized transaction
     * @throws CategoryException if there's an issue assigning the category
     */
    Transaction assignCategory(long transactionId, long categoryId, boolean isAutomatic) throws CategoryException;
    
    /**
     * Remove a category from a transaction.
     * 
     * @param transactionId The transaction ID
     * @return The updated transaction
     * @throws CategoryException if there's an issue removing the category
     */
    Transaction removeCategory(long transactionId) throws CategoryException;
    
    /**
     * Categorize all uncategorized transactions using automatic rules.
     * 
     * @return Number of transactions categorized
     */
    int categorizeAll();
    
    /**
     * Reassign transactions from one category to another.
     * 
     * @param fromCategoryId The source category ID
     * @param toCategoryId The target category ID
     * @return Number of transactions reassigned
     * @throws CategoryException if there's an issue reassigning categories
     */
    int reassignCategory(long fromCategoryId, long toCategoryId) throws CategoryException;
    
    /**
     * Get transactions for a specific category.
     * 
     * @param categoryId The category ID
     * @return List of transactions with the specified category
     */
    List<Transaction> getTransactionsByCategory(long categoryId);
    
    /**
     * Apply the same category to all transactions matching a pattern.
     * 
     * @param pattern The pattern to match in description
     * @param categoryId The category ID to apply
     * @return Number of transactions categorized
     * @throws CategoryException if there's an issue applying the category
     */
    int applyCategoryByPattern(String pattern, long categoryId) throws CategoryException;
    
    /**
     * Apply a category to all transactions with the same merchant.
     * 
     * @param merchantName The merchant name
     * @param categoryId The category ID to apply
     * @return Number of transactions categorized
     * @throws CategoryException if there's an issue applying the category
     */
    int applyCategoryByMerchant(String merchantName, long categoryId) throws CategoryException;
    
    /**
     * Get a summary of transactions by category.
     * 
     * @return Map of category IDs to total amount and count of transactions
     */
    Map<Long, CategorySummary> getTransactionSummaryByCategory();
    
    /**
     * Apply a category to all future transactions matching this one.
     * 
     * @param transactionId The reference transaction ID
     * @param categoryId The category ID to apply
     * @return true if rule was created, false otherwise
     * @throws CategoryException if there's an issue creating the rule
     */
    boolean createRuleFromTransaction(long transactionId, long categoryId) throws CategoryException;
    
    /**
     * Class representing a summary of transactions for a category.
     */
    class CategorySummary {
        private final long categoryId;
        private final String categoryName;
        private final double totalAmount;
        private final int transactionCount;
        
        public CategorySummary(long categoryId, String categoryName, double totalAmount, int transactionCount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
            this.transactionCount = transactionCount;
        }
        
        public long getCategoryId() {
            return categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public int getTransactionCount() {
            return transactionCount;
        }
    }
}