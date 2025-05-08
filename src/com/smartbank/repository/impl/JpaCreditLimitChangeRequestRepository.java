package com.smartbank.repository.impl;

import com.smartbank.model.CreditLimitChangeRequest;
import com.smartbank.repository.CreditLimitChangeRequestRepository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of the CreditLimitChangeRequestRepository interface.
 */
public class JpaCreditLimitChangeRequestRepository extends JpaRepository<CreditLimitChangeRequest, Long> implements CreditLimitChangeRequestRepository {
    
    private static final Logger LOGGER = Logger.getLogger(JpaCreditLimitChangeRequestRepository.class.getName());
    
    public JpaCreditLimitChangeRequestRepository() {
        // The entityClass will be derived from the generic type in the parent constructor
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByAccountNumber(long accountNumber) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.accountNumber = :accountNumber ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by account number", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByStatus(CreditLimitChangeRequest.Status status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.status = :status ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by status", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByAccountNumberAndStatus(long accountNumber, CreditLimitChangeRequest.Status status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.accountNumber = :accountNumber AND r.status = :status ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by account number and status", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByRequestDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.requestDate BETWEEN :startDate AND :endDate ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("startDate", startDateStr);
            query.setParameter("endDate", endDateStr);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by request date range", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByRequestedBy(String userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.requestedBy = :userId ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by requested user", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByApprovedBy(String userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.approvedBy = :userId ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by approved user", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findByRejectedBy(String userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.rejectedBy = :userId ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by rejected user", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findBySource(CreditLimitChangeRequest.Source source) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.source = :source ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("source", source);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by source", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findBySourceAndStatus(CreditLimitChangeRequest.Source source, CreditLimitChangeRequest.Status status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.source = :source AND r.status = :status ORDER BY r.requestDate DESC",
                    CreditLimitChangeRequest.class);
            query.setParameter("source", source);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding credit limit change requests by source and status", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<CreditLimitChangeRequest> findPendingRequestsOlderThan(LocalDateTime date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            TypedQuery<CreditLimitChangeRequest> query = em.createQuery(
                    "SELECT r FROM CreditLimitChangeRequest r WHERE r.status = :status AND r.requestDate < :date ORDER BY r.requestDate ASC",
                    CreditLimitChangeRequest.class);
            query.setParameter("status", CreditLimitChangeRequest.Status.PENDING);
            query.setParameter("date", dateStr);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding pending credit limit change requests older than date", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    @Override
    public int countByAccountNumberAndStatus(long accountNumber, CreditLimitChangeRequest.Status status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM CreditLimitChangeRequest r WHERE r.accountNumber = :accountNumber AND r.status = :status",
                    Long.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("status", status);
            return query.getSingleResult().intValue();
        } catch (NoResultException e) {
            return 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting credit limit change requests by account number and status", e);
            return 0;
        } finally {
            em.close();
        }
    }
}