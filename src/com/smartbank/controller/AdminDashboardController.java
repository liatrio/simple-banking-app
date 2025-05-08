package com.smartbank.controller;

import com.smartbank.model.User;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the admin dashboard.
 */
public class AdminDashboardController {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());
    
    // System Overview Tab
    @FXML private Label lblSystemStatus;
    @FXML private Label lblLastUpdate;
    @FXML private Label lblActiveUserCount;
    @FXML private ProgressBar progressDbStorage;
    @FXML private ProgressBar progressMemory;
    @FXML private PieChart chartUserRoles;
    @FXML private TableView<ActivityLog> tblActivity;
    @FXML private TableColumn<ActivityLog, String> colTimestamp;
    @FXML private TableColumn<ActivityLog, String> colUser;
    @FXML private TableColumn<ActivityLog, String> colAction;
    @FXML private TableColumn<ActivityLog, String> colDetails;
    
    // User Management Tab
    @FXML private Label lblTotalUsers;
    @FXML private Label lblAdminUsers;
    @FXML private Label lblCustomerUsers;
    @FXML private Label lblNewUsers;
    @FXML private TableView<UserActivity> tblUserActivity;
    @FXML private TableColumn<UserActivity, String> colUserActivityUsername;
    @FXML private TableColumn<UserActivity, String> colUserActivityAction;
    @FXML private TableColumn<UserActivity, String> colUserActivityTimestamp;
    @FXML private TableColumn<UserActivity, String> colUserActivityIp;
    @FXML private TableColumn<UserActivity, String> colUserActivityStatus;
    
    // System Settings Tab
    @FXML private TextField txtSessionTimeout;
    @FXML private TextField txtMaxLoginAttempts;
    @FXML private TextField txtLockoutDuration;
    @FXML private TextField txtPasswordExpiry;
    @FXML private TextField txtSmtpServer;
    @FXML private TextField txtSmtpPort;
    @FXML private TextField txtSmtpUsername;
    @FXML private PasswordField txtSmtpPassword;
    @FXML private TextField txtSenderEmail;
    @FXML private TextField txtBackupDirectory;
    @FXML private ComboBox<String> cmbBackupFrequency;
    @FXML private TextField txtRetentionPeriod;
    
    private UserService userService;
    private ObservableList<ActivityLog> activityLogs;
    private ObservableList<UserActivity> userActivities;
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        userService = ServiceFactory.getUserService();
        
        // Initialize activity logs
        activityLogs = FXCollections.observableArrayList();
        userActivities = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Load dashboard data
        loadDashboardData();
        
        // Setup backup frequency combobox
        cmbBackupFrequency.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly", "Manual Only"));
        cmbBackupFrequency.setValue("Daily");
        
        // Load settings
        loadSystemSettings();
    }
    
    /**
     * Setup table columns.
     */
    private void setupTableColumns() {
        // System Activity Table
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        
        // User Activity Table
        colUserActivityUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserActivityAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colUserActivityTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colUserActivityIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colUserActivityStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    /**
     * Load dashboard data.
     */
    private void loadDashboardData() {
        try {
            // Update system status
            lblSystemStatus.setText("All systems operational");
            lblLastUpdate.setText("Last updated: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Simulate active users count
            lblActiveUserCount.setText("12");
            
            // Load resource usage
            progressDbStorage.setProgress(0.35);
            progressMemory.setProgress(0.62);
            
            // Load user statistics
            loadUserStatistics();
            
            // Load activity logs (simulated data for demonstration)
            loadActivityLogs();
            
            // Load user activity (simulated data for demonstration)
            loadUserActivity();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load user statistics.
     */
    private void loadUserStatistics() {
        try {
            // Get all users
            List<User> users = userService.getAllUsers();
            
            // Count users by role
            Map<String, Long> usersByRole = users.stream()
                    .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
            
            // Update user count labels
            lblTotalUsers.setText(String.valueOf(users.size()));
            lblAdminUsers.setText(String.valueOf(usersByRole.getOrDefault("admin", 0L)));
            lblCustomerUsers.setText(String.valueOf(usersByRole.getOrDefault("customer", 0L)));
            lblNewUsers.setText("3"); // Simulated for demonstration
            
            // Update pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            usersByRole.forEach((role, count) -> {
                pieChartData.add(new PieChart.Data(role + " (" + count + ")", count));
            });
            
            chartUserRoles.setData(pieChartData);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load activity logs using actual users from the database.
     */
    private void loadActivityLogs() {
        activityLogs.clear();
        
        try {
            // Get actual users from database for sample logs
            List<User> users = userService.getAllUsers();
            String adminUsername = "admin"; // Default admin user
            
            // Find a regular user to use in logs
            String regularUsername = adminUsername; // Fallback to admin if no others found
            for (User user : users) {
                if (!user.getRole().equalsIgnoreCase("admin")) {
                    regularUsername = user.getUsername();
                    break;
                }
            }
            
            // Add sample activity logs with actual usernames
            activityLogs.add(new ActivityLog("2025-05-06 14:32:15", adminUsername, "System Startup", "Application server started"));
            activityLogs.add(new ActivityLog("2025-05-06 14:35:22", regularUsername, "Login", "Successful login"));
            activityLogs.add(new ActivityLog("2025-05-06 14:40:05", regularUsername, "Account Create", "Created new savings account"));
            activityLogs.add(new ActivityLog("2025-05-06 14:45:18", adminUsername, "User Create", "Created new user"));
            activityLogs.add(new ActivityLog("2025-05-06 14:52:30", regularUsername, "Transaction", "Funds transfer of $1,000.00"));
            
            tblActivity.setItems(activityLogs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading activity logs: " + e.getMessage(), e);
            
            // Fallback to admin-only logs if user service fails
            activityLogs.add(new ActivityLog("2025-05-06 14:32:15", "admin", "System Startup", "Application server started"));
            activityLogs.add(new ActivityLog("2025-05-06 14:45:18", "admin", "User Create", "Created new user"));
            tblActivity.setItems(activityLogs);
        }
    }
    
    /**
     * Load user activity using actual users from the database.
     */
    private void loadUserActivity() {
        userActivities.clear();
        
        try {
            // Get actual users from database for sample logs
            List<User> users = userService.getAllUsers();
            
            // Default to admin if no users found
            String adminUsername = "admin";
            List<String> regularUsernames = new ArrayList<>();
            
            // Find regular users to use in logs
            for (User user : users) {
                if (user.getUsername().equals("admin")) {
                    adminUsername = user.getUsername(); // In case admin username is different
                } else if (!user.getRole().equalsIgnoreCase("admin")) {
                    regularUsernames.add(user.getUsername());
                }
            }
            
            // Use admin if no regular users found
            if (regularUsernames.isEmpty()) {
                regularUsernames.add(adminUsername);
            }
            
            // Add sample user activities with real usernames
            userActivities.add(new UserActivity(adminUsername, "Login", "2025-05-06 14:33:15", "192.168.1.20", "Success"));
            
            // Use available regular users or cycle through the first one
            int userCount = Math.min(regularUsernames.size(), 3);
            for (int i = 0; i < userCount; i++) {
                String username = regularUsernames.get(i);
                String timestamp = String.format("2025-05-06 14:%d:22", 35 + i);
                String ipAddress = String.format("192.168.1.%d", 25 + i);
                userActivities.add(new UserActivity(username, "Login", timestamp, ipAddress, "Success"));
            }
            
            // Add a failed login attempt for a non-existent user
            userActivities.add(new UserActivity("unknown", "Login", "2025-05-06 14:50:15", "192.168.1.100", "Failed"));
            
            // Add a logout entry for the first regular user if available
            if (!regularUsernames.isEmpty()) {
                userActivities.add(new UserActivity(regularUsernames.get(0), "Logout", "2025-05-06 15:10:45", "192.168.1.25", "Success"));
            }
            
            tblUserActivity.setItems(userActivities);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user activity: " + e.getMessage(), e);
            
            // Fallback to admin-only activity if user service fails
            userActivities.add(new UserActivity("admin", "Login", "2025-05-06 14:33:15", "192.168.1.20", "Success"));
            userActivities.add(new UserActivity("unknown", "Login", "2025-05-06 14:50:15", "192.168.1.100", "Failed"));
            userActivities.add(new UserActivity("admin", "Logout", "2025-05-06 15:10:45", "192.168.1.20", "Success"));
            
            tblUserActivity.setItems(userActivities);
        }
    }
    
    /**
     * Load system settings.
     */
    private void loadSystemSettings() {
        // Load default settings
        txtSessionTimeout.setText("30");
        txtMaxLoginAttempts.setText("5");
        txtLockoutDuration.setText("15");
        txtPasswordExpiry.setText("90");
        
        txtSmtpServer.setText("smtp.smartbank.com");
        txtSmtpPort.setText("587");
        txtSmtpUsername.setText("notifications@smartbank.com");
        txtSmtpPassword.setText("********");
        txtSenderEmail.setText("noreply@smartbank.com");
        
        txtBackupDirectory.setText("/var/backups/smartbank");
        txtRetentionPeriod.setText("30");
    }
    
    /**
     * Handle view active sessions button click.
     */
    @FXML
    private void handleViewActiveSessions(ActionEvent event) {
        try {
            VBox content = new VBox(10);
            content.setPrefWidth(600);
            content.setPrefHeight(400);
            
            // Create a table for active sessions
            TableView<ActiveSession> tblSessions = new TableView<>();
            tblSessions.setPrefHeight(350);
            
            TableColumn<ActiveSession, String> colSessionId = new TableColumn<>("Session ID");
            colSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
            colSessionId.setPrefWidth(200);
            
            TableColumn<ActiveSession, String> colSessionUser = new TableColumn<>("Username");
            colSessionUser.setCellValueFactory(new PropertyValueFactory<>("username"));
            colSessionUser.setPrefWidth(100);
            
            TableColumn<ActiveSession, String> colStartTime = new TableColumn<>("Start Time");
            colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            colStartTime.setPrefWidth(150);
            
            TableColumn<ActiveSession, String> colExpiry = new TableColumn<>("Expiry");
            colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryTime"));
            colExpiry.setPrefWidth(150);
            
            tblSessions.getColumns().addAll(colSessionId, colSessionUser, colStartTime, colExpiry);
            
            // Add sample data with actual users
            ObservableList<ActiveSession> sessions = FXCollections.observableArrayList();
            
            try {
                // Get actual users for sample sessions
                List<User> users = userService.getAllUsers();
                
                // Default to admin if no users found
                String adminUsername = "admin";
                List<String> regularUsernames = new ArrayList<>();
                
                // Find regular users to use in logs
                for (User user : users) {
                    if (user.getUsername().equals("admin")) {
                        adminUsername = user.getUsername(); // In case admin username is different
                    } else if (!user.getRole().equalsIgnoreCase("admin")) {
                        regularUsernames.add(user.getUsername());
                    }
                }
                
                // Add admin session
                sessions.add(new ActiveSession("session-1234", adminUsername, 
                    "2025-05-06 14:33:15", "2025-05-06 15:03:15"));
                
                // Add sessions for regular users
                int userCount = Math.min(regularUsernames.size(), 2);
                for (int i = 0; i < userCount; i++) {
                    String username = regularUsernames.get(i);
                    String startTime = String.format("2025-05-06 14:%d:22", 35 + i);
                    String endTime = String.format("2025-05-06 15:%d:22", 5 + i);
                    sessions.add(new ActiveSession("session-" + (5678 + i), username, startTime, endTime));
                }
                
                // If we don't have enough users, fill with admin
                if (userCount == 0) {
                    sessions.add(new ActiveSession("session-5678", adminUsername, 
                        "2025-05-06 14:40:10", "2025-05-06 15:10:10"));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading users for sessions: " + e.getMessage(), e);
                // Default fallback with just admin
                sessions.add(new ActiveSession("session-1234", "admin", "2025-05-06 14:35:22", "2025-05-06 15:05:22"));
            }
            
            tblSessions.setItems(sessions);
            
            // Add button for terminating sessions
            Button btnTerminate = new Button("Terminate Selected Sessions");
            btnTerminate.setOnAction(e -> {
                ActiveSession selectedSession = tblSessions.getSelectionModel().getSelectedItem();
                if (selectedSession != null) {
                    sessions.remove(selectedSession);
                    
                    // Update active user count
                    int currentActiveCount = Integer.parseInt(lblActiveUserCount.getText());
                    lblActiveUserCount.setText(String.valueOf(currentActiveCount - 1));
                }
            });
            
            content.getChildren().addAll(tblSessions, btnTerminate);
            
            // Create a new stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Active User Sessions");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(content);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing active sessions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle create user button click.
     */
    @FXML
    private void handleCreateUser(ActionEvent event) {
        try {
            // Load the user management view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/UserManagementView.fxml"));
            VBox userManagementView = loader.load();
            
            // Show the user management view in the main content area
            VBox root = (VBox) lblSystemStatus.getScene().getRoot();
            root.getChildren().setAll(userManagementView);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user management view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle manage users button click.
     */
    @FXML
    private void handleManageUsers(ActionEvent event) {
        // Use the same logic as handleCreateUser since both navigate to the user management view
        handleCreateUser(event);
    }
    
    /**
     * Handle browse backup directory button click.
     */
    @FXML
    private void handleBrowseBackupDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Directory");
        
        // Set initial directory if one is already set
        String currentDir = txtBackupDirectory.getText();
        if (currentDir != null && !currentDir.isEmpty()) {
            File initialDir = new File(currentDir);
            if (initialDir.exists() && initialDir.isDirectory()) {
                directoryChooser.setInitialDirectory(initialDir);
            }
        }
        
        File selectedDirectory = directoryChooser.showDialog(null);
        
        if (selectedDirectory != null) {
            txtBackupDirectory.setText(selectedDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Handle reset defaults button click.
     */
    @FXML
    private void handleResetDefaults(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset to Defaults");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to reset all settings to default values?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadSystemSettings(); // Reload default settings
            }
        });
    }
    
    /**
     * Handle save settings button click.
     */
    @FXML
    private void handleSaveSettings(ActionEvent event) {
        // Here we would validate and save the settings to the database
        // For demonstration, we'll just show a success message
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("System settings have been saved successfully.");
        alert.showAndWait();
    }
    
    /**
     * Class representing an activity log entry.
     */
    public static class ActivityLog {
        private final String timestamp;
        private final String username;
        private final String action;
        private final String details;
        
        public ActivityLog(String timestamp, String username, String action, String details) {
            this.timestamp = timestamp;
            this.username = username;
            this.action = action;
            this.details = details;
        }
        
        public String getTimestamp() { return timestamp; }
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public String getDetails() { return details; }
    }
    
    /**
     * Class representing a user activity entry.
     */
    public static class UserActivity {
        private final String username;
        private final String action;
        private final String timestamp;
        private final String ipAddress;
        private final String status;
        
        public UserActivity(String username, String action, String timestamp, String ipAddress, String status) {
            this.username = username;
            this.action = action;
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
            this.status = status;
        }
        
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public String getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
        public String getStatus() { return status; }
    }
    
    /**
     * Class representing an active session.
     */
    public static class ActiveSession {
        private final String sessionId;
        private final String username;
        private final String startTime;
        private final String expiryTime;
        
        public ActiveSession(String sessionId, String username, String startTime, String expiryTime) {
            this.sessionId = sessionId;
            this.username = username;
            this.startTime = startTime;
            this.expiryTime = expiryTime;
        }
        
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public String getStartTime() { return startTime; }
        public String getExpiryTime() { return expiryTime; }
    }
}