package com.smartbank.repository.impl;

import com.smartbank.model.Account;
import com.smartbank.model.User;
import com.smartbank.repository.AccountRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

/**
 * JPA implementation of the AccountRepository interface.
 */
public class JpaAccountRepository extends JpaRepository<Account, Long> implements AccountRepository {
    private static final Logger LOGGER = Logger.getLogger(JpaAccountRepository.class.getName());

    @Override
    public List<Account> findByAccountHolder(User user) {
        return executeInTransaction(em -> {
            TypedQuery<Account> query = em.createQuery(
                    "SELECT a FROM Account a WHERE a.accountHolder = :user",
                    Account.class);
            query.setParameter("user", user);
            return query.getResultList();
        });
    }

    @Override
    public List<Account> findByUsername(String username) {
        return executeInTransaction(em -> {
            TypedQuery<Account> query = em.createQuery(
                    "SELECT a FROM Account a JOIN a.accountHolder u WHERE u.username = :username",
                    Account.class);
            query.setParameter("username", username);
            return query.getResultList();
        });
    }

    @Override
    public List<Account> findByBalanceGreaterThan(double minBalance) {
        return executeInTransaction(em -> {
            TypedQuery<Account> query = em.createQuery(
                    "SELECT a FROM Account a WHERE a.balance > :minBalance",
                    Account.class);
            query.setParameter("minBalance", minBalance);
            return query.getResultList();
        });
    }

    @Override
    public List<Account> findByBalanceLessThan(double maxBalance) {
        return executeInTransaction(em -> {
            TypedQuery<Account> query = em.createQuery(
                    "SELECT a FROM Account a WHERE a.balance < :maxBalance",
                    Account.class);
            query.setParameter("maxBalance", maxBalance);
            return query.getResultList();
        });
    }
}