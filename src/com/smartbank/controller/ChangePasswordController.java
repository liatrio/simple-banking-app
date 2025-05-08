package com.smartbank.controller;

import com.smartbank.model.User;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.UserService;
import com.smartbank.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the change password view.
 */
public class ChangePasswordController {
    private static final Logger LOGGER = Logger.getLogger(ChangePasswordController.class.getName());
    
    @FXML private Label lblUsername;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnSave;
    @FXML private Label lblStatus;
    
    private User user;
    private UserService userService;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        userService = ServiceFactory.getUserService();
        
        // Set up validation listeners
        txtNewPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordMatch();
        });
        
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordMatch();
        });
    }
    
    /**
     * Set the user for this controller.
     * @param user The user
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null && lblUsername != null) {
            lblUsername.setText(user.getUsername());
        }
    }
    
    /**
     * Validate that the new password and confirm password fields match.
     */
    private void validatePasswordMatch() {
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        
        if (!confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            txtConfirmPassword.setStyle("-fx-border-color: red;");
            showError("Passwords do not match");
        } else {
            txtConfirmPassword.setStyle("");
            lblStatus.setText("");
        }
    }
    
    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (user == null) {
            showError("No user loaded");
            return;
        }
        
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        
        // Validate inputs
        if (currentPassword.isEmpty()) {
            showError("Current password is required");
            txtCurrentPassword.requestFocus();
            return;
        }
        
        if (newPassword.isEmpty()) {
            showError("New password is required");
            txtNewPassword.requestFocus();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            txtConfirmPassword.requestFocus();
            return;
        }
        
        // Validate password strength
        if (!ValidationUtils.isPasswordStrong(newPassword)) {
            showError(ValidationUtils.getPasswordRequirements());
            txtNewPassword.requestFocus();
            return;
        }
        
        try {
            // Change the password
            userService.changePassword(user.getUserId(), currentPassword, newPassword);
            
            showSuccess("Password changed successfully");
            clearFields();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password: " + e.getMessage(), e);
            showError("Error changing password: " + e.getMessage());
        }
    }
    
    // Password strength validation is now handled by ValidationUtils
    
    /**
     * Clear all password fields.
     */
    private void clearFields() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
    }
    
    /**
     * Handle cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        clearFields();
        lblStatus.setText("");
    }
    
    /**
     * Show error message.
     */
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setTextFill(Color.RED);
    }
    
    /**
     * Show success message.
     */
    private void showSuccess(String message) {
        lblStatus.setText(message);
        lblStatus.setTextFill(Color.GREEN);
    }
}