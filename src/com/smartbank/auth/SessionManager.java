package com.smartbank.auth;

import com.smartbank.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages user sessions for the application, including creation, validation,
 * renewal, and cleanup of expired sessions.
 */
public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    
    // Singleton instance
    private static SessionManager instance;
    
    // Session storage
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, UserSession> tokenSessions = new ConcurrentHashMap<>();
    
    // Session event listeners
    private final List<SessionEventListener> eventListeners = new ArrayList<>();
    
    // Session configuration
    private int defaultSessionTimeoutMinutes = UserSession.DEFAULT_SESSION_TIMEOUT_MINUTES;
    private int extendSessionTimeoutMinutes = 15;
    private int maximumSessionTimeoutMinutes = 120;
    private int rememberMeDays = 30;
    
    // Scheduled executor for cleanup tasks
    private final ScheduledExecutorService scheduledExecutor;
    
    // Security context reference
    private final SecurityContext securityContext;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionManager() {
        this.securityContext = SecurityContext.getInstance();
        
        // Initialize scheduled executor for session cleanup
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SessionManager-Cleanup");
            thread.setDaemon(true);
            return thread;
        });
        
        // Schedule session cleanup task
        scheduledExecutor.scheduleAtFixedRate(
                this::cleanupExpiredSessions,
                1, 1, TimeUnit.MINUTES);
        
        LOGGER.info("SessionManager initialized");
    }
    
    /**
     * Get the singleton instance.
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Create a new user session.
     * @param user The authenticated user
     * @return The created user session
     */
    public UserSession createSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        UserSession session = new UserSession(user, defaultSessionTimeoutMinutes);
        activeSessions.put(session.getSessionId(), session);
        
        // Update security context
        securityContext.setCurrentSession(session);
        
        // Notify listeners
        notifySessionCreated(session);
        
        LOGGER.info("Session created for user: " + user.getUsername());
        return session;
    }
    
    /**
     * Create a long-lived session for remember-me functionality.
     * @param user The authenticated user
     * @return The remember-me token
     */
    public String createRememberMeSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Create a long-lived session (30 days by default)
        UserSession session = new UserSession(user, rememberMeDays * 24 * 60);
        String token = UUID.randomUUID().toString();
        session.setRememberMeToken(token);
        
        // Store the session with the token
        tokenSessions.put(token, session);
        
        // Notify listeners
        notifySessionCreated(session);
        
        LOGGER.info("Remember-me session created for user: " + user.getUsername());
        return token;
    }
    
    /**
     * Get a session by its ID.
     * @param sessionId The session ID
     * @return Optional containing the session if found and valid, empty otherwise
     */
    public Optional<UserSession> getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Optional.empty();
        }
        
        UserSession session = activeSessions.get(sessionId);
        if (session != null && session.isValid()) {
            return Optional.of(session);
        }
        
        // Clean up invalid session
        if (session != null && !session.isValid()) {
            activeSessions.remove(sessionId);
            notifySessionExpired(session);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get a session by remember-me token.
     * @param token The remember-me token
     * @return Optional containing the session if found and valid, empty otherwise
     */
    public Optional<UserSession> getSessionByToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        
        UserSession session = tokenSessions.get(token);
        if (session != null && session.isValid()) {
            return Optional.of(session);
        }
        
        // Clean up invalid session
        if (session != null && !session.isValid()) {
            tokenSessions.remove(token);
            notifySessionExpired(session);
        }
        
        return Optional.empty();
    }
    
    /**
     * Renew a session, extending its expiration time.
     * @param sessionId The session ID
     * @return true if the session was renewed, false otherwise
     */
    public boolean renewSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        UserSession session = activeSessions.get(sessionId);
        if (session != null && session.isValid()) {
            // Extend the session validity
            session.extend(extendSessionTimeoutMinutes);
            
            // Notify listeners
            notifySessionRenewed(session);
            
            LOGGER.fine("Session renewed for user: " + session.getUser().getUsername());
            return true;
        }
        
        return false;
    }
    
    /**
     * Invalidate a session.
     * @param sessionId The session ID
     * @return true if the session was invalidated, false otherwise
     */
    public boolean invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        UserSession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
            
            // If this session has a remember-me token, remove it
            if (session.getRememberMeToken() != null) {
                tokenSessions.remove(session.getRememberMeToken());
            }
            
            // Notify listeners
            notifySessionInvalidated(session);
            
            LOGGER.info("Session invalidated for user: " + session.getUser().getUsername());
            return true;
        }
        
        return false;
    }
    
    /**
     * Invalidate all sessions for a user.
     * @param username The username
     * @return The number of sessions invalidated
     */
    public int invalidateUserSessions(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        
        // Invalidate active sessions
        for (Map.Entry<String, UserSession> entry : activeSessions.entrySet()) {
            UserSession session = entry.getValue();
            if (username.equals(session.getUser().getUsername())) {
                String sessionId = entry.getKey();
                if (invalidateSession(sessionId)) {
                    count++;
                }
            }
        }
        
        // Invalidate token sessions
        List<String> tokensToRemove = new ArrayList<>();
        for (Map.Entry<String, UserSession> entry : tokenSessions.entrySet()) {
            UserSession session = entry.getValue();
            if (username.equals(session.getUser().getUsername())) {
                tokensToRemove.add(entry.getKey());
                session.invalidate();
                notifySessionInvalidated(session);
                count++;
            }
        }
        
        // Remove the tokens from the map
        for (String token : tokensToRemove) {
            tokenSessions.remove(token);
        }
        
        LOGGER.info("Invalidated " + count + " sessions for user: " + username);
        return count;
    }
    
    /**
     * Get all active sessions.
     * @return A list of all active sessions
     */
    public List<UserSession> getAllActiveSessions() {
        List<UserSession> sessions = new ArrayList<>();
        
        for (UserSession session : activeSessions.values()) {
            if (session.isValid()) {
                sessions.add(session);
            }
        }
        
        return sessions;
    }
    
    /**
     * Get the number of active sessions.
     * @return The number of active sessions
     */
    public int getActiveSessionCount() {
        return (int) activeSessions.values().stream()
                .filter(UserSession::isValid)
                .count();
    }
    
    /**
     * Clean up expired sessions.
     */
    public void cleanupExpiredSessions() {
        try {
            int activeCount = 0;
            int removedCount = 0;
            
            // Clean up active sessions
            for (Map.Entry<String, UserSession> entry : activeSessions.entrySet()) {
                UserSession session = entry.getValue();
                if (!session.isValid()) {
                    activeSessions.remove(entry.getKey());
                    notifySessionExpired(session);
                    removedCount++;
                } else {
                    activeCount++;
                }
            }
            
            // Clean up token sessions
            for (Map.Entry<String, UserSession> entry : tokenSessions.entrySet()) {
                UserSession session = entry.getValue();
                if (!session.isValid()) {
                    tokenSessions.remove(entry.getKey());
                    notifySessionExpired(session);
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                LOGGER.info("Cleaned up " + removedCount + " expired sessions. Active sessions: " + activeCount);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during session cleanup: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a session event listener.
     * @param listener The listener to add
     */
    public void addSessionEventListener(SessionEventListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Remove a session event listener.
     * @param listener The listener to remove
     */
    public void removeSessionEventListener(SessionEventListener listener) {
        if (listener != null) {
            eventListeners.remove(listener);
        }
    }
    
    /**
     * Set the default session timeout in minutes.
     * @param minutes The timeout in minutes
     */
    public void setDefaultSessionTimeout(int minutes) {
        if (minutes > 0) {
            this.defaultSessionTimeoutMinutes = minutes;
        }
    }
    
    /**
     * Set the session extension timeout in minutes.
     * @param minutes The extension timeout in minutes
     */
    public void setExtendSessionTimeout(int minutes) {
        if (minutes > 0) {
            this.extendSessionTimeoutMinutes = minutes;
        }
    }
    
    /**
     * Set the maximum session timeout in minutes.
     * @param minutes The maximum timeout in minutes
     */
    public void setMaximumSessionTimeout(int minutes) {
        if (minutes > 0) {
            this.maximumSessionTimeoutMinutes = minutes;
        }
    }
    
    /**
     * Set the remember-me duration in days.
     * @param days The duration in days
     */
    public void setRememberMeDuration(int days) {
        if (days > 0) {
            this.rememberMeDays = days;
        }
    }
    
    /**
     * Get the default session timeout in minutes.
     * @return The default timeout in minutes
     */
    public int getDefaultSessionTimeout() {
        return defaultSessionTimeoutMinutes;
    }
    
    /**
     * Get the session extension timeout in minutes.
     * @return The extension timeout in minutes
     */
    public int getExtendSessionTimeout() {
        return extendSessionTimeoutMinutes;
    }
    
    /**
     * Get the maximum session timeout in minutes.
     * @return The maximum timeout in minutes
     */
    public int getMaximumSessionTimeout() {
        return maximumSessionTimeoutMinutes;
    }
    
    /**
     * Get the remember-me duration in days.
     * @return The duration in days
     */
    public int getRememberMeDuration() {
        return rememberMeDays;
    }
    
    /**
     * Shutdown the session manager.
     */
    public void shutdown() {
        try {
            scheduledExecutor.shutdown();
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Error shutting down session manager: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Event notification methods
    
    private void notifySessionCreated(UserSession session) {
        for (SessionEventListener listener : eventListeners) {
            try {
                listener.onSessionCreated(session);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying listener of session creation: " + e.getMessage(), e);
            }
        }
    }
    
    private void notifySessionRenewed(UserSession session) {
        for (SessionEventListener listener : eventListeners) {
            try {
                listener.onSessionRenewed(session);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying listener of session renewal: " + e.getMessage(), e);
            }
        }
    }
    
    private void notifySessionInvalidated(UserSession session) {
        for (SessionEventListener listener : eventListeners) {
            try {
                listener.onSessionInvalidated(session);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying listener of session invalidation: " + e.getMessage(), e);
            }
        }
    }
    
    private void notifySessionExpired(UserSession session) {
        for (SessionEventListener listener : eventListeners) {
            try {
                listener.onSessionExpired(session);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error notifying listener of session expiration: " + e.getMessage(), e);
            }
        }
    }
}