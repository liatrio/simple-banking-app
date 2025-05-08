package com.smartbank.repository;

import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.impl.JpaRepository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the TransactionCategoryRepository.
 */
public class JpaTransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> implements TransactionCategoryRepository {
    
    public JpaTransactionCategoryRepository() {
        super();
    }

    @Override
    public Optional<TransactionCategory> findByName(String name) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TransactionCategory> query = em.createQuery(
                    "SELECT c FROM TransactionCategory c WHERE c.name = :name", 
                    TransactionCategory.class);
            query.setParameter("name", name);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<TransactionCategory> findRootCategories() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TransactionCategory> query = em.createQuery(
                    "SELECT c FROM TransactionCategory c WHERE c.parent IS NULL", 
                    TransactionCategory.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<TransactionCategory> findByParentId(Long parentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TransactionCategory> query = em.createQuery(
                    "SELECT c FROM TransactionCategory c WHERE c.parent.categoryId = :parentId", 
                    TransactionCategory.class);
            query.setParameter("parentId", parentId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<TransactionCategory> findSystemCategories() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TransactionCategory> query = em.createQuery(
                    "SELECT c FROM TransactionCategory c WHERE c.isSystem = true", 
                    TransactionCategory.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<TransactionCategory> findByKeyword(String keyword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TransactionCategory> query = em.createQuery(
                    "SELECT c FROM TransactionCategory c WHERE c.keywords LIKE :keyword", 
                    TransactionCategory.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}