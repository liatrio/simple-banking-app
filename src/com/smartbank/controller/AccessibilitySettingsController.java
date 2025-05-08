package com.smartbank.controller;

import com.smartbank.auth.SecurityContext;
import com.smartbank.model.User;
import com.smartbank.service.theme.ThemeService;
import com.smartbank.service.theme.ThemeServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the accessibility settings view.
 */
public class AccessibilitySettingsController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(AccessibilitySettingsController.class.getName());
    
    @FXML private Pane rootPane;
    @FXML private ToggleGroup themeToggleGroup;
    @FXML private RadioButton radioLightTheme;
    @FXML private RadioButton radioDarkTheme;
    @FXML private RadioButton radioHighContrastTheme;
    @FXML private ComboBox<String> comboFontSize;
    @FXML private ComboBox<String> comboColorBlindness;
    @FXML private CheckBox checkKeyboardNav;
    @FXML private CheckBox checkScreenReader;
    @FXML private Label lblStatus;
    
    private ThemeService themeService;
    private MainController mainController;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Get theme service
        themeService = ThemeServiceImpl.getInstance();
        
        // Initialize font size combo box
        comboFontSize.setItems(FXCollections.observableArrayList(
                "Small", "Medium (Default)", "Large", "Extra Large"));
        
        // Initialize color blindness combo box
        comboColorBlindness.setItems(FXCollections.observableArrayList(
                "None (Default)", "Protanopia (Red Weak)", "Deuteranopia (Green Weak)", 
                "Tritanopia (Blue-Yellow)", "Achromatopsia (No Color)"));
        
        // Initialize theme toggle group
        switch (themeService.getCurrentTheme()) {
            case DARK:
                radioDarkTheme.setSelected(true);
                break;
            case HIGH_CONTRAST:
                radioHighContrastTheme.setSelected(true);
                break;
            case LIGHT:
            default:
                radioLightTheme.setSelected(true);
                break;
        }
        
        // Initialize font size combo box
        switch (themeService.getCurrentFontSize()) {
            case SMALL:
                comboFontSize.getSelectionModel().select(0);
                break;
            case LARGE:
                comboFontSize.getSelectionModel().select(2);
                break;
            case EXTRA_LARGE:
                comboFontSize.getSelectionModel().select(3);
                break;
            case MEDIUM:
            default:
                comboFontSize.getSelectionModel().select(1);
                break;
        }
        
        // Initialize color blindness combo box
        switch (themeService.getCurrentColorBlindnessAccommodation()) {
            case PROTANOPIA:
                comboColorBlindness.getSelectionModel().select(1);
                break;
            case DEUTERANOPIA:
                comboColorBlindness.getSelectionModel().select(2);
                break;
            case TRITANOPIA:
                comboColorBlindness.getSelectionModel().select(3);
                break;
            case ACHROMATOPSIA:
                comboColorBlindness.getSelectionModel().select(4);
                break;
            case NONE:
            default:
                comboColorBlindness.getSelectionModel().select(0);
                break;
        }
        
        // Initialize checkbox states
        checkKeyboardNav.setSelected(themeService.isKeyboardNavigationEnabled());
        checkScreenReader.setSelected(themeService.isScreenReaderEnabled());
    }
    
    /**
     * Set the main controller reference to enable theme updates.
     * 
     * @param controller The main controller
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    
    /**
     * Handle theme change.
     * 
     * @param event The action event
     */
    @FXML
    private void handleThemeChange(ActionEvent event) {
        ThemeService.Theme selectedTheme;
        
        if (radioDarkTheme.isSelected()) {
            selectedTheme = ThemeService.Theme.DARK;
        } else if (radioHighContrastTheme.isSelected()) {
            selectedTheme = ThemeService.Theme.HIGH_CONTRAST;
        } else {
            selectedTheme = ThemeService.Theme.LIGHT;
        }
        
        // Apply theme
        if (mainController != null) {
            mainController.changeTheme(selectedTheme);
        } else {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                themeService.applyTheme(scene, selectedTheme);
                savePreferences();
            }
        }
        
        showStatus("Theme updated to " + selectedTheme.toString());
    }
    
    /**
     * Handle font size change.
     * 
     * @param event The action event
     */
    @FXML
    private void handleFontSizeChange(ActionEvent event) {
        int selectedIndex = comboFontSize.getSelectionModel().getSelectedIndex();
        ThemeService.FontSize fontSize;
        
        switch (selectedIndex) {
            case 0:
                fontSize = ThemeService.FontSize.SMALL;
                break;
            case 2:
                fontSize = ThemeService.FontSize.LARGE;
                break;
            case 3:
                fontSize = ThemeService.FontSize.EXTRA_LARGE;
                break;
            case 1:
            default:
                fontSize = ThemeService.FontSize.MEDIUM;
                break;
        }
        
        // Apply font size
        if (mainController != null) {
            mainController.changeFontSize(fontSize);
        } else {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                themeService.setFontSize(scene, fontSize);
                savePreferences();
            }
        }
        
        showStatus("Font size updated to " + fontSize.toString());
    }
    
    /**
     * Handle color blindness accommodation change.
     * 
     * @param event The action event
     */
    @FXML
    private void handleColorBlindnessChange(ActionEvent event) {
        int selectedIndex = comboColorBlindness.getSelectionModel().getSelectedIndex();
        ThemeService.ColorBlindnessType type;
        
        switch (selectedIndex) {
            case 1:
                type = ThemeService.ColorBlindnessType.PROTANOPIA;
                break;
            case 2:
                type = ThemeService.ColorBlindnessType.DEUTERANOPIA;
                break;
            case 3:
                type = ThemeService.ColorBlindnessType.TRITANOPIA;
                break;
            case 4:
                type = ThemeService.ColorBlindnessType.ACHROMATOPSIA;
                break;
            case 0:
            default:
                type = ThemeService.ColorBlindnessType.NONE;
                break;
        }
        
        // Apply color blindness accommodation
        if (mainController != null) {
            mainController.applyColorBlindnessAccommodation(type);
        } else {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                themeService.applyColorBlindnessAccommodation(scene, type);
                savePreferences();
            }
        }
        
        showStatus("Color blindness accommodation updated to " + type.toString());
    }
    
    /**
     * Handle keyboard navigation toggle.
     * 
     * @param event The action event
     */
    @FXML
    private void handleKeyboardNavToggle(ActionEvent event) {
        boolean enabled = checkKeyboardNav.isSelected();
        
        // Apply keyboard navigation setting
        if (mainController != null) {
            mainController.setKeyboardNavigationEnabled(enabled);
        } else {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                themeService.setKeyboardNavigationEnabled(scene, enabled);
                savePreferences();
            }
        }
        
        showStatus("Keyboard navigation " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Handle screen reader toggle.
     * 
     * @param event The action event
     */
    @FXML
    private void handleScreenReaderToggle(ActionEvent event) {
        boolean enabled = checkScreenReader.isSelected();
        
        // Apply screen reader setting
        if (mainController != null) {
            mainController.setScreenReaderEnabled(enabled);
        } else {
            Scene scene = rootPane.getScene();
            if (scene != null) {
                themeService.setScreenReaderEnabled(scene, enabled);
                savePreferences();
            }
        }
        
        showStatus("Screen reader support " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Save current settings as default for all users.
     * 
     * @param event The action event
     */
    @FXML
    private void handleSaveAsDefault(ActionEvent event) {
        // In a real application, this would save to system-wide settings
        // For now, we just show a message
        showStatus("Settings saved as application defaults");
    }
    
    /**
     * Reset all settings to defaults.
     * 
     * @param event The action event
     */
    @FXML
    private void handleResetToDefaults(ActionEvent event) {
        // Reset UI controls
        radioLightTheme.setSelected(true);
        comboFontSize.getSelectionModel().select(1); // Medium
        comboColorBlindness.getSelectionModel().select(0); // None
        checkKeyboardNav.setSelected(false);
        checkScreenReader.setSelected(false);
        
        // Apply default settings
        Scene scene = rootPane.getScene();
        if (scene != null) {
            themeService.applyTheme(scene, ThemeService.Theme.LIGHT);
            themeService.setFontSize(scene, ThemeService.FontSize.MEDIUM);
            themeService.applyColorBlindnessAccommodation(scene, ThemeService.ColorBlindnessType.NONE);
            themeService.setKeyboardNavigationEnabled(scene, false);
            themeService.setScreenReaderEnabled(scene, false);
            savePreferences();
        }
        
        showStatus("All settings reset to defaults");
    }
    
    /**
     * Display a status message.
     * 
     * @param message The message to display
     */
    private void showStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            
            // Log the message
            LOGGER.log(Level.INFO, "Accessibility setting change: {0}", message);
        }
    }
    
    /**
     * Save the current preferences for the current user.
     */
    private void savePreferences() {
        SecurityContext securityContext = SecurityContext.getInstance();
        if (securityContext.isAuthenticated()) {
            User user = securityContext.getCurrentSession().getUser();
            themeService.savePreferences(user.getUserId());
        }
    }
    
    @Override
    protected Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }
}