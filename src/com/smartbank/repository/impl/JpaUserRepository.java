package com.smartbank.repository.impl;

import com.smartbank.model.User;
import com.smartbank.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of the UserRepository interface.
 */
public class JpaUserRepository extends JpaRepository<User, String> implements UserRepository {
    private static final Logger LOGGER = Logger.getLogger(JpaUserRepository.class.getName());

    @Override
    public Optional<User> findByUsername(String username) {
        return executeInTransaction(em -> {
            try {
                TypedQuery<User> query = em.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username",
                        User.class);
                query.setParameter("username", username);
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public List<User> findByRole(String role) {
        return executeInTransaction(em -> {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.role = :role",
                    User.class);
            query.setParameter("role", role);
            return query.getResultList();
        });
    }

    @Override
    public boolean existsByUsername(String username) {
        return executeInTransaction(em -> {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                    Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        });
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.checkPassword(password)) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
}