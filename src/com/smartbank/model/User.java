package com.smartbank.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * User class for account ownership and authentication.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "userId")
    private String userId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String role;
    
    @Column
    private String firstName;
    
    @Column
    private String lastName;
    
    @Column
    private String email;
    
    @OneToMany(mappedBy = "accountHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ThemePreference themePreference;

    // Default constructor required by JPA
    protected User() {
    }

    public User(String username, String password, String role) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        // Note: passwordHash will be set by AuthenticationServiceImpl
        this.passwordHash = password;  // Temporary, will be replaced by proper hash
        this.role = role;
    }
    
    public User(String username, String password, String role, String firstName, String lastName, String email) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        // Note: passwordHash will be set by AuthenticationServiceImpl
        this.passwordHash = password;  // Temporary, will be replaced by proper hash
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }
    
    /**
     * Alias for getUserId.
     * @return The user ID
     */
    public String getId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Check if the user has a specific permission.
     * @param permission The permission to check
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permission) {
        // For simplicity, admin role has all permissions
        if ("admin".equalsIgnoreCase(role)) {
            return true;
        }
        
        // For other roles, delegate to RolePermission class
        return com.smartbank.auth.RolePermission.getInstance().hasPermission(role, permission);
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public boolean checkPassword(String password) {
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(password, passwordHash);
        } catch (Exception e) {
            // This handles the case where the hash is not a valid BCrypt hash
            return false;
        }
    }
    
    /**
     * Set the password hash directly.
     * This should only be called by the AuthenticationService.
     * 
     * @param passwordHash The BCrypt password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Get the user's theme preference.
     * @return The theme preference, or null if none exists
     */
    public ThemePreference getThemePreference() {
        return themePreference;
    }
    
    /**
     * Set the user's theme preference.
     * @param themePreference The theme preference
     */
    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference;
    }
    
    /**
     * Create a default theme preference for this user if one doesn't already exist.
     * @return The existing or newly created theme preference
     */
    public ThemePreference createDefaultThemePreference() {
        if (themePreference == null) {
            themePreference = new ThemePreference(this);
        }
        return themePreference;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
