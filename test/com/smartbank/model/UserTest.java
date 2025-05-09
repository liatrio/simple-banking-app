package com.smartbank.model;

import com.smartbank.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the User model class.
 */
public class UserTest extends BaseTest {

    @Test
    @DisplayName("User constructor should initialize fields correctly")
    public void testUserConstructor() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String role = "customer";
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        
        // Act
        User user = new User(username, password, role, firstName, lastName, email);
        
        // Assert
        assertNotNull(user.getUserId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPasswordHash()); // Note: In a real app, this would be hashed
        assertEquals(role, user.getRole());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(email, user.getEmail());
        assertTrue(user.getAccounts().isEmpty());
    }
    
    @Test
    @DisplayName("getId should return the same value as getUserId")
    public void testGetId() {
        // Arrange
        User user = new UserBuilder().build();
        
        // Act & Assert
        assertEquals(user.getUserId(), user.getId());
    }
    
    @Test
    @DisplayName("addAccount should add an account to the user's accounts")
    public void testAddAccount() {
        // Arrange
        User user = new UserBuilder().build();
        CheckingAccount account = new CheckingAccountBuilder()
                .withAccountHolder(user)
                .build();
        
        // Act
        user.addAccount(account);
        
        // Assert
        assertEquals(1, user.getAccounts().size());
        assertTrue(user.getAccounts().contains(account));
    }
    
    @Test
    @DisplayName("checkPassword should verify password correctly")
    public void testCheckPassword() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"; // BCrypt hash for "password123"
        
        User user = new UserBuilder().build();
        user.setPasswordHash(hashedPassword);
        
        // Act & Assert
        try (MockedStatic<org.mindrot.jbcrypt.BCrypt> mockedBCrypt = Mockito.mockStatic(org.mindrot.jbcrypt.BCrypt.class)) {
            // Mock BCrypt.checkpw to return true for the correct password and false otherwise
            mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(Mockito.eq(rawPassword), Mockito.eq(hashedPassword)))
                    .thenReturn(true);
            mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(Mockito.eq("wrongpassword"), Mockito.eq(hashedPassword)))
                    .thenReturn(false);
            
            assertTrue(user.checkPassword(rawPassword));
            assertFalse(user.checkPassword("wrongpassword"));
        }
    }
    
    @Test
    @DisplayName("checkPassword should handle exceptions gracefully")
    public void testCheckPasswordWithException() {
        // Arrange
        User user = new UserBuilder().build();
        user.setPasswordHash("invalid-hash"); // Not a valid BCrypt hash
        
        // Act & Assert
        try (MockedStatic<org.mindrot.jbcrypt.BCrypt> mockedBCrypt = Mockito.mockStatic(org.mindrot.jbcrypt.BCrypt.class)) {
            // Mock BCrypt.checkpw to throw an exception
            mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid hash"));
            
            assertFalse(user.checkPassword("anypassword"));
        }
    }
    
    @ParameterizedTest
    @CsvSource({
        "admin, true",
        "customer, false"
    })
    @DisplayName("hasPermission should check permissions based on role")
    public void testHasPermission(String role, boolean expectedResult) {
        // Arrange
        User user = new UserBuilder()
                .withRole(role)
                .build();
        
        // Act & Assert
        assertEquals(expectedResult, user.hasPermission("any-permission"));
    }
    
    @Test
    @DisplayName("createDefaultThemePreference should create a new theme preference if none exists")
    public void testCreateDefaultThemePreference() {
        // Arrange
        User user = new UserBuilder().build();
        assertNull(user.getThemePreference());
        
        // Act
        ThemePreference themePreference = user.createDefaultThemePreference();
        
        // Assert
        assertNotNull(themePreference);
        assertEquals(user, themePreference.getUser());
        assertEquals(themePreference, user.getThemePreference());
    }
    
    @Test
    @DisplayName("createDefaultThemePreference should return existing theme preference if one exists")
    public void testCreateDefaultThemePreferenceWithExisting() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference existingPreference = new ThemePreference(user);
        user.setThemePreference(existingPreference);
        
        // Act
        ThemePreference themePreference = user.createDefaultThemePreference();
        
        // Assert
        assertSame(existingPreference, themePreference);
    }
    
    @Test
    @DisplayName("equals should return true for same user ID")
    public void testEquals() {
        // Arrange
        User user1 = new UserBuilder().build();
        User user2 = new User(user1.getUsername(), "differentpassword", "differentrole");
        
        // Manually set the same ID
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("userId");
            idField.setAccessible(true);
            idField.set(user2, user1.getUserId());
        } catch (Exception e) {
            fail("Failed to set userId field: " + e.getMessage());
        }
        
        // Act & Assert
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    @DisplayName("toString should return a string representation of the user")
    public void testToString() {
        // Arrange
        User user = new UserBuilder().build();
        
        // Act
        String result = user.toString();
        
        // Assert
        assertTrue(result.contains(user.getUserId()));
        assertTrue(result.contains(user.getUsername()));
        assertTrue(result.contains(user.getRole()));
    }
}
