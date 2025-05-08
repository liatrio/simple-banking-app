package com.smartbank.controller;

import com.smartbank.model.Account;
import com.smartbank.model.User;
import com.smartbank.auth.SecurityContext;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.service.statement.StatementGenerationService;
import com.smartbank.service.statement.StatementRecord;
import com.smartbank.service.statement.StatementType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class StatementGenerationController extends BaseController {

    @FXML
    private ComboBox<Account> accountComboBox;
    
    @FXML
    private ComboBox<StatementType> statementTypeComboBox;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private TableView<StatementRecord> statementsTableView;
    
    @FXML
    private TableColumn<StatementRecord, Date> dateColumn;
    
    @FXML
    private TableColumn<StatementRecord, String> typeColumn;
    
    @FXML
    private TableColumn<StatementRecord, String> periodColumn;
    
    @FXML
    private TableColumn<StatementRecord, String> accountColumn;
    
    @FXML
    private Button generateButton;
    
    @FXML
    private Button viewButton;
    
    @FXML
    private Button downloadButton;
    
    @FXML
    private Button emailButton;
    
    private StatementGenerationService statementService;
    private User currentUser;
    private ObservableList<StatementRecord> statementRecords = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        statementService = com.smartbank.service.ServiceFactory.getStatementGenerationService();
        currentUser = com.smartbank.auth.SecurityContext.getInstance().getCurrentUser();
        
        // Initialize account combo box
        List<Account> accounts = RepositoryFactory.getAccountRepository().findByUser(currentUser);
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
        accountComboBox.setCellFactory(param -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getAccountNumber() + " - " + item.getAccountName());
                }
            }
        });
        accountComboBox.setButtonCell(accountComboBox.getCellFactory().call(null));
        
        // Initialize statement type combo box
        statementTypeComboBox.setItems(FXCollections.observableArrayList(StatementType.values()));
        statementTypeComboBox.setCellFactory(param -> new ListCell<StatementType>() {
            @Override
            protected void updateItem(StatementType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        statementTypeComboBox.setButtonCell(statementTypeComboBox.getCellFactory().call(null));
        statementTypeComboBox.getSelectionModel().selectFirst();
        
        // Initialize date pickers
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        startDatePicker.setValue(firstOfMonth.minusMonths(1));
        endDatePicker.setValue(firstOfMonth.minusDays(1));
        
        // Setup statement history table
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("generationDate"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("statementType"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodDescription"));
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        
        statementsTableView.setItems(statementRecords);
        
        // Load statement history
        loadStatementHistory();
        
        // Button handlers
        setupButtonListeners();
    }
    
    private void setupButtonListeners() {
        generateButton.setOnAction(this::handleGenerateStatement);
        viewButton.setOnAction(this::handleViewStatement);
        downloadButton.setOnAction(this::handleDownloadStatement);
        emailButton.setOnAction(this::handleEmailStatement);
        
        // Disable action buttons until a statement is selected
        viewButton.setDisable(true);
        downloadButton.setDisable(true);
        emailButton.setDisable(true);
        
        statementsTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                viewButton.setDisable(!hasSelection);
                downloadButton.setDisable(!hasSelection);
                emailButton.setDisable(!hasSelection);
            }
        );
    }
    
    private void loadStatementHistory() {
        try {
            statementRecords.clear();
            List<StatementRecord> statements = statementService.getStatementHistory(currentUser);
            if (statements != null) {
                statementRecords.addAll(statements);
            }
        } catch (Exception e) {
            System.err.println("Error loading statement history: " + e.getMessage());
            e.printStackTrace();
            // Show a warning but allow the view to load
            showAlert(Alert.AlertType.WARNING, "Warning", 
                     "Unable to load statement history. Statement generation will still work.");
        }
    }
    
    @FXML
    private void handleGenerateStatement(ActionEvent event) {
        Account selectedAccount = accountComboBox.getValue();
        StatementType selectedType = statementTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (selectedAccount == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an account.");
            return;
        }
        
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Date Selection Required", "Please select start and end dates.");
            return;
        }
        
        if (endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range", "End date cannot be before start date.");
            return;
        }
        
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        try {
            StatementRecord statement = statementService.generateAndStoreStatement(
                selectedAccount, 
                currentUser, 
                start, 
                end, 
                selectedType
            );
            loadStatementHistory();
            statementsTableView.getSelectionModel().select(statement);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Statement generated successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate statement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewStatement(ActionEvent event) {
        StatementRecord selected = statementsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
            statementService.viewStatement(selected);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to view statement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDownloadStatement(ActionEvent event) {
        StatementRecord selected = statementsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Statement");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        String defaultFileName = selected.getDefaultFileName();
        fileChooser.setInitialFileName(defaultFileName);
        
        File file = fileChooser.showSaveDialog(generateButton.getScene().getWindow());
        if (file != null) {
            try {
                byte[] content = statementService.getStatementContent(selected);
                Files.write(file.toPath(), content);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Statement downloaded successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to download statement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleEmailStatement(ActionEvent event) {
        StatementRecord selected = statementsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
            statementService.emailStatement(selected, currentUser.getEmail());
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Statement has been queued for delivery to " + currentUser.getEmail());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to email statement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void showAlert(AlertType type, String title, String message) {
        super.showAlert(type, title, message);
    }
}