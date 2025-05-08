package com.smartbank.controller;

import com.smartbank.auth.SecurityContext;
import com.smartbank.auth.SessionFilter;
import com.smartbank.auth.UserSession;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base controller class that provides common functionality for all controllers.
 * Includes session validation and common UI operations.
 */
public abstract class BaseController {
    private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    // Common references to security components
    protected final SecurityContext securityContext;
    protected final SessionFilter sessionFilter;
    
    /**
     * Constructor.
     */
    public BaseController() {
        this.securityContext = SecurityContext.getInstance();
        this.sessionFilter = SessionFilter.getInstance();
    }
    
    /**
     * Validate the current session.
     * @return true if the session is valid, false otherwise
     */
    protected boolean validateSession() {
        return sessionFilter.processInteraction();
    }
    
    /**
     * Get the current user session if it exists and is valid.
     * @return The current user session, or null if no valid session exists
     */
    protected UserSession getCurrentSession() {
        UserSession session = securityContext.getCurrentSession();
        if (session != null && session.isValid()) {
            return session;
        }
        return null;
    }
    
    /**
     * Check if a user is authenticated and has the required role.
     * @param requiredRole The required role, or null if any role is allowed
     * @return true if the user is authenticated and has the required role
     */
    protected boolean checkAuthentication(String requiredRole) {
        // First validate the session
        if (!validateSession()) {
            showAuthenticationRequired();
            return false;
        }
        
        // Then check the role if required
        if (requiredRole != null && !securityContext.hasRole(requiredRole)) {
            showAccessDenied();
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a user is authenticated and has the required permission.
     * @param permission The required permission
     * @return true if the user is authenticated and has the required permission
     */
    protected boolean checkPermission(String permission) {
        // First validate the session
        if (!validateSession()) {
            showAuthenticationRequired();
            return false;
        }
        
        // Then check the permission
        if (!securityContext.hasPermission(permission)) {
            showAccessDenied();
            return false;
        }
        
        return true;
    }
    
    /**
     * Show authentication required message and redirect to login.
     */
    protected void showAuthenticationRequired() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Authentication Required");
        alert.setHeaderText(null);
        alert.setContentText("Your session has expired. Please log in again.");
        alert.showAndWait();
        
        // Redirect to login
        redirectToLogin();
    }
    
    /**
     * Show access denied message.
     */
    protected void showAccessDenied() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to access this feature.");
        alert.showAndWait();
    }
    
    /**
     * Redirect to the login view.
     */
    protected void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/smartbank/view/LoginView.fxml"));
            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("SmartBank - Login");
                stage.show();
            } else {
                LOGGER.warning("Could not get stage for redirect to login");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the current stage.
     * @return The current stage
     */
    protected Stage getStage() {
        // This method needs to be implemented by each controller
        // to provide access to the current stage
        return null;
    }
    
    /**
     * Load a view into the specified container.
     * @param fxmlPath The path to the FXML file
     * @param container The container to load the view into
     * @return true if the view was loaded successfully, false otherwise
     */
    protected boolean loadView(String fxmlPath, Node container) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (container instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) container).getChildren().setAll(view);
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading view: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Show an information alert.
     * @param title The alert title
     * @param message The alert message
     */
    protected void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Show an error alert.
     * @param title The alert title
     * @param message The alert message
     */
    protected void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Show a warning alert.
     * @param title The alert title
     * @param message The alert message
     */
    protected void showWarningAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}