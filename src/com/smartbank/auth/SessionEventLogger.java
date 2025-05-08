package com.smartbank.auth;

import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SessionEventListener that logs all session events and
 * can optionally update a UI component with session information.
 */
public class SessionEventLogger implements SessionEventListener {
    private static final Logger LOGGER = Logger.getLogger(SessionEventLogger.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Optional UI update callback
    private SessionInfoDisplay sessionInfoDisplay;
    
    /**
     * Constructor.
     */
    public SessionEventLogger() {
        // Default constructor with no UI update
    }
    
    /**
     * Constructor with UI update callback.
     * @param sessionInfoDisplay The callback to update UI with session info
     */
    public SessionEventLogger(SessionInfoDisplay sessionInfoDisplay) {
        this.sessionInfoDisplay = sessionInfoDisplay;
    }
    
    @Override
    public void onSessionCreated(UserSession session) {
        if (session == null) return;
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        String ipAddress = session.getIpAddress();
        
        LOGGER.info(String.format("Session created: User=%s, Session=%s, IP=%s, Time=%s", 
                username, sessionId, ipAddress, timestamp));
        
        // Update UI if callback is provided
        updateSessionInfo(session);
    }
    
    @Override
    public void onSessionRenewed(UserSession session) {
        if (session == null) return;
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        
        LOGGER.fine(String.format("Session renewed: User=%s, Session=%s, Time=%s", 
                username, sessionId, timestamp));
        
        // Update UI if callback is provided
        updateSessionInfo(session);
    }
    
    @Override
    public void onSessionInvalidated(UserSession session) {
        if (session == null) return;
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        
        LOGGER.info(String.format("Session invalidated: User=%s, Session=%s, Time=%s", 
                username, sessionId, timestamp));
        
        // Update UI if callback is provided
        if (sessionInfoDisplay != null) {
            Platform.runLater(() -> {
                sessionInfoDisplay.displaySessionTerminated(username, sessionId, timestamp);
            });
        }
    }
    
    @Override
    public void onSessionExpired(UserSession session) {
        if (session == null) return;
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        
        LOGGER.info(String.format("Session expired: User=%s, Session=%s, Time=%s", 
                username, sessionId, timestamp));
        
        // Update UI if callback is provided
        if (sessionInfoDisplay != null) {
            Platform.runLater(() -> {
                sessionInfoDisplay.displaySessionExpired(username, sessionId, timestamp);
            });
        }
    }
    
    /**
     * Update session info in the UI.
     * @param session The session to display
     */
    private void updateSessionInfo(UserSession session) {
        if (sessionInfoDisplay == null || session == null) return;
        
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        String creationTime = session.getCreationTime().format(FORMATTER);
        String expirationTime = session.getExpirationTime().format(FORMATTER);
        long minutesRemaining = session.getMinutesRemaining();
        String ipAddress = session.getIpAddress();
        
        // Update UI on JavaFX application thread
        Platform.runLater(() -> {
            sessionInfoDisplay.displaySessionInfo(username, sessionId, creationTime, 
                    expirationTime, minutesRemaining, ipAddress);
        });
    }
    
    /**
     * Interface for UI components that display session information.
     */
    public interface SessionInfoDisplay {
        /**
         * Display session information in the UI.
         * @param username The username
         * @param sessionId The session ID
         * @param creationTime The session creation time
         * @param expirationTime The session expiration time
         * @param minutesRemaining The minutes remaining until expiration
         * @param ipAddress The IP address
         */
        void displaySessionInfo(String username, String sessionId, String creationTime, 
                String expirationTime, long minutesRemaining, String ipAddress);
        
        /**
         * Display session termination in the UI.
         * @param username The username
         * @param sessionId The session ID
         * @param timestamp The termination timestamp
         */
        void displaySessionTerminated(String username, String sessionId, String timestamp);
        
        /**
         * Display session expiration in the UI.
         * @param username The username
         * @param sessionId The session ID
         * @param timestamp The expiration timestamp
         */
        void displaySessionExpired(String username, String sessionId, String timestamp);
    }
}