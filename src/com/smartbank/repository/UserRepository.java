package com.smartbank.repository;

import com.smartbank.model.User;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for User entity operations.
 */
public interface UserRepository extends Repository<User, String> {
    
    /**
     * Find a user by username.
     * @param username The username
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find users by role.
     * @param role The role
     * @return A list of users with the specified role
     */
    List<User> findByRole(String role);
    
    /**
     * Check if a username already exists.
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Authenticate a user by username and password.
     * @param username The username
     * @param password The password (unhashed)
     * @return An Optional containing the authenticated user if credentials are valid, 
     *         or empty if authentication fails
     */
    Optional<User> authenticate(String username, String password);
}