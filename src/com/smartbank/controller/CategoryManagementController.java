package com.smartbank.controller;

import com.smartbank.model.TransactionCategory;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.category.CategoryException;
import com.smartbank.service.category.CategoryService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the category management view.
 */
public class CategoryManagementController {
    private static final Logger LOGGER = Logger.getLogger(CategoryManagementController.class.getName());
    
    // FXML Controls
    @FXML private TableView<TransactionCategory> tblCategories;
    @FXML private TableColumn<TransactionCategory, Long> colCategoryId;
    @FXML private TableColumn<TransactionCategory, String> colName;
    @FXML private TableColumn<TransactionCategory, String> colDescription;
    @FXML private TableColumn<TransactionCategory, String> colParent;
    @FXML private TableColumn<TransactionCategory, String> colColor;
    @FXML private TableColumn<TransactionCategory, String> colKeywords;
    @FXML private TableColumn<TransactionCategory, Double> colBudget;
    
    @FXML private TextField txtName;
    @FXML private TextField txtDescription;
    @FXML private ComboBox<TransactionCategory> cmbParent;
    @FXML private ColorPicker colorPicker;
    @FXML private TextField txtKeywords;
    @FXML private TextField txtBudget;
    @FXML private CheckBox chkSystem;
    
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    
    @FXML private Label lblStatus;
    
    // Services
    private final CategoryService categoryService;
    
    // Data
    private ObservableList<TransactionCategory> categories;
    private TransactionCategory selectedCategory;
    
    /**
     * Constructor.
     */
    public CategoryManagementController() {
        this.categoryService = ServiceFactory.getCategoryService();
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        // Initialize the table
        setupCategoryTable();
        
        // Load categories
        loadCategories();
        
        // Setup listeners
        setupListeners();
        
        // Setup parent category combo box
        setupParentCombo();
        
        // Initial state
        clearForm();
    }
    
    /**
     * Setup the category table.
     */
    private void setupCategoryTable() {
        colCategoryId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Parent category name
        colParent.setCellValueFactory(cellData -> {
            TransactionCategory category = cellData.getValue();
            TransactionCategory parent = category.getParent();
            return new SimpleStringProperty(parent != null ? parent.getName() : "");
        });
        
        // Color column with color rectangle
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colColor.setCellFactory(column -> new TableCell<TransactionCategory, String>() {
            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        Rectangle rect = new Rectangle(16, 16);
                        rect.setFill(Color.web(colorHex));
                        rect.setStroke(Color.BLACK);
                        setGraphic(rect);
                        setText(colorHex);
                    } catch (Exception e) {
                        setText(colorHex);
                    }
                }
            }
        });
        
        colKeywords.setCellValueFactory(new PropertyValueFactory<>("keywords"));
        colBudget.setCellValueFactory(new PropertyValueFactory<>("budgetAmount"));
        
        // Row selection listener
        tblCategories.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedCategory = newSelection;
                        populateForm(selectedCategory);
                    }
                });
    }
    
    /**
     * Load categories from service.
     */
    private void loadCategories() {
        try {
            List<TransactionCategory> categoryList = categoryService.getAllCategories();
            categories = FXCollections.observableArrayList(categoryList);
            tblCategories.setItems(categories);
            
            // Also update parent combo
            updateParentCombo();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading categories: " + e.getMessage(), e);
            showError("Error loading categories: " + e.getMessage());
        }
    }
    
    /**
     * Setup listeners for form controls.
     */
    private void setupListeners() {
        // Enable update button when a category is selected
        btnUpdate.disableProperty().bind(
                tblCategories.getSelectionModel().selectedItemProperty().isNull());
        
        // Enable delete button when a non-system category is selected
        btnDelete.disableProperty().bind(
                tblCategories.getSelectionModel().selectedItemProperty().isNull()
                .or(chkSystem.selectedProperty()));
    }
    
    /**
     * Setup parent category combo box.
     */
    private void setupParentCombo() {
        // Converter for displaying category names
        cmbParent.setConverter(new javafx.util.StringConverter<TransactionCategory>() {
            @Override
            public String toString(TransactionCategory category) {
                return category == null ? "" : category.getName();
            }
            
            @Override
            public TransactionCategory fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Cell factory for displaying category names
        cmbParent.setCellFactory(new Callback<ListView<TransactionCategory>, ListCell<TransactionCategory>>() {
            @Override
            public ListCell<TransactionCategory> call(ListView<TransactionCategory> param) {
                return new ListCell<TransactionCategory>() {
                    @Override
                    protected void updateItem(TransactionCategory item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        
        updateParentCombo();
    }
    
    /**
     * Update parent combo box with categories.
     */
    private void updateParentCombo() {
        if (categories != null) {
            // Add empty option for no parent
            ObservableList<TransactionCategory> parentOptions = FXCollections.observableArrayList();
            parentOptions.add(null); // No parent option
            
            // Add all categories except the selected one (to prevent circular references)
            if (selectedCategory != null) {
                parentOptions.addAll(categories.filtered(c -> 
                        c.getCategoryId() != selectedCategory.getCategoryId() && 
                        !c.isDescendantOf(selectedCategory)));
            } else {
                parentOptions.addAll(categories);
            }
            
            cmbParent.setItems(parentOptions);
        }
    }
    
    /**
     * Populate form with category data.
     * 
     * @param category The category to display
     */
    private void populateForm(TransactionCategory category) {
        if (category != null) {
            txtName.setText(category.getName());
            txtDescription.setText(category.getDescription());
            cmbParent.setValue(category.getParent());
            
            if (category.getColor() != null && !category.getColor().isEmpty()) {
                try {
                    colorPicker.setValue(Color.web(category.getColor()));
                } catch (Exception e) {
                    colorPicker.setValue(Color.GRAY);
                }
            } else {
                colorPicker.setValue(Color.GRAY);
            }
            
            txtKeywords.setText(category.getKeywords());
            txtBudget.setText(String.valueOf(category.getBudgetAmount()));
            chkSystem.setSelected(category.isSystem());
            
            // Update buttons
            btnUpdate.setDisable(false);
            btnDelete.setDisable(category.isSystem());
        }
    }
    
    /**
     * Clear the form.
     */
    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        cmbParent.setValue(null);
        colorPicker.setValue(Color.GRAY);
        txtKeywords.clear();
        txtBudget.setText("0.0");
        chkSystem.setSelected(false);
        
        selectedCategory = null;
        tblCategories.getSelectionModel().clearSelection();
    }
    
    /**
     * Validate form input.
     * 
     * @return true if input is valid, false otherwise
     */
    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showError("Category name is required");
            txtName.requestFocus();
            return false;
        }
        
        // Check budget amount
        try {
            if (!txtBudget.getText().trim().isEmpty()) {
                double budget = Double.parseDouble(txtBudget.getText().trim());
                if (budget < 0) {
                    showError("Budget amount cannot be negative");
                    txtBudget.requestFocus();
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            showError("Invalid budget amount");
            txtBudget.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Handle add category button click.
     */
    @FXML
    private void handleAddCategory(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Create a new category
            TransactionCategory category = new TransactionCategory(
                    txtName.getText().trim(),
                    txtDescription.getText().trim());
            
            // Set parent
            category.setParent(cmbParent.getValue());
            
            // Set color
            String colorHex = String.format("#%02X%02X%02X",
                    (int) (colorPicker.getValue().getRed() * 255),
                    (int) (colorPicker.getValue().getGreen() * 255),
                    (int) (colorPicker.getValue().getBlue() * 255));
            category.setColor(colorHex);
            
            // Set keywords
            category.setKeywords(txtKeywords.getText().trim());
            
            // Set budget
            if (!txtBudget.getText().trim().isEmpty()) {
                category.setBudgetAmount(Double.parseDouble(txtBudget.getText().trim()));
            }
            
            // Set system flag
            category.setSystem(chkSystem.isSelected());
            
            // Save category
            TransactionCategory savedCategory = categoryService.createCategory(category);
            
            // Update table
            categories.add(savedCategory);
            tblCategories.refresh();
            
            // Update parent combo
            updateParentCombo();
            
            // Clear form
            clearForm();
            
            showSuccess("Category added successfully");
        } catch (CategoryException e) {
            showError("Error adding category: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding category: " + e.getMessage(), e);
            showError("Error adding category: " + e.getMessage());
        }
    }
    
    /**
     * Handle update category button click.
     */
    @FXML
    private void handleUpdateCategory(ActionEvent event) {
        if (selectedCategory == null) {
            showError("Please select a category to update");
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        try {
            // Update category
            selectedCategory.setName(txtName.getText().trim());
            selectedCategory.setDescription(txtDescription.getText().trim());
            selectedCategory.setParent(cmbParent.getValue());
            
            // Set color
            String colorHex = String.format("#%02X%02X%02X",
                    (int) (colorPicker.getValue().getRed() * 255),
                    (int) (colorPicker.getValue().getGreen() * 255),
                    (int) (colorPicker.getValue().getBlue() * 255));
            selectedCategory.setColor(colorHex);
            
            // Set keywords
            selectedCategory.setKeywords(txtKeywords.getText().trim());
            
            // Set budget
            if (!txtBudget.getText().trim().isEmpty()) {
                selectedCategory.setBudgetAmount(Double.parseDouble(txtBudget.getText().trim()));
            }
            
            // Only allow changing system flag if not already a system category
            if (!selectedCategory.isSystem()) {
                selectedCategory.setSystem(chkSystem.isSelected());
            }
            
            // Save category
            TransactionCategory updatedCategory = categoryService.updateCategory(selectedCategory);
            
            // Update table
            int index = categories.indexOf(selectedCategory);
            if (index >= 0) {
                categories.set(index, updatedCategory);
            }
            tblCategories.refresh();
            
            // Update parent combo
            updateParentCombo();
            
            showSuccess("Category updated successfully");
        } catch (CategoryException e) {
            showError("Error updating category: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating category: " + e.getMessage(), e);
            showError("Error updating category: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete category button click.
     */
    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        if (selectedCategory == null) {
            showError("Please select a category to delete");
            return;
        }
        
        if (selectedCategory.isSystem()) {
            showError("Cannot delete system category");
            return;
        }
        
        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete the category '" + selectedCategory.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Category");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                // Delete category
                categoryService.deleteCategory(selectedCategory.getCategoryId());
                
                // Update table
                categories.remove(selectedCategory);
                tblCategories.refresh();
                
                // Update parent combo
                updateParentCombo();
                
                // Clear form
                clearForm();
                
                showSuccess("Category deleted successfully");
            } catch (CategoryException e) {
                showError("Error deleting category: " + e.getMessage());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting category: " + e.getMessage(), e);
                showError("Error deleting category: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle clear form button click.
     */
    @FXML
    private void handleClearForm(ActionEvent event) {
        clearForm();
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadCategories();
        clearForm();
    }
    
    /**
     * Show an error message.
     * 
     * @param message The error message
     */
    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: red;");
    }
    
    /**
     * Show a success message.
     * 
     * @param message The success message
     */
    private void showSuccess(String message) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: green;");
    }
}