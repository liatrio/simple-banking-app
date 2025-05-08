package com.smartbank.auth;

import com.smartbank.model.User;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.UserRepository;
import com.smartbank.util.ValidationUtils;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the AuthenticationService interface.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationServiceImpl.class.getName());

    // Repository for user data
    private final UserRepository userRepository;
    
    // Security context and session manager
    private final SecurityContext securityContext;
    private final SessionManager sessionManager;
    
    // Login attempt tracking for rate limiting
    private final Map<String, FailedLoginTracker> failedLoginAttempts = new HashMap<>();
    
    // Constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCKOUT_MINUTES = 15;
    private static final int BCRYPT_WORK_FACTOR = 12;
    
    /**
     * Constructor.
     */
    public AuthenticationServiceImpl() {
        this.userRepository = RepositoryFactory.getUserRepository();
        this.securityContext = SecurityContext.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }
    
    @Override
    public Optional<UserSession> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        
        // Check if login is allowed (not rate limited)
        if (!isLoginAllowed(username)) {
            LOGGER.warning("Login attempt for rate-limited user: " + username);
            return Optional.empty();
        }
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (verifyPassword(password, user.getPasswordHash())) {
                    LOGGER.info("User authenticated successfully: " + username);
                    UserSession session = sessionManager.createSession(user);
                    return Optional.of(session);
                }
            }
            
            // Record failed attempt
            recordFailedLoginAttempt(username);
            LOGGER.warning("Failed login attempt for user: " + username);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserSession> loginWithToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        
        return sessionManager.getSessionByToken(token);
    }
    
    @Override
    public User register(String username, String password, String role) {
        if (username == null || password == null || role == null) {
            throw new IllegalArgumentException("Username, password, and role are required");
        }
        
        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        if (!validatePassword(password)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
        
        // Create user with temporary password
        User user = new User(username, password, role);
        
        // Hash the password properly using BCrypt
        String hashedPassword = hashPassword(password);
        user.setPasswordHash(hashedPassword);
        
        // Save the user with properly hashed password
        return userRepository.save(user);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public String generateRememberMeToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        return sessionManager.createRememberMeSession(user);
    }
    
    @Override
    public boolean validatePassword(String password) {
        return ValidationUtils.isPasswordStrong(password);
    }
    
    @Override
    public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }
    
    @Override
    public boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying password: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public void recordFailedLoginAttempt(String username) {
        FailedLoginTracker tracker = failedLoginAttempts.computeIfAbsent(
                username, k -> new FailedLoginTracker());
        tracker.recordFailure();
    }
    
    @Override
    public boolean isLoginAllowed(String username) {
        FailedLoginTracker tracker = failedLoginAttempts.get(username);
        if (tracker == null) {
            return true;
        }
        
        return tracker.getFailureCount() < MAX_LOGIN_ATTEMPTS || 
               tracker.isLockoutExpired();
    }
    
    @Override
    public void logout(UserSession session) {
        if (session == null) {
            return;
        }
        
        // Use session manager to invalidate the session
        sessionManager.invalidateSession(session.getSessionId());
        
        // Clear security context
        securityContext.invalidateSession();
    }
    
    /**
     * Helper class to track failed login attempts.
     */
    private static class FailedLoginTracker {
        private int failureCount = 0;
        private LocalDateTime lockoutExpiration = null;
        
        public void recordFailure() {
            failureCount++;
            
            if (failureCount >= MAX_LOGIN_ATTEMPTS) {
                lockoutExpiration = LocalDateTime.now().plusMinutes(LOGIN_LOCKOUT_MINUTES);
            }
        }
        
        public int getFailureCount() {
            return failureCount;
        }
        
        public boolean isLockoutExpired() {
            if (lockoutExpiration == null) {
                return true;
            }
            
            boolean expired = LocalDateTime.now().isAfter(lockoutExpiration);
            if (expired) {
                // Reset failure count after lockout expires
                failureCount = 0;
                lockoutExpiration = null;
            }
            
            return expired;
        }
    }
}