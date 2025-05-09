package com.smartbank.repository.impl;

import com.smartbank.model.Transaction;
import com.smartbank.repository.TransactionRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * JPA implementation of the TransactionRepository interface.
 */
public class JpaTransactionRepository extends JpaRepository<Transaction, Long> implements TransactionRepository {
    private static final Logger LOGGER = Logger.getLogger(JpaTransactionRepository.class.getName());

    @Override
    public List<Transaction> findByAccountNumber(long accountNumber) {
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        });
    }

    @Override
    public List<Transaction> findByType(Transaction.Type type) {
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.type = :type ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("type", type);
            return query.getResultList();
        });
    }

    @Override
    public List<Transaction> findByDateRange(Date startDate, Date endDate) {
        try {
            // Get all transactions, then filter by date in Java code
            // This avoids potential date format issues in SQLite
            List<Transaction> allTransactions = findAll();
            
            // Filter by date in Java code
            return allTransactions.stream()
                .filter(tx -> isDateInRange(tx.getTimestamp(), startDate, endDate))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // DESC order
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding transactions by date range: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Transaction> findByAccountNumberAndType(long accountNumber, Transaction.Type type) {
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.type = :type ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("type", type);
            return query.getResultList();
        });
    }

    @Override
    public List<Transaction> findByAccountNumberAndDateRange(long accountNumber, Date startDate, Date endDate) {
        try {
            // Get all transactions for the account, then filter by date in Java code
            // This avoids potential date format issues in SQLite
            List<Transaction> allAccountTransactions = findByAccountNumber(accountNumber);
            
            // Filter by date in Java code
            return allAccountTransactions.stream()
                .filter(tx -> isDateInRange(tx.getTimestamp(), startDate, endDate))
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding transactions by account and date range: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    // Helper method to check if a date is within a range
    private boolean isDateInRange(Date date, Date startDate, Date endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return (date.equals(startDate) || date.after(startDate)) && 
              (date.equals(endDate) || date.before(endDate));
    }
    
    @Override
    public List<Transaction> findRecentByAccountAndType(long accountNumber, Transaction.Type type, int limit) {
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.type = :type ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("type", type);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
    
    @Override
    public List<Transaction> findRecentByAccount(long accountNumber, int limit) {
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("accountNumber", accountNumber);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
}