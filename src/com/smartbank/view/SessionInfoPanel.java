package com.smartbank.view;

import com.smartbank.auth.SecurityContext;
import com.smartbank.auth.SessionEventLogger;
import com.smartbank.auth.SessionManager;
import com.smartbank.auth.UserSession;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI component that displays session information and provides session management controls.
 */
public class SessionInfoPanel extends VBox implements SessionEventLogger.SessionInfoDisplay {
    private static final Logger LOGGER = Logger.getLogger(SessionInfoPanel.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // UI components
    private final Label lblUsername = new Label();
    private final Label lblSessionId = new Label();
    private final Label lblCreated = new Label();
    private final Label lblExpiration = new Label();
    private final Label lblRemaining = new Label();
    private final Label lblIpAddress = new Label();
    private final ProgressBar progressSession = new ProgressBar(1.0);
    private final Button btnExtend = new Button("Extend Session");
    private final Button btnLogout = new Button("Logout");
    
    // Session management
    private final SessionManager sessionManager;
    private final SecurityContext securityContext;
    private final SessionEventLogger sessionLogger;
    
    // Timer for updating session info
    private Timer updateTimer;
    
    /**
     * Constructor.
     */
    public SessionInfoPanel() {
        // Get session management components
        sessionManager = SessionManager.getInstance();
        securityContext = SecurityContext.getInstance();
        
        // Create session logger and register with session manager
        sessionLogger = new SessionEventLogger(this);
        sessionManager.addSessionEventListener(sessionLogger);
        
        // Initialize UI
        initializeUi();
        
        // Start update timer
        startUpdateTimer();
        
        // Set style class
        getStyleClass().add("session-info-panel");
    }
    
    /**
     * Initialize the UI components.
     */
    private void initializeUi() {
        // Set padding and spacing
        setPadding(new Insets(10));
        setSpacing(5);
        
        // Create title
        Label title = new Label("Session Information");
        title.getStyleClass().add("session-title");
        
        // Create info grid
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("session-info-box");
        
        // Add user info
        HBox userBox = new HBox(5);
        Label userLabel = new Label("User:");
        userLabel.setMinWidth(80);
        userBox.getChildren().addAll(userLabel, lblUsername);
        lblUsername.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblUsername, Priority.ALWAYS);
        
        // Add session ID
        HBox sessionBox = new HBox(5);
        Label sessionLabel = new Label("Session ID:");
        sessionLabel.setMinWidth(80);
        sessionBox.getChildren().addAll(sessionLabel, lblSessionId);
        lblSessionId.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblSessionId, Priority.ALWAYS);
        
        // Add creation time
        HBox createdBox = new HBox(5);
        Label createdLabel = new Label("Created:");
        createdLabel.setMinWidth(80);
        createdBox.getChildren().addAll(createdLabel, lblCreated);
        
        // Add expiration time
        HBox expirationBox = new HBox(5);
        Label expirationLabel = new Label("Expires:");
        expirationLabel.setMinWidth(80);
        expirationBox.getChildren().addAll(expirationLabel, lblExpiration);
        
        // Add remaining time
        HBox remainingBox = new HBox(5);
        Label remainingLabel = new Label("Remaining:");
        remainingLabel.setMinWidth(80);
        remainingBox.getChildren().addAll(remainingLabel, lblRemaining);
        
        // Add IP address
        HBox ipBox = new HBox(5);
        Label ipLabel = new Label("IP Address:");
        ipLabel.setMinWidth(80);
        ipBox.getChildren().addAll(ipLabel, lblIpAddress);
        
        // Add progress bar
        progressSession.setMaxWidth(Double.MAX_VALUE);
        Tooltip progressTooltip = new Tooltip("Session time remaining");
        Tooltip.install(progressSession, progressTooltip);
        
        // Add all info elements
        infoBox.getChildren().addAll(
                userBox, sessionBox, createdBox, expirationBox, 
                remainingBox, ipBox, progressSession
        );
        
        // Create button box
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(btnExtend, btnLogout);
        
        // Setup extend button
        btnExtend.setOnAction(e -> extendSession());
        btnExtend.getStyleClass().add("extend-button");
        
        // Setup logout button
        btnLogout.setOnAction(e -> logout());
        btnLogout.getStyleClass().add("logout-button");
        
        // Add all components to panel
        getChildren().addAll(title, infoBox, buttonBox);
        
        // Set initial state
        updateSessionInfo();
    }
    
    /**
     * Start the timer for updating session information.
     */
    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateSessionInfo());
            }
        }, 0, 1000); // Update every second
    }
    
    /**
     * Stop the update timer.
     */
    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
    
    /**
     * Update the session information display.
     */
    private void updateSessionInfo() {
        UserSession session = securityContext.getCurrentSession();
        
        if (session == null || !session.isValid()) {
            lblUsername.setText("No active session");
            lblSessionId.setText("");
            lblCreated.setText("");
            lblExpiration.setText("");
            lblRemaining.setText("");
            lblIpAddress.setText("");
            progressSession.setProgress(0);
            btnExtend.setDisable(true);
            return;
        }
        
        // Get session details
        String username = session.getUser().getUsername();
        String sessionId = session.getSessionId();
        LocalDateTime creationTime = session.getCreationTime();
        LocalDateTime expirationTime = session.getExpirationTime();
        long minutesRemaining = session.getMinutesRemaining();
        String ipAddress = session.getIpAddress();
        
        // Update labels
        lblUsername.setText(username);
        lblSessionId.setText(sessionId);
        lblCreated.setText(creationTime.format(FORMATTER));
        lblExpiration.setText(expirationTime.format(FORMATTER));
        lblRemaining.setText(minutesRemaining + " minutes");
        lblIpAddress.setText(ipAddress);
        
        // Update progress bar
        Duration totalDuration = Duration.between(creationTime, expirationTime);
        Duration remainingDuration = Duration.between(LocalDateTime.now(), expirationTime);
        double progress = (double) remainingDuration.toMillis() / totalDuration.toMillis();
        progressSession.setProgress(Math.max(0, Math.min(1, progress)));
        
        // Update progress bar color based on remaining time
        if (minutesRemaining < 5) {
            progressSession.setStyle("-fx-accent: red;");
        } else if (minutesRemaining < 15) {
            progressSession.setStyle("-fx-accent: orange;");
        } else {
            progressSession.setStyle("-fx-accent: green;");
        }
        
        // Enable/disable extend button
        btnExtend.setDisable(minutesRemaining > sessionManager.getMaximumSessionTimeout() - 5);
    }
    
    /**
     * Extend the current session.
     */
    private void extendSession() {
        UserSession session = securityContext.getCurrentSession();
        if (session == null || !session.isValid()) {
            return;
        }
        
        if (sessionManager.renewSession(session.getSessionId())) {
            // Update immediately
            updateSessionInfo();
        }
    }
    
    /**
     * Logout the current user.
     */
    private void logout() {
        UserSession session = securityContext.getCurrentSession();
        if (session == null) {
            return;
        }
        
        try {
            sessionManager.invalidateSession(session.getSessionId());
            // The UI should handle navigation to the login screen
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during logout: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clean up resources when the component is no longer needed.
     */
    public void cleanup() {
        stopUpdateTimer();
        sessionManager.removeSessionEventListener(sessionLogger);
    }
    
    @Override
    public void displaySessionInfo(String username, String sessionId, String creationTime, 
                                 String expirationTime, long minutesRemaining, String ipAddress) {
        // Not used - we're directly reading from the session
        updateSessionInfo();
    }
    
    @Override
    public void displaySessionTerminated(String username, String sessionId, String timestamp) {
        // Session terminated - update the display
        updateSessionInfo();
    }
    
    @Override
    public void displaySessionExpired(String username, String sessionId, String timestamp) {
        // Session expired - update the display
        updateSessionInfo();
    }
}