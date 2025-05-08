package com.smartbank.controller;

import com.smartbank.auth.*;
import com.smartbank.auth.SessionActivityTracker.SessionActivity;
import com.smartbank.auth.SessionActivityTracker.SessionActivityType;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the session monitoring view.
 */
public class SessionMonitoringController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(SessionMonitoringController.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Active Sessions Tab
    @FXML private Label lblActiveSessionCount;
    @FXML private TableView<SessionTableItem> tblActiveSessions;
    @FXML private TableColumn<SessionTableItem, String> colSessionId;
    @FXML private TableColumn<SessionTableItem, String> colUsername;
    @FXML private TableColumn<SessionTableItem, String> colCreationTime;
    @FXML private TableColumn<SessionTableItem, String> colExpirationTime;
    @FXML private TableColumn<SessionTableItem, String> colRemainingTime;
    @FXML private TableColumn<SessionTableItem, String> colIpAddress;
    @FXML private TableColumn<SessionTableItem, Button> colActions;
    
    // Session Activity Tab
    @FXML private ChoiceBox<String> choiceActivityFilter;
    @FXML private TextField txtActivitySearch;
    @FXML private TableView<ActivityTableItem> tblSessionActivity;
    @FXML private TableColumn<ActivityTableItem, String> colActivityTimestamp;
    @FXML private TableColumn<ActivityTableItem, String> colActivityUsername;
    @FXML private TableColumn<ActivityTableItem, String> colActivitySessionId;
    @FXML private TableColumn<ActivityTableItem, String> colActivityType;
    @FXML private TableColumn<ActivityTableItem, String> colActivityDetails;
    
    // Statistics Tab
    @FXML private Label lblSessionsCreated;
    @FXML private Label lblSessionsRenewed;
    @FXML private Label lblSessionsInvalidated;
    @FXML private Label lblSessionsExpired;
    @FXML private Label lblStatsActiveSessions;
    @FXML private Label lblFailedLogins;
    @FXML private PieChart chartSessionTypes;
    
    // Settings Tab
    @FXML private TextField txtDefaultTimeout;
    @FXML private TextField txtExtendTimeout;
    @FXML private TextField txtMaxTimeout;
    @FXML private TextField txtInactivityWarning;
    @FXML private TextField txtRememberMeDays;
    @FXML private CheckBox chkAutoRenew;
    
    // Components
    private final SessionManager sessionManager;
    private final SessionActivityTracker activityTracker;
    private final SessionFilter sessionFilter;
    
    // Data
    private ObservableList<SessionTableItem> activeSessions;
    private ObservableList<ActivityTableItem> sessionActivities;
    
    /**
     * Constructor.
     */
    public SessionMonitoringController() {
        super();
        this.sessionManager = SessionManager.getInstance();
        this.activityTracker = SessionActivityTracker.getInstance();
        this.sessionFilter = SessionFilter.getInstance();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Check permissions
        if (!checkPermission("SYSTEM_ADMIN")) {
            return;
        }
        
        // Initialize data
        activeSessions = FXCollections.observableArrayList();
        sessionActivities = FXCollections.observableArrayList();
        
        // Setup active sessions table
        setupActiveSessionsTable();
        
        // Setup activity table
        setupActivityTable();
        
        // Setup activity filter
        setupActivityFilter();
        
        // Load initial data
        refreshData();
        
        // Load settings
        loadSettings();
    }
    
    /**
     * Setup the active sessions table.
     */
    private void setupActiveSessionsTable() {
        colSessionId.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSessionId()));
        
        colUsername.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getUsername()));
        
        colCreationTime.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCreationTime()));
        
        colExpirationTime.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getExpirationTime()));
        
        colRemainingTime.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRemainingTime()));
        
        colIpAddress.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getIpAddress()));
        
        // Setup actions column
        colActions.setCellValueFactory(param -> {
            Button terminateButton = new Button("Terminate");
            terminateButton.setOnAction(event -> terminateSession(param.getValue().getSessionId()));
            terminateButton.getStyleClass().add("terminate-button");
            return new SimpleObjectProperty<>(terminateButton);
        });
        
        tblActiveSessions.setItems(activeSessions);
    }
    
    /**
     * Setup the activity table.
     */
    private void setupActivityTable() {
        colActivityTimestamp.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getTimestamp()));
        
        colActivityUsername.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getUsername()));
        
        colActivitySessionId.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSessionId()));
        
        colActivityType.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getActivityType()));
        
        colActivityDetails.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDetails()));
        
        tblSessionActivity.setItems(sessionActivities);
    }
    
    /**
     * Setup the activity filter.
     */
    private void setupActivityFilter() {
        choiceActivityFilter.setItems(FXCollections.observableArrayList(
                "All Activities", "Created", "Renewed", "Invalidated", "Expired", 
                "Login", "Logout", "Login Failed", "Access Denied"));
        
        choiceActivityFilter.setValue("All Activities");
        
        choiceActivityFilter.setOnAction(e -> refreshActivityData());
    }
    
    /**
     * Refresh all data.
     */
    private void refreshData() {
        refreshActiveSessionsData();
        refreshActivityData();
        refreshStatisticsData();
    }
    
    /**
     * Refresh active sessions data.
     */
    private void refreshActiveSessionsData() {
        activeSessions.clear();
        
        List<UserSession> sessions = sessionManager.getAllActiveSessions();
        lblActiveSessionCount.setText(String.valueOf(sessions.size()));
        
        for (UserSession session : sessions) {
            SessionTableItem item = new SessionTableItem(
                    session.getSessionId(),
                    session.getUser().getUsername(),
                    session.getCreationTime().format(FORMATTER),
                    session.getExpirationTime().format(FORMATTER),
                    session.getMinutesRemaining() + " min",
                    session.getIpAddress()
            );
            
            activeSessions.add(item);
        }
    }
    
    /**
     * Refresh activity data.
     */
    private void refreshActivityData() {
        sessionActivities.clear();
        
        String filter = choiceActivityFilter.getValue();
        String search = txtActivitySearch.getText().trim().toLowerCase();
        
        List<SessionActivity> activities;
        
        if (!search.isEmpty()) {
            // Filter by username
            activities = activityTracker.getUserActivities(search, 100);
        } else {
            // Get recent activities
            activities = activityTracker.getRecentActivities(100);
        }
        
        // Apply activity type filter
        for (SessionActivity activity : activities) {
            if (!filter.equals("All Activities")) {
                if (!activity.getType().name().equalsIgnoreCase(filter)) {
                    continue;
                }
            }
            
            ActivityTableItem item = new ActivityTableItem(
                    activity.getTimestamp().format(FORMATTER),
                    activity.getUsername(),
                    activity.getSessionId(),
                    activity.getType().name(),
                    activity.getDetails()
            );
            
            sessionActivities.add(item);
        }
    }
    
    /**
     * Refresh statistics data.
     */
    private void refreshStatisticsData() {
        // Update labels
        lblSessionsCreated.setText(String.valueOf(activityTracker.getSessionCreatedCount()));
        lblSessionsRenewed.setText(String.valueOf(activityTracker.getSessionRenewedCount()));
        lblSessionsInvalidated.setText(String.valueOf(activityTracker.getSessionInvalidatedCount()));
        lblSessionsExpired.setText(String.valueOf(activityTracker.getSessionExpiredCount()));
        lblStatsActiveSessions.setText(String.valueOf(sessionManager.getActiveSessionCount()));
        
        // Get failed login count
        lblFailedLogins.setText(String.valueOf(activityTracker.getLoginFailedCount()));
        
        // Update chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Created", activityTracker.getSessionCreatedCount()),
                new PieChart.Data("Renewed", activityTracker.getSessionRenewedCount()),
                new PieChart.Data("Invalidated", activityTracker.getSessionInvalidatedCount()),
                new PieChart.Data("Expired", activityTracker.getSessionExpiredCount())
        );
        
        chartSessionTypes.setData(pieChartData);
    }
    
    /**
     * Load session settings.
     */
    private void loadSettings() {
        txtDefaultTimeout.setText(String.valueOf(sessionManager.getDefaultSessionTimeout()));
        txtExtendTimeout.setText(String.valueOf(sessionManager.getExtendSessionTimeout()));
        txtMaxTimeout.setText(String.valueOf(sessionManager.getMaximumSessionTimeout()));
        txtInactivityWarning.setText(String.valueOf(sessionFilter.getInactivityWarningMinutes()));
        txtRememberMeDays.setText(String.valueOf(sessionManager.getRememberMeDuration()));
        chkAutoRenew.setSelected(sessionFilter.isAutoRenewSessions());
    }
    
    /**
     * Save session settings.
     */
    private void saveSettings() {
        try {
            int defaultTimeout = Integer.parseInt(txtDefaultTimeout.getText().trim());
            int extendTimeout = Integer.parseInt(txtExtendTimeout.getText().trim());
            int maxTimeout = Integer.parseInt(txtMaxTimeout.getText().trim());
            int inactivityWarning = Integer.parseInt(txtInactivityWarning.getText().trim());
            int rememberMeDays = Integer.parseInt(txtRememberMeDays.getText().trim());
            boolean autoRenew = chkAutoRenew.isSelected();
            
            // Update settings
            sessionManager.setDefaultSessionTimeout(defaultTimeout);
            sessionManager.setExtendSessionTimeout(extendTimeout);
            sessionManager.setMaximumSessionTimeout(maxTimeout);
            sessionManager.setRememberMeDuration(rememberMeDays);
            sessionFilter.setInactivityWarningMinutes(inactivityWarning);
            sessionFilter.setAutoRenewSessions(autoRenew);
            
            showInfoAlert("Settings Saved", "Session settings have been saved successfully.");
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Settings", "Please enter valid numbers for all timeout settings.");
        }
    }
    
    /**
     * Reset settings to defaults.
     */
    private void resetSettings() {
        txtDefaultTimeout.setText("30");
        txtExtendTimeout.setText("15");
        txtMaxTimeout.setText("120");
        txtInactivityWarning.setText("5");
        txtRememberMeDays.setText("30");
        chkAutoRenew.setSelected(true);
    }
    
    /**
     * Terminate a session.
     * @param sessionId The session ID to terminate
     */
    private void terminateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        
        // Check if trying to terminate own session
        UserSession currentSession = securityContext.getCurrentSession();
        if (currentSession != null && sessionId.equals(currentSession.getSessionId())) {
            showWarningAlert("Cannot Terminate Own Session", 
                    "You cannot terminate your own session. Use the logout button instead.");
            return;
        }
        
        // Confirm termination
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Session Termination");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to terminate this session?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sessionManager.invalidateSession(sessionId)) {
                refreshData();
            } else {
                showErrorAlert("Termination Failed", 
                        "Failed to terminate session. It may have already expired.");
            }
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshData();
    }
    
    /**
     * Handle terminate selected button click.
     */
    @FXML
    private void handleTerminateSelected(ActionEvent event) {
        SessionTableItem selectedItem = tblActiveSessions.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showWarningAlert("No Session Selected", "Please select a session to terminate.");
            return;
        }
        
        terminateSession(selectedItem.getSessionId());
    }
    
    /**
     * Handle terminate all button click.
     */
    @FXML
    private void handleTerminateAll(ActionEvent event) {
        // Confirm termination
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Terminate All Sessions");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to terminate ALL active sessions except your own?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int count = 0;
            
            // Get current session ID to avoid terminating own session
            String currentSessionId = null;
            UserSession currentSession = securityContext.getCurrentSession();
            if (currentSession != null) {
                currentSessionId = currentSession.getSessionId();
            }
            
            // Terminate all sessions except current one
            for (SessionTableItem item : activeSessions) {
                String sessionId = item.getSessionId();
                if (!sessionId.equals(currentSessionId)) {
                    if (sessionManager.invalidateSession(sessionId)) {
                        count++;
                    }
                }
            }
            
            refreshData();
            showInfoAlert("Sessions Terminated", 
                    count + " sessions have been terminated successfully.");
        }
    }
    
    /**
     * Handle search activity button click.
     */
    @FXML
    private void handleSearchActivity(ActionEvent event) {
        refreshActivityData();
    }
    
    /**
     * Handle clear activity search button click.
     */
    @FXML
    private void handleClearActivitySearch(ActionEvent event) {
        txtActivitySearch.clear();
        refreshActivityData();
    }
    
    /**
     * Handle reset statistics button click.
     */
    @FXML
    private void handleResetStatistics(ActionEvent event) {
        // Confirm reset
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Reset Statistics");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to reset all session statistics?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            activityTracker.resetStatistics();
            refreshStatisticsData();
        }
    }
    
    /**
     * Handle reset defaults button click.
     */
    @FXML
    private void handleResetDefaults(ActionEvent event) {
        // Confirm reset
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Reset Settings");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to reset all settings to default values?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            resetSettings();
        }
    }
    
    /**
     * Handle save settings button click.
     */
    @FXML
    private void handleSaveSettings(ActionEvent event) {
        saveSettings();
    }
    
    /**
     * Class representing an active session in the table.
     */
    public static class SessionTableItem {
        private final String sessionId;
        private final String username;
        private final String creationTime;
        private final String expirationTime;
        private final String remainingTime;
        private final String ipAddress;
        
        public SessionTableItem(String sessionId, String username, String creationTime, 
                              String expirationTime, String remainingTime, String ipAddress) {
            this.sessionId = sessionId;
            this.username = username;
            this.creationTime = creationTime;
            this.expirationTime = expirationTime;
            this.remainingTime = remainingTime;
            this.ipAddress = ipAddress;
        }
        
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public String getCreationTime() { return creationTime; }
        public String getExpirationTime() { return expirationTime; }
        public String getRemainingTime() { return remainingTime; }
        public String getIpAddress() { return ipAddress; }
    }
    
    /**
     * Class representing a session activity in the table.
     */
    public static class ActivityTableItem {
        private final String timestamp;
        private final String username;
        private final String sessionId;
        private final String activityType;
        private final String details;
        
        public ActivityTableItem(String timestamp, String username, String sessionId, 
                               String activityType, String details) {
            this.timestamp = timestamp;
            this.username = username;
            this.sessionId = sessionId;
            this.activityType = activityType;
            this.details = details;
        }
        
        public String getTimestamp() { return timestamp; }
        public String getUsername() { return username; }
        public String getSessionId() { return sessionId; }
        public String getActivityType() { return activityType; }
        public String getDetails() { return details; }
    }
}