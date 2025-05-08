package com.smartbank.controller;

import com.smartbank.model.TransactionCategory;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.budgeting.BudgetException;
import com.smartbank.service.budgeting.BudgetService;
import com.smartbank.service.category.CategoryService;
import com.smartbank.controller.SampleDataController;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the budget management view.
 */
public class BudgetController {
    private static final Logger LOGGER = Logger.getLogger(BudgetController.class.getName());
    
    // FXML Controls
    @FXML private TableView<BudgetStatus> tblBudget;
    @FXML private TableColumn<BudgetStatus, String> colCategory;
    @FXML private TableColumn<BudgetStatus, Double> colBudget;
    @FXML private TableColumn<BudgetStatus, Double> colSpent;
    @FXML private TableColumn<BudgetStatus, Double> colRemaining;
    @FXML private TableColumn<BudgetStatus, Double> colPercentage;
    @FXML private TableColumn<BudgetStatus, String> colStatus;
    @FXML private TableColumn<BudgetStatus, Double> colDailyAllowance;
    
    @FXML private ComboBox<AccountItem> cmbAccount;
    @FXML private ComboBox<String> cmbPeriod;
    @FXML private ComboBox<TransactionCategory> cmbCategory;
    @FXML private TextField txtBudgetAmount;
    @FXML private Button btnSetBudget;
    
    @FXML private PieChart chartSpending;
    @FXML private BarChart<String, Number> chartBudgetVsActual;
    
    @FXML private Button btnGenerateReport;
    @FXML private ComboBox<String> cmbReportFormat;
    
    @FXML private Label lblPeriod;
    @FXML private Label lblDaysRemaining;
    @FXML private Label lblStatus;
    
    // Add buttons for creating sample data
    @FXML private Button btnGenerateSampleData;
    @FXML private Button btnGenerateBudgetData;
    
    // Services
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    
    // State
    private long currentAccountNumber = -1; // Will be set when account is selected
    private String currentPeriod = "month"; // Default period
    private ObservableList<BudgetStatus> budgetStatuses;
    private String currentUsername; // Current logged-in user
    private final Random random = new Random(); // For randomizing budget amounts
    
    /**
     * Constructor.
     */
    public BudgetController() {
        this.budgetService = ServiceFactory.getBudgetService();
        this.categoryService = ServiceFactory.getCategoryService();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Setup combo boxes
        setupCombos();
        
        // Setup budget table
        setupBudgetTable();
        
        // Allow time for account selection to be set before checking budgets
        javafx.application.Platform.runLater(() -> {
            // Check if any budgets are defined and create defaults if needed
            checkAndCreateDefaultBudgets();
            
            // Load budget data
            loadBudgetData();
            
            // Update charts
            updateCharts();
        });
    }
    
    /**
     * Setup combo boxes.
     */
    private void setupCombos() {
        // Account combo
        cmbAccount.setOnAction(e -> {
            AccountItem selectedAccount = cmbAccount.getSelectionModel().getSelectedItem();
            if (selectedAccount != null) {
                currentAccountNumber = selectedAccount.getAccountNumber();
                LOGGER.info("Account selected: " + currentAccountNumber);
                loadBudgetData();
                updateCharts();
            }
        });
        
        // Try to get current user
        try {
            com.smartbank.auth.SecurityContext securityContext = com.smartbank.auth.SecurityContext.getInstance();
            if (securityContext.isAuthenticated()) {
                currentUsername = securityContext.getCurrentSession().getUser().getUsername();
                // Load user accounts
                loadUserAccounts(currentUsername);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting current user: " + e.getMessage(), e);
        }
        
        // Period combo
        cmbPeriod.setItems(FXCollections.observableArrayList("Day", "Week", "Month"));
        cmbPeriod.setValue("Month");
        cmbPeriod.setOnAction(e -> {
            currentPeriod = cmbPeriod.getValue().toLowerCase();
            loadBudgetData();
            updateCharts();
        });
        
        // Category combo for setting budgets
        cmbCategory.setConverter(new javafx.util.StringConverter<TransactionCategory>() {
            @Override
            public String toString(TransactionCategory category) {
                return category == null ? "" : category.getName();
            }
            
            @Override
            public TransactionCategory fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Load categories
        loadCategories();
        
        // Report format combo
        cmbReportFormat.setItems(FXCollections.observableArrayList("HTML", "CSV", "Text"));
        cmbReportFormat.setValue("HTML");
    }
    
    /**
     * Load user accounts and populate the account combo box.
     * 
     * @param username The username
     */
    private void loadUserAccounts(String username) {
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
                    LOGGER.info("Found userId " + userId + " for username " + username);
                } else {
                    LOGGER.warning("Could not find userId for username: " + username);
                }
            }
            
            if (userId != null) {
                // Get all accounts for this user
                try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT accountNumber, type, balance FROM accounts WHERE userId = ?")) {
                    stmt.setString(1, userId);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        long accountNumber = rs.getLong("accountNumber");
                        String type = rs.getString("type");
                        double balance = rs.getDouble("balance");
                        accountItems.add(new AccountItem(accountNumber, type, balance));
                    }
                }
            }
            
            cmbAccount.setItems(accountItems);
            
            // Select the first account if available
            if (!accountItems.isEmpty()) {
                cmbAccount.getSelectionModel().selectFirst();
                currentAccountNumber = accountItems.get(0).getAccountNumber();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user accounts: " + e.getMessage(), e);
            showError("Error loading user accounts: " + e.getMessage());
        }
    }
    
    /**
     * Setup budget table.
     */
    private void setupBudgetTable() {
        colCategory.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        
        colBudget.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getBudgetAmount()).asObject());
        colBudget.setCellFactory(column -> new TableCell<BudgetStatus, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        colSpent.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getSpentAmount()).asObject());
        colSpent.setCellFactory(column -> new TableCell<BudgetStatus, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        colRemaining.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getRemainingAmount()).asObject());
        colRemaining.setCellFactory(column -> new TableCell<BudgetStatus, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                    if (amount < 0) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        
        colPercentage.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getPercentageUsed()).asObject());
        colPercentage.setCellFactory(column -> new TableCell<BudgetStatus, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%.1f%%", percentage));
                    
                    // Add progress bar
                    javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
                    progressBar.setPrefWidth(80);
                    progressBar.setProgress(Math.min(percentage / 100.0, 1.0));
                    
                    // Color based on percentage
                    if (percentage >= 100) {
                        progressBar.setStyle("-fx-accent: red;");
                    } else if (percentage >= 85) {
                        progressBar.setStyle("-fx-accent: orange;");
                    } else {
                        progressBar.setStyle("-fx-accent: green;");
                    }
                    
                    setGraphic(progressBar);
                }
            }
        });
        
        colStatus.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isOverBudget() ? "Over Budget" : "Under Budget"));
        colStatus.setCellFactory(column -> new TableCell<BudgetStatus, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    if (status.equals("Over Budget")) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        
        colDailyAllowance.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(getDailyAllowance(cellData.getValue())));
        colDailyAllowance.setCellFactory(column -> new TableCell<BudgetStatus, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f/day", amount));
                }
            }
        });
    }
    
    /**
     * Load categories for budget setting.
     */
    private void loadCategories() {
        try {
            List<TransactionCategory> categories = categoryService.getAllCategories();
            ObservableList<TransactionCategory> observableCategories = FXCollections.observableArrayList(categories);
            cmbCategory.setItems(observableCategories);
            
            if (!categories.isEmpty()) {
                cmbCategory.setValue(categories.get(0));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading categories: " + e.getMessage(), e);
            showError("Error loading categories: " + e.getMessage());
        }
    }
    
    /**
     * Check if any budgets exist and create defaults based on transaction history if not.
     */
    private void checkAndCreateDefaultBudgets() {
        try {
            // Check if any categories have budgets defined
            List<TransactionCategory> categories = categoryService.getAllCategories();
            boolean anyBudgetsExist = categories.stream().anyMatch(c -> c.getBudgetAmount() > 0);
            
            if (!anyBudgetsExist && currentAccountNumber > 0) {
                // No budgets exist, generate defaults based on transaction history
                LOGGER.info("No budgets found. Generating default budgets based on transaction history");
                                
                // Create a map to track spending by category across all user accounts
                Map<Long, Double> categoryTotals = new HashMap<>();
                
                // Find all accounts for the current user
                List<AccountItem> userAccounts = new ArrayList<>(cmbAccount.getItems());
                
                if (userAccounts.isEmpty()) {
                    LOGGER.warning("No accounts found for generating default budgets");
                    return;
                }
                
                // For each account, get transactions and calculate spending by category
                for (AccountItem account : userAccounts) {
                    // Get a month's worth of transactions (could be expanded or customized)
                    Calendar startCal = Calendar.getInstance();
                    startCal.add(Calendar.MONTH, -3); // Look at last 3 months of data
                    Date startDate = startCal.getTime();
                    Date endDate = new Date(); // Current date
                    
                    // Use CategoryReportService to get spending by category
                    Map<Long, com.smartbank.service.reporting.CategoryReportService.CategorySpending> spending = 
                            ServiceFactory.getCategoryReportService().getSpendingByCategory(
                                    account.getAccountNumber(), startDate, endDate);
                    
                    // Add spending to totals
                    for (Map.Entry<Long, com.smartbank.service.reporting.CategoryReportService.CategorySpending> entry 
                            : spending.entrySet()) {
                        Long categoryId = entry.getKey();
                        Double amount = Math.abs(entry.getValue().getAmount());
                        
                        // Add to totals
                        if (categoryTotals.containsKey(categoryId)) {
                            categoryTotals.put(categoryId, categoryTotals.get(categoryId) + amount);
                        } else {
                            categoryTotals.put(categoryId, amount);
                        }
                    }
                }
                
                // Set budget amounts based on historical spending with realistic variations
                for (Map.Entry<Long, Double> entry : categoryTotals.entrySet()) {
                    Long categoryId = entry.getKey();
                    Double totalSpent = entry.getValue();
                    
                    // Calculate monthly average
                    double monthlyAverage = totalSpent / 3.0; // 3 months of data
                    
                    // Apply a personalization factor to make budgets look manually set
                    // Different categories get different treatment - some conservative, some optimistic
                    double personalizedFactor = 0.85 + (random.nextDouble() * 0.5); // Range from 85% to 135%
                    
                    // Get category details for more personalized adjustments
                    Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
                    if (categoryOpt.isPresent()) {
                        String categoryName = categoryOpt.get().getName().toLowerCase();
                        
                        // Apply different logic for different category types
                        if (categoryName.contains("grocer") || categoryName.contains("food") || 
                            categoryName.contains("dining") || categoryName.contains("restaurant")) {
                            // Food/groceries - people typically round to the nearest $50
                            double baseAmount = monthlyAverage * personalizedFactor;
                            double roundedAmount = Math.ceil(baseAmount / 50.0) * 50.0;
                            setBudgetWithRealisticAmount(categoryId, roundedAmount);
                        }
                        else if (categoryName.contains("entertain") || categoryName.contains("shopping") || 
                                 categoryName.contains("travel")) {
                            // Discretionary spending - people typically are more conservative, round to $25
                            double baseAmount = monthlyAverage * personalizedFactor * 0.9; // More conservative
                            double roundedAmount = Math.ceil(baseAmount / 25.0) * 25.0;
                            setBudgetWithRealisticAmount(categoryId, roundedAmount);
                        }
                        else if (categoryName.contains("bill") || categoryName.contains("utilities") || 
                                 categoryName.contains("rent") || categoryName.contains("mortgage")) {
                            // Fixed expenses - people typically round up slightly
                            double baseAmount = monthlyAverage * 1.05; // Add 5% buffer
                            double roundedAmount = Math.ceil(baseAmount / 10.0) * 10.0;
                            setBudgetWithRealisticAmount(categoryId, roundedAmount);
                        }
                        else {
                            // Other categories - use a mix of common budget amounts
                            int[] typicalBudgetUnits = {25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 750, 1000};
                            double baseAmount = monthlyAverage * personalizedFactor;
                            
                            // Find the closest "typical" budget amount
                            double closestAmount = findClosestRealisticAmount(baseAmount, typicalBudgetUnits);
                            setBudgetWithRealisticAmount(categoryId, closestAmount);
                        }
                    } else {
                        // If category not found, just use a simple approach
                        double baseAmount = monthlyAverage * personalizedFactor;
                        double roundedAmount = Math.round(baseAmount / 10.0) * 10.0; // Round to nearest $10
                        setBudgetWithRealisticAmount(categoryId, roundedAmount);
                    }
                }
                
                // Helper method to find closest realistic budget amount
                // Added here in the edit to access local variables
                
                // For any category without transactions, set realistic looking budgets
                for (TransactionCategory category : categories) {
                    if (!categoryTotals.containsKey(category.getCategoryId())) {
                        String categoryName = category.getName().toLowerCase();
                        // Common budget amounts people might set
                        double[] commonBudgetAmounts = {25.0, 50.0, 75.0, 100.0, 150.0, 200.0, 250.0, 300.0};
                        
                        // Choose a budget amount based on category type
                        double budgetAmount;
                        
                        // Different budgets for different category types to look more realistic
                        if (categoryName.contains("grocer") || categoryName.contains("food")) {
                            budgetAmount = commonBudgetAmounts[3 + random.nextInt(4)]; // 100-300 range
                        }
                        else if (categoryName.contains("entertain") || categoryName.contains("shopping")) {
                            budgetAmount = commonBudgetAmounts[1 + random.nextInt(3)]; // 50-150 range
                        }
                        else if (categoryName.contains("bill") || categoryName.contains("utilities")) {
                            budgetAmount = commonBudgetAmounts[2 + random.nextInt(3)]; // 75-200 range
                        }
                        else if (categoryName.contains("health") || categoryName.contains("medical")) {
                            budgetAmount = commonBudgetAmounts[1 + random.nextInt(2)]; // 50-100 range
                        }
                        else if (categoryName.contains("travel") || categoryName.contains("vacation")) {
                            budgetAmount = commonBudgetAmounts[4 + random.nextInt(3)]; // 200-300 range
                        }
                        else {
                            // For other categories, pick a random common amount
                            budgetAmount = commonBudgetAmounts[random.nextInt(commonBudgetAmounts.length)];
                        }
                        
                        try {
                            // Set the budget with a realistic amount
                            setBudgetWithRealisticAmount(category.getCategoryId(), budgetAmount);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error setting default budget for category " + 
                                    category.getCategoryId(), e);
                        }
                    }
                }
                
                showSuccess("Default budgets have been created based on your spending history");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking or creating default budgets: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load budget data.
     */
    private void loadBudgetData() {
        // Check if account is selected
        if (currentAccountNumber <= 0) {
            showError("Please select an account to view budget data");
            budgetStatuses = FXCollections.observableArrayList();
            tblBudget.setItems(budgetStatuses);
            return;
        }
        
        try {
            // Get budget period date range
            BudgetService.DateRange dateRange = budgetService.getBudgetPeriodRange(currentPeriod, null);
            
            // Update period labels
            lblPeriod.setText(dateRange.getLabel());
            lblDaysRemaining.setText(dateRange.getDaysRemaining() + " days remaining");
            
            // Get budget status for all categories
            Map<Long, BudgetService.BudgetStatus> statusMap = 
                    budgetService.getBudgetStatus(currentAccountNumber, currentPeriod);
            
            // Convert to controller's BudgetStatus objects
            List<BudgetStatus> statuses = new ArrayList<>();
            
            for (Map.Entry<Long, BudgetService.BudgetStatus> entry : statusMap.entrySet()) {
                BudgetService.BudgetStatus status = entry.getValue();
                
                // Get the category
                Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(status.getCategoryId());
                if (categoryOpt.isPresent()) {
                    statuses.add(new BudgetStatus(
                            categoryOpt.get(),
                            status.getBudgetAmount(),
                            status.getSpentAmount(),
                            status.getRemainingAmount(),
                            status.getPercentageUsed(),
                            status.isOverBudget()));
                }
            }
            
            // Sort by percentage used (descending)
            statuses.sort((s1, s2) -> Double.compare(s2.getPercentageUsed(), s1.getPercentageUsed()));
            
            // Update table
            budgetStatuses = FXCollections.observableArrayList(statuses);
            tblBudget.setItems(budgetStatuses);
            
            if (statuses.isEmpty()) {
                showError("No budget data available for this account and period");
            } else {
                lblStatus.setText("Budget data loaded successfully");
                lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading budget data: " + e.getMessage(), e);
            showError("Error loading budget data: " + e.getMessage());
        }
    }
    
    /**
     * Update charts with budget data.
     */
    private void updateCharts() {
        if (budgetStatuses == null || budgetStatuses.isEmpty()) {
            return;
        }
        
        // Update pie chart
        updatePieChart();
        
        // Update bar chart
        updateBarChart();
    }
    
    /**
     * Update pie chart with spending data.
     */
    private void updatePieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        boolean hasSpending = false;
        
        // Log for debugging
        LOGGER.info("Updating pie chart with " + budgetStatuses.size() + " budget statuses");
        
        // Add data for categories with spending
        for (BudgetStatus status : budgetStatuses) {
            double spentAmount = status.getSpentAmount();
            if (spentAmount > 0) {
                String categoryName = status.getCategory().getName();
                pieChartData.add(new PieChart.Data(categoryName, spentAmount));
                LOGGER.info("Pie chart - Category: " + categoryName + ", Spent: $" + spentAmount);
                hasSpending = true;
            }
        }
        
        chartSpending.setData(pieChartData);
        
        if (hasSpending) {
            chartSpending.setTitle("Spending by Category");
            
            // Add custom styling and labels
            for (PieChart.Data data : pieChartData) {
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-pie-color: derive(" + data.getNode().getStyle() + ", 30%);");
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle("");
                });
            }
        } else {
            chartSpending.setTitle("No Spending Data Available");
        }
    }
    
    /**
     * Update bar chart with budget vs actual spending.
     */
    private void updateBarChart() {
        // Clear existing data
        chartBudgetVsActual.getData().clear();
        
        // Create budget series
        XYChart.Series<String, Number> budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budget");
        
        // Create actual series
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual");
        
        // Log for debugging
        LOGGER.info("Updating bar chart with " + budgetStatuses.size() + " budget statuses");
        
        // Limit to top 5 categories by percentage used
        int count = 0;
        for (BudgetStatus status : budgetStatuses) {
            if (count >= 5) break;
            
            String categoryName = status.getCategory().getName();
            double budgetAmount = status.getBudgetAmount();
            double spentAmount = status.getSpentAmount();
            
            LOGGER.info("Chart data - Category: " + categoryName + 
                      ", Budget: $" + budgetAmount + 
                      ", Spent: $" + spentAmount + 
                      ", Percentage: " + status.getPercentageUsed() + "%");
            
            budgetSeries.getData().add(new XYChart.Data<>(categoryName, budgetAmount));
            actualSeries.getData().add(new XYChart.Data<>(categoryName, spentAmount));
            
            count++;
        }
        
        // Add series to chart
        chartBudgetVsActual.getData().addAll(budgetSeries, actualSeries);
        chartBudgetVsActual.setTitle("Budget vs Actual (Top 5 Categories)");
        
        // Add custom styling to distinguish the bars
        for (XYChart.Series<String, Number> series : chartBudgetVsActual.getData()) {
            if (series.getName().equals("Budget")) {
                for (XYChart.Data<String, Number> item : series.getData()) {
                    item.getNode().setStyle("-fx-bar-fill: #6495ED;"); // CornflowerBlue
                }
            } else if (series.getName().equals("Actual")) {
                for (XYChart.Data<String, Number> item : series.getData()) {
                    item.getNode().setStyle("-fx-bar-fill: #FF7F50;"); // Coral
                }
            }
        }
    }
    
    /**
     * Handle set budget button click.
     */
    @FXML
    private void handleSetBudget(ActionEvent event) {
        TransactionCategory category = cmbCategory.getValue();
        
        if (category == null) {
            showError("Please select a category");
            return;
        }
        
        try {
            double budgetAmount = Double.parseDouble(txtBudgetAmount.getText().trim());
            
            if (budgetAmount < 0) {
                showError("Budget amount cannot be negative");
                return;
            }
            
            // Set budget amount
            boolean success = budgetService.setBudgetAmount(category.getCategoryId(), budgetAmount);
            
            if (success) {
                showSuccess("Budget set successfully");
                
                // Clear fields
                txtBudgetAmount.clear();
                
                // Reload data
                loadBudgetData();
                updateCharts();
            } else {
                showError("Failed to set budget");
            }
        } catch (NumberFormatException e) {
            showError("Invalid budget amount");
        } catch (BudgetException e) {
            showError("Error setting budget: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting budget: " + e.getMessage(), e);
            showError("Error setting budget: " + e.getMessage());
        }
    }
    
    /**
     * Handle generate report button click.
     */
    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String format = cmbReportFormat.getValue().toLowerCase();
        
        // File extension based on format
        String extension = ".html";
        if (format.equals("csv")) {
            extension = ".csv";
        } else if (format.equals("text")) {
            extension = ".txt";
        }
        
        try {
            // Generate report data
            byte[] reportData = budgetService.generateBudgetReport(currentAccountNumber, currentPeriod, format);
            
            if (reportData.length == 0) {
                showError("No data to generate report");
                return;
            }
            
            // Show file save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Budget Report");
            fileChooser.setInitialFileName("budget_report_" + currentPeriod + extension);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Report Files", "*" + extension));
            
            File file = fileChooser.showSaveDialog(btnGenerateReport.getScene().getWindow());
            
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(reportData);
                }
                
                showSuccess("Report saved to: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report: " + e.getMessage(), e);
            showError("Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBudgetData();
        updateCharts();
    }
    
    /**
     * Handle create default budgets button click.
     */
    /**
     * Handle generate sample data button click.
     */
    @FXML
    private void handleGenerateSampleData(ActionEvent event) {
        if (currentAccountNumber <= 0) {
            showError("Please select an account first");
            return;
        }
        
        // Create sample data controller
        SampleDataController sampleDataController = new SampleDataController();
        
        // Generate 20 random transactions
        boolean success = sampleDataController.generateSampleTransactions(currentAccountNumber, 20);
        
        if (success) {
            showSuccess("Generated 20 sample transactions for testing");
            // Reload data and charts
            loadBudgetData();
            updateCharts();
        } else {
            showError("Failed to generate sample data");
        }
    }
    
    /**
     * Handle generate budget data button click.
     */
    @FXML
    private void handleGenerateBudgetData(ActionEvent event) {
        if (currentAccountNumber <= 0) {
            showError("Please select an account first");
            return;
        }
        
        // Create sample data controller
        SampleDataController sampleDataController = new SampleDataController();
        
        // Generate realistic budgeted transactions
        boolean success = sampleDataController.generateBudgetedTransactions(currentAccountNumber);
        
        if (success) {
            showSuccess("Generated budgeted transactions for testing");
            // Reload data and charts
            loadBudgetData();
            updateCharts();
        } else {
            showError("Failed to generate budget data");
        }
    }
    
    @FXML
    private void handleCreateDefaultBudgets(ActionEvent event) {
        // Force creation of default budgets even if some already exist
        try {
            // Create a map to track spending by category across all user accounts
            Map<Long, Double> categoryTotals = new HashMap<>();
            
            // Find all accounts for the current user
            List<AccountItem> userAccounts = new ArrayList<>(cmbAccount.getItems());
            
            if (userAccounts.isEmpty()) {
                showError("No accounts found for generating default budgets");
                return;
            }
            
            // For each account, get transactions and calculate spending by category
            for (AccountItem account : userAccounts) {
                // Get a month's worth of transactions (could be expanded or customized)
                Calendar startCal = Calendar.getInstance();
                startCal.add(Calendar.MONTH, -3); // Look at last 3 months of data
                Date startDate = startCal.getTime();
                Date endDate = new Date(); // Current date
                
                // Use CategoryReportService to get spending by category
                Map<Long, com.smartbank.service.reporting.CategoryReportService.CategorySpending> spending = 
                        ServiceFactory.getCategoryReportService().getSpendingByCategory(
                                account.getAccountNumber(), startDate, endDate);
                
                // Add spending to totals
                for (Map.Entry<Long, com.smartbank.service.reporting.CategoryReportService.CategorySpending> entry 
                        : spending.entrySet()) {
                    Long categoryId = entry.getKey();
                    Double amount = Math.abs(entry.getValue().getAmount());
                    
                    // Add to totals
                    if (categoryTotals.containsKey(categoryId)) {
                        categoryTotals.put(categoryId, categoryTotals.get(categoryId) + amount);
                    } else {
                        categoryTotals.put(categoryId, amount);
                    }
                }
            }
            
            // Get all categories
            List<TransactionCategory> categories = categoryService.getAllCategories();
            
            // Set budget amounts based on historical spending with realistic variations
            for (Map.Entry<Long, Double> entry : categoryTotals.entrySet()) {
                Long categoryId = entry.getKey();
                Double totalSpent = entry.getValue();
                
                // Calculate monthly average
                double monthlyAverage = totalSpent / 3.0; // 3 months of data
                
                // Apply a personalization factor to make budgets look manually set
                // Different categories get different treatment - some conservative, some optimistic
                double personalizedFactor = 0.85 + (random.nextDouble() * 0.5); // Range from 85% to 135%
                
                // Get category details for more personalized adjustments
                Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
                if (categoryOpt.isPresent()) {
                    String categoryName = categoryOpt.get().getName().toLowerCase();
                    
                    // Apply different logic for different category types
                    if (categoryName.contains("grocer") || categoryName.contains("food") || 
                        categoryName.contains("dining") || categoryName.contains("restaurant")) {
                        // Food/groceries - people typically round to the nearest $50
                        double baseAmount = monthlyAverage * personalizedFactor;
                        double roundedAmount = Math.ceil(baseAmount / 50.0) * 50.0;
                        setBudgetWithRealisticAmount(categoryId, roundedAmount);
                    }
                    else if (categoryName.contains("entertain") || categoryName.contains("shopping") || 
                             categoryName.contains("travel")) {
                        // Discretionary spending - people typically are more conservative, round to $25
                        double baseAmount = monthlyAverage * personalizedFactor * 0.9; // More conservative
                        double roundedAmount = Math.ceil(baseAmount / 25.0) * 25.0;
                        setBudgetWithRealisticAmount(categoryId, roundedAmount);
                    }
                    else if (categoryName.contains("bill") || categoryName.contains("utilities") || 
                             categoryName.contains("rent") || categoryName.contains("mortgage")) {
                        // Fixed expenses - people typically round up slightly
                        double baseAmount = monthlyAverage * 1.05; // Add 5% buffer
                        double roundedAmount = Math.ceil(baseAmount / 10.0) * 10.0;
                        setBudgetWithRealisticAmount(categoryId, roundedAmount);
                    }
                    else {
                        // Other categories - use a mix of common budget amounts
                        int[] typicalBudgetUnits = {25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 750, 1000};
                        double baseAmount = monthlyAverage * personalizedFactor;
                        
                        // Find the closest "typical" budget amount
                        double closestAmount = findClosestRealisticAmount(baseAmount, typicalBudgetUnits);
                        setBudgetWithRealisticAmount(categoryId, closestAmount);
                    }
                } else {
                    // If category not found, just use a simple approach
                    double baseAmount = monthlyAverage * personalizedFactor;
                    double roundedAmount = Math.round(baseAmount / 10.0) * 10.0; // Round to nearest $10
                    setBudgetWithRealisticAmount(categoryId, roundedAmount);
                }
            }
            
            // For any category without transactions, set realistic looking budgets
            for (TransactionCategory category : categories) {
                if (!categoryTotals.containsKey(category.getCategoryId())) {
                    String categoryName = category.getName().toLowerCase();
                    // Common budget amounts people might set
                    double[] commonBudgetAmounts = {25.0, 50.0, 75.0, 100.0, 150.0, 200.0, 250.0, 300.0};
                    
                    // Choose a budget amount based on category type
                    double budgetAmount;
                    
                    // Different budgets for different category types to look more realistic
                    if (categoryName.contains("grocer") || categoryName.contains("food")) {
                        budgetAmount = commonBudgetAmounts[3 + random.nextInt(4)]; // 100-300 range
                    }
                    else if (categoryName.contains("entertain") || categoryName.contains("shopping")) {
                        budgetAmount = commonBudgetAmounts[1 + random.nextInt(3)]; // 50-150 range
                    }
                    else if (categoryName.contains("bill") || categoryName.contains("utilities")) {
                        budgetAmount = commonBudgetAmounts[2 + random.nextInt(3)]; // 75-200 range
                    }
                    else if (categoryName.contains("health") || categoryName.contains("medical")) {
                        budgetAmount = commonBudgetAmounts[1 + random.nextInt(2)]; // 50-100 range
                    }
                    else if (categoryName.contains("travel") || categoryName.contains("vacation")) {
                        budgetAmount = commonBudgetAmounts[4 + random.nextInt(3)]; // 200-300 range
                    }
                    else {
                        // For other categories, pick a random common amount
                        budgetAmount = commonBudgetAmounts[random.nextInt(commonBudgetAmounts.length)];
                    }
                    
                    try {
                        // Set the budget with a realistic amount
                        setBudgetWithRealisticAmount(category.getCategoryId(), budgetAmount);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error setting default budget for category " + 
                                category.getCategoryId(), e);
                    }
                }
            }
            
            // Reload data
            loadBudgetData();
            updateCharts();
            
            showSuccess("Default budgets have been created based on your spending history");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating default budgets: " + e.getMessage(), e);
            showError("Error creating default budgets: " + e.getMessage());
        }
    }
    
    /**
     * Get daily allowance for a budget status.
     */
    private double getDailyAllowance(BudgetStatus status) {
        return budgetService.calculateDailyAllowance(
                currentAccountNumber, status.getCategory().getCategoryId(), currentPeriod);
    }
    
    /**
     * Show an error message.
     * 
     * @param message The error message
     */
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setTextFill(Color.RED);
    }
    
    /**
     * Show a success message.
     * 
     * @param message The success message
     */
    private void showSuccess(String message) {
        lblStatus.setText(message);
        lblStatus.setTextFill(Color.GREEN);
    }
    
    /**
     * Set a budget amount with realistic values.
     * 
     * @param categoryId The category ID
     * @param amount The suggested amount
     */
    private void setBudgetWithRealisticAmount(long categoryId, double amount) {
        try {
            // Make sure amount is reasonable
            amount = Math.max(25.0, amount); // Minimum $25 budget
            amount = Math.min(5000.0, amount); // Maximum $5000 budget
            
            // Small chance of ending in .99 or .95 to look more like manual entry
            if (random.nextDouble() < 0.15) { // 15% chance
                amount = Math.floor(amount) - 0.01; // End in .99
            } else if (random.nextDouble() < 0.20) { // 20% chance
                amount = Math.floor(amount) - 0.05; // End in .95
            }
            
            // Set the budget
            budgetService.setBudgetAmount(categoryId, amount);
            LOGGER.info("Created budget for category ID " + categoryId + ": $" + String.format("%.2f", amount));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting budget for category " + categoryId, e);
        }
    }
    
    /**
     * Find the closest "realistic" budget amount to a calculated value.
     * 
     * @param calculatedAmount The calculated amount
     * @param typicalAmounts Array of typical budget amounts
     * @return The closest realistic amount
     */
    private double findClosestRealisticAmount(double calculatedAmount, int[] typicalAmounts) {
        // Find the closest typical budget amount
        int closest = typicalAmounts[0];
        int closestDiff = Math.abs(typicalAmounts[0] - (int)calculatedAmount);
        
        for (int amount : typicalAmounts) {
            int diff = Math.abs(amount - (int)calculatedAmount);
            if (diff < closestDiff) {
                closest = amount;
                closestDiff = diff;
            }
        }
        
        // Small chance to add some cents to make it look more realistic
        if (random.nextDouble() < 0.2) { // 20% chance
            if (random.nextBoolean()) {
                return closest - 0.01; // End in .99
            } else {
                return closest - 0.05; // End in .95
            }
        }
        
        return closest;
    }
    
    /**
     * Class representing an account for selection.
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
            return String.format("%s - %d (%.2f)", 
                       type.substring(0, 1).toUpperCase() + type.substring(1),
                       accountNumber, 
                       balance);
        }
    }
    
    /**
     * Class representing budget status for a category.
     */
    public static class BudgetStatus {
        private final TransactionCategory category;
        private final double budgetAmount;
        private final double spentAmount;
        private final double remainingAmount;
        private final double percentageUsed;
        private final boolean overBudget;
        
        public BudgetStatus(TransactionCategory category, double budgetAmount, double spentAmount,
                           double remainingAmount, double percentageUsed, boolean overBudget) {
            this.category = category;
            this.budgetAmount = budgetAmount;
            this.spentAmount = spentAmount;
            this.remainingAmount = remainingAmount;
            this.percentageUsed = percentageUsed;
            this.overBudget = overBudget;
        }
        
        public TransactionCategory getCategory() {
            return category;
        }
        
        public double getBudgetAmount() {
            return budgetAmount;
        }
        
        public double getSpentAmount() {
            return spentAmount;
        }
        
        public double getRemainingAmount() {
            return remainingAmount;
        }
        
        public double getPercentageUsed() {
            return percentageUsed;
        }
        
        public boolean isOverBudget() {
            return overBudget;
        }
    }
}