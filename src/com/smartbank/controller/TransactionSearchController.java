package com.smartbank.controller;

import com.smartbank.auth.SecurityContext;
import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.category.CategoryService;
import com.smartbank.service.search.SearchResult;
import com.smartbank.service.search.TransactionSearchCriteria;
import com.smartbank.service.search.TransactionSearchService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the advanced transaction search functionality.
 */
public class TransactionSearchController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(TransactionSearchController.class.getName());
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    // Service references
    private final TransactionSearchService searchService;
    private final CategoryService categoryService;
    
    // UI components
    @FXML private ComboBox<String> accountComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private TextField descriptionField;
    @FXML private TextField merchantField;
    @FXML private ComboBox<TransactionCategory> categoryComboBox;
    @FXML private CheckBox includeSubcategoriesCheckbox;
    @FXML private CheckBox isRecurringCheckbox;
    @FXML private CheckBox isAutoCategorizedCheckbox;
    
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private ComboBox<String> sortDirectionComboBox;
    @FXML private TextField pageSizeField;
    
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Long> idColumn;
    @FXML private TableColumn<Transaction, Long> accountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, Date> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> merchantColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    
    @FXML private Pagination pagination;
    @FXML private Label resultCountLabel;
    @FXML private Label timeLabel;
    
    @FXML private VBox savedSearchesBox;
    @FXML private Button saveSearchButton;
    @FXML private Button clearSearchButton;
    @FXML private Button exportButton;
    @FXML private ComboBox<String> exportFormatComboBox;
    
    // State
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private Map<String, Long> accountMap = new HashMap<>();
    private List<TransactionSearchCriteria> savedSearches = new ArrayList<>();
    private TransactionSearchCriteria currentCriteria;
    private String currentUserId;
    private long currentTotalElements;
    private int currentPage;
    
    /**
     * Constructor for TransactionSearchController.
     */
    public TransactionSearchController() {
        this.searchService = ServiceFactory.getTransactionSearchService();
        this.categoryService = ServiceFactory.getCategoryService();
        
        // Get the current user ID
        SecurityContext securityContext = SecurityContext.getInstance();
        if (securityContext.isAuthenticated()) {
            currentUserId = securityContext.getCurrentSession().getUser().getUserId();
        }
    }
    
    @FXML
    public void initialize() {
        LOGGER.info("Initializing TransactionSearchController");
        
        setupTableColumns();
        setupComboBoxes();
        setupPagination();
        setupExportOptions();
        loadSavedSearches();
        
        // Setup clear button
        clearSearchButton.setOnAction(event -> clearSearch());
        
        // Setup save search button
        saveSearchButton.setOnAction(event -> saveCurrentSearch());
        
        // Load initial data
        performSearch();
    }
    
    /**
     * Setup the table columns.
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        merchantColumn.setCellValueFactory(new PropertyValueFactory<>("merchantName"));
        
        // Format the amount column to show currency
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
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
        
        // Format the date column
        dateColumn.setCellFactory(column -> new TableCell<Transaction, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMAT.format(date));
                }
            }
        });
        
        // Setup category column to display category name
        categoryColumn.setCellValueFactory(cellData -> {
            TransactionCategory category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getName() : "");
        });
        
        // Set transactions to the table
        transactionTable.setItems(transactions);
    }
    
    /**
     * Setup the combo boxes.
     */
    private void setupComboBoxes() {
        // Setup account combo box
        accountComboBox.getItems().add("All Accounts");
        accountComboBox.setValue("All Accounts");
        loadAccounts();
        
        // Setup transaction type combo box
        typeComboBox.getItems().add("All Types");
        typeComboBox.setValue("All Types");
        for (Transaction.Type type : Transaction.Type.values()) {
            typeComboBox.getItems().add(type.toString());
        }
        
        // Setup category combo box
        categoryComboBox.getItems().add(null); // Add "All Categories" option
        categoryComboBox.setPromptText("All Categories");
        categoryComboBox.setConverter(new StringConverter<TransactionCategory>() {
            @Override
            public String toString(TransactionCategory category) {
                return category == null ? "All Categories" : category.getName();
            }

            @Override
            public TransactionCategory fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        loadCategories();
        
        // Setup sort by combo box
        sortByComboBox.getItems().addAll(
                "Date",
                "Amount",
                "Type",
                "Description",
                "Merchant",
                "Category"
        );
        sortByComboBox.setValue("Date");
        
        // Setup sort direction combo box
        sortDirectionComboBox.getItems().addAll(
                "Descending",
                "Ascending"
        );
        sortDirectionComboBox.setValue("Descending");
        
        // Setup auto-complete for merchant and description fields
        setupAutoComplete();
    }
    
    /**
     * Setup pagination controls.
     */
    private void setupPagination() {
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(10);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            currentPage = newIndex.intValue();
            performSearch();
        });
        
        pageSizeField.setText("20");
        pageSizeField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pageSizeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    /**
     * Setup export options.
     */
    private void setupExportOptions() {
        exportFormatComboBox.getItems().addAll("CSV", "JSON");
        exportFormatComboBox.setValue("CSV");
        
        exportButton.setOnAction(event -> exportResults());
    }
    
    /**
     * Load user accounts into the combo box.
     */
    private void loadAccounts() {
        try {
            if (currentUserId != null) {
                // Get all accounts for this user
                try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT accountNumber, type FROM accounts WHERE userId = ?")) {
                    stmt.setString(1, currentUserId);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        long accountNumber = rs.getLong("accountNumber");
                        String type = rs.getString("type");
                        String displayName = type + " - " + accountNumber;
                        accountComboBox.getItems().add(displayName);
                        accountMap.put(displayName, accountNumber);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading accounts", e);
        }
    }
    
    /**
     * Load transaction categories into the combo box.
     */
    private void loadCategories() {
        try {
            List<TransactionCategory> categories = categoryService.getAllCategories();
            categoryComboBox.getItems().addAll(categories);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading categories", e);
        }
    }
    
    /**
     * Setup auto-complete for merchant and description fields.
     */
    private void setupAutoComplete() {
        // Auto-complete for merchant field
        AutoCompletionTextFieldBinding<String> merchantAutoComplete = 
                new AutoCompletionTextFieldBinding<>(merchantField, param -> {
                    String prefix = param.getUserText();
                    List<String> suggestions = searchService.getSuggestions(
                            currentUserId, prefix, "merchantName", 10);
                    return FXCollections.observableArrayList(suggestions);
                });
        
        // Auto-complete for description field
        AutoCompletionTextFieldBinding<String> descriptionAutoComplete = 
                new AutoCompletionTextFieldBinding<>(descriptionField, param -> {
                    String prefix = param.getUserText();
                    List<String> suggestions = searchService.getSuggestions(
                            currentUserId, prefix, "description", 10);
                    return FXCollections.observableArrayList(suggestions);
                });
    }
    
    /**
     * Load saved searches from the service.
     */
    private void loadSavedSearches() {
        if (currentUserId != null) {
            try {
                savedSearches = searchService.getRecentSearchCriteria(currentUserId, 10);
                updateSavedSearchesUI();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading saved searches", e);
            }
        }
    }
    
    /**
     * Update the saved searches UI.
     */
    private void updateSavedSearchesUI() {
        savedSearchesBox.getChildren().clear();
        
        for (TransactionSearchCriteria criteria : savedSearches) {
            HBox searchItem = new HBox(5);
            
            Button loadButton = new Button("Load");
            loadButton.setOnAction(event -> loadSavedSearch(criteria));
            
            Label nameLabel = new Label(generateSearchName(criteria));
            nameLabel.setMaxWidth(200);
            nameLabel.setStyle("-fx-font-size: 12px;");
            
            searchItem.getChildren().addAll(loadButton, nameLabel);
            savedSearchesBox.getChildren().add(searchItem);
        }
    }
    
    /**
     * Load a saved search criteria.
     * @param criteria The search criteria to load
     */
    private void loadSavedSearch(TransactionSearchCriteria criteria) {
        // Load criteria into UI
        if (criteria.getAccountNumber() != null) {
            String accountKey = accountMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(criteria.getAccountNumber()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("All Accounts");
            accountComboBox.setValue(accountKey);
        } else {
            accountComboBox.setValue("All Accounts");
        }
        
        typeComboBox.setValue(criteria.getType() != null ? criteria.getType().toString() : "All Types");
        
        if (criteria.getStartDate() != null) {
            startDatePicker.setValue(criteria.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        } else {
            startDatePicker.setValue(null);
        }
        
        if (criteria.getEndDate() != null) {
            endDatePicker.setValue(criteria.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        } else {
            endDatePicker.setValue(null);
        }
        
        minAmountField.setText(criteria.getMinAmount() != null ? criteria.getMinAmount().toString() : "");
        maxAmountField.setText(criteria.getMaxAmount() != null ? criteria.getMaxAmount().toString() : "");
        descriptionField.setText(criteria.getDescription() != null ? criteria.getDescription() : "");
        merchantField.setText(criteria.getMerchantName() != null ? criteria.getMerchantName() : "");
        
        TransactionCategory selectedCategory = null;
        if (criteria.getCategoryId() != null) {
            selectedCategory = categoryComboBox.getItems().stream()
                    .filter(c -> c != null && c.getCategoryId() == criteria.getCategoryId())
                    .findFirst()
                    .orElse(null);
        }
        categoryComboBox.setValue(selectedCategory);
        
        includeSubcategoriesCheckbox.setSelected(criteria.getIncludeChildCategories() != null && 
                criteria.getIncludeChildCategories());
        isRecurringCheckbox.setSelected(criteria.isRecurring() != null && criteria.isRecurring());
        isAutoCategorizedCheckbox.setSelected(criteria.isCategorizedAutomatically() != null && 
                criteria.isCategorizedAutomatically());
        
        String sortByValue = "";
        switch (criteria.getSortBy()) {
            case "amount":
                sortByValue = "Amount";
                break;
            case "type":
                sortByValue = "Type";
                break;
            case "description":
                sortByValue = "Description";
                break;
            case "merchantName":
                sortByValue = "Merchant";
                break;
            case "category":
                sortByValue = "Category";
                break;
            case "timestamp":
            default:
                sortByValue = "Date";
                break;
        }
        sortByComboBox.setValue(sortByValue);
        
        sortDirectionComboBox.setValue(criteria.getSortDirection() == 
                TransactionSearchCriteria.SortDirection.ASCENDING ? "Ascending" : "Descending");
        
        pageSizeField.setText(criteria.getPageSize().toString());
        
        // Perform search with loaded criteria
        performSearch();
    }
    
    /**
     * Perform a search with the current UI values.
     */
    @FXML
    public void performSearch() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Build search criteria from UI values
            currentCriteria = buildSearchCriteriaFromUI();
            
            // Execute the search
            SearchResult<Transaction> result = searchService.searchTransactions(currentCriteria);
            
            // Update UI with results
            transactions.clear();
            transactions.addAll(result.getContent());
            
            currentTotalElements = result.getTotalElements();
            int totalPages = result.getTotalPages();
            
            pagination.setPageCount(totalPages > 0 ? totalPages : 1);
            pagination.setCurrentPageIndex(result.getPage());
            
            resultCountLabel.setText("Found " + currentTotalElements + " transactions");
            
            long endTime = System.currentTimeMillis();
            timeLabel.setText("Search completed in " + (endTime - startTime) + "ms");
            
            LOGGER.info("Search completed with " + currentTotalElements + " results");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing search", e);
            showAlert(AlertType.ERROR, "Search Error", "An error occurred while searching: " + e.getMessage());
        }
    }
    
    /**
     * Clear the search form.
     */
    private void clearSearch() {
        accountComboBox.setValue("All Accounts");
        typeComboBox.setValue("All Types");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        minAmountField.clear();
        maxAmountField.clear();
        descriptionField.clear();
        merchantField.clear();
        categoryComboBox.setValue(null);
        includeSubcategoriesCheckbox.setSelected(false);
        isRecurringCheckbox.setSelected(false);
        isAutoCategorizedCheckbox.setSelected(false);
        sortByComboBox.setValue("Date");
        sortDirectionComboBox.setValue("Descending");
        pageSizeField.setText("20");
        
        performSearch();
    }
    
    /**
     * Save the current search criteria.
     */
    private void saveCurrentSearch() {
        if (currentUserId != null && currentCriteria != null) {
            try {
                long searchId = searchService.saveSearchCriteria(currentUserId, currentCriteria);
                LOGGER.info("Saved search criteria with ID: " + searchId);
                loadSavedSearches(); // Refresh the saved searches list
                showAlert(AlertType.INFORMATION, "Search Saved", "Your search has been saved successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving search criteria", e);
                showAlert(AlertType.ERROR, "Save Error", "An error occurred while saving your search: " + e.getMessage());
            }
        }
    }
    
    /**
     * Export the current search results.
     */
    private void exportResults() {
        if (transactions.isEmpty()) {
            showAlert(AlertType.WARNING, "Export Warning", "There are no transactions to export.");
            return;
        }
        
        String format = exportFormatComboBox.getValue().toLowerCase();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.toUpperCase() + " Files", "*." + format));
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileChooser.setInitialFileName("transactions_export_" + timestamp + "." + format);
        
        // Show directory chooser dialog
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        
        if (file != null) {
            try {
                boolean success = searchService.exportTransactions(
                        new ArrayList<>(transactions), format, file.getAbsolutePath());
                
                if (success) {
                    showAlert(AlertType.INFORMATION, "Export Successful", 
                            "Transactions have been exported to " + file.getAbsolutePath());
                } else {
                    showAlert(AlertType.ERROR, "Export Error", 
                            "An error occurred while exporting transactions.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error exporting transactions", e);
                showAlert(AlertType.ERROR, "Export Error", 
                        "An error occurred while exporting transactions: " + e.getMessage());
            }
        }
    }
    
    /**
     * Build a search criteria object from the current UI values.
     * @return The search criteria
     */
    private TransactionSearchCriteria buildSearchCriteriaFromUI() {
        TransactionSearchCriteria.Builder builder = new TransactionSearchCriteria.Builder();
        
        // Account
        String selectedAccount = accountComboBox.getValue();
        if (selectedAccount != null && !selectedAccount.equals("All Accounts")) {
            builder.accountNumber(accountMap.get(selectedAccount));
        }
        
        // Transaction type
        String selectedType = typeComboBox.getValue();
        if (selectedType != null && !selectedType.equals("All Types")) {
            builder.type(Transaction.Type.valueOf(selectedType));
        }
        
        // Date range
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        Date startDateObj = null;
        Date endDateObj = null;
        
        if (startDate != null) {
            startDateObj = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        
        if (endDate != null) {
            // Set end date to the end of the day
            endDateObj = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusSeconds(1).toInstant());
        }
        
        builder.dateRange(startDateObj, endDateObj);
        
        // Amount range
        try {
            if (!minAmountField.getText().isEmpty()) {
                builder.amountRange(Double.parseDouble(minAmountField.getText()), null);
            }
            
            if (!maxAmountField.getText().isEmpty()) {
                Double minAmount = builder.build().getMinAmount();
                builder.amountRange(minAmount, Double.parseDouble(maxAmountField.getText()));
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid amount format: " + e.getMessage());
        }
        
        // Description and merchant
        if (!descriptionField.getText().isEmpty()) {
            builder.description(descriptionField.getText());
        }
        
        if (!merchantField.getText().isEmpty()) {
            builder.merchantName(merchantField.getText());
        }
        
        // Category
        TransactionCategory selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null) {
            builder.category(selectedCategory);
        }
        
        // Checkboxes
        builder.includeChildCategories(includeSubcategoriesCheckbox.isSelected());
        
        if (isRecurringCheckbox.isSelected()) {
            builder.recurring(true);
        }
        
        if (isAutoCategorizedCheckbox.isSelected()) {
            builder.categorizedAutomatically(true);
        }
        
        // Sorting
        String sortBy = sortByComboBox.getValue();
        String sortByField;
        switch (sortBy) {
            case "Amount":
                sortByField = "amount";
                break;
            case "Type":
                sortByField = "type";
                break;
            case "Description":
                sortByField = "description";
                break;
            case "Merchant":
                sortByField = "merchantName";
                break;
            case "Category":
                sortByField = "category";
                break;
            case "Date":
            default:
                sortByField = "timestamp";
                break;
        }
        
        TransactionSearchCriteria.SortDirection direction = 
                sortDirectionComboBox.getValue().equals("Ascending") 
                        ? TransactionSearchCriteria.SortDirection.ASCENDING 
                        : TransactionSearchCriteria.SortDirection.DESCENDING;
        
        builder.sorting(sortByField, direction);
        
        // Pagination
        int pageSize = 20;
        try {
            pageSize = Integer.parseInt(pageSizeField.getText());
            if (pageSize <= 0) {
                pageSize = 20;
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid page size: " + e.getMessage());
        }
        
        builder.pagination(currentPage, pageSize);
        
        return builder.build();
    }
    
    /**
     * Generate a descriptive name for the search criteria for display purposes.
     * @param criteria The search criteria
     * @return A descriptive name
     */
    private String generateSearchName(TransactionSearchCriteria criteria) {
        List<String> parts = new ArrayList<>();
        
        // Add account number if present
        if (criteria.getAccountNumber() != null) {
            String accountKey = accountMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(criteria.getAccountNumber()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("Account #" + criteria.getAccountNumber());
            parts.add(accountKey);
        }
        
        // Add transaction type if present
        if (criteria.getType() != null) {
            parts.add(criteria.getType().toString());
        }
        
        // Add date range if present
        if (criteria.getStartDate() != null || criteria.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            String dateRange = "";
            if (criteria.getStartDate() != null) {
                dateRange += sdf.format(criteria.getStartDate());
            } else {
                dateRange += "All";
            }
            dateRange += " to ";
            if (criteria.getEndDate() != null) {
                dateRange += sdf.format(criteria.getEndDate());
            } else {
                dateRange += "Now";
            }
            parts.add(dateRange);
        }
        
        // Create the name
        if (parts.isEmpty()) {
            return "All Transactions";
        } else if (parts.size() == 1) {
            return parts.get(0);
        } else {
            return String.join(", ", parts);
        }
    }
    
    /**
     * Show an alert dialog.
     * @param type The alert type
     * @param title The alert title
     * @param message The alert message
     */
    @Override
    protected void showAlert(AlertType type, String title, String message) {
        super.showAlert(type, title, message);
    }
    
    /**
     * Helper class for text field auto-completion.
     * @param <T> The type of items to suggest
     */
    private class AutoCompletionTextFieldBinding<T> {
        private final TextField textField;
        private final SuggestionProvider<T> suggestionProvider;
        
        public AutoCompletionTextFieldBinding(TextField textField, SuggestionProvider<T> suggestionProvider) {
            this.textField = textField;
            this.suggestionProvider = suggestionProvider;
            
            textField.setOnKeyReleased(event -> {
                switch (event.getCode()) {
                    case ENTER:
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        handleUserInput();
                        break;
                }
            });
        }
        
        private void handleUserInput() {
            String userText = textField.getText();
            if (userText.length() < 2) {
                return;
            }
            
            // Get suggestions
            ObservableList<T> suggestions = suggestionProvider.call(new AutoCompletionRequest(userText));
            
            // Show suggestions if there are any
            if (!suggestions.isEmpty()) {
                // In a real implementation, this would show a dropdown or popup with suggestions
                // For simplicity in this implementation, we'll just update the field if there's a perfect match
                if (suggestions.size() == 1) {
                    String suggestion = suggestions.get(0).toString();
                    if (suggestion.equalsIgnoreCase(userText)) {
                        textField.setText(suggestion);
                        textField.positionCaret(suggestion.length());
                    }
                }
            }
        }
    }
    
    /**
     * Interface for suggestion providers.
     * @param <T> The type of items to suggest
     */
    private interface SuggestionProvider<T> {
        ObservableList<T> call(AutoCompletionRequest request);
    }
    
    /**
     * Class representing an auto-completion request.
     */
    private class AutoCompletionRequest {
        private final String userText;
        
        public AutoCompletionRequest(String userText) {
            this.userText = userText;
        }
        
        public String getUserText() {
            return userText;
        }
    }
}