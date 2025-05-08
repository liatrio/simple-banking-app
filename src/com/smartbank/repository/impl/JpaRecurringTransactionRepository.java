package com.smartbank.repository.impl;

import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.Transaction;
import com.smartbank.repository.RecurringTransactionRepository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of the RecurringTransactionRepository interface.
 */
public class JpaRecurringTransactionRepository implements RecurringTransactionRepository {
    private static final Logger LOGGER = Logger.getLogger(JpaRecurringTransactionRepository.class.getName());
    
    private final EntityManager entityManager;
    
    public JpaRecurringTransactionRepository() {
        this.entityManager = JPAUtil.getEntityManager();
    }
    
    @Override
    public RecurringTransaction save(RecurringTransaction recurringTransaction) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(recurringTransaction);
            entityManager.getTransaction().commit();
            LOGGER.info("Saved recurring transaction with ID: " + recurringTransaction.getRecurringTransactionId());
            return recurringTransaction;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving recurring transaction", e);
            throw e;
        }
    }
    
    @Override
    public RecurringTransaction update(RecurringTransaction recurringTransaction) {
        try {
            entityManager.getTransaction().begin();
            RecurringTransaction updatedRecurringTransaction = entityManager.merge(recurringTransaction);
            entityManager.getTransaction().commit();
            LOGGER.info("Updated recurring transaction with ID: " + recurringTransaction.getRecurringTransactionId());
            return updatedRecurringTransaction;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating recurring transaction", e);
            throw e;
        }
    }
    
    @Override
    public Optional<RecurringTransaction> findById(long recurringTransactionId) {
        try {
            RecurringTransaction recurringTransaction = entityManager.find(RecurringTransaction.class, recurringTransactionId);
            return Optional.ofNullable(recurringTransaction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transaction by ID", e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<RecurringTransaction> findAll() {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all recurring transactions", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findBySourceAccountNumber(long sourceAccountNumber) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.sourceAccountNumber = :sourceAccountNumber ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("sourceAccountNumber", sourceAccountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by source account number", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findByTargetAccountNumber(long targetAccountNumber) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.targetAccountNumber = :targetAccountNumber ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("targetAccountNumber", targetAccountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by target account number", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findByAccountNumber(long accountNumber) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.sourceAccountNumber = :accountNumber OR rt.targetAccountNumber = :accountNumber ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("accountNumber", accountNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by account number", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findByType(Transaction.Type type) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.type = :type ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by type", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findByStatus(RecurringTransaction.Status status) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.status = :status ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by status", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findDueTransactions(LocalDate date) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.status = :status AND rt.nextExecutionDate <= :date ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("status", RecurringTransaction.Status.ACTIVE);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding due recurring transactions", e);
            throw e;
        }
    }
    
    @Override
    public List<RecurringTransaction> findByFrequency(RecurringTransaction.Frequency frequency) {
        try {
            TypedQuery<RecurringTransaction> query = entityManager.createQuery(
                    "SELECT rt FROM RecurringTransaction rt WHERE rt.frequency = :frequency ORDER BY rt.nextExecutionDate ASC", 
                    RecurringTransaction.class);
            query.setParameter("frequency", frequency);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding recurring transactions by frequency", e);
            throw e;
        }
    }
    
    @Override
    public boolean deleteById(long recurringTransactionId) {
        try {
            entityManager.getTransaction().begin();
            RecurringTransaction recurringTransaction = entityManager.find(RecurringTransaction.class, recurringTransactionId);
            if (recurringTransaction != null) {
                entityManager.remove(recurringTransaction);
                entityManager.getTransaction().commit();
                LOGGER.info("Deleted recurring transaction with ID: " + recurringTransactionId);
                return true;
            } else {
                entityManager.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting recurring transaction", e);
            throw e;
        }
    }
}