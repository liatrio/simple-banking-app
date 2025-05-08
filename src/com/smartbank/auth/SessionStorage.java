package com.smartbank.auth;

import com.smartbank.model.User;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides persistence for session data.
 * Allows for storage and retrieval of session tokens,
 * particularly for remember-me functionality.
 */
public class SessionStorage {
    private static final Logger LOGGER = Logger.getLogger(SessionStorage.class.getName());
    
    // Singleton instance
    private static SessionStorage instance;
    
    // Storage configuration
    private final Path storagePath;
    private final String storageFilename = "sessions.properties";
    private final Properties sessionStore = new Properties();
    
    // Repository
    private final UserRepository userRepository;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionStorage() {
        // Determine storage path
        String appData = System.getProperty("user.home") + File.separator + ".smartbank";
        storagePath = Paths.get(appData);
        
        // Ensure directory exists
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating storage directory: " + e.getMessage(), e);
        }
        
        // Load any existing session data
        loadSessionData();
        
        // Get user repository
        userRepository = RepositoryFactory.getUserRepository();
    }
    
    /**
     * Get the singleton instance.
     * @return The SessionStorage instance
     */
    public static synchronized SessionStorage getInstance() {
        if (instance == null) {
            instance = new SessionStorage();
        }
        return instance;
    }
    
    /**
     * Store a remember-me token with user data.
     * @param token The token
     * @param userId The user ID
     * @param expirationTime The token expiration time
     * @return true if the token was stored successfully, false otherwise
     */
    public boolean storeToken(String token, String userId, LocalDateTime expirationTime) {
        if (token == null || userId == null || expirationTime == null) {
            return false;
        }
        
        // Create token entry with expiration and user ID
        String entry = userId + ":" + expirationTime.toString();
        sessionStore.setProperty(token, entry);
        
        // Save to disk
        return saveSessionData();
    }
    
    /**
     * Get a user by remember-me token.
     * @param token The token
     * @return Optional containing the user if the token is valid, empty otherwise
     */
    public Optional<User> getUserByToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        
        // Check if token exists in store
        String entry = sessionStore.getProperty(token);
        if (entry == null || entry.isEmpty()) {
            return Optional.empty();
        }
        
        // Parse entry
        String[] parts = entry.split(":", 2);
        if (parts.length != 2) {
            LOGGER.warning("Invalid token entry format: " + entry);
            removeToken(token);
            return Optional.empty();
        }
        
        String userId = parts[0];
        LocalDateTime expirationTime;
        
        try {
            expirationTime = LocalDateTime.parse(parts[1]);
        } catch (Exception e) {
            LOGGER.warning("Invalid expiration time in token entry: " + entry);
            removeToken(token);
            return Optional.empty();
        }
        
        // Check if token is expired
        if (expirationTime.isBefore(LocalDateTime.now())) {
            LOGGER.info("Remember-me token expired: " + token);
            removeToken(token);
            return Optional.empty();
        }
        
        // Retrieve user
        return userRepository.findById(userId);
    }
    
    /**
     * Remove a token from storage.
     * @param token The token to remove
     * @return true if the token was removed, false otherwise
     */
    public boolean removeToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        sessionStore.remove(token);
        return saveSessionData();
    }
    
    /**
     * Remove all tokens for a specific user.
     * @param userId The user ID
     * @return true if tokens were removed, false otherwise
     */
    public boolean removeTokensForUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        
        boolean found = false;
        
        // Collect all tokens for the user
        String[] tokensToRemove = sessionStore.stringPropertyNames().stream()
                .filter(token -> {
                    String entry = sessionStore.getProperty(token);
                    if (entry != null && entry.startsWith(userId + ":")) {
                        return true;
                    }
                    return false;
                })
                .toArray(String[]::new);
        
        // Remove the tokens
        for (String token : tokensToRemove) {
            sessionStore.remove(token);
            found = true;
        }
        
        // Save changes if any tokens were removed
        if (found) {
            return saveSessionData();
        }
        
        return false;
    }
    
    /**
     * Generate a new remember-me token.
     * @return A new unique token
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Load session data from disk.
     * @return true if data was loaded successfully, false otherwise
     */
    private boolean loadSessionData() {
        Path filePath = storagePath.resolve(storageFilename);
        
        // If file doesn't exist, no data to load
        if (!Files.exists(filePath)) {
            return true;
        }
        
        try (InputStream is = Files.newInputStream(filePath)) {
            sessionStore.load(is);
            cleanupExpiredTokens();
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading session data: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Save session data to disk.
     * @return true if data was saved successfully, false otherwise
     */
    private boolean saveSessionData() {
        Path filePath = storagePath.resolve(storageFilename);
        
        try (OutputStream os = Files.newOutputStream(filePath)) {
            sessionStore.store(os, "SmartBank Session Data");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving session data: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Clean up expired tokens from storage.
     */
    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        // Collect all expired tokens
        String[] tokensToRemove = sessionStore.stringPropertyNames().stream()
                .filter(token -> {
                    String entry = sessionStore.getProperty(token);
                    if (entry == null || entry.isEmpty()) {
                        return true;
                    }
                    
                    String[] parts = entry.split(":", 2);
                    if (parts.length != 2) {
                        return true;
                    }
                    
                    try {
                        LocalDateTime expirationTime = LocalDateTime.parse(parts[1]);
                        return expirationTime.isBefore(now);
                    } catch (Exception e) {
                        return true;
                    }
                })
                .toArray(String[]::new);
        
        // Remove the tokens
        for (String token : tokensToRemove) {
            sessionStore.remove(token);
        }
        
        // Save changes if any tokens were removed
        if (tokensToRemove.length > 0) {
            saveSessionData();
            LOGGER.info("Cleaned up " + tokensToRemove.length + " expired tokens");
        }
    }
}