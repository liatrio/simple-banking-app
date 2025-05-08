package com.smartbank.auth;

/**
 * Interface for listeners to session events.
 */
public interface SessionEventListener {
    
    /**
     * Called when a new session is created.
     * @param session The created session
     */
    void onSessionCreated(UserSession session);
    
    /**
     * Called when a session is renewed.
     * @param session The renewed session
     */
    void onSessionRenewed(UserSession session);
    
    /**
     * Called when a session is manually invalidated.
     * @param session The invalidated session
     */
    void onSessionInvalidated(UserSession session);
    
    /**
     * Called when a session expires.
     * @param session The expired session
     */
    void onSessionExpired(UserSession session);
}