package com.smartbank.auth;

/**
 * Provides a centralized access point for security-related operations
 * including authentication and authorization.
 */
public class SecurityContext {
    
    // Singleton instance
    private static SecurityContext instance;
    
    // Current user session
    private UserSession currentSession;
    
    // Role permission service
    private final RolePermission rolePermission;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SecurityContext() {
        this.rolePermission = RolePermission.getInstance();
    }
    
    /**
     * Get the singleton instance.
     * @return The SecurityContext instance
     */
    public static synchronized SecurityContext getInstance() {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }
    
    /**
     * Set the current user session.
     * @param session The user session
     */
    public void setCurrentSession(UserSession session) {
        this.currentSession = session;
    }
    
    /**
     * Get the current user session.
     * @return The current user session
     */
    public UserSession getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Check if the current user has the required role.
     * @param requiredRole The required role
     * @return true if the user has the required role, false otherwise
     */
    public boolean hasRole(String requiredRole) {
        return currentSession != null 
                && currentSession.isValid() 
                && currentSession.hasRole(requiredRole);
    }
    
    /**
     * Check if the current user has the required permission.
     * @param permission The required permission
     * @return true if the user has the required permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        if (currentSession == null || !currentSession.isValid()) {
            return false;
        }
        
        String role = currentSession.getUser().getRole();
        return rolePermission.hasPermission(role, permission);
    }
    
    /**
     * Invalidate the current session.
     */
    public void invalidateSession() {
        if (currentSession != null) {
            currentSession.invalidate();
            currentSession = null;
        }
    }
    
    /**
     * Check if there is an authenticated user.
     * @return true if there is an authenticated user, false otherwise
     */
    public boolean isAuthenticated() {
        return currentSession != null && currentSession.isValid();
    }
    
    /**
     * Get the current authenticated user.
     * @return The current user, or null if no user is authenticated
     */
    public static com.smartbank.model.User getCurrentUser() {
        SecurityContext instance = getInstance();
        if (instance.isAuthenticated()) {
            return instance.currentSession.getUser();
        }
        return null;
    }
}