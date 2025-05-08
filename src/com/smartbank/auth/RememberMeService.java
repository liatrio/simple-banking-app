package com.smartbank.auth;

import com.smartbank.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Service for handling "Remember Me" functionality.
 */
public class RememberMeService {
    private static final Logger LOGGER = Logger.getLogger(RememberMeService.class.getName());
    
    // Singleton instance
    private static RememberMeService instance;
    
    // Components
    private final SessionManager sessionManager;
    private final SessionStorage sessionStorage;
    private final Preferences preferences;
    
    // Constants
    private static final String PREF_REMEMBER_ME_TOKEN = "rememberMeToken";
    private static final int DEFAULT_REMEMBER_ME_DAYS = 30;
    
    /**
     * Private constructor for singleton pattern.
     */
    private RememberMeService() {
        this.sessionManager = SessionManager.getInstance();
        this.sessionStorage = SessionStorage.getInstance();
        this.preferences = Preferences.userRoot().node("com.smartbank.auth");
    }
    
    /**
     * Get the singleton instance.
     * @return The RememberMeService instance
     */
    public static synchronized RememberMeService getInstance() {
        if (instance == null) {
            instance = new RememberMeService();
        }
        return instance;
    }
    
    /**
     * Enable remember-me for a user.
     * @param user The user
     * @return The generated token, or null if failed
     */
    public String enableRememberMe(User user) {
        if (user == null) {
            return null;
        }
        
        try {
            // Generate a token
            String token = sessionStorage.generateToken();
            
            // Calculate expiration (default 30 days)
            LocalDateTime expirationTime = LocalDateTime.now().plusDays(DEFAULT_REMEMBER_ME_DAYS);
            
            // Store token in session storage
            boolean stored = sessionStorage.storeToken(token, user.getUserId(), expirationTime);
            
            if (stored) {
                // Store token in preferences
                preferences.put(PREF_REMEMBER_ME_TOKEN, token);
                LOGGER.info("Remember-me enabled for user: " + user.getUsername());
                return token;
            } else {
                LOGGER.warning("Failed to store remember-me token for user: " + user.getUsername());
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error enabling remember-me: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Disable remember-me for the current user.
     * @return true if disabled successfully, false otherwise
     */
    public boolean disableRememberMe() {
        try {
            // Get token from preferences
            String token = preferences.get(PREF_REMEMBER_ME_TOKEN, null);
            if (token == null || token.isEmpty()) {
                return true; // Nothing to disable
            }
            
            // Remove token from storage
            boolean removed = sessionStorage.removeToken(token);
            
            // Remove token from preferences
            preferences.remove(PREF_REMEMBER_ME_TOKEN);
            
            LOGGER.info("Remember-me disabled");
            return removed;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error disabling remember-me: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Attempt to login with a remember-me token.
     * @return Optional containing a UserSession if successful, empty otherwise
     */
    public Optional<UserSession> autoLogin() {
        try {
            // Get token from preferences
            String token = preferences.get(PREF_REMEMBER_ME_TOKEN, null);
            if (token == null || token.isEmpty()) {
                return Optional.empty();
            }
            
            // Try to get user from token
            Optional<User> userOpt = sessionStorage.getUserByToken(token);
            if (!userOpt.isPresent()) {
                // Token is invalid, remove it
                preferences.remove(PREF_REMEMBER_ME_TOKEN);
                return Optional.empty();
            }
            
            User user = userOpt.get();
            
            // Create a session for the user
            UserSession session = sessionManager.createSession(user);
            
            // Generate a new token to enhance security
            String newToken = enableRememberMe(user);
            if (newToken != null) {
                session.setRememberMeToken(newToken);
            }
            
            LOGGER.info("Auto-login successful for user: " + user.getUsername());
            return Optional.of(session);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during auto-login: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Check if remember-me is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean isRememberMeEnabled() {
        String token = preferences.get(PREF_REMEMBER_ME_TOKEN, null);
        return token != null && !token.isEmpty();
    }
    
    /**
     * Clear all remember-me data. Use with caution.
     */
    public void clearAll() {
        try {
            // Clear preferences
            preferences.clear();
            LOGGER.info("Remember-me data cleared");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error clearing remember-me data: " + e.getMessage(), e);
        }
    }
}