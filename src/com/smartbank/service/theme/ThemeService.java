package com.smartbank.service.theme;

import javafx.scene.Scene;

/**
 * Service interface for managing application themes and accessibility options.
 */
public interface ThemeService {
    
    /**
     * Available theme options.
     */
    enum Theme {
        LIGHT,
        DARK,
        HIGH_CONTRAST
    }
    
    /**
     * Available font size options.
     */
    enum FontSize {
        SMALL,
        MEDIUM,
        LARGE,
        EXTRA_LARGE
    }
    
    /**
     * Color blindness accommodation types.
     */
    enum ColorBlindnessType {
        NONE,
        PROTANOPIA,  // Red-green color blindness (red weak)
        DEUTERANOPIA, // Red-green color blindness (green weak)
        TRITANOPIA,  // Blue-yellow color blindness
        ACHROMATOPSIA // Complete color blindness
    }
    
    /**
     * Apply the specified theme to the application.
     * 
     * @param scene The JavaFX scene to apply the theme to
     * @param theme The theme to apply
     */
    void applyTheme(Scene scene, Theme theme);
    
    /**
     * Get the current theme.
     * 
     * @return The current theme
     */
    Theme getCurrentTheme();
    
    /**
     * Set the font size for the application.
     * 
     * @param scene The JavaFX scene to apply the font size to
     * @param fontSize The font size to apply
     */
    void setFontSize(Scene scene, FontSize fontSize);
    
    /**
     * Get the current font size.
     * 
     * @return The current font size
     */
    FontSize getCurrentFontSize();
    
    /**
     * Apply color blindness accommodations.
     * 
     * @param scene The JavaFX scene to apply the accommodations to
     * @param type The type of color blindness to accommodate
     */
    void applyColorBlindnessAccommodation(Scene scene, ColorBlindnessType type);
    
    /**
     * Get the current color blindness accommodation.
     * 
     * @return The current color blindness accommodation
     */
    ColorBlindnessType getCurrentColorBlindnessAccommodation();
    
    /**
     * Enable keyboard navigation support.
     * 
     * @param scene The JavaFX scene to enable keyboard navigation for
     * @param enabled True to enable, false to disable
     */
    void setKeyboardNavigationEnabled(Scene scene, boolean enabled);
    
    /**
     * Check if keyboard navigation is enabled.
     * 
     * @return True if keyboard navigation is enabled, false otherwise
     */
    boolean isKeyboardNavigationEnabled();
    
    /**
     * Enable screen reader support.
     * 
     * @param scene The JavaFX scene to enable screen reader support for
     * @param enabled True to enable, false to disable
     */
    void setScreenReaderEnabled(Scene scene, boolean enabled);
    
    /**
     * Check if screen reader support is enabled.
     * 
     * @return True if screen reader support is enabled, false otherwise
     */
    boolean isScreenReaderEnabled();
    
    /**
     * Save the current theme preferences for the specified user.
     * 
     * @param userId The ID of the user to save preferences for
     */
    void savePreferences(String userId);
    
    /**
     * Load theme preferences for the specified user.
     * 
     * @param userId The ID of the user to load preferences for
     */
    void loadPreferences(String userId);
}