package com.smartbank.repository.impl;

import com.smartbank.repository.Repository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract JPA implementation of the Repository interface.
 * @param <T> The entity type
 * @param <ID> The type of the entity's ID
 */
public abstract class JpaRepository<T, ID> implements Repository<T, ID> {
    protected final Class<T> entityClass;
    private static final Logger LOGGER = Logger.getLogger(JpaRepository.class.getName());

    @SuppressWarnings("unchecked")
    public JpaRepository() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected EntityManager getEntityManager() {
        return JPAUtil.getEntityManager();
    }

    @Override
    public T save(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Use merge instead of persist to handle both new and existing entities
            T savedEntity = em.merge(entity);
            
            tx.commit();
            return savedEntity;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving entity: " + e.getMessage(), e);
            throw new RuntimeException("Error saving entity", e);
        } finally {
            em.close();
        }
    }

    @Override
    public T update(T entity) {
        // Now simply delegates to save() since both operations use merge
        return save(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding entity by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error finding entity by ID", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            return em.createQuery(cq).getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all entities: " + e.getMessage(), e);
            throw new RuntimeException("Error finding all entities", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting entity: " + e.getMessage(), e);
            throw new RuntimeException("Error deleting entity", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean deleteById(ID id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                tx.commit();
                return true;
            } else {
                tx.rollback();
                return false;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting entity by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error deleting entity by ID", e);
        } finally {
            em.close();
        }
    }

    /**
     * Execute a JPA transaction with a provided function.
     * @param function The function to execute within the transaction
     * @param <R> The return type of the function
     * @return The result of the function
     */
    protected <R> R executeInTransaction(TransactionFunction<EntityManager, R> function) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Transaction error: " + e.getMessage(), e);
            throw new RuntimeException("Transaction error", e);
        } finally {
            em.close();
        }
    }

    /**
     * Functional interface for executing code within a transaction.
     * @param <T> The input type
     * @param <R> The return type
     */
    @FunctionalInterface
    protected interface TransactionFunction<T, R> {
        R apply(T t) throws Exception;
    }
}