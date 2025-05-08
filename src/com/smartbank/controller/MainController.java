package com.smartbank.controller;

import com.smartbank.auth.AuthenticationService;
import com.smartbank.auth.AuthenticationServiceImpl;
import com.smartbank.auth.SecurityContext;
import com.smartbank.auth.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    @FXML private StackPane contentArea;
    @FXML private HBox userInfoBox;
    @FXML private Label lblUserName;
    @FXML private Label lblSessionTime;
    @FXML private MenuItem menuLogout;
    @FXML private MenuItem menuAdmin;
    
    private UserSession userSession;
    private AuthenticationService authService;
    private Timer sessionTimer;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Create authentication service
        authService = new AuthenticationServiceImpl();
        
        // Default user info (visible=false until login)
        if (userInfoBox != null) {
            userInfoBox.setVisible(false);
        }
        
        // Default state for admin menu
        if (menuAdmin != null) {
            menuAdmin.setVisible(false);
        }
        
        // Default content
        showWelcomeView();
    }
    
    /**
     * Set the user session after successful login.
     * @param session The user session
     */
    public void setUserSession(UserSession session) {
        this.userSession = session;
        
        // Update security context with the current session
        SecurityContext.getInstance().setCurrentSession(session);
        
        // Update UI for authenticated user
        if (userSession != null) {
            // Display user info
            if (lblUserName != null) {
                lblUserName.setText(userSession.getUser().getUsername());
            }
            
            // Show/hide admin menu based on role
            if (menuAdmin != null) {
                menuAdmin.setVisible(userSession.hasRole("admin"));
            }
            
            if (userInfoBox != null) {
                userInfoBox.setVisible(true);
            }
            
            // Start session timer
            startSessionTimer();
            
            // Show account list as default view
            showAccountList(null);
        }
    }
    
    /**
     * Check if user is authenticated and has required role.
     * @param requiredRole The required role, or null if any role is allowed
     * @return true if the user is authenticated and has the required role, false otherwise
     */
    private boolean checkAuthentication(String requiredRole) {
        SecurityContext securityContext = SecurityContext.getInstance();
        
        if (!securityContext.isAuthenticated()) {
            showAuthenticationRequired();
            return false;
        }
        
        if (requiredRole != null && !securityContext.hasRole(requiredRole)) {
            showAccessDenied();
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if user is authenticated and has required permission.
     * @param requiredPermission The required permission
     * @return true if the user is authenticated and has the required permission, false otherwise
     */
    private boolean checkPermission(String requiredPermission) {
        SecurityContext securityContext = SecurityContext.getInstance();
        
        if (!securityContext.isAuthenticated()) {
            showAuthenticationRequired();
            return false;
        }
        
        if (!securityContext.hasPermission(requiredPermission)) {
            showAccessDenied();
            return false;
        }
        
        return true;
    }
    
    /**
     * Start a timer to update session time remaining.
     */
    private void startSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
        
        sessionTimer = new Timer(true);
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (userSession != null && userSession.isValid()) {
                    long minutesRemaining = userSession.getMinutesRemaining();
                    
                    Platform.runLater(() -> {
                        if (lblSessionTime != null) {
                            lblSessionTime.setText("Session: " + minutesRemaining + " min");
                        }
                    });
                } else {
                    // Session expired
                    Platform.runLater(() -> logout(null));
                }
            }
        }, 0, 60 * 1000); // Update every minute
    }
    
    /**
     * Show welcome view.
     */
    private void showWelcomeView() {
        // For now, just show a message
        try {
            Node welcomeNode = new javafx.scene.control.Label("Welcome to SmartBank! Please log in to continue.");
            welcomeNode.setStyle("-fx-font-size: 18px; -fx-padding: 20px;");
            contentArea.getChildren().setAll(welcomeNode);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing welcome view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Show authentication required message.
     */
    private void showAuthenticationRequired() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Authentication Required");
        alert.setHeaderText(null);
        alert.setContentText("You need to log in to access this feature.");
        alert.showAndWait();
        
        // Redirect to login page
        logout(null);
    }
    
    /**
     * Show access denied message.
     */
    private void showAccessDenied() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to access this feature.");
        alert.showAndWait();
    }
    
    @FXML
    private void showAccountList(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/AccountListView.fxml");
        }
    }

    @FXML
    private void showAccountForm(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/AccountFormView.fxml");
        }
    }

    @FXML
    private void showTransactionForm(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/TransactionFormView.fxml");
        }
    }

    @FXML
    private void showTransactionHistory(ActionEvent event) {
        if (checkAuthentication(null)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/TransactionHistoryView.fxml"));
                Node view = loader.load();
                
                // Get the controller and load transactions for the current user
                TransactionHistoryController controller = loader.getController();
                
                // Have the controller load all user accounts
                SecurityContext securityContext = SecurityContext.getInstance();
                if (securityContext.isAuthenticated()) {
                    String username = securityContext.getCurrentSession().getUser().getUsername();
                    // Let the controller handle account loading and selection
                    controller.loadUserAccounts(username);
                }
                
                // Set grow priorities for the node to ensure it fills available space
                if (view instanceof Pane) {
                    Pane pane = (Pane) view;
                    VBox.setVgrow(pane, Priority.ALWAYS);
                    HBox.setHgrow(pane, Priority.ALWAYS);
                    
                    // Set fill width/height for VBox and HBox
                    if (pane instanceof VBox) {
                        ((VBox) pane).setFillWidth(true);
                    } else if (pane instanceof HBox) {
                        ((HBox) pane).setFillHeight(true);
                    }
                }
                
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading transaction history view: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void showTransferForm(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/TransferView.fxml");
        }
    }
    
    @FXML
    private void showCategoryManagement(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/CategoryManagementView.fxml");
        }
    }
    
    @FXML
    private void showBudgetManagement(ActionEvent event) {
        if (checkAuthentication(null)) {
            loadView("/com/smartbank/view/BudgetView.fxml");
        }
    }
    
    @FXML
    private void showUserManagement(ActionEvent event) {
        // Admin users always have access to user management
        if (userSession != null && userSession.hasRole("admin")) {
            loadView("/com/smartbank/view/UserManagementView.fxml");
        } else if (checkPermission("USER_LIST")) {
            loadView("/com/smartbank/view/UserManagementView.fxml");
        }
    }
    
    @FXML
    private void showUserProfile(ActionEvent event) {
        if (checkPermission("USER_READ_SELF")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/UserProfileView.fxml"));
                Node view = loader.load();
                
                // Pass the current user to the profile controller
                UserProfileController controller = loader.getController();
                controller.setUser(userSession.getUser());
                
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading user profile view: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void showChangePassword(ActionEvent event) {
        if (checkPermission("USER_UPDATE_SELF")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/ChangePasswordView.fxml"));
                Node view = loader.load();
                
                // Pass the current user to the change password controller
                ChangePasswordController controller = loader.getController();
                controller.setUser(userSession.getUser());
                
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading change password view: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void showAdminPanel(ActionEvent event) {
        // Direct admin role check for admin dashboard
        if (userSession != null && userSession.hasRole("admin")) {
            loadView("/com/smartbank/view/AdminDashboardView.fxml");
        } else if (checkPermission("SYSTEM_ADMIN")) {
            loadView("/com/smartbank/view/AdminDashboardView.fxml");
        }
    }
    
    @FXML
    private void logout(ActionEvent event) {
        // Invalidate session
        if (userSession != null) {
            authService.logout(userSession);
            userSession = null;
        }
        
        // Clear security context
        SecurityContext.getInstance().invalidateSession();
        
        // Stop session timer
        if (sessionTimer != null) {
            sessionTimer.cancel();
            sessionTimer = null;
        }
        
        // Clear remember-me token
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.remove("rememberMeToken");
        
        // Navigate to login view
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/smartbank/view/LoginView.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartBank - Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login view: " + e.getMessage(), e);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            
            // Set grow priorities for the node to ensure it fills available space
            if (node instanceof Pane) {
                Pane pane = (Pane) node;
                VBox.setVgrow(pane, Priority.ALWAYS);
                HBox.setHgrow(pane, Priority.ALWAYS);
                
                // Set fill width/height for VBox and HBox
                if (pane instanceof VBox) {
                    ((VBox) pane).setFillWidth(true);
                } else if (pane instanceof HBox) {
                    ((HBox) pane).setFillHeight(true);
                }
            }
            
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading view: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
