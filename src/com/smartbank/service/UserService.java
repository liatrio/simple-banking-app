package com.smartbank.service;

import com.smartbank.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user-related business logic.
 */
public interface UserService {
    
    /**
     * Create a new user.
     * @param username The username
     * @param password The password (unhashed)
     * @param role The user's role
     * @return The created user
     * @throws IllegalArgumentException If the username already exists
     */
    User createUser(String username, String password, String role) throws IllegalArgumentException;
    
    /**
     * Create a new user with additional profile information.
     * @param username The username
     * @param password The password (unhashed)
     * @param role The user's role
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param email The user's email
     * @return The created user
     * @throws IllegalArgumentException If the username already exists
     */
    User createUser(String username, String password, String role, 
                    String firstName, String lastName, String email) throws IllegalArgumentException;
    
    /**
     * Get a user by their ID.
     * @param userId The user ID
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> getUserById(String userId);
    
    /**
     * Get a user by their username.
     * @param username The username
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * Get all users in the system.
     * @return A list of all users
     */
    List<User> getAllUsers();
    
    /**
     * Get users by role.
     * @param role The role
     * @return A list of users with the specified role
     */
    List<User> getUsersByRole(String role);
    
    /**
     * Update a user's role.
     * @param userId The user ID
     * @param newRole The new role
     * @return The updated user
     * @throws Exception If the user does not exist
     */
    User updateUserRole(String userId, String newRole) throws Exception;
    
    /**
     * Update a user's profile information.
     * @param userId The user ID
     * @param role The user's role
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param email The user's email
     * @return The updated user
     * @throws Exception If the user does not exist
     */
    User updateUser(String userId, String role, String firstName, String lastName, String email) throws Exception;
    
    /**
     * Change a user's password.
     * @param userId The user ID
     * @param oldPassword The old password (unhashed)
     * @param newPassword The new password (unhashed)
     * @return The updated user
     * @throws Exception If the user does not exist or the old password is incorrect
     */
    User changePassword(String userId, String oldPassword, String newPassword) throws Exception;
    
    /**
     * Authenticate a user by username and password.
     * @param username The username
     * @param password The password (unhashed)
     * @return An Optional containing the authenticated user if credentials are valid, or empty if authentication fails
     */
    Optional<User> authenticate(String username, String password);
    
    /**
     * Delete a user by their ID.
     * @param userId The user ID
     * @return true if the user was deleted, false if they did not exist
     */
    boolean deleteUser(String userId);
}