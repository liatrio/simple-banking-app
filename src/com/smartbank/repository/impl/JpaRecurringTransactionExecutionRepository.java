package com.smartbank.repository.impl;

import com.smartbank.repository.RecurringTransactionExecutionRepository;
import com.smartbank.service.recurring.RecurringTransactionExecution;
import com.smartbank.service.recurring.RecurringTransactionResult;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of the RecurringTransactionExecutionRepository interface.
 */
public class JpaRecurringTransactionExecutionRepository implements RecurringTransactionExecutionRepository {
    private static final Logger LOGGER = Logger.getLogger(JpaRecurringTransactionExecutionRepository.class.getName());
    
    private final EntityManager entityManager;
    
    public JpaRecurringTransactionExecutionRepository() {
        this.entityManager = JPAUtil.getEntityManager();
    }
    
    @Override
    public RecurringTransactionExecution save(RecurringTransactionExecution execution) {
        try {
            entityManager.getTransaction().begin();
            
            // For now, we'll create a simple record in a more basic table
            // In a full implementation, we would use a proper entity class
            Query query = entityManager.createNativeQuery(
                    "INSERT INTO recurring_transaction_executions " +
                    "(recurring_transaction_id, transaction_id, execution_time, status, message) " +
                    "VALUES (?, ?, ?, ?, ?)");
            
            query.setParameter(1, execution.getRecurringTransactionId());
            query.setParameter(2, execution.getTransactionId());
            query.setParameter(3, execution.getExecutionTime());
            query.setParameter(4, execution.getStatus().name());
            query.setParameter(5, execution.getMessage());
            
            int result = query.executeUpdate();
            entityManager.getTransaction().commit();
            
            // Retrieve the generated ID if available
            if (result > 0) {
                Query idQuery = entityManager.createNativeQuery(
                        "SELECT LAST_INSERT_ID()");
                long executionId = ((Number) idQuery.getSingleResult()).longValue();
                
                return new RecurringTransactionExecution(
                        executionId,
                        execution.getRecurringTransactionId(),
                        execution.getTransactionId(),
                        execution.getExecutionTime(),
                        execution.getStatus(),
                        execution.getMessage());
            }
            
            return execution;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving recurring transaction execution", e);
            throw e;
        }
    }
    
    @Override
    public RecurringTransactionExecution createFromResult(RecurringTransactionResult result) {
        long transactionId = 0;
        if (result.getTransaction() != null) {
            transactionId = result.getTransaction().getTransactionId();
        }
        
        RecurringTransactionExecution execution = new RecurringTransactionExecution(
                0, // ID will be generated
                result.getRecurringTransactionId(),
                transactionId,
                result.getExecutionTime(),
                result.getStatus(),
                result.getMessage());
        
        return save(execution);
    }
    
    @Override
    public List<RecurringTransactionExecution> findByRecurringTransactionId(long recurringTransactionId) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT id, recurring_transaction_id, transaction_id, execution_time, status, message " +
                    "FROM recurring_transaction_executions " +
                    "WHERE recurring_transaction_id = ? " +
                    "ORDER BY execution_time DESC");
            
            query.setParameter(1, recurringTransactionId);
            
            List<Object[]> results = query.getResultList();
            List<RecurringTransactionExecution> executions = new ArrayList<>();
            
            for (Object[] row : results) {
                long id = ((Number) row[0]).longValue();
                long rtId = ((Number) row[1]).longValue();
                long txId = ((Number) row[2]).longValue();
                LocalDateTime executionTime = (LocalDateTime) row[3];
                RecurringTransactionResult.Status status = RecurringTransactionResult.Status.valueOf((String) row[4]);
                String message = (String) row[5];
                
                executions.add(new RecurringTransactionExecution(id, rtId, txId, executionTime, status, message));
            }
            
            return executions;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding execution records by recurring transaction ID", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<RecurringTransactionExecution> findByRecurringTransactionIdAndDateRange(
            long recurringTransactionId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT id, recurring_transaction_id, transaction_id, execution_time, status, message " +
                    "FROM recurring_transaction_executions " +
                    "WHERE recurring_transaction_id = ? " +
                    "AND execution_time >= ? " +
                    "AND execution_time <= ? " +
                    "ORDER BY execution_time DESC");
            
            query.setParameter(1, recurringTransactionId);
            query.setParameter(2, startDateTime);
            query.setParameter(3, endDateTime);
            
            List<Object[]> results = query.getResultList();
            List<RecurringTransactionExecution> executions = new ArrayList<>();
            
            for (Object[] row : results) {
                long id = ((Number) row[0]).longValue();
                long rtId = ((Number) row[1]).longValue();
                long txId = ((Number) row[2]).longValue();
                LocalDateTime executionTime = (LocalDateTime) row[3];
                RecurringTransactionResult.Status status = RecurringTransactionResult.Status.valueOf((String) row[4]);
                String message = (String) row[5];
                
                executions.add(new RecurringTransactionExecution(id, rtId, txId, executionTime, status, message));
            }
            
            return executions;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding execution records by recurring transaction ID and date range", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<RecurringTransactionExecution> findByStatus(RecurringTransactionResult.Status status) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT id, recurring_transaction_id, transaction_id, execution_time, status, message " +
                    "FROM recurring_transaction_executions " +
                    "WHERE status = ? " +
                    "ORDER BY execution_time DESC");
            
            query.setParameter(1, status.name());
            
            List<Object[]> results = query.getResultList();
            List<RecurringTransactionExecution> executions = new ArrayList<>();
            
            for (Object[] row : results) {
                long id = ((Number) row[0]).longValue();
                long rtId = ((Number) row[1]).longValue();
                long txId = ((Number) row[2]).longValue();
                LocalDateTime executionTime = (LocalDateTime) row[3];
                RecurringTransactionResult.Status execStatus = RecurringTransactionResult.Status.valueOf((String) row[4]);
                String message = (String) row[5];
                
                executions.add(new RecurringTransactionExecution(id, rtId, txId, executionTime, execStatus, message));
            }
            
            return executions;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding execution records by status", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<RecurringTransactionExecution> findByTransactionId(long transactionId) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT id, recurring_transaction_id, transaction_id, execution_time, status, message " +
                    "FROM recurring_transaction_executions " +
                    "WHERE transaction_id = ? " +
                    "ORDER BY execution_time DESC");
            
            query.setParameter(1, transactionId);
            
            List<Object[]> results = query.getResultList();
            List<RecurringTransactionExecution> executions = new ArrayList<>();
            
            for (Object[] row : results) {
                long id = ((Number) row[0]).longValue();
                long rtId = ((Number) row[1]).longValue();
                long txId = ((Number) row[2]).longValue();
                LocalDateTime executionTime = (LocalDateTime) row[3];
                RecurringTransactionResult.Status status = RecurringTransactionResult.Status.valueOf((String) row[4]);
                String message = (String) row[5];
                
                executions.add(new RecurringTransactionExecution(id, rtId, txId, executionTime, status, message));
            }
            
            return executions;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding execution records by transaction ID", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public int getExecutionCountByDateAndStatus(LocalDate date, RecurringTransactionResult.Status status) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) " +
                    "FROM recurring_transaction_executions " +
                    "WHERE DATE(execution_time) = ? " +
                    "AND status = ?");
            
            query.setParameter(1, date);
            query.setParameter(2, status.name());
            
            Number count = (Number) query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting execution count by date and status", e);
            return 0;
        }
    }
    
    @Override
    public int getExecutionCountByRecurringTransactionIdAndStatus(
            long recurringTransactionId, RecurringTransactionResult.Status status) {
        try {
            // Using native query for simplicity
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) " +
                    "FROM recurring_transaction_executions " +
                    "WHERE recurring_transaction_id = ? " +
                    "AND status = ?");
            
            query.setParameter(1, recurringTransactionId);
            query.setParameter(2, status.name());
            
            Number count = (Number) query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting execution count by recurring transaction ID and status", e);
            return 0;
        }
    }
    
    @Override
    public int deleteByRecurringTransactionId(long recurringTransactionId) {
        try {
            entityManager.getTransaction().begin();
            
            Query query = entityManager.createNativeQuery(
                    "DELETE FROM recurring_transaction_executions " +
                    "WHERE recurring_transaction_id = ?");
            
            query.setParameter(1, recurringTransactionId);
            
            int result = query.executeUpdate();
            
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting execution records by recurring transaction ID", e);
            throw e;
        }
    }
}