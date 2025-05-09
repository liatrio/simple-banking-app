package com.smartbank.model;

import com.smartbank.BaseTest;
import com.smartbank.service.theme.ThemeService.Theme;
import com.smartbank.service.theme.ThemeService.FontSize;
import com.smartbank.service.theme.ThemeService.ColorBlindnessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ThemePreference model class.
 */
public class ThemePreferenceTest extends BaseTest {

    @Test
    @DisplayName("ThemePreference constructor should initialize fields with default values")
    public void testThemePreferenceConstructor() {
        // Arrange
        User user = new UserBuilder().build();
        
        // Act
        ThemePreference preference = new ThemePreference(user);
        
        // Assert
        assertEquals(user.getUserId(), preference.getUserId());
        assertEquals(user, preference.getUser());
        assertEquals(Theme.LIGHT, preference.getTheme());
        assertEquals(FontSize.MEDIUM, preference.getFontSize());
        assertEquals(ColorBlindnessType.NONE, preference.getColorBlindnessType());
        assertFalse(preference.isKeyboardNavigationEnabled());
        assertFalse(preference.isScreenReaderEnabled());
    }
    
    @Test
    @DisplayName("setTheme should update the theme")
    public void testSetTheme() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        preference.setTheme(Theme.DARK);
        
        // Assert
        assertEquals(Theme.DARK, preference.getTheme());
    }
    
    @Test
    @DisplayName("setFontSize should update the font size")
    public void testSetFontSize() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        preference.setFontSize(FontSize.LARGE);
        
        // Assert
        assertEquals(FontSize.LARGE, preference.getFontSize());
    }
    
    @Test
    @DisplayName("setColorBlindnessType should update the color blindness type")
    public void testSetColorBlindnessType() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        preference.setColorBlindnessType(ColorBlindnessType.PROTANOPIA);
        
        // Assert
        assertEquals(ColorBlindnessType.PROTANOPIA, preference.getColorBlindnessType());
    }
    
    @Test
    @DisplayName("setKeyboardNavigationEnabled should update the keyboard navigation flag")
    public void testSetKeyboardNavigationEnabled() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        preference.setKeyboardNavigationEnabled(true);
        
        // Assert
        assertTrue(preference.isKeyboardNavigationEnabled());
    }
    
    @Test
    @DisplayName("setScreenReaderEnabled should update the screen reader flag")
    public void testSetScreenReaderEnabled() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        preference.setScreenReaderEnabled(true);
        
        // Assert
        assertTrue(preference.isScreenReaderEnabled());
    }
    
    @Test
    @DisplayName("equals should return true for preferences with same user ID")
    public void testEquals() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference1 = new ThemePreference(user);
        ThemePreference preference2 = new ThemePreference(user);
        
        // Make them different in other ways
        preference2.setTheme(Theme.DARK);
        preference2.setFontSize(FontSize.LARGE);
        
        // Act & Assert
        assertEquals(preference1, preference2);
        assertEquals(preference1.hashCode(), preference2.hashCode());
    }
    
    @Test
    @DisplayName("equals should return false for preferences with different user IDs")
    public void testNotEquals() {
        // Arrange
        User user1 = new UserBuilder().build();
        User user2 = new UserBuilder().build();
        ThemePreference preference1 = new ThemePreference(user1);
        ThemePreference preference2 = new ThemePreference(user2);
        
        // Act & Assert
        assertNotEquals(preference1, preference2);
        assertNotEquals(preference1.hashCode(), preference2.hashCode());
    }
    
    @Test
    @DisplayName("toString should return a string representation of the preference")
    public void testToString() {
        // Arrange
        User user = new UserBuilder().build();
        ThemePreference preference = new ThemePreference(user);
        
        // Act
        String result = preference.toString();
        
        // Assert
        assertTrue(result.contains(user.getUserId()));
        assertTrue(result.contains(preference.getTheme().toString()));
        assertTrue(result.contains(preference.getFontSize().toString()));
        assertTrue(result.contains(preference.getColorBlindnessType().toString()));
        assertTrue(result.contains(String.valueOf(preference.isKeyboardNavigationEnabled())));
        assertTrue(result.contains(String.valueOf(preference.isScreenReaderEnabled())));
    }
}
