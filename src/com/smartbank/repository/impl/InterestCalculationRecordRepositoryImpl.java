package com.smartbank.repository.impl;

import com.smartbank.repository.InterestCalculationRecordRepository;
import com.smartbank.service.interest.InterestCalculationRecord;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of InterestCalculationRecordRepository.
 */
public class InterestCalculationRecordRepositoryImpl implements InterestCalculationRecordRepository {
    private static final Logger LOGGER = Logger.getLogger(InterestCalculationRecordRepositoryImpl.class.getName());
    
    private final EntityManager entityManager;
    
    public InterestCalculationRecordRepositoryImpl() {
        this.entityManager = JPAUtil.getEntityManager();
    }
    
    @Override
    public InterestCalculationRecord save(InterestCalculationRecord record) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(record);
            entityManager.getTransaction().commit();
            return record;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving interest calculation record", e);
            throw e;
        }
    }
    
    @Override
    public InterestCalculationRecord update(InterestCalculationRecord record) {
        try {
            entityManager.getTransaction().begin();
            InterestCalculationRecord updatedRecord = entityManager.merge(record);
            entityManager.getTransaction().commit();
            return updatedRecord;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating interest calculation record", e);
            throw e;
        }
    }
    
    @Override
    public Optional<InterestCalculationRecord> findById(long recordId) {
        try {
            InterestCalculationRecord record = entityManager.find(InterestCalculationRecord.class, recordId);
            return Optional.ofNullable(record);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation record by ID", e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findByAccountNumber(long accountNumber) {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r WHERE r.accountNumber = :accountNumber ORDER BY r.date DESC",
                    InterestCalculationRecord.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation records by account number", e);
            throw e;
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findByAccountNumberAndDateRange(long accountNumber, LocalDate startDate, LocalDate endDate) {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r " +
                    "WHERE r.accountNumber = :accountNumber " +
                    "AND r.date >= :startDate " +
                    "AND r.date <= :endDate " +
                    "ORDER BY r.date DESC",
                    InterestCalculationRecord.class);
            query.setParameter("accountNumber", accountNumber);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation records by account number and date range", e);
            throw e;
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findByOperationType(InterestCalculationRecord.OperationType operationType) {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r WHERE r.operationType = :operationType ORDER BY r.date DESC",
                    InterestCalculationRecord.class);
            query.setParameter("operationType", operationType);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation records by operation type", e);
            throw e;
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findByDate(LocalDate date) {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r WHERE r.date = :date",
                    InterestCalculationRecord.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation records by date", e);
            throw e;
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r " +
                    "WHERE r.date >= :startDate " +
                    "AND r.date <= :endDate " +
                    "ORDER BY r.date DESC",
                    InterestCalculationRecord.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding interest calculation records by date range", e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteById(long recordId) {
        try {
            entityManager.getTransaction().begin();
            InterestCalculationRecord record = entityManager.find(InterestCalculationRecord.class, recordId);
            if (record != null) {
                entityManager.remove(record);
                entityManager.getTransaction().commit();
                return true;
            } else {
                entityManager.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting interest calculation record", e);
            throw e;
        }
    }
    
    @Override
    public int deleteByAccountNumber(long accountNumber) {
        try {
            entityManager.getTransaction().begin();
            int deletedCount = entityManager.createQuery(
                    "DELETE FROM InterestCalculationRecord r WHERE r.accountNumber = :accountNumber")
                    .setParameter("accountNumber", accountNumber)
                    .executeUpdate();
            entityManager.getTransaction().commit();
            return deletedCount;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting interest calculation records by account number", e);
            throw e;
        }
    }
    
    @Override
    public List<InterestCalculationRecord> findAll() {
        try {
            TypedQuery<InterestCalculationRecord> query = entityManager.createQuery(
                    "SELECT r FROM InterestCalculationRecord r ORDER BY r.date DESC",
                    InterestCalculationRecord.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all interest calculation records", e);
            throw e;
        }
    }
}