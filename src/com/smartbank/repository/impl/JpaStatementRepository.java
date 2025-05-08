package com.smartbank.repository.impl;

import com.smartbank.repository.StatementRepository;
import com.smartbank.service.statement.StatementRecord;
import com.smartbank.service.statement.StatementType;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * JPA implementation of the StatementRepository interface.
 */
public class JpaStatementRepository extends JpaRepository<StatementRecord, Long> implements StatementRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JpaStatementRepository.class.getName());
    
    /**
     * Creates a new JPA statement repository.
     */
    public JpaStatementRepository() {
        // Using default constructor from JpaRepository
    }
    
    @Override
    public List<StatementRecord> findByAccountNumber(long accountNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.accountNumber = :accountNumber ORDER BY s.generationDate DESC",
                    StatementRecord.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<StatementRecord> findByAccountNumberAndDateRange(long accountNumber, Date startDate, Date endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.accountNumber = :accountNumber " +
                    "AND s.startDate >= :startDate AND s.endDate <= :endDate ORDER BY s.generationDate DESC",
                    StatementRecord.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<StatementRecord> findByUserId(String userId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.userId = :userId ORDER BY s.generationDate DESC",
                    StatementRecord.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<StatementRecord> findByStatementType(StatementType statementType) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.statementType = :statementType ORDER BY s.generationDate DESC",
                    StatementRecord.class);
            query.setParameter("statementType", statementType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<StatementRecord> findPendingEmailDelivery() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.emailRecipient IS NOT NULL " +
                    "AND s.emailDelivered = false ORDER BY s.generationDate ASC",
                    StatementRecord.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    public int countByAccountNumber(long accountNumber) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT COUNT(s) FROM StatementRecord s WHERE s.accountNumber = :accountNumber");
            query.setParameter("accountNumber", accountNumber);
            return ((Long) query.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    @Override
    public StatementRecord findMostRecentByAccountNumber(long accountNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<StatementRecord> query = em.createQuery(
                    "SELECT s FROM StatementRecord s WHERE s.accountNumber = :accountNumber " +
                    "ORDER BY s.generationDate DESC",
                    StatementRecord.class);
            query.setParameter("accountNumber", accountNumber);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    
    @Override
    public int pruneOldStatements(long accountNumber, int keepCount) {
        // Get all statements for the account, ordered by generationDate descending
        List<StatementRecord> statements = findByAccountNumber(accountNumber);
        
        int deleted = 0;
        
        // If we have more statements than we want to keep
        if (statements.size() > keepCount) {
            // Skip the first 'keepCount' statements (the most recent ones)
            for (int i = keepCount; i < statements.size(); i++) {
                StatementRecord record = statements.get(i);
                deleteById(record.getStatementId());
                deleted++;
            }
        }
        
        return deleted;
    }
}