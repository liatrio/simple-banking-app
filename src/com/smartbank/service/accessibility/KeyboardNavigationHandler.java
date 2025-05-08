package com.smartbank.service.accessibility;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for enhanced keyboard navigation.
 */
public class KeyboardNavigationHandler {
    private static final Logger LOGGER = Logger.getLogger(KeyboardNavigationHandler.class.getName());
    
    private final Scene scene;
    private boolean enabled = false;
    private final Map<KeyCombination, Runnable> keyboardShortcuts = new HashMap<>();
    
    /**
     * Create a new keyboard navigation handler.
     * 
     * @param scene The scene to add keyboard navigation to
     */
    public KeyboardNavigationHandler(Scene scene) {
        this.scene = scene;
    }
    
    /**
     * Enable or disable keyboard navigation.
     * 
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        
        this.enabled = enabled;
        
        if (enabled) {
            enableKeyboardNavigation();
        } else {
            disableKeyboardNavigation();
        }
        
        LOGGER.log(Level.INFO, "Keyboard navigation {0}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Check if keyboard navigation is enabled.
     * 
     * @return True if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable keyboard navigation.
     */
    private void enableKeyboardNavigation() {
        // Add global event handler for focus indicators
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        
        // Add keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Apply style class to focusable elements for better focus visibility
        applyFocusStyleToControls(scene.getRoot());
    }
    
    /**
     * Set up keyboard shortcuts.
     */
    private void setupKeyboardShortcuts() {
        // Define keyboard shortcuts
        // Alt+D - Dashboard
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN),
                () -> LOGGER.info("Dashboard shortcut activated"));
        
        // Alt+A - Account List
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN),
                () -> LOGGER.info("Account List shortcut activated"));
        
        // Alt+T - Transactions
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN),
                () -> LOGGER.info("Transactions shortcut activated"));
        
        // Alt+P - Profile
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN),
                () -> LOGGER.info("Profile shortcut activated"));
        
        // Alt+H - Help/Accessibility
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN),
                () -> LOGGER.info("Help/Accessibility shortcut activated"));
        
        // Add the shortcuts to the scene
        for (Map.Entry<KeyCombination, Runnable> entry : keyboardShortcuts.entrySet()) {
            scene.getAccelerators().put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Handle key press events.
     * 
     * @param event The key event
     */
    private void handleKeyPressed(KeyEvent event) {
        if (!enabled) {
            return;
        }
        
        // Check for Tab key for enhanced focus indicators
        if (event.getCode() == KeyCode.TAB) {
            // The focus will be handled by the default JavaFX focus traversal
            LOGGER.fine("Tab key pressed for focus navigation");
        }
        
        // Handle keyboard shortcuts
        for (Map.Entry<KeyCombination, Runnable> entry : keyboardShortcuts.entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                event.consume();
                break;
            }
        }
    }
    
    /**
     * Disable keyboard navigation.
     */
    private void disableKeyboardNavigation() {
        // Remove global event handler
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        
        // Remove keyboard shortcuts
        for (KeyCombination keyCombination : keyboardShortcuts.keySet()) {
            scene.getAccelerators().remove(keyCombination);
        }
        
        // Remove focus style from controls
        removeFocusStyleFromControls(scene.getRoot());
    }
    
    /**
     * Apply focus style to controls.
     * 
     * @param node The node to apply styles to
     */
    private void applyFocusStyleToControls(Node node) {
        if (node instanceof Control) {
            // Add a CSS class for better focus visibility
            node.getStyleClass().add("control-with-focus");
        }
        
        // Process children
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyFocusStyleToControls(child);
            }
        }
    }
    
    /**
     * Remove focus style from controls.
     * 
     * @param node The node to remove styles from
     */
    private void removeFocusStyleFromControls(Node node) {
        if (node instanceof Control) {
            // Remove the CSS class
            node.getStyleClass().remove("control-with-focus");
        }
        
        // Process children
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                removeFocusStyleFromControls(child);
            }
        }
    }
    
    /**
     * Add screen reader descriptions to common controls.
     * 
     * @param node The node to add descriptions to
     */
    public void addAccessibilityDescriptions(Node node) {
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getAccessibleText() == null) {
                button.setAccessibleText(button.getText() + " button");
            }
        } else if (node instanceof TextField) {
            TextField textField = (TextField) node;
            if (textField.getAccessibleText() == null && textField.getPromptText() != null) {
                textField.setAccessibleText(textField.getPromptText() + " input field");
            }
        } else if (node instanceof Label) {
            Label label = (Label) node;
            if (label.getAccessibleText() == null) {
                label.setAccessibleText(label.getText());
            }
        }
        
        // Process children
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                addAccessibilityDescriptions(child);
            }
        }
    }
}