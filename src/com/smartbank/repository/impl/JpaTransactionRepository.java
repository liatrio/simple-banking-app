package com.smartbank.repository.impl;

import com.smartbank.model.Transaction;
import com.smartbank.repository.TransactionRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        });
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
        return executeInTransaction(em -> {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC",
                    Transaction.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        });
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
}