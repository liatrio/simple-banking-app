package com.smartbank.repository.impl;

import com.smartbank.model.SearchHistory;
import com.smartbank.repository.SearchHistoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * JPA implementation of the SearchHistoryRepository interface.
 */
public class JpaSearchHistoryRepository extends JpaRepository<SearchHistory, Long> implements SearchHistoryRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JpaSearchHistoryRepository.class.getName());
    
    @Override
    public List<SearchHistory> findByUserId(String userId) {
        return executeInTransaction(em -> {
            TypedQuery<SearchHistory> query = em.createQuery(
                    "SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId ORDER BY sh.lastUsedDate DESC",
                    SearchHistory.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        });
    }
    
    @Override
    public List<SearchHistory> findByUserIdAndSearchType(String userId, String searchType) {
        return executeInTransaction(em -> {
            TypedQuery<SearchHistory> query = em.createQuery(
                    "SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId AND sh.searchType = :searchType ORDER BY sh.lastUsedDate DESC",
                    SearchHistory.class);
            query.setParameter("userId", userId);
            query.setParameter("searchType", searchType);
            return query.getResultList();
        });
    }
    
    @Override
    public List<SearchHistory> findRecentByUserIdAndType(String userId, String searchType, int limit) {
        return executeInTransaction(em -> {
            TypedQuery<SearchHistory> query = em.createQuery(
                    "SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId AND sh.searchType = :searchType ORDER BY sh.lastUsedDate DESC",
                    SearchHistory.class);
            query.setParameter("userId", userId);
            query.setParameter("searchType", searchType);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
    
    @Override
    public List<SearchHistory> findFavoritesByUserId(String userId) {
        return executeInTransaction(em -> {
            TypedQuery<SearchHistory> query = em.createQuery(
                    "SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId AND sh.isFavorite = true ORDER BY sh.name ASC",
                    SearchHistory.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        });
    }
    
    @Override
    public int deleteOlderThan(int days) {
        return executeInTransaction(em -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date cutoffDate = calendar.getTime();
            
            int deletedCount = em.createQuery(
                    "DELETE FROM SearchHistory sh WHERE sh.lastUsedDate < :cutoffDate AND sh.isFavorite = false")
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
            
            LOGGER.info("Deleted " + deletedCount + " search history entries older than " + days + " days");
            return deletedCount;
        });
    }
}