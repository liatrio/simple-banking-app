package com.smartbank.model;

import com.smartbank.service.theme.ThemeService.Theme;
import com.smartbank.service.theme.ThemeService.FontSize;
import com.smartbank.service.theme.ThemeService.ColorBlindnessType;

import javax.persistence.*;
import java.util.Objects;

/**
 * Model to store user theme and accessibility preferences.
 */
@Entity
@Table(name = "theme_preferences")
public class ThemePreference {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "theme")
    private Theme theme;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "font_size")
    private FontSize fontSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "color_blindness_type")
    private ColorBlindnessType colorBlindnessType;
    
    @Column(name = "keyboard_navigation_enabled")
    private boolean keyboardNavigationEnabled;
    
    @Column(name = "screen_reader_enabled")
    private boolean screenReaderEnabled;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Default constructor for JPA.
     */
    protected ThemePreference() {
    }
    
    /**
     * Create new theme preferences for a user.
     * 
     * @param user The user to create preferences for
     */
    public ThemePreference(User user) {
        this.user = user;
        this.userId = user.getUserId();
        
        // Default values
        this.theme = Theme.LIGHT;
        this.fontSize = FontSize.MEDIUM;
        this.colorBlindnessType = ColorBlindnessType.NONE;
        this.keyboardNavigationEnabled = false;
        this.screenReaderEnabled = false;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Theme getTheme() {
        return theme;
    }
    
    public void setTheme(Theme theme) {
        this.theme = theme;
    }
    
    public FontSize getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(FontSize fontSize) {
        this.fontSize = fontSize;
    }
    
    public ColorBlindnessType getColorBlindnessType() {
        return colorBlindnessType;
    }
    
    public void setColorBlindnessType(ColorBlindnessType colorBlindnessType) {
        this.colorBlindnessType = colorBlindnessType;
    }
    
    public boolean isKeyboardNavigationEnabled() {
        return keyboardNavigationEnabled;
    }
    
    public void setKeyboardNavigationEnabled(boolean keyboardNavigationEnabled) {
        this.keyboardNavigationEnabled = keyboardNavigationEnabled;
    }
    
    public boolean isScreenReaderEnabled() {
        return screenReaderEnabled;
    }
    
    public void setScreenReaderEnabled(boolean screenReaderEnabled) {
        this.screenReaderEnabled = screenReaderEnabled;
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemePreference that = (ThemePreference) o;
        return Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return "ThemePreference{" +
                "userId='" + userId + '\'' +
                ", theme=" + theme +
                ", fontSize=" + fontSize +
                ", colorBlindnessType=" + colorBlindnessType +
                ", keyboardNavigationEnabled=" + keyboardNavigationEnabled +
                ", screenReaderEnabled=" + screenReaderEnabled +
                '}';
    }
}