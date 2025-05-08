package com.smartbank.repository.impl;

import com.smartbank.model.CreditHistory;
import com.smartbank.repository.CreditHistoryRepository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of the CreditHistoryRepository interface.
 */
public class JpaCreditHistoryRepository extends JpaRepository<CreditHistory, Long> implements CreditHistoryRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JpaCreditHistoryRepository.class.getName());
    
    public JpaCreditHistoryRepository() {
        // The entityClass will be derived from the generic type in the parent constructor
    }
    
    @Override
    public List<CreditHistory> findByAccountNumber(long accountNumber) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit history by account number", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findByEventType(CreditHistory.EventType eventType) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.eventType = :eventType ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("eventType", eventType);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit history by event type", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.eventDate BETWEEN :startDate AND :endDate ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("startDate", startDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            query.setParameter("endDate", endDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit history by date range", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findByAccountNumberAndEventType(long accountNumber, CreditHistory.EventType eventType) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber AND ch.eventType = :eventType ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("eventType", eventType);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit history by account number and event type", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findByAccountNumberAndDateRange(long accountNumber, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber AND ch.eventDate BETWEEN :startDate AND :endDate ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("startDate", startDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            query.setParameter("endDate", endDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit history by account number and date range", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findRecentByAccount(long accountNumber, int limit) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("accountNumber", accountNumber);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recent credit history by account", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditHistory> findRecentByAccountAndEventType(long accountNumber, CreditHistory.EventType eventType, int limit) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditHistory> query = em.createQuery(
                    "SELECT ch FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber AND ch.eventType = :eventType ORDER BY ch.eventDate DESC",
                    CreditHistory.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("eventType", eventType);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recent credit history by account and event type", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public int countByAccountNumberAndEventType(long accountNumber, CreditHistory.EventType eventType) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(ch) FROM CreditHistory ch WHERE ch.accountNumber = :accountNumber AND ch.eventType = :eventType",
                    Long.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("eventType", eventType);
            return query.getSingleResult().intValue();
        } catch (NoResultException e) {
            return 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting credit history by account number and event type", e);
            return 0;
        } finally {
            em.close();
        }
    }
}