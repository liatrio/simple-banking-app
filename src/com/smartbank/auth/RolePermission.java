package com.smartbank.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages role-based permissions in the application.
 */
public class RolePermission {
    
    // Common role constants
    public static final String ADMIN = "admin";
    
    // Singleton instance
    private static RolePermission instance;
    
    // Permission map by role
    private final Map<String, Set<String>> permissionsByRole;
    
    /**
     * Private constructor for singleton pattern.
     */
    private RolePermission() {
        permissionsByRole = new HashMap<>();
        initializePermissions();
    }
    
    /**
     * Initialize default permission settings.
     */
    private void initializePermissions() {
        // Admin role has all permissions
        Set<String> adminPermissions = new HashSet<>();
        adminPermissions.add("USER_CREATE");
        adminPermissions.add("USER_READ");
        adminPermissions.add("USER_UPDATE");
        adminPermissions.add("USER_DELETE");
        adminPermissions.add("USER_LIST");
        adminPermissions.add("ACCOUNT_CREATE");
        adminPermissions.add("ACCOUNT_READ");
        adminPermissions.add("ACCOUNT_UPDATE");
        adminPermissions.add("ACCOUNT_DELETE");
        adminPermissions.add("TRANSACTION_CREATE");
        adminPermissions.add("TRANSACTION_READ");
        adminPermissions.add("SYSTEM_ADMIN");
        permissionsByRole.put("admin", adminPermissions);
        
        // Customer role has limited permissions
        Set<String> customerPermissions = new HashSet<>();
        customerPermissions.add("USER_READ_SELF");
        customerPermissions.add("USER_UPDATE_SELF");
        customerPermissions.add("ACCOUNT_READ_OWN");
        customerPermissions.add("TRANSACTION_CREATE");
        customerPermissions.add("TRANSACTION_READ_OWN");
        permissionsByRole.put("customer", customerPermissions);
    }
    
    /**
     * Get the singleton instance.
     * @return The RolePermission instance
     */
    public static synchronized RolePermission getInstance() {
        if (instance == null) {
            instance = new RolePermission();
        }
        return instance;
    }
    
    /**
     * Check if a role has a specific permission.
     * @param role The role to check
     * @param permission The permission to check for
     * @return true if the role has the permission, false otherwise
     */
    public boolean hasPermission(String role, String permission) {
        if (role == null || permission == null) {
            return false;
        }
        
        // Make role case-insensitive by converting to lowercase
        String normalizedRole = role.toLowerCase();
        
        // Handle admin role specifically - admin has all permissions
        if (normalizedRole.equals("admin")) {
            return true;
        }
        
        // For other roles, check the permission map
        // First try with the original role
        Set<String> permissions = permissionsByRole.get(role);
        if (permissions != null && permissions.contains(permission)) {
            return true;
        }
        
        // Also try with lowercase role if the original wasn't found
        if (!role.equals(normalizedRole)) {
            permissions = permissionsByRole.get(normalizedRole);
            return permissions != null && permissions.contains(permission);
        }
        
        return false;
    }
    
    /**
     * Add a permission to a role.
     * @param role The role
     * @param permission The permission to add
     */
    public void addPermission(String role, String permission) {
        if (role == null || permission == null) {
            return;
        }
        
        permissionsByRole.computeIfAbsent(role, k -> new HashSet<>()).add(permission);
    }
    
    /**
     * Remove a permission from a role.
     * @param role The role
     * @param permission The permission to remove
     */
    public void removePermission(String role, String permission) {
        if (role == null || permission == null) {
            return;
        }
        
        Set<String> permissions = permissionsByRole.get(role);
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
    
    /**
     * Get all permissions for a role.
     * @param role The role
     * @return A set of permissions for the role
     */
    public Set<String> getPermissions(String role) {
        if (role == null) {
            return new HashSet<>();
        }
        
        return new HashSet<>(permissionsByRole.getOrDefault(role, new HashSet<>()));
    }
    
    /**
     * Add a new role with the given permissions.
     * @param role The role to add
     * @param permissions The permissions for the role
     */
    public void addRole(String role, Set<String> permissions) {
        if (role == null) {
            return;
        }
        
        permissionsByRole.put(role, permissions != null ? new HashSet<>(permissions) : new HashSet<>());
    }
    
    /**
     * Remove a role.
     * @param role The role to remove
     */
    public void removeRole(String role) {
        if (role == null) {
            return;
        }
        
        permissionsByRole.remove(role);
    }
    
    /**
     * Get all defined roles.
     * @return A set of all roles
     */
    public Set<String> getRoles() {
        return new HashSet<>(permissionsByRole.keySet());
    }
}