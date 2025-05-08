package com.smartbank.controller;

import com.smartbank.model.Transaction;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.TransactionService;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionHistoryController {
    private static final Logger LOGGER = Logger.getLogger(TransactionHistoryController.class.getName());
    
    @FXML private TableView<TransactionRecord> transactionTable;
    @FXML private TableColumn<TransactionRecord, String> colDate;
    @FXML private TableColumn<TransactionRecord, Long> colAccount;
    @FXML private TableColumn<TransactionRecord, String> colType;
    @FXML private TableColumn<TransactionRecord, Double> colAmount;
    @FXML private TableColumn<TransactionRecord, String> colDescription;
    @FXML private TableColumn<TransactionRecord, String> colMerchant;
    @FXML private TableColumn<TransactionRecord, String> colCategory;
    @FXML private ComboBox<AccountItem> accountComboBox;

    private ObservableList<TransactionRecord> transactions = FXCollections.observableArrayList();
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @FXML
    public void initialize() {
        LOGGER.info("Initializing TransactionHistoryController");
        
        // Initialize repository and service
        transactionRepository = RepositoryFactory.getTransactionRepository();
        transactionService = ServiceFactory.getTransactionService();
        
        if (transactionRepository == null) {
            LOGGER.severe("Failed to get TransactionRepository");
        }
        if (transactionService == null) {
            LOGGER.severe("Failed to get TransactionService");
        }
        
        // Setup table columns
        LOGGER.info("Setting up transaction table columns");
        try {
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colAccount.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
            colType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            
            // Format the amount column to show currency
            colAmount.setCellFactory(column -> new javafx.scene.control.TableCell<TransactionRecord, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance();
                        setText(currencyFormat.format(amount));
                    }
                }
            });
            
            // Setup additional columns if they exist
            if (colDescription != null) {
                colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
            }
            if (colMerchant != null) {
                colMerchant.setCellValueFactory(new PropertyValueFactory<>("merchantName"));
            }
            if (colCategory != null) {
                colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
            }
            
            // Set initial placeholder text
            transactionTable.setPlaceholder(new javafx.scene.control.Label("Select an account to view transactions"));
            
            // Set the transactions list to the table
            transactionTable.setItems(transactions);
            
            LOGGER.info("TransactionHistoryController initialization complete");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing transaction table: " + e.getMessage(), e);
        }
        
        // Try to load current user's accounts if we can get the current user
        try {
            LOGGER.info("Attempting to load current user accounts");
            com.smartbank.auth.SecurityContext securityContext = com.smartbank.auth.SecurityContext.getInstance();
            if (securityContext.isAuthenticated()) {
                String currentUsername = securityContext.getCurrentSession().getUser().getUsername();
                LOGGER.info("Found authenticated user: " + currentUsername);
                loadUserAccounts(currentUsername);
            } else {
                LOGGER.info("No authenticated user found during initialization");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not automatically load user accounts: " + e.getMessage(), e);
        }
    }

    // Load transactions from the database for a given account number
    public ObservableList<TransactionRecord> getTransactionsForAccount(long accountNumber) {
        ObservableList<TransactionRecord> txList = FXCollections.observableArrayList();
        
        if (accountNumber <= 0) {
            LOGGER.warning("Invalid account number: " + accountNumber);
            return txList;
        }
        
        LOGGER.info("Loading transactions for account: " + accountNumber);
        
        try {
            // First try JPA repository
            LOGGER.info("Attempting to load transactions using JPA repository");
            List<Transaction> transactionList = transactionRepository.findByAccountNumber(accountNumber);
            
            if (transactionList != null) {
                LOGGER.info("JPA repository returned " + transactionList.size() + " transactions");
                
                if (!transactionList.isEmpty()) {
                    for (Transaction tx : transactionList) {
                        String date = tx.getTimestamp().toString();
                        long accNum = tx.getAccountNumber();
                        String type = tx.getType().toString();
                        double amount = tx.getAmount();
                        String description = tx.getDescription();
                        String merchantName = tx.getMerchantName();
                        
                        // Get category name if available
                        String category = "";
                        if (tx.getCategory() != null) {
                            category = tx.getCategory().getName();
                        }
                        
                        txList.add(new TransactionRecord(accNum, type, amount, date, description, merchantName, category));
                    }
                } else {
                    LOGGER.info("JPA repository returned empty transaction list, trying direct SQL");
                    // Fall back to direct SQL if JPA returns empty results
                    loadTransactionsViaSQL(accountNumber, txList);
                }
            } else {
                LOGGER.info("JPA repository returned null, trying direct SQL");
                // Fall back to direct SQL if JPA returns null
                loadTransactionsViaSQL(accountNumber, txList);
            }
            
            // Log the number of transactions found
            LOGGER.info("Found " + txList.size() + " transactions for account " + accountNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading transactions for account: " + e.getMessage(), e);
            e.printStackTrace();
            
            // Try direct SQL as last resort
            if (txList.isEmpty()) {
                LOGGER.info("Trying direct SQL as fallback after JPA exception");
                loadTransactionsViaSQL(accountNumber, txList);
            }
        }
        
        if (txList.isEmpty()) {
            LOGGER.warning("No transactions found for account " + accountNumber);
        }
        
        return txList;
    }
    
    // Helper method to load transactions via direct SQL
    private void loadTransactionsViaSQL(long accountNumber, ObservableList<TransactionRecord> txList) {
        try {
            LOGGER.info("Loading transactions via direct SQL for account: " + accountNumber);
            java.sql.ResultSet rs = com.smartbank.util.DatabaseManager.getTransactionsForAccount(accountNumber);
            if (rs != null) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    long accNum = rs.getLong("accountNumber");
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String date = rs.getString("timestamp");
                    String description = rs.getString("description");
                    String merchantName = rs.getString("merchantName");
                    String category = ""; // Category would need a separate lookup
                    
                    txList.add(new TransactionRecord(accNum, type, amount, date, description, merchantName, category));
                }
                rs.close();
                LOGGER.info("SQL query found " + count + " transactions");
            } else {
                LOGGER.warning("SQL query returned null ResultSet");
            }
        } catch (Exception sqlEx) {
            LOGGER.log(Level.SEVERE, "Error loading transactions from direct SQL: " + sqlEx.getMessage(), sqlEx);
            sqlEx.printStackTrace();
        }
    }

    // Show transactions for a given account (for UI wiring)
    public void showTransactionsForAccount(long accountNumber) {
        LOGGER.info("Showing transactions for account: " + accountNumber);
        
        // Validate account number
        if (accountNumber <= 0) {
            LOGGER.warning("Invalid account number: " + accountNumber);
            transactions.clear();
            transactionTable.setItems(transactions);
            return;
        }
        
        // Check if the account is in the combo box
        boolean accountFound = false;
        for (AccountItem item : accountComboBox.getItems()) {
            if (item.getAccountNumber() == accountNumber) {
                accountFound = true;
                // Make sure this account is selected in the combo box
                AccountItem currentSelection = accountComboBox.getSelectionModel().getSelectedItem();
                if (currentSelection == null || currentSelection.getAccountNumber() != accountNumber) {
                    LOGGER.info("Setting combo box selection to account: " + accountNumber);
                    accountComboBox.getSelectionModel().select(item);
                }
                break;
            }
        }
        
        if (!accountFound) {
            LOGGER.warning("Account " + accountNumber + " not found in the combo box items");
        }
        
        // Get and display transactions
        ObservableList<TransactionRecord> accountTransactions = getTransactionsForAccount(accountNumber);
        
        // Update the table with transactions
        LOGGER.info("Setting table items with " + accountTransactions.size() + " transactions");
        transactions.clear();
        transactions.addAll(accountTransactions);
        transactionTable.setItems(transactions);
        
        // Add a label to the table if no transactions were found
        if (accountTransactions.isEmpty()) {
            LOGGER.warning("No transactions found for account: " + accountNumber);
            // Setup a placeholder message for when there are no transactions
            transactionTable.setPlaceholder(new javafx.scene.control.Label("No transactions found for this account"));
        } else {
            LOGGER.info("Successfully displaying " + accountTransactions.size() + " transactions");
            // Reset placeholder to default
            transactionTable.setPlaceholder(new javafx.scene.control.Label("No transactions"));
        }
    }
    
    @FXML
    private void handleAccountSelection(ActionEvent event) {
        AccountItem selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            long accountNumber = selectedAccount.getAccountNumber();
            LOGGER.info("Account selected from dropdown: " + accountNumber);
            showTransactionsForAccount(accountNumber);
        } else {
            LOGGER.warning("No account selected in dropdown");
            transactions.clear();
            transactionTable.setItems(transactions);
        }
    }
    
    // Set up the account dropdown with all accounts for current user
    public void loadUserAccounts(String username) {
        if (username == null || username.isEmpty()) {
            LOGGER.warning("Invalid username provided to loadUserAccounts: " + username);
            return;
        }
        
        LOGGER.info("Loading accounts for user: " + username);
        ObservableList<AccountItem> accountItems = FXCollections.observableArrayList();
        
        try {
            // Get userId from username
            String userId = null;
            try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                 java.sql.PreparedStatement userStmt = conn.prepareStatement("SELECT userId FROM users WHERE username = ?")) {
                userStmt.setString(1, username);
                java.sql.ResultSet userRs = userStmt.executeQuery();
                if (userRs.next()) {
                    userId = userRs.getString("userId");
                    LOGGER.info("Found userId for " + username + ": " + userId);
                } else {
                    LOGGER.warning("No userId found for username: " + username);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error querying userId: " + e.getMessage(), e);
            }
            
            if (userId != null) {
                // Get all accounts for this user
                try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT accountNumber, type, balance FROM accounts WHERE userId = ?")) {
                    stmt.setString(1, userId);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        long accountNumber = rs.getLong("accountNumber");
                        String type = rs.getString("type");
                        double balance = rs.getDouble("balance");
                        accountItems.add(new AccountItem(accountNumber, type, balance));
                        LOGGER.info("Added account: " + accountNumber + " (" + type + ") with balance: " + balance);
                    }
                    LOGGER.info("Found " + count + " accounts for user " + username);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error querying user accounts: " + e.getMessage(), e);
                }
            }
            
            if (accountItems.isEmpty()) {
                LOGGER.warning("No accounts found for user: " + username);
            }
            
            // Set items to the combo box
            accountComboBox.setItems(accountItems);
            
            // Select the first account if available
            if (!accountItems.isEmpty()) {
                AccountItem firstAccount = accountItems.get(0);
                LOGGER.info("Selecting first account: " + firstAccount.getAccountNumber());
                accountComboBox.getSelectionModel().selectFirst();
                showTransactionsForAccount(firstAccount.getAccountNumber());
            } else {
                LOGGER.warning("No accounts to select for user: " + username);
                // Clear any existing transactions when no accounts found
                transactions.clear();
                transactionTable.setItems(transactions);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user accounts: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    // Item class for account combo box
    public static class AccountItem {
        private final long accountNumber;
        private final String type;
        private final double balance;
        
        public AccountItem(long accountNumber, String type, double balance) {
            this.accountNumber = accountNumber;
            this.type = type;
            this.balance = balance;
        }
        
        public long getAccountNumber() { return accountNumber; }
        public String getType() { return type; }
        public double getBalance() { return balance; }
        
        @Override
        public String toString() {
            return String.format("%s - %d (%.2f)", 
                       type.substring(0, 1).toUpperCase() + type.substring(1),
                       accountNumber, 
                       balance);
        }
    }

    public static class TransactionRecord {
        private final String date;
        private final long accountNumber;
        private final String type;
        private final double amount;
        private final String description;
        private final String merchantName;
        private final String category;
        
        public TransactionRecord(long accountNumber, String type, double amount, String date) {
            this(accountNumber, type, amount, date, "", "", "");
        }
        
        public TransactionRecord(long accountNumber, String type, double amount, String date, 
                                 String description, String merchantName, String category) {
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount;
            this.date = date;
            this.description = description;
            this.merchantName = merchantName;
            this.category = category;
        }
        
        public String getDate() { return date; }
        public long getAccountNumber() { return accountNumber; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
        public String getMerchantName() { return merchantName; }
        public String getCategory() { return category; }
    }
}

