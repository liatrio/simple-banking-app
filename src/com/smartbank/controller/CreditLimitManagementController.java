package com.smartbank.controller;

import com.smartbank.model.Account;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.CreditHistory;
import com.smartbank.model.CreditLimitChangeRequest;
import com.smartbank.model.User;
import com.smartbank.service.AccountService;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.credit.CreditLimitEvaluationResult;
import com.smartbank.service.credit.CreditLimitService;
import com.smartbank.auth.RolePermission;
import com.smartbank.auth.SecurityContext;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the credit limit management view.
 */
public class CreditLimitManagementController extends BaseController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(CreditLimitManagementController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
    
    private final CreditLimitService creditLimitService;
    private final AccountService accountService;
    
    @FXML private ComboBox<CreditAccount> accountComboBox;
    
    // Account details
    @FXML private Label accountNumberLabel;
    @FXML private Label currentCreditLimitLabel;
    @FXML private Label initialCreditLimitLabel;
    @FXML private Label creditScoreLabel;
    @FXML private Label onTimePaymentsLabel;
    @FXML private Label latePaymentsLabel;
    @FXML private Label averageMonthlyBalanceLabel;
    @FXML private Label lastCreditLimitChangeLabel;
    @FXML private Label lastCreditScoreUpdateLabel;
    @FXML private Label automaticReviewsLabel;
    @FXML private Button toggleAutomaticReviewsButton;
    
    // Evaluation results
    @FXML private TitledPane evaluationResultsPane;
    @FXML private Label recommendationLabel;
    @FXML private Label evaluationCurrentLimitLabel;
    @FXML private Label recommendedLimitLabel;
    @FXML private Label changeAmountLabel;
    @FXML private Label reasonLabel;
    @FXML private Button applyRecommendationButton;
    
    // Credit history
    @FXML private TableView<CreditHistory> creditHistoryTable;
    @FXML private TableColumn<CreditHistory, String> historyDateColumn;
    @FXML private TableColumn<CreditHistory, String> historyEventTypeColumn;
    @FXML private TableColumn<CreditHistory, String> historyDescriptionColumn;
    @FXML private TableColumn<CreditHistory, String> historyOldValueColumn;
    @FXML private TableColumn<CreditHistory, String> historyNewValueColumn;
    
    // Change requests
    @FXML private ToggleButton showAllRequestsToggle;
    @FXML private TableView<CreditLimitChangeRequest> changeRequestsTable;
    @FXML private TableColumn<CreditLimitChangeRequest, Long> requestIdColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> requestDateColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> requestStatusColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> requestSourceColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, Double> requestCurrentLimitColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, Double> requestNewLimitColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> requestChangeColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> requestReasonColumn;
    @FXML private Button approveRequestButton;
    @FXML private Button rejectRequestButton;
    
    // Admin tab
    @FXML private Tab adminTab;
    @FXML private Label batchOperationResultLabel;
    @FXML private TableView<CreditLimitChangeRequest> pendingRequestsTable;
    @FXML private TableColumn<CreditLimitChangeRequest, Long> pendingIdColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, Long> pendingAccountColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> pendingDateColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> pendingSourceColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, Double> pendingCurrentLimitColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, Double> pendingNewLimitColumn;
    @FXML private TableColumn<CreditLimitChangeRequest, String> pendingChangeColumn;
    
    // State
    private CreditLimitEvaluationResult currentEvaluation;
    
    /**
     * Constructor.
     */
    public CreditLimitManagementController() {
        this.creditLimitService = ServiceFactory.getCreditLimitService();
        this.accountService = ServiceFactory.getAccountService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up account combo box
        setupAccountComboBox();
        
        // Set up tables
        setupCreditHistoryTable();
        setupChangeRequestsTable();
        setupPendingRequestsTable();
        
        // Set up visibility based on user role
        setupRoleBasedVisibility();
        
        // Clear evaluation result pane
        clearEvaluationResults();
        
        // Initial refresh of pending requests for admins
        if (SecurityContext.getCurrentUser().hasPermission(RolePermission.ADMIN)) {
            refreshPendingRequests();
        }
    }
    
    /**
     * Set up the account combo box with credit accounts.
     */
    private void setupAccountComboBox() {
        try {
            // Get current user
            User currentUser = SecurityContext.getCurrentUser();
            
            // Get list of credit accounts
            List<Account> accounts = accountService.getAccountsByUser(currentUser.getUsername());
            List<CreditAccount> creditAccounts = accounts.stream()
                    .filter(a -> a instanceof CreditAccount)
                    .map(a -> (CreditAccount) a)
                    .collect(Collectors.toList());
            
            // Populate combo box
            ObservableList<CreditAccount> accountsList = FXCollections.observableArrayList(creditAccounts);
            accountComboBox.setItems(accountsList);
            
            // Set converter to display account number and balance
            accountComboBox.setConverter(new StringConverter<CreditAccount>() {
                @Override
                public String toString(CreditAccount account) {
                    if (account == null) return null;
                    return String.format("Account #%d - Balance: %s - Credit Limit: %s", 
                            account.getAccountNumber(), 
                            CURRENCY_FORMAT.format(account.getBalance()),
                            CURRENCY_FORMAT.format(account.getCreditLimit()));
                }
                
                @Override
                public CreditAccount fromString(String string) {
                    return null; // Not needed for display
                }
            });
            
            // Auto-select first account if available
            if (!creditAccounts.isEmpty()) {
                accountComboBox.getSelectionModel().selectFirst();
                loadAccountDetails();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading credit accounts", e);
            showErrorAlert("Error", "Failed to load credit accounts", e.getMessage());
        }
    }
    
    /**
     * Set up the credit history table.
     */
    private void setupCreditHistoryTable() {
        historyDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getEventDateTime().format(DATE_FORMATTER)));
                
        historyEventTypeColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(formatEventType(cellData.getValue().getEventType())));
                
        historyDescriptionColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDescription()));
                
        historyOldValueColumn.setCellValueFactory(cellData -> {
            double oldValue = cellData.getValue().getOldValue();
            int oldCreditScore = cellData.getValue().getOldCreditScore();
            
            if (oldCreditScore > 0) {
                return new SimpleStringProperty(String.valueOf(oldCreditScore));
            } else if (oldValue != 0) {
                return new SimpleStringProperty(CURRENCY_FORMAT.format(oldValue));
            } else {
                return new SimpleStringProperty("");
            }
        });
        
        historyNewValueColumn.setCellValueFactory(cellData -> {
            double newValue = cellData.getValue().getNewValue();
            int newCreditScore = cellData.getValue().getNewCreditScore();
            
            if (newCreditScore > 0) {
                return new SimpleStringProperty(String.valueOf(newCreditScore));
            } else if (newValue != 0) {
                return new SimpleStringProperty(CURRENCY_FORMAT.format(newValue));
            } else {
                return new SimpleStringProperty("");
            }
        });
    }
    
    /**
     * Format event type for display.
     */
    private String formatEventType(CreditHistory.EventType eventType) {
        if (eventType == null) return "";
        
        switch (eventType) {
            case PAYMENT_ON_TIME:
                return "On-Time Payment";
            case PAYMENT_LATE:
                return "Late Payment";
            case CREDIT_LIMIT_INCREASE:
                return "Limit Increase";
            case CREDIT_LIMIT_DECREASE:
                return "Limit Decrease";
            case CREDIT_SCORE_UPDATE:
                return "Score Update";
            case ACCOUNT_OVERDRAWN:
                return "Account Overdrawn";
            case BALANCE_PAID_IN_FULL:
                return "Balance Paid in Full";
            case AUTOMATIC_REVIEW:
                return "Automatic Review";
            case MANUAL_REVIEW:
                return "Manual Review";
            default:
                return eventType.toString();
        }
    }
    
    /**
     * Set up the change requests table.
     */
    private void setupChangeRequestsTable() {
        requestIdColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getId()));
                
        requestDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRequestDateTime().format(DATE_FORMATTER)));
                
        requestStatusColumn.setCellValueFactory(cellData -> {
            CreditLimitChangeRequest.Status status = cellData.getValue().getStatus();
            return new SimpleStringProperty(status.toString());
        });
        requestStatusColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("APPROVED")) {
                        setTextFill(Color.GREEN);
                    } else if (item.equals("REJECTED")) {
                        setTextFill(Color.RED);
                    } else if (item.equals("PENDING")) {
                        setTextFill(Color.BLUE);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        
        requestSourceColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(formatSource(cellData.getValue().getSource())));
                
        requestCurrentLimitColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getCurrentCreditLimit()).asObject());
        requestCurrentLimitColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CURRENCY_FORMAT.format(item));
                }
            }
        });
        
        requestNewLimitColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getRequestedCreditLimit()).asObject());
        requestNewLimitColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CURRENCY_FORMAT.format(item));
                }
            }
        });
        
        requestChangeColumn.setCellValueFactory(cellData -> {
            double percentChange = cellData.getValue().getPercentageChange();
            return new SimpleStringProperty(String.format("%+.1f%%", percentChange));
        });
        requestChangeColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+")) {
                        setTextFill(Color.GREEN);
                    } else if (item.startsWith("-")) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        
        requestReasonColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getReason()));
    }
    
    /**
     * Format source for display.
     */
    private String formatSource(CreditLimitChangeRequest.Source source) {
        if (source == null) return "";
        
        switch (source) {
            case USER_REQUESTED:
                return "User";
            case SYSTEM_AUTOMATIC:
                return "System";
            case ADMIN_INITIATED:
                return "Admin";
            default:
                return source.toString();
        }
    }
    
    /**
     * Set up the pending requests table.
     */
    private void setupPendingRequestsTable() {
        pendingIdColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getId()));
                
        pendingAccountColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getAccountNumber()));
                
        pendingDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRequestDateTime().format(DATE_FORMATTER)));
                
        pendingSourceColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(formatSource(cellData.getValue().getSource())));
                
        pendingCurrentLimitColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getCurrentCreditLimit()).asObject());
        pendingCurrentLimitColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CURRENCY_FORMAT.format(item));
                }
            }
        });
        
        pendingNewLimitColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getRequestedCreditLimit()).asObject());
        pendingNewLimitColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CURRENCY_FORMAT.format(item));
                }
            }
        });
        
        pendingChangeColumn.setCellValueFactory(cellData -> {
            double percentChange = cellData.getValue().getPercentageChange();
            return new SimpleStringProperty(String.format("%+.1f%%", percentChange));
        });
        pendingChangeColumn.setCellFactory(column -> new TableCell<CreditLimitChangeRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+")) {
                        setTextFill(Color.GREEN);
                    } else if (item.startsWith("-")) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
    }
    
    /**
     * Set up visibility based on user role.
     */
    private void setupRoleBasedVisibility() {
        User currentUser = SecurityContext.getCurrentUser();
        boolean isAdmin = currentUser.hasPermission(RolePermission.ADMIN);
        
        adminTab.setDisable(!isAdmin);
        
        // Only admins can approve/reject requests from other users
        changeRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String requestedBy = newVal.getRequestedBy();
                boolean isOwnRequest = requestedBy.equals(currentUser.getUsername());
                boolean isPending = newVal.getStatus() == CreditLimitChangeRequest.Status.PENDING;
                
                approveRequestButton.setDisable(!isPending || (!isAdmin && !isOwnRequest));
                rejectRequestButton.setDisable(!isPending || (!isAdmin && !isOwnRequest));
            } else {
                approveRequestButton.setDisable(true);
                rejectRequestButton.setDisable(true);
            }
        });
    }
    
    /**
     * Load account details when an account is selected.
     */
    @FXML
    public void loadAccountDetails() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) {
            clearAccountDetails();
            return;
        }
        
        try {
            // Refresh account data
            Optional<Account> refreshedAccount = accountService.getAccount(account.getAccountNumber());
            if (!refreshedAccount.isPresent() || !(refreshedAccount.get() instanceof CreditAccount)) {
                throw new Exception("Account not found or not a credit account");
            }
            
            account = (CreditAccount) refreshedAccount.get();
            
            // Update account details
            accountNumberLabel.setText(String.valueOf(account.getAccountNumber()));
            currentCreditLimitLabel.setText(CURRENCY_FORMAT.format(account.getCreditLimit()));
            initialCreditLimitLabel.setText(CURRENCY_FORMAT.format(account.getInitialCreditLimit()));
            creditScoreLabel.setText(String.valueOf(account.getCreditScore()));
            onTimePaymentsLabel.setText(String.valueOf(account.getNumberOfOnTimePayments()));
            latePaymentsLabel.setText(String.valueOf(account.getNumberOfLatePayments()));
            averageMonthlyBalanceLabel.setText(CURRENCY_FORMAT.format(account.getAverageMonthlyBalance()));
            
            LocalDateTime lastCreditLimitChange = account.getLastCreditLimitChangeDateTime();
            lastCreditLimitChangeLabel.setText(lastCreditLimitChange.format(DATE_FORMATTER));
            
            LocalDateTime lastCreditScoreUpdate = account.getLastCreditScoreUpdateDateTime();
            lastCreditScoreUpdateLabel.setText(lastCreditScoreUpdate.format(DATE_FORMATTER));
            
            boolean automaticReviewsEnabled = account.isAutomaticCreditLimitReviewEnabled();
            automaticReviewsLabel.setText(automaticReviewsEnabled ? "Enabled" : "Disabled");
            toggleAutomaticReviewsButton.setText(automaticReviewsEnabled ? "Disable" : "Enable");
            
            // Load credit history
            refreshCreditHistory();
            
            // Load change requests
            refreshChangeRequests();
            
            // Clear evaluation results
            clearEvaluationResults();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading account details", e);
            showErrorAlert("Error", "Failed to load account details", e.getMessage());
        }
    }
    
    /**
     * Clear account details.
     */
    private void clearAccountDetails() {
        accountNumberLabel.setText("");
        currentCreditLimitLabel.setText("");
        initialCreditLimitLabel.setText("");
        creditScoreLabel.setText("");
        onTimePaymentsLabel.setText("");
        latePaymentsLabel.setText("");
        averageMonthlyBalanceLabel.setText("");
        lastCreditLimitChangeLabel.setText("");
        lastCreditScoreUpdateLabel.setText("");
        automaticReviewsLabel.setText("");
        toggleAutomaticReviewsButton.setText("Enable");
        
        creditHistoryTable.getItems().clear();
        changeRequestsTable.getItems().clear();
        clearEvaluationResults();
    }
    
    /**
     * Clear evaluation results.
     */
    private void clearEvaluationResults() {
        evaluationResultsPane.setExpanded(false);
        recommendationLabel.setText("");
        evaluationCurrentLimitLabel.setText("");
        recommendedLimitLabel.setText("");
        changeAmountLabel.setText("");
        reasonLabel.setText("");
        currentEvaluation = null;
        applyRecommendationButton.setDisable(true);
    }
    
    /**
     * Toggle automatic credit limit reviews.
     */
    @FXML
    public void toggleAutomaticReviews() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) return;
        
        try {
            boolean currentSetting = account.isAutomaticCreditLimitReviewEnabled();
            boolean newSetting;
            
            if (currentSetting) {
                newSetting = creditLimitService.disableAutomaticCreditLimitReviews(account.getAccountNumber());
            } else {
                newSetting = creditLimitService.enableAutomaticCreditLimitReviews(account.getAccountNumber());
            }
            
            // Update display
            automaticReviewsLabel.setText(newSetting ? "Enabled" : "Disabled");
            toggleAutomaticReviewsButton.setText(newSetting ? "Disable" : "Enable");
            
            showInformationAlert("Automatic Reviews", 
                    "Automatic credit limit reviews have been " + (newSetting ? "enabled" : "disabled"),
                    "Changes will be applied during the next scheduled review.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error toggling automatic reviews", e);
            showErrorAlert("Error", "Failed to toggle automatic reviews", e.getMessage());
        }
    }
    
    /**
     * Evaluate credit limit for the selected account.
     */
    @FXML
    public void evaluateCreditLimit() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) return;
        
        try {
            // Get evaluation
            currentEvaluation = creditLimitService.evaluateForCreditLimitAdjustment(account.getAccountNumber());
            
            // Display results
            evaluationResultsPane.setExpanded(true);
            
            String recommendation;
            Color recommendationColor;
            
            switch (currentEvaluation.getRecommendation()) {
                case INCREASE:
                    recommendation = "INCREASE RECOMMENDED";
                    recommendationColor = Color.GREEN;
                    break;
                case DECREASE:
                    recommendation = "DECREASE RECOMMENDED";
                    recommendationColor = Color.RED;
                    break;
                default:
                    recommendation = "NO CHANGE RECOMMENDED";
                    recommendationColor = Color.BLACK;
                    break;
            }
            
            recommendationLabel.setText(recommendation);
            recommendationLabel.setTextFill(recommendationColor);
            
            evaluationCurrentLimitLabel.setText(CURRENCY_FORMAT.format(currentEvaluation.getCurrentCreditLimit()));
            recommendedLimitLabel.setText(CURRENCY_FORMAT.format(currentEvaluation.getRecommendedCreditLimit()));
            
            double changeAmount = currentEvaluation.getCreditLimitDifference();
            changeAmountLabel.setText(String.format("%s (%+.1f%%)", 
                    CURRENCY_FORMAT.format(changeAmount),
                    currentEvaluation.getPercentageChange()));
            
            if (changeAmount > 0) {
                changeAmountLabel.setTextFill(Color.GREEN);
            } else if (changeAmount < 0) {
                changeAmountLabel.setTextFill(Color.RED);
            } else {
                changeAmountLabel.setTextFill(Color.BLACK);
            }
            
            reasonLabel.setText(currentEvaluation.getReason());
            
            // Enable apply button if there is a recommended change
            applyRecommendationButton.setDisable(currentEvaluation.getRecommendation() == 
                    CreditLimitEvaluationResult.Recommendation.NO_CHANGE);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error evaluating credit limit", e);
            showErrorAlert("Error", "Failed to evaluate credit limit", e.getMessage());
        }
    }
    
    /**
     * Apply the recommended credit limit change.
     */
    @FXML
    public void applyRecommendation() {
        if (currentEvaluation == null || accountComboBox.getValue() == null) return;
        
        try {
            long accountNumber = accountComboBox.getValue().getAccountNumber();
            
            // Confirm with user
            boolean confirmed = showConfirmationDialog("Apply Recommendation", 
                    "Are you sure you want to apply the recommended credit limit change?",
                    String.format("This will change the credit limit from %s to %s.",
                            CURRENCY_FORMAT.format(currentEvaluation.getCurrentCreditLimit()),
                            CURRENCY_FORMAT.format(currentEvaluation.getRecommendedCreditLimit())));
            
            if (!confirmed) return;
            
            // Apply recommendation (creates and auto-approves a change request)
            CreditAccount updatedAccount = creditLimitService.autoAdjustCreditLimit(accountNumber);
            
            // Refresh the view
            accountComboBox.setValue(updatedAccount);
            loadAccountDetails();
            
            // Clear evaluation results
            clearEvaluationResults();
            
            showInformationAlert("Success", "Credit limit updated successfully", 
                    "The new credit limit is " + CURRENCY_FORMAT.format(updatedAccount.getCreditLimit()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying recommendation", e);
            showErrorAlert("Error", "Failed to apply recommendation", e.getMessage());
        }
    }
    
    /**
     * Show dialog to request a credit limit change.
     */
    @FXML
    public void showRequestCreditLimitChangeDialog() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) return;
        
        // Create dialog
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Request Credit Limit Change");
        dialog.setHeaderText("Enter new credit limit for account #" + account.getAccountNumber());
        
        // Set buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField newLimitField = new TextField();
        newLimitField.setPromptText("New credit limit");
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Reason for change");
        reasonField.setPrefRowCount(3);
        
        Label currentLimitLabel = new Label("Current limit: " + CURRENCY_FORMAT.format(account.getCreditLimit()));
        
        grid.add(new Label("Current Credit Limit:"), 0, 0);
        grid.add(currentLimitLabel, 1, 0);
        grid.add(new Label("New Credit Limit:"), 0, 1);
        grid.add(newLimitField, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(reasonField, 1, 2);
        
        GridPane.setHgrow(reasonField, Priority.ALWAYS);
        
        dialog.getDialogPane().setContent(grid);
        
        // Validate input
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        newLimitField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double newLimit = Double.parseDouble(newValue);
                okButton.setDisable(newLimit <= 0 || reasonField.getText().trim().isEmpty());
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
            }
        });
        
        reasonField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double newLimit = Double.parseDouble(newLimitField.getText());
                okButton.setDisable(newLimit <= 0 || newValue.trim().isEmpty());
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
            }
        });
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    return Double.parseDouble(newLimitField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Double> result = dialog.showAndWait();
        
        result.ifPresent(newLimit -> {
            try {
                long requestId = creditLimitService.requestCreditLimitChange(
                        account.getAccountNumber(), 
                        newLimit, 
                        SecurityContext.getCurrentUser().getUsername(), 
                        reasonField.getText().trim());
                
                if (requestId > 0) {
                    showInformationAlert("Request Submitted", 
                            "Credit limit change request submitted successfully",
                            "Request ID: " + requestId);
                    
                    // Refresh change requests
                    refreshChangeRequests();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error requesting credit limit change", e);
                showErrorAlert("Error", "Failed to submit credit limit change request", e.getMessage());
            }
        });
    }
    
    /**
     * Refresh credit history.
     */
    @FXML
    public void refreshCreditHistory() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) return;
        
        try {
            List<CreditHistory> history = creditLimitService.getCreditHistoryByAccount(account.getAccountNumber());
            creditHistoryTable.setItems(FXCollections.observableArrayList(history));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing credit history", e);
            showErrorAlert("Error", "Failed to refresh credit history", e.getMessage());
        }
    }
    
    /**
     * Refresh change requests.
     */
    private void refreshChangeRequests() {
        CreditAccount account = accountComboBox.getValue();
        if (account == null) return;
        
        try {
            List<CreditLimitChangeRequest> requests = creditLimitService.getCreditLimitChangeRequestsByAccount(account.getAccountNumber());
            
            // Filter if showing only pending
            if (!showAllRequestsToggle.isSelected()) {
                requests = requests.stream()
                        .filter(r -> r.getStatus() == CreditLimitChangeRequest.Status.PENDING)
                        .collect(Collectors.toList());
            }
            
            changeRequestsTable.setItems(FXCollections.observableArrayList(requests));
            
            // Disable approve/reject buttons until a request is selected
            approveRequestButton.setDisable(true);
            rejectRequestButton.setDisable(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing change requests", e);
            showErrorAlert("Error", "Failed to refresh change requests", e.getMessage());
        }
    }
    
    /**
     * Toggle between showing all requests or only pending ones.
     */
    @FXML
    public void toggleRequestView() {
        refreshChangeRequests();
    }
    
    /**
     * View detailed information about a selected change request.
     */
    @FXML
    public void viewRequestDetails() {
        CreditLimitChangeRequest request = changeRequestsTable.getSelectionModel().getSelectedItem();
        if (request == null) return;
        
        // Create details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Change Request Details");
        alert.setHeaderText("Credit Limit Change Request #" + request.getId());
        
        // Format content
        StringBuilder content = new StringBuilder();
        content.append("Account: ").append(request.getAccountNumber()).append("\n\n");
        content.append("Status: ").append(request.getStatus()).append("\n");
        content.append("Source: ").append(formatSource(request.getSource())).append("\n");
        content.append("Requested By: ").append(request.getRequestedBy()).append("\n");
        content.append("Request Date: ").append(request.getRequestDateTime().format(DATE_FORMATTER)).append("\n\n");
        
        content.append("Current Limit: ").append(CURRENCY_FORMAT.format(request.getCurrentCreditLimit())).append("\n");
        content.append("Requested Limit: ").append(CURRENCY_FORMAT.format(request.getRequestedCreditLimit())).append("\n");
        content.append("Change Amount: ").append(CURRENCY_FORMAT.format(request.getChangeAmount())).append(" (");
        content.append(String.format("%+.1f%%", request.getPercentageChange())).append(")\n\n");
        
        content.append("Reason: ").append(request.getReason()).append("\n\n");
        
        if (request.getStatus() == CreditLimitChangeRequest.Status.APPROVED) {
            content.append("Approved By: ").append(request.getApprovedBy()).append("\n");
            content.append("Approval Date: ").append(request.getDecisionDateTime().format(DATE_FORMATTER)).append("\n");
            if (request.getDecisionComments() != null && !request.getDecisionComments().isEmpty()) {
                content.append("Comments: ").append(request.getDecisionComments()).append("\n");
            }
        } else if (request.getStatus() == CreditLimitChangeRequest.Status.REJECTED) {
            content.append("Rejected By: ").append(request.getRejectedBy()).append("\n");
            content.append("Rejection Date: ").append(request.getDecisionDateTime().format(DATE_FORMATTER)).append("\n");
            if (request.getDecisionComments() != null && !request.getDecisionComments().isEmpty()) {
                content.append("Reason: ").append(request.getDecisionComments()).append("\n");
            }
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * Approve a selected change request.
     */
    @FXML
    public void approveRequest() {
        CreditLimitChangeRequest request = changeRequestsTable.getSelectionModel().getSelectedItem();
        if (request == null) return;
        
        // Only allow approval of pending requests
        if (request.getStatus() != CreditLimitChangeRequest.Status.PENDING) {
            showErrorAlert("Error", "Cannot approve request", 
                    "Only pending requests can be approved. This request is " + request.getStatus());
            return;
        }
        
        // Show dialog for comments
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Change Request");
        dialog.setHeaderText("Approve credit limit change from " + 
                CURRENCY_FORMAT.format(request.getCurrentCreditLimit()) + " to " + 
                CURRENCY_FORMAT.format(request.getRequestedCreditLimit()));
        dialog.setContentText("Comments (optional):");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            try {
                String comments = result.get().trim();
                CreditAccount updatedAccount = creditLimitService.approveCreditLimitChange(
                        request.getId(), 
                        SecurityContext.getCurrentUser().getUsername(), 
                        comments);
                
                if (updatedAccount != null) {
                    showInformationAlert("Success", "Credit limit change approved", 
                            "The new credit limit is " + CURRENCY_FORMAT.format(updatedAccount.getCreditLimit()));
                    
                    // Refresh the view
                    if (updatedAccount.getAccountNumber() == accountComboBox.getValue().getAccountNumber()) {
                        accountComboBox.setValue(updatedAccount);
                        loadAccountDetails();
                    } else {
                        refreshChangeRequests();
                        refreshPendingRequests();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error approving change request", e);
                showErrorAlert("Error", "Failed to approve change request", e.getMessage());
            }
        }
    }
    
    /**
     * Reject a selected change request.
     */
    @FXML
    public void rejectRequest() {
        CreditLimitChangeRequest request = changeRequestsTable.getSelectionModel().getSelectedItem();
        if (request == null) return;
        
        // Only allow rejection of pending requests
        if (request.getStatus() != CreditLimitChangeRequest.Status.PENDING) {
            showErrorAlert("Error", "Cannot reject request", 
                    "Only pending requests can be rejected. This request is " + request.getStatus());
            return;
        }
        
        // Show dialog for rejection reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Change Request");
        dialog.setHeaderText("Reject credit limit change request");
        dialog.setContentText("Reason for rejection:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                showErrorAlert("Error", "Rejection reason required", "You must provide a reason for rejection.");
                return;
            }
            
            try {
                boolean success = creditLimitService.rejectCreditLimitChange(
                        request.getId(), 
                        SecurityContext.getCurrentUser().getUsername(), 
                        reason);
                
                if (success) {
                    showInformationAlert("Success", "Credit limit change rejected", 
                            "The change request has been rejected.");
                    
                    // Refresh the view
                    refreshChangeRequests();
                    refreshPendingRequests();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error rejecting change request", e);
                showErrorAlert("Error", "Failed to reject change request", e.getMessage());
            }
        }
    }
    
    /**
     * Run automatic credit limit reviews for all accounts.
     */
    @FXML
    public void runAutomaticCreditLimitReviews() {
        try {
            // Confirm with user
            boolean confirmed = showConfirmationDialog("Run Automatic Reviews", 
                    "Are you sure you want to run automatic credit limit reviews for all accounts?",
                    "This will evaluate all credit accounts and may adjust credit limits based on account history and credit scores.");
            
            if (!confirmed) return;
            
            // Run reviews
            int adjustedCount = creditLimitService.runAutomaticCreditLimitReviews();
            
            // Update result label
            batchOperationResultLabel.setText("Completed. " + adjustedCount + " accounts adjusted.");
            
            // Refresh the view
            loadAccountDetails();
            refreshPendingRequests();
            
            showInformationAlert("Automatic Reviews Complete", 
                    "Automatic credit limit reviews completed",
                    adjustedCount + " accounts had their credit limits adjusted.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running automatic reviews", e);
            showErrorAlert("Error", "Failed to run automatic reviews", e.getMessage());
        }
    }
    
    /**
     * Refresh the list of pending requests.
     */
    @FXML
    public void refreshPendingRequests() {
        try {
            List<CreditLimitChangeRequest> pendingRequests = creditLimitService.getPendingCreditLimitChangeRequests();
            pendingRequestsTable.setItems(FXCollections.observableArrayList(pendingRequests));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing pending requests", e);
            showErrorAlert("Error", "Failed to refresh pending requests", e.getMessage());
        }
    }
    
    /**
     * Process a selected pending request.
     */
    @FXML
    public void processSelectedRequest() {
        CreditLimitChangeRequest request = pendingRequestsTable.getSelectionModel().getSelectedItem();
        if (request == null) return;
        
        // Show options dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Process Request");
        alert.setHeaderText("Process Credit Limit Change Request #" + request.getId());
        alert.setContentText("Choose action:");
        
        ButtonType approveButton = new ButtonType("Approve");
        ButtonType rejectButton = new ButtonType("Reject");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(approveButton, rejectButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == approveButton) {
            // Show dialog for comments
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Approve Change Request");
            dialog.setHeaderText("Approve credit limit change from " + 
                    CURRENCY_FORMAT.format(request.getCurrentCreditLimit()) + " to " + 
                    CURRENCY_FORMAT.format(request.getRequestedCreditLimit()));
            dialog.setContentText("Comments (optional):");
            
            Optional<String> commentsResult = dialog.showAndWait();
            
            if (commentsResult.isPresent()) {
                try {
                    String comments = commentsResult.get().trim();
                    CreditAccount updatedAccount = creditLimitService.approveCreditLimitChange(
                            request.getId(), 
                            SecurityContext.getCurrentUser().getUsername(), 
                            comments);
                    
                    if (updatedAccount != null) {
                        showInformationAlert("Success", "Credit limit change approved", 
                                "The new credit limit is " + CURRENCY_FORMAT.format(updatedAccount.getCreditLimit()));
                        
                        // Refresh pending requests
                        refreshPendingRequests();
                        
                        // Refresh main view if showing the same account
                        if (accountComboBox.getValue() != null && 
                                updatedAccount.getAccountNumber() == accountComboBox.getValue().getAccountNumber()) {
                            accountComboBox.setValue(updatedAccount);
                            loadAccountDetails();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error approving change request", e);
                    showErrorAlert("Error", "Failed to approve change request", e.getMessage());
                }
            }
        } else if (result.get() == rejectButton) {
            // Show dialog for rejection reason
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reject Change Request");
            dialog.setHeaderText("Reject credit limit change request");
            dialog.setContentText("Reason for rejection:");
            
            Optional<String> reasonResult = dialog.showAndWait();
            
            if (reasonResult.isPresent()) {
                String reason = reasonResult.get().trim();
                if (reason.isEmpty()) {
                    showErrorAlert("Error", "Rejection reason required", "You must provide a reason for rejection.");
                    return;
                }
                
                try {
                    boolean success = creditLimitService.rejectCreditLimitChange(
                            request.getId(), 
                            SecurityContext.getCurrentUser().getUsername(), 
                            reason);
                    
                    if (success) {
                        showInformationAlert("Success", "Credit limit change rejected", 
                                "The change request has been rejected.");
                        
                        // Refresh pending requests
                        refreshPendingRequests();
                        
                        // Refresh main view if showing the same account
                        if (accountComboBox.getValue() != null && 
                                request.getAccountNumber() == accountComboBox.getValue().getAccountNumber()) {
                            loadAccountDetails();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error rejecting change request", e);
                    showErrorAlert("Error", "Failed to reject change request", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Close the view.
     */
    @FXML
    public void close() {
        getStage().close();
    }
}