package com.smartbank.controller;

import com.smartbank.auth.SecurityContext;
import com.smartbank.model.Account;
import com.smartbank.model.User;
import com.smartbank.service.AccountService;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.visualization.ChartService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the chart visualization view.
 */
public class ChartController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(ChartController.class.getName());
    
    @FXML private ComboBox<AccountItem> accountComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<ChartType> chartTypeComboBox;
    @FXML private ComboBox<ChartService.PeriodType> periodTypeComboBox;
    @FXML private Button generateButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private StackPane chartContainer;
    @FXML private VBox optionsContainer;
    @FXML private Label statusLabel;
    
    private Chart currentChart;
    private ChartService chartService;
    private AccountService accountService;
    
    /**
     * Enum representing the available chart types.
     */
    public enum ChartType {
        BALANCE_HISTORY("Balance History"),
        CATEGORY_DISTRIBUTION("Category Distribution"),
        INCOME_EXPENSE("Income vs Expense"),
        MONTHLY_COMPARISON("Monthly Comparison");
        
        private final String displayName;
        
        ChartType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    @FXML
    public void initialize() {
        LOGGER.info("Initializing ChartController");
        
        // Initialize services
        chartService = ServiceFactory.getChartService();
        accountService = ServiceFactory.getAccountService();
        
        if (chartService == null) {
            LOGGER.severe("Failed to get ChartService");
        }
        
        if (accountService == null) {
            LOGGER.severe("Failed to get AccountService");
        }
        
        // Setup date pickers
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        
        startDatePicker.setValue(oneMonthAgo);
        endDatePicker.setValue(now);
        
        // Setup chart type combo box
        chartTypeComboBox.setItems(FXCollections.observableArrayList(ChartType.values()));
        chartTypeComboBox.setValue(ChartType.BALANCE_HISTORY);
        
        // Setup period type combo box (for income/expense chart)
        periodTypeComboBox.setItems(FXCollections.observableArrayList(ChartService.PeriodType.values()));
        periodTypeComboBox.setValue(ChartService.PeriodType.MONTH);
        periodTypeComboBox.setDisable(true); // Initially disabled, enabled only for income/expense chart
        
        // Setup chart type change listener
        chartTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == ChartType.INCOME_EXPENSE) {
                periodTypeComboBox.setDisable(false);
            } else {
                periodTypeComboBox.setDisable(true);
            }
        });
        
        // Setup buttons
        generateButton.setOnAction(this::handleGenerateChart);
        exportButton.setOnAction(this::handleExportChart);
        refreshButton.setOnAction(this::handleRefreshChart);
        
        // Initially disable export button until a chart is generated
        exportButton.setDisable(true);
        
        // Try to load current user's accounts
        try {
            LOGGER.info("Attempting to load current user accounts");
            SecurityContext securityContext = SecurityContext.getInstance();
            if (securityContext.isAuthenticated()) {
                User currentUser = securityContext.getCurrentSession().getUser();
                LOGGER.info("Found authenticated user: " + currentUser.getUsername());
                loadUserAccounts(currentUser);
            } else {
                LOGGER.info("No authenticated user found during initialization");
                statusLabel.setText("Please log in to view charts.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not automatically load user accounts: " + e.getMessage(), e);
            statusLabel.setText("Error loading accounts: " + e.getMessage());
        }
    }
    
    /**
     * Load the accounts for the specified user and populate the account combo box.
     * @param user The user to load accounts for
     */
    private void loadUserAccounts(User user) {
        if (user == null) {
            LOGGER.warning("Cannot load accounts for null user");
            return;
        }
        
        LOGGER.info("Loading accounts for user: " + user.getUsername());
        ObservableList<AccountItem> accountItems = FXCollections.observableArrayList();
        
        try {
            List<Account> accounts = accountService.getAccountsByUsername(user.getUsername());
            
            if (accounts != null && !accounts.isEmpty()) {
                for (Account account : accounts) {
                    accountItems.add(new AccountItem(
                            account.getAccountNumber(),
                            account.getClass().getSimpleName(),
                            account.getBalance()));
                    LOGGER.info("Added account: " + account.getAccountNumber() + " with balance: " + account.getBalance());
                }
                
                // Set items to the combo box
                accountComboBox.setItems(accountItems);
                
                // Select the first account if available
                if (!accountItems.isEmpty()) {
                    accountComboBox.getSelectionModel().selectFirst();
                }
            } else {
                LOGGER.warning("No accounts found for user: " + user.getUsername());
                statusLabel.setText("No accounts found. Please create an account first.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user accounts: " + e.getMessage(), e);
            statusLabel.setText("Error loading accounts: " + e.getMessage());
        }
    }
    
    /**
     * Handle the Generate Chart button click event.
     * @param event The action event
     */
    private void handleGenerateChart(ActionEvent event) {
        LOGGER.info("Generate chart button clicked");
        
        AccountItem selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            statusLabel.setText("Please select an account");
            return;
        }
        
        ChartType selectedChartType = chartTypeComboBox.getValue();
        if (selectedChartType == null) {
            statusLabel.setText("Please select a chart type");
            return;
        }
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            statusLabel.setText("Please select start and end dates");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            statusLabel.setText("Start date must be before end date");
            return;
        }
        
        // Convert LocalDate to Date
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusNanos(1).toInstant());
        
        long accountNumber = selectedAccount.getAccountNumber();
        statusLabel.setText("Generating chart...");
        
        try {
            // Clear existing chart from container
            chartContainer.getChildren().clear();
            
            Chart newChart = null;
            switch (selectedChartType) {
                case BALANCE_HISTORY:
                    newChart = generateBalanceHistoryChart(accountNumber, start, end);
                    break;
                    
                case CATEGORY_DISTRIBUTION:
                    newChart = generateCategoryDistributionChart(accountNumber, start, end);
                    break;
                    
                case INCOME_EXPENSE:
                    ChartService.PeriodType periodType = periodTypeComboBox.getValue();
                    if (periodType == null) {
                        periodType = ChartService.PeriodType.MONTH;
                    }
                    newChart = generateIncomeExpenseChart(accountNumber, start, end, periodType);
                    break;
                    
                case MONTHLY_COMPARISON:
                    newChart = generateMonthlyComparisonChart(accountNumber, start, end);
                    break;
            }
            
            if (newChart != null) {
                // Set up the new chart
                currentChart = newChart;
                currentChart.setPrefWidth(chartContainer.getWidth());
                currentChart.setPrefHeight(chartContainer.getHeight());
                
                // Add new chart to container
                chartContainer.getChildren().add(currentChart);
                
                // Enable export button
                exportButton.setDisable(false);
                
                // Update status
                statusLabel.setText("Chart generated successfully");
            } else {
                statusLabel.setText("Failed to generate chart");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating chart: " + e.getMessage(), e);
            statusLabel.setText("Error generating chart: " + e.getMessage());
        }
    }
    
    /**
     * Generate a balance history line chart.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return The generated chart
     */
    private LineChart<String, Number> generateBalanceHistoryChart(long accountNumber, Date startDate, Date endDate) {
        LOGGER.info("Generating balance history chart for account " + accountNumber);
        return chartService.createBalanceHistoryChart(accountNumber, startDate, endDate);
    }
    
    /**
     * Generate a category distribution pie chart.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return The generated chart
     */
    private PieChart generateCategoryDistributionChart(long accountNumber, Date startDate, Date endDate) {
        LOGGER.info("Generating category distribution chart for account " + accountNumber);
        return chartService.createCategoryDistributionChart(accountNumber, startDate, endDate);
    }
    
    /**
     * Generate an income vs expense bar chart.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @param periodType The period type
     * @return The generated chart
     */
    private BarChart<String, Number> generateIncomeExpenseChart(long accountNumber, Date startDate, Date endDate, ChartService.PeriodType periodType) {
        LOGGER.info("Generating income/expense chart for account " + accountNumber);
        return chartService.createIncomeExpenseChart(accountNumber, startDate, endDate, periodType);
    }
    
    /**
     * Generate a monthly comparison bar chart.
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return The generated chart
     */
    private BarChart<String, Number> generateMonthlyComparisonChart(long accountNumber, Date startDate, Date endDate) {
        LOGGER.info("Generating monthly comparison chart for account " + accountNumber);
        return chartService.createMonthlyComparisonChart(accountNumber, startDate, endDate);
    }
    
    /**
     * Handle the Export Chart button click event.
     * @param event The action event
     */
    private void handleExportChart(ActionEvent event) {
        LOGGER.info("Export chart button clicked");
        
        if (currentChart == null) {
            statusLabel.setText("No chart to export");
            return;
        }
        
        // Set up the file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Chart");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("SVG Image", "*.svg"),
            new FileChooser.ExtensionFilter("PDF Document", "*.pdf")
        );
        
        // Set initial filename based on chart type
        String chartType = chartTypeComboBox.getValue().toString();
        String accountNumber = String.valueOf(accountComboBox.getSelectionModel().getSelectedItem().getAccountNumber());
        fileChooser.setInitialFileName("smartbank_" + accountNumber + "_" + chartType.replace(" ", "_").toLowerCase() + ".png");
        
        // Show save dialog
        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // Determine export format based on file extension
                String fileName = file.getName().toLowerCase();
                ChartService.ExportFormat format;
                
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    format = ChartService.ExportFormat.JPG;
                } else if (fileName.endsWith(".svg")) {
                    format = ChartService.ExportFormat.SVG;
                } else if (fileName.endsWith(".pdf")) {
                    format = ChartService.ExportFormat.PDF;
                } else {
                    // Default to PNG
                    format = ChartService.ExportFormat.PNG;
                }
                
                // Export the chart
                boolean success = chartService.exportChartToImage(currentChart, file.getAbsolutePath(), format);
                
                if (success) {
                    statusLabel.setText("Chart exported successfully to " + file.getName());
                } else {
                    statusLabel.setText("Failed to export chart");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error exporting chart: " + e.getMessage(), e);
                statusLabel.setText("Error exporting chart: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle the Refresh Chart button click event.
     * @param event The action event
     */
    private void handleRefreshChart(ActionEvent event) {
        LOGGER.info("Refresh chart button clicked");
        
        // Simply trigger the generate chart handler
        handleGenerateChart(event);
    }
    
    /**
     * Item class for the account combo box.
     */
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
            return String.format("%s - %d ($%.2f)", 
                    type,
                    accountNumber, 
                    balance);
        }
    }
}