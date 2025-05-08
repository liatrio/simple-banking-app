package com.smartbank.auth;

import com.smartbank.model.User;
import java.util.Optional;

/**
 * Service interface for user authentication.
 */
public interface AuthenticationService {
    
    /**
     * Authenticate a user with username and password.
     * @param username The username
     * @param password The password (unhashed)
     * @return Optional containing UserSession if authentication is successful, empty otherwise
     */
    Optional<UserSession> login(String username, String password);
    
    /**
     * Authenticate a user with remember-me token.
     * @param token The remember-me token
     * @return Optional containing UserSession if authentication is successful, empty otherwise
     */
    Optional<UserSession> loginWithToken(String token);
    
    /**
     * Create a new user account.
     * @param username The username
     * @param password The password (unhashed)
     * @param role The user's role
     * @return The created user
     * @throws IllegalArgumentException if the username already exists
     */
    User register(String username, String password, String role);
    
    /**
     * Check if a username is available (not already taken).
     * @param username The username to check
     * @return true if the username is available, false if it already exists
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Generate a remember-me token for the specified user.
     * @param user The user
     * @return The generated token
     */
    String generateRememberMeToken(User user);
    
    /**
     * Validate a password according to security policies.
     * @param password The password to validate
     * @return true if the password meets security requirements, false otherwise
     */
    boolean validatePassword(String password);
    
    /**
     * Hash a password using a secure algorithm (BCrypt).
     * @param password The password to hash
     * @return The hashed password
     */
    String hashPassword(String password);
    
    /**
     * Verify a password against a hash.
     * @param password The password (unhashed)
     * @param hash The password hash
     * @return true if the password matches the hash, false otherwise
     */
    boolean verifyPassword(String password, String hash);
    
    /**
     * Record a failed login attempt for rate limiting.
     * @param username The username that failed to login
     */
    void recordFailedLoginAttempt(String username);
    
    /**
     * Check if a user is allowed to attempt login (not rate limited).
     * @param username The username to check
     * @return true if the user is allowed to attempt login, false if rate limited
     */
    boolean isLoginAllowed(String username);
    
    /**
     * Logout the current user session.
     * @param session The user session to invalidate
     */
    void logout(UserSession session);
}