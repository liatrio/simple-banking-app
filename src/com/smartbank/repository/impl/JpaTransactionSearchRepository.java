package com.smartbank.repository.impl;

import com.smartbank.model.Transaction;
import com.smartbank.repository.TransactionSearchRepository;
import com.smartbank.service.search.SearchResult;
import com.smartbank.service.search.TransactionSearchCriteria;
import com.smartbank.service.search.TransactionSearchCriteria.SortDirection;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JPA implementation of the TransactionSearchRepository interface.
 * Provides advanced search functionality for transactions.
 */
public class JpaTransactionSearchRepository extends JpaRepository<Transaction, Long> implements TransactionSearchRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JpaTransactionSearchRepository.class.getName());
    
    @Override
    public SearchResult<Transaction> searchTransactions(TransactionSearchCriteria criteria) {
        return executeInTransaction(em -> {
            // Build the query
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
            Root<Transaction> transaction = query.from(Transaction.class);
            
            // Apply filters
            List<Predicate> predicates = buildPredicates(criteria, cb, transaction);
            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            
            // Apply sorting
            query.orderBy(buildOrderBy(criteria, cb, transaction));
            
            // Execute query with pagination
            TypedQuery<Transaction> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            typedQuery.setMaxResults(criteria.getPageSize());
            List<Transaction> results = typedQuery.getResultList();
            
            // Count total elements
            long totalElements = countTransactions(criteria);
            
            // Create and return the search result
            return new SearchResult<>(results, criteria.getPageNumber(), criteria.getPageSize(), totalElements);
        });
    }
    
    @Override
    public List<String> getSuggestions(String userId, String prefix, String field, int limit) {
        return executeInTransaction(em -> {
            String fieldName = getSuggestionFieldName(field);
            
            String queryStr = "SELECT DISTINCT t." + fieldName + " FROM Transaction t";
            
            // Add user filter if userId is provided
            if (userId != null && !userId.isEmpty()) {
                queryStr += " JOIN Account a ON t.accountNumber = a.accountNumber" +
                            " WHERE a.userId = :userId";
                
                // Add prefix filter if prefix is provided
                if (prefix != null && !prefix.isEmpty()) {
                    queryStr += " AND LOWER(t." + fieldName + ") LIKE LOWER(:prefix)";
                }
            } else if (prefix != null && !prefix.isEmpty()) {
                queryStr += " WHERE LOWER(t." + fieldName + ") LIKE LOWER(:prefix)";
            }
            
            queryStr += " AND t." + fieldName + " IS NOT NULL" +
                       " ORDER BY t." + fieldName;
            
            TypedQuery<String> query = em.createQuery(queryStr, String.class);
            
            if (userId != null && !userId.isEmpty()) {
                query.setParameter("userId", userId);
            }
            
            if (prefix != null && !prefix.isEmpty()) {
                query.setParameter("prefix", prefix + "%");
            }
            
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
    
    private String getSuggestionFieldName(String field) {
        switch (field.toLowerCase()) {
            case "merchant":
            case "merchantname":
                return "merchantName";
            case "description":
                return "description";
            default:
                throw new IllegalArgumentException("Unsupported suggestion field: " + field);
        }
    }
    
    @Override
    public long countTransactions(TransactionSearchCriteria criteria) {
        return executeInTransaction(em -> {
            // Build the count query
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Transaction> transaction = countQuery.from(Transaction.class);
            
            // Apply the same filters as the main query
            List<Predicate> predicates = buildPredicates(criteria, cb, transaction);
            if (!predicates.isEmpty()) {
                countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            
            // Count results
            countQuery.select(cb.count(transaction));
            return em.createQuery(countQuery).getSingleResult();
        });
    }
    
    private List<Predicate> buildPredicates(TransactionSearchCriteria criteria, CriteriaBuilder cb, Root<Transaction> transaction) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Account number filter
        if (criteria.getAccountNumber() != null) {
            predicates.add(cb.equal(transaction.get("accountNumber"), criteria.getAccountNumber()));
        }
        
        // Transaction type filter
        if (criteria.getType() != null) {
            predicates.add(cb.equal(transaction.get("type"), criteria.getType()));
        }
        
        // Date range filter
        if (criteria.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("timestamp"), criteria.getStartDate()));
        }
        if (criteria.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("timestamp"), criteria.getEndDate()));
        }
        
        // Amount range filter
        if (criteria.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("amount"), criteria.getMinAmount()));
        }
        if (criteria.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("amount"), criteria.getMaxAmount()));
        }
        
        // Description filter (case-insensitive contains)
        if (criteria.getDescription() != null && !criteria.getDescription().isEmpty()) {
            predicates.add(cb.like(cb.lower(transaction.get("description")), 
                    "%" + criteria.getDescription().toLowerCase() + "%"));
        }
        
        // Merchant name filter (case-insensitive contains)
        if (criteria.getMerchantName() != null && !criteria.getMerchantName().isEmpty()) {
            predicates.add(cb.like(cb.lower(transaction.get("merchantName")), 
                    "%" + criteria.getMerchantName().toLowerCase() + "%"));
        }
        
        // Category filter
        if (criteria.getCategoryId() != null) {
            Join<Object, Object> categoryJoin = transaction.join("category");
            
            if (criteria.getIncludeChildCategories()) {
                // For including child categories, we would need recursive query capability
                // which is difficult with JPA Criteria. In a real implementation, we might:
                // 1. Pre-fetch all child category IDs and use an IN clause
                // 2. Use a native SQL query with recursive capabilities
                // 3. Use a graph traversal algorithm
                
                // Simplified approach for now - just match the exact category
                predicates.add(cb.equal(categoryJoin.get("categoryId"), criteria.getCategoryId()));
            } else {
                predicates.add(cb.equal(categoryJoin.get("categoryId"), criteria.getCategoryId()));
            }
        }
        
        // Recurring transaction filter
        if (criteria.isRecurring() != null) {
            predicates.add(cb.equal(transaction.get("isRecurring"), criteria.isRecurring()));
        }
        
        // Automatic categorization filter
        if (criteria.isCategorizedAutomatically() != null) {
            predicates.add(cb.equal(transaction.get("isCategorizedAutomatically"), 
                    criteria.isCategorizedAutomatically()));
        }
        
        // Excluded accounts filter
        if (!criteria.getExcludedAccounts().isEmpty()) {
            predicates.add(transaction.get("accountNumber").in(criteria.getExcludedAccounts()).not());
        }
        
        // Excluded types filter
        if (!criteria.getExcludedTypes().isEmpty()) {
            predicates.add(transaction.get("type").in(criteria.getExcludedTypes()).not());
        }
        
        // Excluded categories filter
        if (!criteria.getExcludedCategories().isEmpty()) {
            predicates.add(cb.or(
                    cb.isNull(transaction.get("category")),
                    transaction.join("category", JoinType.LEFT).get("categoryId").in(criteria.getExcludedCategories()).not()
            ));
        }
        
        return predicates;
    }
    
    private List<Order> buildOrderBy(TransactionSearchCriteria criteria, CriteriaBuilder cb, Root<Transaction> transaction) {
        List<Order> orders = new ArrayList<>();
        
        // Get the field to sort by
        Path<?> sortPath;
        switch (criteria.getSortBy()) {
            case "amount":
                sortPath = transaction.get("amount");
                break;
            case "type":
                sortPath = transaction.get("type");
                break;
            case "description":
                sortPath = transaction.get("description");
                break;
            case "merchantName":
                sortPath = transaction.get("merchantName");
                break;
            case "category":
                // This requires a join
                Join<Object, Object> categoryJoin = transaction.join("category", JoinType.LEFT);
                sortPath = categoryJoin.get("name");
                break;
            case "timestamp":
            default:
                sortPath = transaction.get("timestamp");
                break;
        }
        
        // Apply the sort direction
        if (criteria.getSortDirection() == SortDirection.ASCENDING) {
            orders.add(cb.asc(sortPath));
        } else {
            orders.add(cb.desc(sortPath));
        }
        
        // Always add a secondary sort by timestamp (desc) for consistent ordering
        if (!criteria.getSortBy().equals("timestamp")) {
            orders.add(cb.desc(transaction.get("timestamp")));
        }
        
        // Always add a sort by ID as final tiebreaker for completely consistent ordering
        orders.add(cb.desc(transaction.get("transactionId")));
        
        return orders;
    }
}