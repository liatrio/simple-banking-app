package com.smartbank.auth;

import com.smartbank.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user session in the application.
 */
public class UserSession {
    private final String sessionId;
    private final User user;
    private final LocalDateTime creationTime;
    private LocalDateTime lastAccessTime;
    private LocalDateTime expirationTime;
    private boolean valid;
    private String rememberMeToken;
    private final String ipAddress;
    private String lastAccessIpAddress;
    
    /**
     * Default session timeout in minutes.
     */
    public static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 30;
    
    /**
     * Create a new user session with default timeout.
     * @param user The authenticated user
     */
    public UserSession(User user) {
        this(user, DEFAULT_SESSION_TIMEOUT_MINUTES);
    }
    
    /**
     * Create a new user session with default timeout.
     * @param user The authenticated user
     * @param ipAddress The IP address of the client
     */
    public UserSession(User user, String ipAddress) {
        this(user, DEFAULT_SESSION_TIMEOUT_MINUTES, ipAddress);
    }
    
    /**
     * Create a new user session with specified timeout.
     * @param user The authenticated user
     * @param timeoutMinutes Session timeout in minutes
     */
    public UserSession(User user, int timeoutMinutes) {
        this(user, timeoutMinutes, "unknown");
    }
    
    /**
     * Create a new user session with specified timeout and IP address.
     * @param user The authenticated user
     * @param timeoutMinutes Session timeout in minutes
     * @param ipAddress The IP address of the client
     */
    public UserSession(User user, int timeoutMinutes, String ipAddress) {
        this.sessionId = UUID.randomUUID().toString();
        this.user = user;
        this.creationTime = LocalDateTime.now();
        this.lastAccessTime = this.creationTime;
        this.expirationTime = this.creationTime.plusMinutes(timeoutMinutes);
        this.valid = true;
        this.rememberMeToken = null;
        this.ipAddress = ipAddress != null ? ipAddress : "unknown";
        this.lastAccessIpAddress = this.ipAddress;
    }
    
    /**
     * Get the session ID.
     * @return The session ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Get the authenticated user.
     * @return The user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get the session creation time.
     * @return The creation time
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }
    
    /**
     * Get the last access time.
     * @return The last access time
     */
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }
    
    /**
     * Get the session expiration time.
     * @return The expiration time
     */
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }
    
    /**
     * Check if the session is valid.
     * @return true if the session is valid, false otherwise
     */
    public boolean isValid() {
        return valid && LocalDateTime.now().isBefore(expirationTime);
    }
    
    /**
     * Invalidate the session.
     */
    public void invalidate() {
        this.valid = false;
    }
    
    /**
     * Update the last access time to extend the session.
     */
    public void touch() {
        this.lastAccessTime = LocalDateTime.now();
    }
    
    /**
     * Update the last access time and IP address.
     * @param ipAddress The current IP address
     */
    public void touch(String ipAddress) {
        this.lastAccessTime = LocalDateTime.now();
        if (ipAddress != null) {
            this.lastAccessIpAddress = ipAddress;
        }
    }
    
    /**
     * Extend the session expiration time.
     * @param additionalMinutes Additional minutes to add to the expiration time
     */
    public void extend(int additionalMinutes) {
        if (additionalMinutes > 0) {
            this.expirationTime = LocalDateTime.now().plusMinutes(additionalMinutes);
            this.lastAccessTime = LocalDateTime.now();
        }
    }
    
    /**
     * Get the remember-me token.
     * @return The remember-me token
     */
    public String getRememberMeToken() {
        return rememberMeToken;
    }
    
    /**
     * Set the remember-me token.
     * @param rememberMeToken The remember-me token
     */
    public void setRememberMeToken(String rememberMeToken) {
        this.rememberMeToken = rememberMeToken;
    }
    
    /**
     * Check if the user has the specified role.
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        if (user == null || role == null || user.getRole() == null) {
            return false;
        }
        // Make role check case-insensitive
        return role.equalsIgnoreCase(user.getRole());
    }
    
    /**
     * Get the time remaining until session expiration.
     * @return The minutes remaining until expiration
     */
    public long getMinutesRemaining() {
        return java.time.Duration.between(LocalDateTime.now(), expirationTime).toMinutes();
    }
    
    /**
     * Get the session creation IP address.
     * @return The IP address used to create the session
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * Get the last access IP address.
     * @return The IP address of the last access
     */
    public String getLastAccessIpAddress() {
        return lastAccessIpAddress;
    }
    
    /**
     * Get the session age in minutes.
     * @return The age of the session in minutes
     */
    public long getSessionAgeMinutes() {
        return java.time.Duration.between(creationTime, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Get the idle time in minutes.
     * @return The idle time in minutes
     */
    public long getIdleTimeMinutes() {
        return java.time.Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes();
    }
}