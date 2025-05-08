package com.smartbank.controller;

import com.smartbank.model.Account;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.SavingsAccount;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.service.transfer.*;
import com.smartbank.controller.TransactionHistoryController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the account transfer view.
 */
public class TransferController {
    private static final Logger LOGGER = Logger.getLogger(TransferController.class.getName());
    
    // FXML Controls
    @FXML private ComboBox<Account> cmbFromAccount;
    @FXML private ComboBox<Account> cmbToAccount;
    @FXML private Button btnBrowseAccounts;
    @FXML private Button btnExternalAccount;
    @FXML private HBox hboxExternalAccount;
    @FXML private TextField txtExternalAccount;
    @FXML private Label lblAvailableBalance;
    @FXML private TextField txtAmount;
    @FXML private TextArea txtDescription;
    @FXML private HBox hboxFee;
    @FXML private Label lblFee;
    @FXML private HBox hboxTotal;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblDailyLimit;
    @FXML private Label lblRemainingLimit;
    @FXML private Text txtErrorMessage;
    @FXML private Button btnValidate;
    @FXML private Button btnTransfer;
    @FXML private Button btnCancel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox chkScheduleTransfer;
    @FXML private HBox hboxScheduleDate;
    @FXML private DatePicker dateSchedule;
    
    // Services and repositories
    private final TransferService transferService;
    private final AccountRepository accountRepository;
    
    // State
    private boolean isExternal = false;
    private boolean isValidated = false;
    private double currentFee = 0.0;
    
    // Formatters
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor.
     */
    public TransferController() {
        transferService = new TransferServiceImpl();
        accountRepository = RepositoryFactory.getAccountRepository();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Setup account combos
        setupAccountComboBoxes();
        
        // Setup listeners
        setupListeners();
        
        // Set initial values
        resetForm();
        
        // Set today's date as minimum date for scheduled transfers
        dateSchedule.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });
        dateSchedule.setValue(LocalDate.now());
    }
    
    /**
     * Setup account combo boxes.
     */
    private void setupAccountComboBoxes() {
        // Setup account combo box cell factories
        StringConverter<Account> accountConverter = new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                if (account == null) {
                    return "";
                }
                String accountType = account instanceof SavingsAccount ? "Savings" : 
                                    account instanceof CreditAccount ? "Credit" : "Account";
                return accountType + " - " + account.getAccountNumber() + 
                       " (" + currencyFormat.format(account.getBalance()) + ")";
            }
            
            @Override
            public Account fromString(String string) {
                return null; // Not needed for combo box
            }
        };
        
        cmbFromAccount.setConverter(accountConverter);
        cmbToAccount.setConverter(accountConverter);
        
        // Load accounts
        loadAccounts();
    }
    
    /**
     * Setup listeners for input fields.
     */
    private void setupListeners() {
        // From account changes
        cmbFromAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateAvailableBalance();
            updateTransferLimits();
            clearValidation();
        });
        
        // To account changes
        cmbToAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            clearValidation();
        });
        
        // External account changes
        txtExternalAccount.textProperty().addListener((obs, oldVal, newVal) -> {
            clearValidation();
        });
        
        // Amount changes
        txtAmount.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double amount = Double.parseDouble(newVal);
                updateFeeAndTotal();
            } catch (NumberFormatException e) {
                // Invalid amount, leave fee and total as is
            }
            clearValidation();
        });
        
        // Description changes
        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            // No need to clear validation for description changes
        });
    }
    
    /**
     * Load accounts from the repository.
     */
    private void loadAccounts() {
        try {
            // Get current user
            String currentUsername = com.smartbank.auth.SecurityContext.getInstance().getCurrentSession().getUser().getUsername();
            
            // Get accounts for current user specifically
            List<Account> accounts = accountRepository.findByUser(com.smartbank.auth.SecurityContext.getInstance().getCurrentUser());
            
            LOGGER.info("Found " + accounts.size() + " accounts for user: " + currentUsername);
            
            cmbFromAccount.getItems().clear();
            cmbToAccount.getItems().clear();
            
            cmbFromAccount.getItems().addAll(accounts);
            cmbToAccount.getItems().addAll(accounts);
            
            if (!accounts.isEmpty()) {
                cmbFromAccount.getSelectionModel().select(0);
                // Select different account for "to" if possible
                if (accounts.size() > 1) {
                    cmbToAccount.getSelectionModel().select(1);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading accounts: " + e.getMessage(), e);
            showError("Error loading accounts: " + e.getMessage());
        }
    }
    
    /**
     * Handle browse accounts button click.
     */
    @FXML
    private void handleBrowseAccounts(ActionEvent event) {
        // TODO: Show account browser dialog
        // For now, we just use the combo box
    }
    
    /**
     * Handle external account button click.
     */
    @FXML
    private void handleExternalAccount(ActionEvent event) {
        isExternal = !isExternal;
        
        if (isExternal) {
            btnExternalAccount.setText("Select");
            cmbToAccount.setDisable(true);
            btnBrowseAccounts.setDisable(true);
            hboxExternalAccount.setVisible(true);
            hboxExternalAccount.setManaged(true);
            txtExternalAccount.requestFocus();
        } else {
            btnExternalAccount.setText("External");
            cmbToAccount.setDisable(false);
            btnBrowseAccounts.setDisable(false);
            hboxExternalAccount.setVisible(false);
            hboxExternalAccount.setManaged(false);
        }
        
        clearValidation();
    }
    
    /**
     * Handle schedule transfer checkbox.
     */
    @FXML
    private void handleScheduleTransfer(ActionEvent event) {
        boolean isScheduled = chkScheduleTransfer.isSelected();
        
        hboxScheduleDate.setVisible(isScheduled);
        hboxScheduleDate.setManaged(isScheduled);
        
        if (isScheduled) {
            btnTransfer.setText("Schedule Transfer");
        } else {
            btnTransfer.setText("Transfer Now");
        }
        
        clearValidation();
    }
    
    /**
     * Handle validate button click.
     */
    @FXML
    private void handleValidate(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        
        Account fromAccount = cmbFromAccount.getValue();
        long sourceAccountNumber = fromAccount.getAccountNumber();
        long targetAccountNumber = getTargetAccountNumber();
        double amount = getAmount();
        
        // Validate the transfer
        try {
            ValidationResult validationResult = transferService.validateTransfer(
                    sourceAccountNumber, targetAccountNumber, amount);
            
            if (validationResult.isValid()) {
                showMessage(validationResult.getFirstMessage("Transfer is valid"), false);
                isValidated = true;
                btnTransfer.setDisable(false);
            } else {
                showError(validationResult.getFirstMessage("Transfer validation failed"));
                isValidated = false;
                btnTransfer.setDisable(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating transfer: " + e.getMessage(), e);
            showError("Error validating transfer: " + e.getMessage());
            isValidated = false;
            btnTransfer.setDisable(true);
        }
    }
    
    /**
     * Handle transfer button click.
     */
    @FXML
    private void handleTransfer(ActionEvent event) {
        if (!isValidated && !validateInput()) {
            return;
        }
        
        Account fromAccount = cmbFromAccount.getValue();
        long sourceAccountNumber = fromAccount.getAccountNumber();
        long targetAccountNumber = getTargetAccountNumber();
        double amount = getAmount();
        String description = txtDescription.getText();
        
        // Disable controls during transfer
        setLoading(true);
        
        // Perform transfer in background thread
        new Thread(() -> {
            try {
                if (chkScheduleTransfer.isSelected()) {
                    // Schedule transfer
                    LocalDate localDate = dateSchedule.getValue();
                    Date scheduledDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    
                    ScheduledTransfer scheduledTransfer = transferService.scheduleTransfer(
                            sourceAccountNumber, targetAccountNumber, amount, description, scheduledDate);
                    
                    Platform.runLater(() -> {
                        showSuccessMessage("Transfer scheduled successfully for " + localDate);
                        resetForm();
                        setLoading(false);
                        showTransferHistory();
                    });
                } else {
                    // Perform immediate transfer
                    TransferResult result = transferService.transfer(
                            sourceAccountNumber, targetAccountNumber, amount, description);
                    
                    Platform.runLater(() -> {
                        showSuccessMessage("Transfer completed successfully");
                        loadAccounts(); // Reload accounts to refresh balances
                        resetForm();
                        setLoading(false);
                        showTransferHistory();
                    });
                }
            } catch (TransferException e) {
                Platform.runLater(() -> {
                    showError("Transfer failed: " + e.getMessage());
                    setLoading(false);
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during transfer: " + e.getMessage(), e);
                Platform.runLater(() -> {
                    showError("Error during transfer: " + e.getMessage());
                    setLoading(false);
                });
            }
        }).start();
    }
    
    /**
     * Handle cancel button click.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        resetForm();
    }
    
    /**
     * Update available balance display.
     */
    private void updateAvailableBalance() {
        Account selectedAccount = cmbFromAccount.getValue();
        if (selectedAccount != null) {
            double balance = selectedAccount.getBalance();
            
            // For credit accounts, show available credit too
            if (selectedAccount instanceof CreditAccount) {
                CreditAccount creditAccount = (CreditAccount) selectedAccount;
                double creditLimit = creditAccount.getCreditLimit();
                lblAvailableBalance.setText(currencyFormat.format(balance) + 
                        " (+" + currencyFormat.format(creditLimit) + " credit)");
            } else {
                lblAvailableBalance.setText(currencyFormat.format(balance));
            }
        } else {
            lblAvailableBalance.setText("$0.00");
        }
    }
    
    /**
     * Update transfer limits display.
     */
    private void updateTransferLimits() {
        Account selectedAccount = cmbFromAccount.getValue();
        if (selectedAccount != null) {
            long accountNumber = selectedAccount.getAccountNumber();
            double dailyLimit = transferService.getDailyTransferLimit(accountNumber);
            double remainingLimit = transferService.getRemainingDailyTransferAmount(accountNumber);
            
            lblDailyLimit.setText(currencyFormat.format(dailyLimit));
            lblRemainingLimit.setText(currencyFormat.format(remainingLimit));
        } else {
            lblDailyLimit.setText("$0.00");
            lblRemainingLimit.setText("$0.00");
        }
    }
    
    /**
     * Update fee and total display.
     */
    private void updateFeeAndTotal() {
        Account fromAccount = cmbFromAccount.getValue();
        if (fromAccount == null) {
            return;
        }
        
        try {
            double amount = getAmount();
            long targetAccountNumber = getTargetAccountNumber();
            
            if (amount > 0 && targetAccountNumber > 0) {
                currentFee = transferService.calculateTransferFee(
                        fromAccount.getAccountNumber(), targetAccountNumber, amount);
                
                lblFee.setText(currencyFormat.format(currentFee));
                lblTotalAmount.setText(currencyFormat.format(amount + currentFee));
            } else {
                currentFee = 0.0;
                lblFee.setText("$0.00");
                lblTotalAmount.setText("$0.00");
            }
        } catch (Exception e) {
            currentFee = 0.0;
            lblFee.setText("$0.00");
            lblTotalAmount.setText("$0.00");
        }
    }
    
    /**
     * Get the target account number based on UI state.
     * 
     * @return The target account number, or -1 if invalid
     */
    private long getTargetAccountNumber() {
        if (isExternal) {
            try {
                return Long.parseLong(txtExternalAccount.getText().trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            Account toAccount = cmbToAccount.getValue();
            return toAccount != null ? toAccount.getAccountNumber() : -1;
        }
    }
    
    /**
     * Get the transfer amount from the UI.
     * 
     * @return The transfer amount, or -1 if invalid
     */
    private double getAmount() {
        try {
            return Double.parseDouble(txtAmount.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Validate form input.
     * 
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        // Check from account
        if (cmbFromAccount.getValue() == null) {
            showError("Please select the source account");
            cmbFromAccount.requestFocus();
            return false;
        }
        
        // Check to account
        if (isExternal) {
            if (txtExternalAccount.getText().trim().isEmpty()) {
                showError("Please enter the target account number");
                txtExternalAccount.requestFocus();
                return false;
            }
            
            try {
                long targetAccountNumber = Long.parseLong(txtExternalAccount.getText().trim());
                if (targetAccountNumber <= 0) {
                    showError("Invalid account number");
                    txtExternalAccount.requestFocus();
                    return false;
                }
                
                if (targetAccountNumber == cmbFromAccount.getValue().getAccountNumber()) {
                    showError("Source and target accounts cannot be the same");
                    txtExternalAccount.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Invalid account number");
                txtExternalAccount.requestFocus();
                return false;
            }
        } else {
            if (cmbToAccount.getValue() == null) {
                showError("Please select the target account");
                cmbToAccount.requestFocus();
                return false;
            }
            
            if (cmbFromAccount.getValue().getAccountNumber() == cmbToAccount.getValue().getAccountNumber()) {
                showError("Source and target accounts cannot be the same");
                cmbToAccount.requestFocus();
                return false;
            }
        }
        
        // Check amount
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                showError("Amount must be positive");
                txtAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount");
            txtAmount.requestFocus();
            return false;
        }
        
        // Check scheduled date
        if (chkScheduleTransfer.isSelected()) {
            if (dateSchedule.getValue() == null) {
                showError("Please select a date for the scheduled transfer");
                dateSchedule.requestFocus();
                return false;
            }
            
            if (dateSchedule.getValue().isBefore(LocalDate.now())) {
                showError("Scheduled date must be in the future");
                dateSchedule.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Reset the form to its initial state.
     */
    private void resetForm() {
        // Reset to account selection
        isExternal = false;
        btnExternalAccount.setText("External");
        cmbToAccount.setDisable(false);
        btnBrowseAccounts.setDisable(false);
        hboxExternalAccount.setVisible(false);
        hboxExternalAccount.setManaged(false);
        
        // Reset scheduled transfer
        chkScheduleTransfer.setSelected(false);
        hboxScheduleDate.setVisible(false);
        hboxScheduleDate.setManaged(false);
        dateSchedule.setValue(LocalDate.now());
        btnTransfer.setText("Transfer Now");
        
        // Clear input fields
        if (!cmbFromAccount.getItems().isEmpty() && cmbFromAccount.getValue() == null) {
            cmbFromAccount.getSelectionModel().select(0);
        }
        
        if (!cmbToAccount.getItems().isEmpty()) {
            // Select a different account than from account
            Account fromAccount = cmbFromAccount.getValue();
            for (Account account : cmbToAccount.getItems()) {
                if (fromAccount == null || account.getAccountNumber() != fromAccount.getAccountNumber()) {
                    cmbToAccount.getSelectionModel().select(account);
                    break;
                }
            }
        }
        
        txtExternalAccount.clear();
        txtAmount.clear();
        txtDescription.clear();
        
        // Reset validation
        clearValidation();
        
        // Update displays
        updateAvailableBalance();
        updateTransferLimits();
        updateFeeAndTotal();
    }
    
    /**
     * Clear validation state.
     */
    private void clearValidation() {
        isValidated = false;
        btnTransfer.setDisable(true);
        hideError();
    }
    
    /**
     * Show an error message.
     * 
     * @param message The error message
     */
    private void showError(String message) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setStyle("-fx-fill: red;");
        txtErrorMessage.setVisible(true);
    }
    
    /**
     * Show a success message.
     * 
     * @param message The success message
     */
    private void showSuccessMessage(String message) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setStyle("-fx-fill: green;");
        txtErrorMessage.setVisible(true);
    }
    
    /**
     * Show a message.
     * 
     * @param message The message
     * @param isError Whether the message is an error
     */
    private void showMessage(String message, boolean isError) {
        txtErrorMessage.setText(message);
        txtErrorMessage.setStyle(isError ? "-fx-fill: red;" : "-fx-fill: green;");
        txtErrorMessage.setVisible(true);
    }
    
    /**
     * Hide the error message.
     */
    private void hideError() {
        txtErrorMessage.setVisible(false);
    }
    
    /**
     * Set the loading state of the form.
     * 
     * @param loading Whether the form is loading
     */
    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        
        cmbFromAccount.setDisable(loading);
        cmbToAccount.setDisable(loading || isExternal);
        btnBrowseAccounts.setDisable(loading || isExternal);
        btnExternalAccount.setDisable(loading);
        txtExternalAccount.setDisable(loading);
        txtAmount.setDisable(loading);
        txtDescription.setDisable(loading);
        chkScheduleTransfer.setDisable(loading);
        dateSchedule.setDisable(loading);
        
        btnValidate.setDisable(loading);
        btnTransfer.setDisable(loading || !isValidated);
        btnCancel.setDisable(loading);
    }
    
    /**
     * Show transfer history after a successful transfer.
     * Redirects to the transaction history view with filter for transfers.
     */
    private void showTransferHistory() {
        try {
            LOGGER.info("Showing transfer history after successful transfer");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/TransactionHistoryView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up with the from account data
            TransactionHistoryController controller = loader.getController();
            if (controller != null) {
                LOGGER.info("Got TransactionHistoryController, initializing with current user data");
                
                // Get current user
                try {
                    com.smartbank.auth.SecurityContext securityContext = com.smartbank.auth.SecurityContext.getInstance();
                    if (securityContext.isAuthenticated()) {
                        String currentUsername = securityContext.getCurrentSession().getUser().getUsername();
                        LOGGER.info("Current username: " + currentUsername);
                        
                        // Set up accounts for current user
                        controller.loadUserAccounts(currentUsername);
                        
                        // If we have a from account selected, show transactions for that account
                        Account fromAccount = cmbFromAccount.getValue();
                        if (fromAccount != null) {
                            long accountNumber = fromAccount.getAccountNumber();
                            LOGGER.info("Setting initial account to: " + accountNumber);
                            controller.showTransactionsForAccount(accountNumber);
                        }
                    } else {
                        LOGGER.warning("No authenticated user found when showing transfer history");
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error getting current user info: " + ex.getMessage(), ex);
                }
            } else {
                LOGGER.warning("Could not get TransactionHistoryController");
            }
            
            // Change scene to show history
            Scene scene = txtAmount.getScene();
            if (scene != null) {
                StackPane contentArea = (StackPane) scene.lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(root);
                } else {
                    LOGGER.warning("Could not find #contentArea in scene");
                }
            } else {
                LOGGER.warning("No scene found for txtAmount control");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing transfer history: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
}