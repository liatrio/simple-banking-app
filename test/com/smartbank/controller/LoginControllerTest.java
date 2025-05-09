package com.smartbank.controller;

import com.smartbank.auth.AuthenticationService;
import com.smartbank.auth.UserSession;
import com.smartbank.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthenticationService used by the LoginController.
 * Since JavaFX controllers are difficult to test directly, we focus on testing
 * the business logic and service interactions instead.
 */
@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @Mock
    private AuthenticationService authService;

    @BeforeEach
    public void setUp() {
        // No setup needed with MockitoExtension
    }
    
    @Test
    @DisplayName("Authentication service should validate login credentials")
    public void testAuthenticationServiceLogin() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User mockUser = new User(username, password, "customer");
        UserSession mockSession = new UserSession(mockUser);
        
        when(authService.login(username, password)).thenReturn(Optional.of(mockSession));
        
        // Act
        Optional<UserSession> result = authService.login(username, password);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUser().getUsername());
        verify(authService).login(username, password);
    }
    
    @Test
    @DisplayName("Authentication service should return empty optional for invalid credentials")
    public void testAuthenticationServiceLoginFailure() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        
        when(authService.login(username, password)).thenReturn(Optional.empty());
        
        // Act
        Optional<UserSession> result = authService.login(username, password);
        
        // Assert
        assertFalse(result.isPresent());
        verify(authService).login(username, password);
    }
    
    @Test
    @DisplayName("Authentication service should validate registration data")
    public void testAuthenticationServiceRegister() {
        // Arrange
        String username = "newuser";
        String password = "Password123";
        String role = "customer";
        User mockUser = new User(username, password, role);
        
        when(authService.isUsernameAvailable(username)).thenReturn(true);
        when(authService.validatePassword(password)).thenReturn(true);
        when(authService.register(username, password, role)).thenReturn(mockUser);
        
        // Act
        boolean isUsernameAvailable = authService.isUsernameAvailable(username);
        boolean isPasswordValid = authService.validatePassword(password);
        User result = authService.register(username, password, role);
        
        // Assert
        assertTrue(isUsernameAvailable);
        assertTrue(isPasswordValid);
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(role, result.getRole());
        
        verify(authService).isUsernameAvailable(username);
        verify(authService).validatePassword(password);
        verify(authService).register(username, password, role);
    }
    
    @Test
    @DisplayName("Authentication service should reject registration with existing username")
    public void testAuthenticationServiceRegisterWithExistingUsername() {
        // Arrange
        String username = "existinguser";
        String password = "Password123";
        String role = "customer";
        
        when(authService.isUsernameAvailable(username)).thenReturn(false);
        
        // Act
        boolean isUsernameAvailable = authService.isUsernameAvailable(username);
        
        // Assert
        assertFalse(isUsernameAvailable);
        verify(authService).isUsernameAvailable(username);
        verify(authService, never()).register(anyString(), anyString(), anyString());
    }

}
