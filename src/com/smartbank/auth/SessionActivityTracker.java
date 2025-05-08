package com.smartbank.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks session activity for audit and statistical purposes.
 */
public class SessionActivityTracker implements SessionEventListener {
    private static final Logger LOGGER = Logger.getLogger(SessionActivityTracker.class.getName());
    
    // Singleton instance
    private static SessionActivityTracker instance;
    
    // Activity logs
    private final Map<String, List<SessionActivity>> activityByUser = new ConcurrentHashMap<>();
    private final List<SessionActivity> recentActivities = Collections.synchronizedList(new ArrayList<>());
    private final int maxRecentActivities = 1000;
    
    // Session statistics
    private int sessionCreatedCount = 0;
    private int sessionRenewedCount = 0;
    private int sessionInvalidatedCount = 0;
    private int sessionExpiredCount = 0;
    private int loginFailedCount = 0;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionActivityTracker() {
        // Register this tracker with the session manager
        SessionManager.getInstance().addSessionEventListener(this);
    }
    
    /**
     * Get the singleton instance.
     * @return The SessionActivityTracker instance
     */
    public static synchronized SessionActivityTracker getInstance() {
        if (instance == null) {
            instance = new SessionActivityTracker();
        }
        return instance;
    }
    
    @Override
    public void onSessionCreated(UserSession session) {
        if (session == null) return;
        
        try {
            // Record activity
            SessionActivity activity = new SessionActivity(
                    session.getSessionId(),
                    session.getUser().getUsername(),
                    LocalDateTime.now(),
                    SessionActivityType.CREATED,
                    "Session created from " + session.getIpAddress()
            );
            
            addActivity(activity);
            sessionCreatedCount++;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording session creation: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onSessionRenewed(UserSession session) {
        if (session == null) return;
        
        try {
            // Record activity
            SessionActivity activity = new SessionActivity(
                    session.getSessionId(),
                    session.getUser().getUsername(),
                    LocalDateTime.now(),
                    SessionActivityType.RENEWED,
                    "Session renewed"
            );
            
            addActivity(activity);
            sessionRenewedCount++;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording session renewal: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onSessionInvalidated(UserSession session) {
        if (session == null) return;
        
        try {
            // Record activity
            SessionActivity activity = new SessionActivity(
                    session.getSessionId(),
                    session.getUser().getUsername(),
                    LocalDateTime.now(),
                    SessionActivityType.INVALIDATED,
                    "Session invalidated"
            );
            
            addActivity(activity);
            sessionInvalidatedCount++;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording session invalidation: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onSessionExpired(UserSession session) {
        if (session == null) return;
        
        try {
            // Record activity
            SessionActivity activity = new SessionActivity(
                    session.getSessionId(),
                    session.getUser().getUsername(),
                    LocalDateTime.now(),
                    SessionActivityType.EXPIRED,
                    "Session expired"
            );
            
            addActivity(activity);
            sessionExpiredCount++;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording session expiration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add an activity to the logs.
     * @param activity The activity to add
     */
    private void addActivity(SessionActivity activity) {
        // Add to user-specific log
        List<SessionActivity> userActivities = activityByUser.computeIfAbsent(
                activity.getUsername(), k -> Collections.synchronizedList(new ArrayList<>()));
        userActivities.add(activity);
        
        // Add to recent activities
        synchronized (recentActivities) {
            recentActivities.add(activity);
            
            // Trim if needed
            if (recentActivities.size() > maxRecentActivities) {
                recentActivities.remove(0);
            }
        }
    }
    
    /**
     * Manually record a session activity.
     * @param sessionId The session ID
     * @param username The username
     * @param type The activity type
     * @param details The activity details
     */
    public void recordActivity(String sessionId, String username, SessionActivityType type, String details) {
        if (sessionId == null || username == null || type == null) return;
        
        SessionActivity activity = new SessionActivity(
                sessionId,
                username,
                LocalDateTime.now(),
                type,
                details
        );
        
        addActivity(activity);
        
        // Update counter
        switch (type) {
            case CREATED:
                sessionCreatedCount++;
                break;
            case RENEWED:
                sessionRenewedCount++;
                break;
            case INVALIDATED:
                sessionInvalidatedCount++;
                break;
            case EXPIRED:
                sessionExpiredCount++;
                break;
            case LOGIN_FAILED:
                loginFailedCount++;
                break;
            default:
                // No counter for other types
                break;
        }
    }
    
    /**
     * Get recent activities.
     * @param limit The maximum number of activities to return
     * @return A list of recent activities
     */
    public List<SessionActivity> getRecentActivities(int limit) {
        List<SessionActivity> result = new ArrayList<>();
        
        synchronized (recentActivities) {
            int startIndex = Math.max(0, recentActivities.size() - limit);
            for (int i = startIndex; i < recentActivities.size(); i++) {
                result.add(recentActivities.get(i));
            }
        }
        
        return result;
    }
    
    /**
     * Get activities for a specific user.
     * @param username The username
     * @param limit The maximum number of activities to return
     * @return A list of user activities
     */
    public List<SessionActivity> getUserActivities(String username, int limit) {
        if (username == null) return new ArrayList<>();
        
        List<SessionActivity> userActivities = activityByUser.get(username);
        if (userActivities == null || userActivities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SessionActivity> result = new ArrayList<>();
        
        synchronized (userActivities) {
            int startIndex = Math.max(0, userActivities.size() - limit);
            for (int i = startIndex; i < userActivities.size(); i++) {
                result.add(userActivities.get(i));
            }
        }
        
        return result;
    }
    
    /**
     * Get the number of sessions created.
     * @return The number of sessions created
     */
    public int getSessionCreatedCount() {
        return sessionCreatedCount;
    }
    
    /**
     * Get the number of sessions renewed.
     * @return The number of sessions renewed
     */
    public int getSessionRenewedCount() {
        return sessionRenewedCount;
    }
    
    /**
     * Get the number of sessions invalidated.
     * @return The number of sessions invalidated
     */
    public int getSessionInvalidatedCount() {
        return sessionInvalidatedCount;
    }
    
    /**
     * Get the number of sessions expired.
     * @return The number of sessions expired
     */
    public int getSessionExpiredCount() {
        return sessionExpiredCount;
    }
    
    /**
     * Get the number of failed login attempts.
     * @return The number of failed login attempts
     */
    public int getLoginFailedCount() {
        return loginFailedCount;
    }
    
    /**
     * Reset all statistics.
     */
    public void resetStatistics() {
        sessionCreatedCount = 0;
        sessionRenewedCount = 0;
        sessionInvalidatedCount = 0;
        sessionExpiredCount = 0;
        loginFailedCount = 0;
    }
    
    /**
     * Clear all activity logs.
     */
    public void clearActivityLogs() {
        synchronized (recentActivities) {
            recentActivities.clear();
        }
        
        activityByUser.clear();
    }
    
    /**
     * Enum for session activity types.
     */
    public enum SessionActivityType {
        CREATED,
        RENEWED,
        INVALIDATED,
        EXPIRED,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        ACCESS_DENIED
    }
    
    /**
     * Class representing a session activity.
     */
    public static class SessionActivity {
        private final String sessionId;
        private final String username;
        private final LocalDateTime timestamp;
        private final SessionActivityType type;
        private final String details;
        
        /**
         * Constructor.
         * @param sessionId The session ID
         * @param username The username
         * @param timestamp The activity timestamp
         * @param type The activity type
         * @param details The activity details
         */
        public SessionActivity(String sessionId, String username, LocalDateTime timestamp, 
                               SessionActivityType type, String details) {
            this.sessionId = sessionId;
            this.username = username;
            this.timestamp = timestamp;
            this.type = type;
            this.details = details;
        }
        
        /**
         * Get the session ID.
         * @return The session ID
         */
        public String getSessionId() {
            return sessionId;
        }
        
        /**
         * Get the username.
         * @return The username
         */
        public String getUsername() {
            return username;
        }
        
        /**
         * Get the activity timestamp.
         * @return The timestamp
         */
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        /**
         * Get the activity type.
         * @return The type
         */
        public SessionActivityType getType() {
            return type;
        }
        
        /**
         * Get the activity details.
         * @return The details
         */
        public String getDetails() {
            return details;
        }
    }
}