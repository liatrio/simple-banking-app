package com.smartbank.service.search;

import com.smartbank.model.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for advanced transaction searching functionality.
 */
public interface TransactionSearchService {
    
    /**
     * Search for transactions based on the provided search criteria.
     * @param criteria The search criteria
     * @return A paginated search result containing the matching transactions
     */
    SearchResult<Transaction> searchTransactions(TransactionSearchCriteria criteria);
    
    /**
     * Get a list of recent search criteria used by a specific user.
     * @param userId The user ID
     * @param limit Maximum number of search histories to return
     * @return A list of recent search criteria
     */
    List<TransactionSearchCriteria> getRecentSearchCriteria(String userId, int limit);
    
    /**
     * Save a search criteria to the user's search history.
     * @param userId The user ID
     * @param criteria The search criteria to save
     * @return The saved search history ID
     */
    long saveSearchCriteria(String userId, TransactionSearchCriteria criteria);
    
    /**
     * Delete a search criteria from the user's search history.
     * @param searchHistoryId The search history ID to delete
     * @return true if successfully deleted, false otherwise
     */
    boolean deleteSearchCriteria(long searchHistoryId);
    
    /**
     * Get a search criteria by its ID.
     * @param searchHistoryId The search history ID
     * @return An Optional containing the search criteria if found, or empty if not found
     */
    Optional<TransactionSearchCriteria> getSearchCriteriaById(long searchHistoryId);
    
    /**
     * Export a list of transactions to a file in the specified format.
     * @param transactions The list of transactions to export
     * @param format The export format (e.g., "csv", "pdf", "json")
     * @param filePath The path where the exported file should be saved
     * @return true if the export was successful, false otherwise
     */
    boolean exportTransactions(List<Transaction> transactions, String format, String filePath);
    
    /**
     * Get a list of suggestions for search terms based on user's transaction history.
     * @param userId The user ID
     * @param prefix The prefix to match suggestions against (can be empty for all suggestions)
     * @param category The category of suggestions (e.g., "merchantName", "description", "category")
     * @param limit Maximum number of suggestions to return
     * @return A list of suggestions
     */
    List<String> getSuggestions(String userId, String prefix, String category, int limit);
}