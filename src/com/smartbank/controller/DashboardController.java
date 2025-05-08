package com.smartbank.controller;

import com.smartbank.auth.SecurityContext;
import com.smartbank.model.Account;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.model.User;
import com.smartbank.service.AccountService;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.TransactionService;
import com.smartbank.service.category.CategoryService;
import com.smartbank.service.reporting.CategoryReportService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController extends BaseController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label totalBalanceLabel;
    
    @FXML
    private GridPane accountSummaryGrid;
    
    @FXML
    private TableView<Transaction> recentTransactionsTable;
    
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    
    @FXML
    private TableColumn<Transaction, String> amountColumn;
    
    @FXML
    private TableColumn<Transaction, String> accountColumn;
    
    @FXML
    private PieChart spendingByCategoryChart;
    
    @FXML
    private Button newTransactionButton;
    
    @FXML
    private Button transferButton;
    
    @FXML
    private Button accountsButton;
    
    @FXML
    private Button refreshButton;
    
    private User currentUser;
    private AccountService accountService;
    private TransactionService transactionService;
    private CategoryService categoryService;
    private CategoryReportService reportService;
    
    @FXML
    public void initialize() {
        // Check authentication
        if (!checkAuthentication(null)) {
            return;
        }

        // Initialize services
        accountService = ServiceFactory.getAccountService();
        transactionService = ServiceFactory.getTransactionService();
        categoryService = ServiceFactory.getCategoryService();
        reportService = ServiceFactory.getCategoryReportService();
        
        // Initialize default categories if they don't exist yet
        categoryService.initializeDefaultCategories();
        
        // Get current user from security context
        currentUser = SecurityContext.getInstance().getCurrentUser();
        
        // Set welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
        
        // Setup table columns
        setupTableColumns();
        
        // Load dashboard data
        loadDashboardData();
        
        // Setup button actions
        setupButtonActions();
    }
    
    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            return new SimpleStringProperty(dateFormat.format(cellData.getValue().getTimestamp()));
        });
        
        // Description column
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Amount column
        amountColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            double amount = cellData.getValue().getSignedAmount();
            return new SimpleStringProperty(currencyFormat.format(amount));
        });
        
        // Account column
        accountColumn.setCellValueFactory(cellData -> {
            long accountNumber = cellData.getValue().getAccountNumber();
            Optional<Account> accountOpt = accountService.getAccountByNumber(accountNumber);
            return new SimpleStringProperty(accountOpt.isPresent() ? 
                accountOpt.get().getAccountNumber() + " (" + getAccountTypeString(accountOpt.get()) + ")" : 
                String.valueOf(accountNumber));
        });
    }
    
    private String getAccountTypeString(Account account) {
        if (account instanceof SavingsAccount) {
            return "Savings";
        } else if (account instanceof CreditAccount) {
            return "Credit";
        } else {
            return "Account";
        }
    }
    
    private void loadDashboardData() {
        // Load account summaries
        loadAccountSummaries();
        
        // Load recent transactions
        loadRecentTransactions();
        
        // Load spending by category
        loadSpendingByCategory();
    }
    
    private void loadAccountSummaries() {
        List<Account> userAccounts = accountService.getAccountsByUsername(currentUser.getUsername());
        
        double totalBalance = 0.0;
        accountSummaryGrid.getChildren().clear();
        
        int row = 0;
        for (Account account : userAccounts) {
            // Create account summary card
            VBox accountCard = createAccountSummaryCard(account);
            
            // Add to grid
            accountSummaryGrid.add(accountCard, row % 2, row / 2);
            row++;
            
            // Update total balance (subtract for credit accounts)
            if (account instanceof CreditAccount) {
                totalBalance -= account.getBalance();
            } else {
                totalBalance += account.getBalance();
            }
        }
        
        // Update total balance label
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        totalBalanceLabel.setText("Total Balance: " + currencyFormat.format(totalBalance));
    }
    
    private VBox createAccountSummaryCard(Account account) {
        VBox card = new VBox(5);
        card.getStyleClass().add("account-card");
        
        // Account name/number
        Label accountLabel = new Label(getAccountTypeString(account) + " #" + account.getAccountNumber());
        accountLabel.getStyleClass().add("account-title");
        
        // Account balance
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        Label balanceLabel = new Label(currencyFormat.format(account.getBalance()));
        balanceLabel.getStyleClass().add("account-balance");
        
        // Account last activity
        Transaction lastTransaction = getLastTransaction(account.getAccountNumber());
        Label lastActivityLabel = new Label("Last activity: " + 
            (lastTransaction != null ? new SimpleDateFormat("MM/dd/yyyy").format(lastTransaction.getTimestamp()) : "None"));
        
        // Add components to card
        card.getChildren().addAll(accountLabel, balanceLabel, lastActivityLabel);
        
        return card;
    }
    
    private Transaction getLastTransaction(long accountNumber) {
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountNumber);
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }
        
        // Find most recent transaction
        return transactions.stream()
                .max((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()))
                .orElse(null);
    }
    
    private void loadRecentTransactions() {
        // Get all user accounts
        List<Account> userAccounts = accountService.getAccountsByUsername(currentUser.getUsername());
        
        // Get recent transactions (last 30 days) for all accounts
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = calendar.getTime();
        
        List<Transaction> allRecentTransactions = userAccounts.stream()
            .flatMap(account -> transactionService.getTransactionsByAccountAndDateRange(
                account.getAccountNumber(), startDate, endDate).stream())
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Sort by date descending
            .limit(10) // Show only 10 most recent
            .collect(Collectors.toList());
        
        // Update table
        recentTransactionsTable.setItems(FXCollections.observableArrayList(allRecentTransactions));
    }
    
    private void loadSpendingByCategory() {
        // Get all user accounts
        List<Account> userAccounts = accountService.getAccountsByUsername(currentUser.getUsername());
        
        // Get spending data for last month
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);
        Date startDate = calendar.getTime();
        
        // Ensure we have categories
        List<TransactionCategory> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            categories = categoryService.initializeDefaultCategories();
            System.out.println("Initialized " + categories.size() + " default categories");
        }
        
        // Aggregate spending by category across all accounts
        Map<String, Double> categorySpending = new HashMap<>();
        boolean hasTransactionData = false;
        
        for (Account account : userAccounts) {
            try {
                // Get transactions for this account
                List<Transaction> transactions = transactionService.getTransactionsByAccountAndDateRange(
                    account.getAccountNumber(), startDate, endDate);
                
                if (transactions != null && !transactions.isEmpty()) {
                    hasTransactionData = true;
                    
                    // Process each transaction
                    for (Transaction tx : transactions) {
                        if (tx.isSpending() && tx.getCategory() != null) {
                            String categoryName = tx.getCategory().getName();
                            double amount = tx.getSignedAmount(); // Negative for spending
                            
                            categorySpending.put(categoryName, 
                                categorySpending.getOrDefault(categoryName, 0.0) + amount);
                        }
                    }
                }
                
                // Also try the report service
                Map<Long, CategoryReportService.CategorySpending> accountSpending = 
                    reportService.getSpendingByCategory(account.getAccountNumber(), startDate, endDate);
                
                if (accountSpending != null && !accountSpending.isEmpty()) {
                    hasTransactionData = true;
                    
                    // Merge with aggregated data
                    for (Map.Entry<Long, CategoryReportService.CategorySpending> entry : accountSpending.entrySet()) {
                        String categoryName = entry.getValue().getCategoryName();
                        double amount = entry.getValue().getAmount();
                        
                        categorySpending.put(categoryName, 
                            categorySpending.getOrDefault(categoryName, 0.0) + amount);
                    }
                }
            } catch (Exception e) {
                // Handle case where no categories exist yet
                System.err.println("Error loading category data: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Create chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            // For spending chart, we want to display the absolute value (positive numbers)
            // The sign in the data is negative for spending
            double absValue = Math.abs(entry.getValue());
            if (absValue > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), absValue));
            }
        }
        
        // Update chart
        spendingByCategoryChart.setData(pieChartData);
        spendingByCategoryChart.setTitle("Spending by Category");
        
        // If no data, add a placeholder with appropriate message
        if (pieChartData.isEmpty()) {
            if (hasTransactionData) {
                pieChartData.add(new PieChart.Data("No categorized spending", 1));
                spendingByCategoryChart.setTitle("No categorized spending in the last month");
            } else {
                pieChartData.add(new PieChart.Data("No transaction data", 1));
                spendingByCategoryChart.setTitle("No transactions in the last month");
            }
        }
    }
    
    private void setupButtonActions() {
        newTransactionButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/TransactionFormView.fxml"));
                Node view = loader.load();
                ((javafx.scene.layout.Pane) getStage().getScene().getRoot().lookup("#contentArea")).getChildren().setAll(view);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Unable to load Transaction Form view");
            }
        });
        
        transferButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/TransferView.fxml"));
                Node view = loader.load();
                ((javafx.scene.layout.Pane) getStage().getScene().getRoot().lookup("#contentArea")).getChildren().setAll(view);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Unable to load Transfer view");
            }
        });
        
        accountsButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/AccountListView.fxml"));
                Node view = loader.load();
                ((javafx.scene.layout.Pane) getStage().getScene().getRoot().lookup("#contentArea")).getChildren().setAll(view);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Unable to load Account List view");
            }
        });
        
        refreshButton.setOnAction(event -> {
            loadDashboardData();
        });
    }
    
    @Override
    protected Stage getStage() {
        return (Stage) welcomeLabel.getScene().getWindow();
    }
}