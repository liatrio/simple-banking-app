package com.smartbank.service.category;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;

import java.util.List;

/**
 * Service interface for transaction categorization rules.
 */
public interface CategorizationRuleService {
    
    /**
     * Apply categorization rules to a transaction.
     * 
     * @param transaction The transaction to categorize
     * @return The categorized transaction
     */
    Transaction categorizeTransaction(Transaction transaction);
    
    /**
     * Apply categorization rules to a list of transactions.
     * 
     * @param transactions The transactions to categorize
     * @return The list of categorized transactions
     */
    List<Transaction> categorizeTransactions(List<Transaction> transactions);
    
    /**
     * Find the best matching category for a transaction based on its description.
     * 
     * @param transaction The transaction to find a category for
     * @return The best matching category, or null if no match found
     */
    TransactionCategory findBestMatchingCategory(Transaction transaction);
    
    /**
     * Train the categorization system based on existing categorized transactions.
     * This will update category keywords based on the descriptions of transactions
     * that have been manually categorized.
     * 
     * @return Number of categories updated
     */
    int trainCategorizationSystem();
    
    /**
     * Add a keyword to a category for automatic categorization.
     * 
     * @param categoryId The category ID
     * @param keyword The keyword to add
     * @return The updated category
     * @throws CategoryException if there's an issue adding the keyword
     */
    TransactionCategory addKeyword(long categoryId, String keyword) throws CategoryException;
    
    /**
     * Remove a keyword from a category.
     * 
     * @param categoryId The category ID
     * @param keyword The keyword to remove
     * @return The updated category
     * @throws CategoryException if there's an issue removing the keyword
     */
    TransactionCategory removeKeyword(long categoryId, String keyword) throws CategoryException;
    
    /**
     * Get all available keywords for all categories.
     * 
     * @return Map of category IDs to their keywords
     */
    java.util.Map<Long, List<String>> getAllKeywords();
}