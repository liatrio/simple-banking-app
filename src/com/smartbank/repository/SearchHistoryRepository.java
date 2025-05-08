package com.smartbank.repository;

import com.smartbank.model.SearchHistory;
import java.util.List;

/**
 * Repository interface for SearchHistory entity operations.
 */
public interface SearchHistoryRepository extends Repository<SearchHistory, Long> {
    
    /**
     * Find search history entries by user ID.
     * @param userId The user ID
     * @return A list of search history entries for the specified user
     */
    List<SearchHistory> findByUserId(String userId);
    
    /**
     * Find search history entries by user ID and search type.
     * @param userId The user ID
     * @param searchType The search type
     * @return A list of search history entries for the specified user and search type
     */
    List<SearchHistory> findByUserIdAndSearchType(String userId, String searchType);
    
    /**
     * Find recent search history entries by user ID and search type, ordered by last used date.
     * @param userId The user ID
     * @param searchType The search type
     * @param limit The maximum number of entries to return
     * @return A list of recent search history entries
     */
    List<SearchHistory> findRecentByUserIdAndType(String userId, String searchType, int limit);
    
    /**
     * Find favorite search history entries by user ID.
     * @param userId The user ID
     * @return A list of favorite search history entries for the specified user
     */
    List<SearchHistory> findFavoritesByUserId(String userId);
    
    /**
     * Delete all search history entries older than the specified number of days.
     * @param days The number of days
     * @return The number of deleted entries
     */
    int deleteOlderThan(int days);
}