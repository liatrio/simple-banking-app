package com.smartbank.controller;

import com.smartbank.model.User;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.UserService;
import com.smartbank.util.ValidationUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the user management view.
 */
public class UserManagementController {
    private static final Logger LOGGER = Logger.getLogger(UserManagementController.class.getName());
    
    // FXML Controls
    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, String> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActions;
    
    // These methods are called from the FXML
    @FXML
    private void handleSearch(ActionEvent event) {
        // The listener on txtSearch will handle the filtering
    }
    
    @FXML
    private void handleReset(ActionEvent event) {
        txtSearch.clear();
    }
    
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbRole;
    
    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private TextField txtSearch;
    
    @FXML private Label lblStatus;
    
    // Services
    private final UserService userService;
    
    // Data
    private ObservableList<User> users;
    private FilteredList<User> filteredUsers;
    private User selectedUser;
    
    /**
     * Constructor.
     */
    public UserManagementController() {
        this.userService = ServiceFactory.getUserService();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Setup the users table
        setupUsersTable();
        
        // Setup roles combo box
        cmbRole.setItems(FXCollections.observableArrayList("admin", "customer"));
        cmbRole.setValue("customer");
        
        // Load all users
        loadUsers();
        
        // Setup listeners
        setupListeners();
        
        // Initial state
        clearForm();
    }
    
    /**
     * Setup the users table.
     */
    private void setupUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFirstName.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFirstName() != null ? cellData.getValue().getFirstName() : ""));
        colLastName.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getLastName() != null ? cellData.getValue().getLastName() : ""));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : ""));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Setup actions column with edit and delete buttons
        colActions.setCellFactory(createActionCellFactory());
        
        // Row selection listener
        tblUsers.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedUser = newSelection;
                        populateForm(selectedUser);
                    }
                });
        
        // Filter
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredUsers != null) {
                filteredUsers.setPredicate(user -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newVal.toLowerCase();
                    if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return user.getRole().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });
    }
    
    /**
     * Create a cell factory for the actions column.
     */
    private Callback<TableColumn<User, Boolean>, TableCell<User, Boolean>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<User, Boolean> call(TableColumn<User, Boolean> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    
                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                        
                        editButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            selectedUser = user;
                            populateForm(user);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUserFromTable(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create an HBox to hold the buttons
                            javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(5);
                            buttonsBox.getChildren().addAll(editButton, deleteButton);
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Load all users into the table.
     */
    private void loadUsers() {
        try {
            List<User> userList = userService.getAllUsers();
            users = FXCollections.observableArrayList(userList);
            filteredUsers = new FilteredList<>(users, p -> true);
            tblUsers.setItems(filteredUsers);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading users: " + e.getMessage(), e);
            showError("Error loading users: " + e.getMessage());
        }
    }
    
    /**
     * Setup listeners for form controls.
     */
    private void setupListeners() {
        // Password validation listener
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!txtPassword.getText().equals(newVal)) {
                txtConfirmPassword.setStyle("-fx-border-color: red;");
            } else {
                txtConfirmPassword.setStyle("");
            }
        });
        
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(txtConfirmPassword.getText()) && !txtConfirmPassword.getText().isEmpty()) {
                txtConfirmPassword.setStyle("-fx-border-color: red;");
            } else if (newVal.equals(txtConfirmPassword.getText())) {
                txtConfirmPassword.setStyle("");
            }
        });
        
        // Update button enable/disable state
        btnUpdate.disableProperty().bind(
                tblUsers.getSelectionModel().selectedItemProperty().isNull());
    }
    
    /**
     * Populate form with user data.
     */
    private void populateForm(User user) {
        if (user != null) {
            txtUsername.setText(user.getUsername());
            txtPassword.clear();
            txtConfirmPassword.clear();
            txtFirstName.setText(user.getFirstName() != null ? user.getFirstName() : "");
            txtLastName.setText(user.getLastName() != null ? user.getLastName() : "");
            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            cmbRole.setValue(user.getRole());
            
            // Disable username field during edit
            txtUsername.setDisable(true);
            
            // Show update button, hide create button
            btnUpdate.setVisible(true);
            btnCreate.setVisible(false);
        }
    }
    
    /**
     * Clear the form.
     */
    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        cmbRole.setValue("customer");
        
        // Enable username field for new user
        txtUsername.setDisable(false);
        
        // Show create button, hide update button
        btnCreate.setVisible(true);
        btnUpdate.setVisible(false);
        
        // Clear selection
        selectedUser = null;
        tblUsers.getSelectionModel().clearSelection();
    }
    
    /**
     * Validate the form.
     */
    private boolean validateForm(boolean isPasswordRequired) {
        // Validate username
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            showError("Username is required");
            txtUsername.requestFocus();
            return false;
        }
        
        if (!ValidationUtils.isValidUsername(username)) {
            showError("Username must be 3-20 characters and can only contain letters, numbers, and underscores");
            txtUsername.requestFocus();
            return false;
        }
        
        // Validate password
        if (isPasswordRequired && txtPassword.getText().isEmpty()) {
            showError("Password is required");
            txtPassword.requestFocus();
            return false;
        }
        
        if (isPasswordRequired && !txtPassword.getText().equals(txtConfirmPassword.getText())) {
            showError("Passwords do not match");
            txtConfirmPassword.requestFocus();
            return false;
        }
        
        if (isPasswordRequired && !ValidationUtils.isPasswordStrong(txtPassword.getText())) {
            showError(ValidationUtils.getPasswordRequirements());
            txtPassword.requestFocus();
            return false;
        }
        
        // Validate role
        if (cmbRole.getValue() == null || cmbRole.getValue().trim().isEmpty()) {
            showError("Role is required");
            cmbRole.requestFocus();
            return false;
        }
        
        // Validate first name if provided
        String firstName = txtFirstName.getText().trim();
        if (!firstName.isEmpty() && !ValidationUtils.isValidName(firstName)) {
            showError("First name can only contain letters, spaces, hyphens, and apostrophes");
            txtFirstName.requestFocus();
            return false;
        }
        
        // Validate last name if provided
        String lastName = txtLastName.getText().trim();
        if (!lastName.isEmpty() && !ValidationUtils.isValidName(lastName)) {
            showError("Last name can only contain letters, spaces, hyphens, and apostrophes");
            txtLastName.requestFocus();
            return false;
        }
        
        // Validate email if provided
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            showError("Email address is not valid");
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Handle create user button click.
     */
    @FXML
    private void handleCreateUser(ActionEvent event) {
        if (!validateForm(true)) {
            return;
        }
        
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cmbRole.getValue();
        
        try {
            User newUser = userService.createUser(username, password, role, firstName, lastName, email);
            users.add(newUser);
            
            showSuccess("User created successfully: " + username);
            clearForm();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + e.getMessage(), e);
            showError("Error creating user: " + e.getMessage());
        }
    }
    
    /**
     * Handle update user button click.
     */
    @FXML
    private void handleUpdateUser(ActionEvent event) {
        if (selectedUser == null) {
            showError("No user selected");
            return;
        }
        
        if (!validateForm(false)) {
            return;
        }
        
        String role = cmbRole.getValue();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        
        try {
            // Update user information
            User updatedUser = userService.updateUser(
                    selectedUser.getUserId(), 
                    role,
                    firstName,
                    lastName,
                    email);
            
            // Update password if provided
            if (!txtPassword.getText().isEmpty()) {
                // We use a dummy old password here because we're an admin updating
                // In a real system, this would be handled differently
                updatedUser = userService.changePassword(
                        updatedUser.getUserId(), 
                        "dummy-old-password-admin-override", 
                        txtPassword.getText());
            }
            
            // Update the user in the list
            int index = findUserIndex(selectedUser.getUserId());
            if (index >= 0) {
                users.set(index, updatedUser);
            }
            
            showSuccess("User updated successfully: " + updatedUser.getUsername());
            clearForm();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + e.getMessage(), e);
            showError("Error updating user: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete user button click.
     */
    @FXML
    private void handleDeleteUser(ActionEvent event) {
        if (selectedUser == null) {
            showError("No user selected");
            return;
        }
        
        handleDeleteUserFromTable(selectedUser);
    }
    
    /**
     * Delete a user from the table.
     */
    private void handleDeleteUserFromTable(User user) {
        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user: " + user.getUsername() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = userService.deleteUser(user.getUserId());
                
                if (deleted) {
                    users.remove(user);
                    showSuccess("User deleted successfully: " + user.getUsername());
                    clearForm();
                } else {
                    showError("Failed to delete user: " + user.getUsername());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting user: " + e.getMessage(), e);
                showError("Error deleting user: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle clear form button click.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        clearForm();
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUsers();
        clearForm();
    }
    
    /**
     * Find the index of a user in the observable list.
     */
    private int findUserIndex(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                return i;
            }
        }
        return -1;
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