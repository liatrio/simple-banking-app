package com.smartbank.repository;

import com.smartbank.model.Transaction;
import com.smartbank.service.search.SearchResult;
import com.smartbank.service.search.TransactionSearchCriteria;

/**
 * Repository interface for advanced transaction search operations.
 */
public interface TransactionSearchRepository {
    
    /**
     * Search for transactions based on the provided search criteria.
     * @param criteria The search criteria
     * @return A paginated search result containing the matching transactions
     */
    SearchResult<Transaction> searchTransactions(TransactionSearchCriteria criteria);
    
    /**
     * Get a suggestion list for a specific field from the transaction history.
     * @param userId The user ID (to limit suggestions to user's transactions)
     * @param prefix The prefix to match (can be empty for all values)
     * @param field The field to get suggestions for (e.g., "merchantName", "description")
     * @param limit Maximum number of suggestions to return
     * @return A list of suggested values
     */
    java.util.List<String> getSuggestions(String userId, String prefix, String field, int limit);
    
    /**
     * Count transactions matching the provided search criteria.
     * @param criteria The search criteria
     * @return The count of matching transactions
     */
    long countTransactions(TransactionSearchCriteria criteria);
}