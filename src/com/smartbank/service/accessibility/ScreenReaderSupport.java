package com.smartbank.service.accessibility;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for screen reader support.
 */
public class ScreenReaderSupport {
    private static final Logger LOGGER = Logger.getLogger(ScreenReaderSupport.class.getName());
    
    private final Scene scene;
    private boolean enabled = false;
    
    /**
     * Create a new screen reader support handler.
     * 
     * @param scene The scene to add screen reader support to
     */
    public ScreenReaderSupport(Scene scene) {
        this.scene = scene;
    }
    
    /**
     * Enable or disable screen reader support.
     * 
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        
        this.enabled = enabled;
        
        if (enabled) {
            enableScreenReaderSupport();
        } else {
            disableScreenReaderSupport();
        }
        
        LOGGER.log(Level.INFO, "Screen reader support {0}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Check if screen reader support is enabled.
     * 
     * @return True if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable screen reader support.
     */
    private void enableScreenReaderSupport() {
        // Apply ARIA attributes to nodes
        applyAriaAttributes(scene.getRoot());
    }
    
    /**
     * Disable screen reader support.
     */
    private void disableScreenReaderSupport() {
        // No need to remove ARIA attributes, as they don't harm non-screen reader users
    }
    
    /**
     * Apply ARIA attributes to nodes.
     * 
     * @param node The node to apply attributes to
     */
    private void applyAriaAttributes(Node node) {
        if (node == null) {
            return;
        }
        
        // Set accessibility role and text based on node type
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getAccessibleRole() == null) {
                button.setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
            }
            if (button.getAccessibleText() == null) {
                button.setAccessibleText(button.getText() + " button");
            }
        } else if (node instanceof TextField) {
            TextField textField = (TextField) node;
            if (textField.getAccessibleRole() == null) {
                textField.setAccessibleRole(javafx.scene.AccessibleRole.TEXT_FIELD);
            }
            if (textField.getAccessibleText() == null && textField.getPromptText() != null) {
                textField.setAccessibleText(textField.getPromptText() + " input field");
            }
        } else if (node instanceof PasswordField) {
            PasswordField passwordField = (PasswordField) node;
            if (passwordField.getAccessibleRole() == null) {
                passwordField.setAccessibleRole(javafx.scene.AccessibleRole.PASSWORD_FIELD);
            }
            if (passwordField.getAccessibleText() == null && passwordField.getPromptText() != null) {
                passwordField.setAccessibleText(passwordField.getPromptText() + " password field");
            }
        } else if (node instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) node;
            if (checkBox.getAccessibleRole() == null) {
                checkBox.setAccessibleRole(javafx.scene.AccessibleRole.CHECK_BOX);
            }
            if (checkBox.getAccessibleText() == null) {
                checkBox.setAccessibleText(checkBox.getText() + " checkbox");
            }
        } else if (node instanceof RadioButton) {
            RadioButton radioButton = (RadioButton) node;
            if (radioButton.getAccessibleRole() == null) {
                radioButton.setAccessibleRole(javafx.scene.AccessibleRole.RADIO_BUTTON);
            }
            if (radioButton.getAccessibleText() == null) {
                radioButton.setAccessibleText(radioButton.getText() + " radio button");
            }
        } else if (node instanceof ComboBox) {
            ComboBox<?> comboBox = (ComboBox<?>) node;
            if (comboBox.getAccessibleRole() == null) {
                comboBox.setAccessibleRole(javafx.scene.AccessibleRole.COMBO_BOX);
            }
            if (comboBox.getAccessibleText() == null && comboBox.getPromptText() != null) {
                comboBox.setAccessibleText(comboBox.getPromptText() + " dropdown");
            }
        } else if (node instanceof TableView) {
            TableView<?> tableView = (TableView<?>) node;
            if (tableView.getAccessibleRole() == null) {
                tableView.setAccessibleRole(javafx.scene.AccessibleRole.TABLE_VIEW);
            }
            if (tableView.getAccessibleText() == null) {
                tableView.setAccessibleText("Table with " + tableView.getColumns().size() + " columns");
            }
        } else if (node instanceof Label) {
            Label label = (Label) node;
            if (label.getAccessibleRole() == null) {
                label.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
            }
            if (label.getAccessibleText() == null) {
                label.setAccessibleText(label.getText());
            }
        } else if (node instanceof TitledPane) {
            TitledPane titledPane = (TitledPane) node;
            if (titledPane.getAccessibleRole() == null) {
                titledPane.setAccessibleRole(javafx.scene.AccessibleRole.TITLED_PANE);
            }
            if (titledPane.getAccessibleText() == null) {
                titledPane.setAccessibleText(titledPane.getText() + " section");
            }
        } else if (node instanceof MenuBar) {
            MenuBar menuBar = (MenuBar) node;
            if (menuBar.getAccessibleRole() == null) {
                menuBar.setAccessibleRole(javafx.scene.AccessibleRole.MENU_BAR);
            }
            if (menuBar.getAccessibleText() == null) {
                menuBar.setAccessibleText("Main menu");
            }
        } else if (node instanceof TabPane) {
            TabPane tabPane = (TabPane) node;
            if (tabPane.getAccessibleRole() == null) {
                tabPane.setAccessibleRole(javafx.scene.AccessibleRole.TAB_PANE);
            }
            if (tabPane.getAccessibleText() == null) {
                tabPane.setAccessibleText("Tab panel with " + tabPane.getTabs().size() + " tabs");
            }
        } else if (node instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) node;
            if (scrollPane.getAccessibleRole() == null) {
                scrollPane.setAccessibleRole(javafx.scene.AccessibleRole.SCROLL_PANE);
            }
        } else if (node instanceof Separator) {
            Separator separator = (Separator) node;
            if (separator.getAccessibleRole() == null) {
                // Use a different accessible role since SEPARATOR isn't available
                separator.setAccessibleRole(javafx.scene.AccessibleRole.NODE);
            }
            if (separator.getAccessibleText() == null) {
                separator.setAccessibleText("Separator");
            }
        } else if (node instanceof Pane) {
            // Do nothing specific for generic panes
        }
        
        // Process children
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyAriaAttributes(child);
            }
        }
    }
    
    /**
     * Add live region announcements for a node.
     * 
     * @param node The node to add live region announcements to
     * @param message The message to announce
     */
    public void announceForScreenReader(Node node, String message) {
        if (!enabled || node == null || message == null || message.isEmpty()) {
            return;
        }
        
        // In a real implementation, this would use the JavaFX Accessibility API
        // to make an announcement for screen readers
        node.setAccessibleText(message);
        
        LOGGER.log(Level.INFO, "Screen reader announcement: {0}", message);
    }
}