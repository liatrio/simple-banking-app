package com.smartbank.controller;

import com.smartbank.auth.AuthenticationService;
import com.smartbank.auth.UserSession;
import com.smartbank.model.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Controller for the login view.
 */
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    
    // FXML Components
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRememberMe;
    @FXML private Text txtErrorMessage;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private ProgressIndicator progressIndicator;
    
    // Authentication service
    private AuthenticationService authService;
    
    // Preferences for remember-me functionality
    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_REMEMBER_ME_TOKEN = "rememberMeToken";
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Create authentication service
        authService = new com.smartbank.auth.AuthenticationServiceImpl();
        
        // Set up event handlers
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        
        // Try to login with remember-me token
        String token = prefs.get(PREF_REMEMBER_ME_TOKEN, null);
        if (token != null && !token.isEmpty()) {
            // Show loading indicator
            setLoading(true);
            
            // Try to login with token in a background thread
            new Thread(() -> {
                try {
                    Optional<UserSession> session = authService.loginWithToken(token);
                    
                    Platform.runLater(() -> {
                        if (session.isPresent()) {
                            // Login successful, navigate to main view
                            UserSession userSession = session.get();
                            LOGGER.info("Auto-login successful for user: " + userSession.getUser().getUsername());
                            navigateToMainView(userSession);
                        } else {
                            // Token invalid or expired, clear it
                            prefs.remove(PREF_REMEMBER_ME_TOKEN);
                            setLoading(false);
                        }
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Auto-login failed: " + e.getMessage(), e);
                    
                    Platform.runLater(() -> {
                        prefs.remove(PREF_REMEMBER_ME_TOKEN);
                        setLoading(false);
                    });
                }
            }).start();
        }
    }
    
    /**
     * Handle login button click.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            showError("Username is required");
            txtUsername.requestFocus();
            return;
        }
        
        if (password == null || password.isEmpty()) {
            showError("Password is required");
            txtPassword.requestFocus();
            return;
        }
        
        // Disable controls and show loading indicator
        setLoading(true);
        
        // Perform login in a background thread
        new Thread(() -> {
            try {
                // Check if user is rate limited
                if (!authService.isLoginAllowed(username)) {
                    Platform.runLater(() -> {
                        showError("Too many failed login attempts. Please try again later.");
                        setLoading(false);
                    });
                    return;
                }
                
                // Attempt login
                Optional<UserSession> sessionOpt = authService.login(username, password);
                
                Platform.runLater(() -> {
                    if (sessionOpt.isPresent()) {
                        // Login successful
                        UserSession session = sessionOpt.get();
                        LOGGER.info("Login successful for user: " + username);
                        
                        // Handle remember-me
                        if (chkRememberMe.isSelected()) {
                            String token = authService.generateRememberMeToken(session.getUser());
                            prefs.put(PREF_REMEMBER_ME_TOKEN, token);
                            LOGGER.info("Remember-me token created for user: " + username);
                        } else {
                            // Clear any existing token
                            prefs.remove(PREF_REMEMBER_ME_TOKEN);
                        }
                        
                        // Navigate to main view
                        navigateToMainView(session);
                    } else {
                        // Login failed
                        showError("Invalid username or password");
                        setLoading(false);
                        txtPassword.clear();
                        txtPassword.requestFocus();
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Login error: " + e.getMessage(), e);
                
                Platform.runLater(() -> {
                    showError("An error occurred during login. Please try again.");
                    setLoading(false);
                });
            }
        }).start();
    }
    
    /**
     * Handle register button click.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Show registration dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        dialog.setHeaderText("Create a new account");
        
        // Set the button types
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        
        // Create form fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("customer", "admin");
        roleComboBox.setValue("customer");
        roleComboBox.setPromptText("Role");
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field by default
        Platform.runLater(usernameField::requestFocus);
        
        // Enable/disable register button based on validation
        Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.setDisable(true);
        
        // Add validation listeners
        Runnable validateFields = () -> {
            boolean valid = true;
            
            String username = usernameField.getText();
            if (username == null || username.trim().isEmpty()) {
                valid = false;
            }
            
            String password = passwordField.getText();
            if (password == null || password.isEmpty()) {
                valid = false;
            }
            
            String confirmPassword = confirmPasswordField.getText();
            if (!password.equals(confirmPassword)) {
                valid = false;
            }
            
            registerButton.setDisable(!valid);
        };
        
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        
        // Convert the result to a User when the register button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String role = roleComboBox.getValue();
                
                try {
                    // Check if username is available
                    if (!authService.isUsernameAvailable(username)) {
                        throw new IllegalArgumentException("Username already exists");
                    }
                    
                    // Validate password
                    if (!authService.validatePassword(password)) {
                        throw new IllegalArgumentException("Password does not meet security requirements");
                    }
                    
                    // Register user
                    return authService.register(username, password, role);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Registration Error");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<User> result = dialog.showAndWait();
        
        result.ifPresent(user -> {
            showMessage("Registration successful. You can now log in.");
            txtUsername.setText(user.getUsername());
            txtPassword.requestFocus();
        });
    }
    
    /**
     * Navigate to the main view after successful login.
     */
    private void navigateToMainView(UserSession session) {
        try {
            // Get the main controller
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/smartbank/view/MainView.fxml"));
            Parent root = loader.load();
            
            // Pass the user session to the main controller
            MainController mainController = loader.getController();
            mainController.setUserSession(session);
            
            // Create the main scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartBank - Welcome " + session.getUser().getUsername());
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading main view: " + e.getMessage(), e);
            showError("Error loading main view");
            setLoading(false);
        }
    }
    
    /**
     * Show an error message.
     */
    private void showError(String message) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setVisible(true);
    }
    
    /**
     * Hide the error message.
     */
    private void hideError() {
        txtErrorMessage.setVisible(false);
    }
    
    /**
     * Show a temporary success message.
     */
    private void showMessage(String message) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setStyle("-fx-fill: green;");
        txtErrorMessage.setVisible(true);
        
        // Hide the message after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            txtErrorMessage.setVisible(false);
            txtErrorMessage.setStyle("-fx-fill: red;");
        });
        pause.play();
    }
    
    /**
     * Set the loading state of the form.
     */
    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        txtUsername.setDisable(loading);
        txtPassword.setDisable(loading);
        chkRememberMe.setDisable(loading);
        btnLogin.setDisable(loading);
        btnRegister.setDisable(loading);
    }
}