package com.smartbank.auth;

import javafx.application.Platform;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter to validate sessions for each user interaction.
 * This class integrates with JavaFX to provide session validation
 * for each user interaction with the application.
 */
public class SessionFilter {
    private static final Logger LOGGER = Logger.getLogger(SessionFilter.class.getName());
    
    // Singleton instance
    private static SessionFilter instance;
    
    // References to other required components
    private final SessionManager sessionManager;
    private final SecurityContext securityContext;
    
    // Session configuration
    private boolean autoRenewSessions = true;
    private int inactivityWarningMinutes = 5;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionFilter() {
        this.sessionManager = SessionManager.getInstance();
        this.securityContext = SecurityContext.getInstance();
        
        // Register a session expiration listener to handle UI updates
        sessionManager.addSessionEventListener(new SessionExpirationListener());
    }
    
    /**
     * Get the singleton instance.
     * @return The SessionFilter instance
     */
    public static synchronized SessionFilter getInstance() {
        if (instance == null) {
            instance = new SessionFilter();
        }
        return instance;
    }
    
    /**
     * Process a user interaction to validate and renew the session.
     * This method should be called for each significant user interaction.
     * @return true if the session is valid, false otherwise
     */
    public boolean processInteraction() {
        UserSession currentSession = securityContext.getCurrentSession();
        if (currentSession == null) {
            return false;
        }
        
        // Check if the session is valid
        if (!currentSession.isValid()) {
            LOGGER.info("Session expired: " + currentSession.getSessionId());
            handleSessionExpiration(currentSession);
            return false;
        }
        
        // Auto-renew session if enabled
        if (autoRenewSessions) {
            String sessionId = currentSession.getSessionId();
            sessionManager.renewSession(sessionId);
        }
        
        // Check for imminent expiration and warn user if needed
        long minutesRemaining = currentSession.getMinutesRemaining();
        if (minutesRemaining <= inactivityWarningMinutes) {
            handleImminentExpiration(currentSession);
        }
        
        return true;
    }
    
    /**
     * Handle a session expiration.
     * @param session The expired session
     */
    private void handleSessionExpiration(UserSession session) {
        securityContext.invalidateSession();
        
        // Notify UI on the JavaFX application thread
        Platform.runLater(() -> {
            // This would be handled by the UI to show login screen
            LOGGER.info("Session expired event delivered to UI");
        });
    }
    
    /**
     * Handle an imminent session expiration.
     * @param session The session about to expire
     */
    private void handleImminentExpiration(UserSession session) {
        long minutesRemaining = session.getMinutesRemaining();
        
        // Notify UI on the JavaFX application thread
        Platform.runLater(() -> {
            // This would be handled by the UI to show warning
            LOGGER.info("Session expiration warning: " + minutesRemaining + " minutes remaining");
        });
    }
    
    /**
     * Set whether sessions should be automatically renewed.
     * @param autoRenew true to auto-renew sessions, false otherwise
     */
    public void setAutoRenewSessions(boolean autoRenew) {
        this.autoRenewSessions = autoRenew;
    }
    
    /**
     * Set the inactivity warning threshold in minutes.
     * @param minutes The warning threshold in minutes
     */
    public void setInactivityWarningMinutes(int minutes) {
        if (minutes > 0) {
            this.inactivityWarningMinutes = minutes;
        }
    }
    
    /**
     * Get the inactivity warning threshold in minutes.
     * @return The warning threshold in minutes
     */
    public int getInactivityWarningMinutes() {
        return inactivityWarningMinutes;
    }
    
    /**
     * Check if auto-renewal of sessions is enabled.
     * @return true if auto-renewal is enabled, false otherwise
     */
    public boolean isAutoRenewSessions() {
        return autoRenewSessions;
    }
    
    /**
     * Private class to handle session expiration events.
     */
    private class SessionExpirationListener implements SessionEventListener {
        @Override
        public void onSessionCreated(UserSession session) {
            // Not needed for this listener
        }
        
        @Override
        public void onSessionRenewed(UserSession session) {
            // Not needed for this listener
        }
        
        @Override
        public void onSessionInvalidated(UserSession session) {
            if (session == null) {
                return;
            }
            
            // Check if this is the current session
            UserSession currentSession = securityContext.getCurrentSession();
            if (currentSession != null && session.getSessionId().equals(currentSession.getSessionId())) {
                handleSessionExpiration(session);
            }
        }
        
        @Override
        public void onSessionExpired(UserSession session) {
            if (session == null) {
                return;
            }
            
            // Check if this is the current session
            UserSession currentSession = securityContext.getCurrentSession();
            if (currentSession != null && session.getSessionId().equals(currentSession.getSessionId())) {
                handleSessionExpiration(session);
            }
        }
    }
}