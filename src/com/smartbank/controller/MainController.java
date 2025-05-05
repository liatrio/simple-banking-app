package com.smartbank.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import com.smartbank.model.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the main view
 */
public class MainController implements Initializable {
    
    // UI components for accounts table
    @FXML private TableView<BankAccount> accountsTableView;
    @FXML private TableColumn<BankAccount, Integer> accountNumberColumn;
    @FXML private TableColumn<BankAccount, String> holderNameColumn;
    @FXML private TableColumn<BankAccount, String> accountTypeColumn;
    @FXML private TableColumn<BankAccount, Double> balanceColumn;
    
    // UI components for transactions table
    @FXML private ComboBox<BankAccount> accountSelector;
    @FXML private TableView<Transaction> transactionsTableView;
    @FXML private TableColumn<Transaction, LocalDateTime> transactionDateColumn;
    @FXML private TableColumn<Transaction, TransactionType> transactionTypeColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;
    
    // Other UI components
    @FXML private Label statusLabel;
    
    // Data models
    private final Bank bank = Bank.getInstance();
    private final ObservableList<BankAccount> accountsList = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize accounts table
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        holderNameColumn.setCellValueFactory(new PropertyValueFactory<>("holderName"));
        accountTypeColumn.setCellValueFactory(cellData -> {
            BankAccount account = cellData.getValue();
            String accountType = account.getClass().getSimpleName();
            return javafx.beans.binding.Bindings.createStringBinding(() -> accountType);
        });
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        accountsTableView.setItems(accountsList);
        
        // Initialize transactions table
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        transactionDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        transactionsTableView.setItems(transactionsList);
        
        // Initialize account selector
        accountSelector.setItems(accountsList);
        accountSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(BankAccount account) {
                if (account == null) {
                    return null;
                }
                return String.format("%d - %s", account.getAccountNumber(), account.getHolderName());
            }
            
            @Override
            public BankAccount fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        accountSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTransactions(newVal);
            }
        });
        
        // Load initial data
        refreshAccounts();
    }
    
    /**
     * Refreshes the accounts list
     */
    @FXML
    private void handleRefreshAccounts() {
        refreshAccounts();
        statusLabel.setText("Accounts refreshed");
    }
    
    /**
     * Refreshes the transactions list
     */
    @FXML
    private void handleRefreshTransactions() {
        BankAccount selectedAccount = accountSelector.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            loadTransactions(selectedAccount);
            statusLabel.setText("Transactions refreshed");
        } else {
            statusLabel.setText("No account selected");
        }
    }
    
    /**
     * Handles creating a new account
     */
    @FXML
    private void handleNewAccount() {
        Dialog<BankAccount> dialog = new Dialog<>();
        dialog.setTitle("Create New Account");
        dialog.setHeaderText("Enter account details");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField holderNameField = new TextField();
        holderNameField.setPromptText("Account Holder Name");
        
        TextField initialDepositField = new TextField();
        initialDepositField.setPromptText("Initial Deposit");
        
        ComboBox<String> accountTypeCombo = new ComboBox<>();
        accountTypeCombo.getItems().addAll("Savings Account", "Credit Account");
        accountTypeCombo.setValue("Savings Account");
        
        grid.add(new Label("Account Type:"), 0, 0);
        grid.add(accountTypeCombo, 1, 0);
        grid.add(new Label("Holder Name:"), 0, 1);
        grid.add(holderNameField, 1, 1);
        grid.add(new Label("Initial Deposit:"), 0, 2);
        grid.add(initialDepositField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to a BankAccount when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String holderName = holderNameField.getText();
                    double initialDeposit = Double.parseDouble(initialDepositField.getText());
                    
                    if (holderName.isEmpty()) {
                        throw new IllegalArgumentException("Holder name cannot be empty");
                    }
                    
                    if (initialDeposit < 0) {
                        throw new IllegalArgumentException("Initial deposit cannot be negative");
                    }
                    
                    if ("Savings Account".equals(accountTypeCombo.getValue())) {
                        return bank.createSavingsAccount(holderName, initialDeposit);
                    } else {
                        return bank.createCreditAccount(holderName, initialDeposit);
                    }
                } catch (NumberFormatException e) {
                    statusLabel.setText("Error: Invalid deposit amount");
                    return null;
                } catch (IllegalArgumentException e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<BankAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            refreshAccounts();
            statusLabel.setText("Account created successfully: " + account.getAccountNumber());
        });
    }
    
    /**
     * Handles depositing money into an account
     */
    @FXML
    private void handleDeposit() {
        BankAccount selectedAccount = accountsTableView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            statusLabel.setText("No account selected");
            return;
        }
        
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit to Account #" + selectedAccount.getAccountNumber());
        
        // Set the button types
        ButtonType depositButtonType = new ButtonType("Deposit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(depositButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to a Double when the deposit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == depositButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Error: Invalid amount");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                selectedAccount.deposit(amount);
                refreshAccounts();
                statusLabel.setText(String.format("Deposited $%.2f to account #%d", amount, selectedAccount.getAccountNumber()));
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handles withdrawing money from an account
     */
    @FXML
    private void handleWithdraw() {
        BankAccount selectedAccount = accountsTableView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            statusLabel.setText("No account selected");
            return;
        }
        
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw from Account #" + selectedAccount.getAccountNumber());
        
        // Set the button types
        ButtonType withdrawButtonType = new ButtonType("Withdraw", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(withdrawButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to a Double when the withdraw button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == withdrawButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Error: Invalid amount");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                selectedAccount.withdraw(amount);
                refreshAccounts();
                statusLabel.setText(String.format("Withdrew $%.2f from account #%d", amount, selectedAccount.getAccountNumber()));
            } catch (IllegalArgumentException | InsufficientBalanceException e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handles viewing account details
     */
    @FXML
    private void handleViewAccountDetails() {
        BankAccount selectedAccount = accountsTableView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            statusLabel.setText("No account selected");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Details");
        alert.setHeaderText("Account #" + selectedAccount.getAccountNumber());
        alert.setContentText(selectedAccount.displayAccountDetails());
        alert.showAndWait();
    }
    
    /**
     * Handles viewing transaction history
     */
    @FXML
    private void handleViewTransactionHistory() {
        BankAccount selectedAccount = accountsTableView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            statusLabel.setText("No account selected");
            return;
        }
        
        accountSelector.getSelectionModel().select(selectedAccount);
        TabPane tabPane = (TabPane) transactionsTableView.getScene().lookup("TabPane");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1); // Select the Transaction History tab
        }
    }
    
    /**
     * Refreshes the accounts list
     */
    private void refreshAccounts() {
        accountsList.clear();
        accountsList.addAll(bank.getAllAccounts());
    }
    
    /**
     * Loads transactions for the selected account
     * 
     * @param account The account to load transactions for
     */
    private void loadTransactions(BankAccount account) {
        transactionsList.clear();
        transactionsList.addAll(account.getTransactionHistory());
    }
}
