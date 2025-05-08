package com.smartbank.controller;

import com.smartbank.model.User;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.UserService;
import com.smartbank.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the user profile view.
 */
public class UserProfileController {
    private static final Logger LOGGER = Logger.getLogger(UserProfileController.class.getName());
    
    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
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
    }
    
    /**
     * Set the user for this profile.
     * @param user The user
     */
    public void setUser(User user) {
        this.user = user;
        populateForm();
    }
    
    /**
     * Populate the form with user data.
     */
    private void populateForm() {
        if (user == null) {
            return;
        }
        
        lblUsername.setText(user.getUsername());
        lblRole.setText(user.getRole());
        txtFirstName.setText(user.getFirstName() != null ? user.getFirstName() : "");
        txtLastName.setText(user.getLastName() != null ? user.getLastName() : "");
        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
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
        
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        
        // Validate name fields
        if (!firstName.isEmpty() && !ValidationUtils.isValidName(firstName)) {
            showError("First name can only contain letters, spaces, hyphens, and apostrophes");
            txtFirstName.requestFocus();
            return;
        }
        
        if (!lastName.isEmpty() && !ValidationUtils.isValidName(lastName)) {
            showError("Last name can only contain letters, spaces, hyphens, and apostrophes");
            txtLastName.requestFocus();
            return;
        }
        
        // Validate email if provided
        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            showError("Invalid email format");
            txtEmail.requestFocus();
            return;
        }
        
        try {
            // Update user profile
            User updatedUser = userService.updateUser(
                    user.getUserId(), 
                    user.getRole(),  // Keep the same role
                    firstName, 
                    lastName, 
                    email);
            
            this.user = updatedUser;
            showSuccess("Profile updated successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating profile: " + e.getMessage(), e);
            showError("Error updating profile: " + e.getMessage());
        }
    }
    
    // Validation is now handled by ValidationUtils
    
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