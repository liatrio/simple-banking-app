package com.smartbank.controller;

import com.smartbank.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AccountFormController {
    @FXML private TextField txtHolder;
    @FXML private ChoiceBox<String> choiceType;
    @FXML private TextField txtBalance;
    @FXML private TextField txtInterest;
    @FXML private TextField txtCreditLimit;

    @FXML
    public void initialize() {
        choiceType.getItems().addAll("Savings", "Credit");
        choiceType.setValue("Savings");
    }

    @FXML
    private void handleCreateAccount() {
        String holder = txtHolder.getText();
        String type = choiceType.getValue();
        String balanceStr = txtBalance.getText();
        String interestStr = txtInterest.getText();
        String creditLimitStr = txtCreditLimit.getText();
        if (holder.isEmpty() || balanceStr.isEmpty()) {
            showAlert("Error", "Account holder and initial balance are required.");
            return;
        }
        double balance;
        try {
            balance = Double.parseDouble(balanceStr);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid balance.");
            return;
        }
        User user = new User(holder, "password", "customer"); // Placeholder password/role
        long accountNumber = System.currentTimeMillis(); // Simple unique number for demo
        String creationDate = java.time.LocalDateTime.now().toString();
        Double interest = null, creditLimit = null;
        if (type.equals("Savings")) {
            if (!interestStr.isEmpty()) {
                try { interest = Double.parseDouble(interestStr); } catch (NumberFormatException ignored) {}
            }
        } else {
            if (!creditLimitStr.isEmpty()) {
                try { creditLimit = Double.parseDouble(creditLimitStr); } catch (NumberFormatException ignored) {}
            }
        }
        boolean inserted = com.smartbank.util.DatabaseManager.insertAccount(
            accountNumber,
            holder,
            balance,
            creationDate,
            type,
            interest,
            creditLimit
        );
        if (inserted) {
            showAlert("Success", "Account created successfully.");
            clearForm();
        } else {
            showAlert("Error", "Failed to create account in database.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(title.equals("Error") ? AlertType.ERROR : AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void clearForm() {
        txtHolder.clear();
        txtBalance.clear();
        txtInterest.clear();
        txtCreditLimit.clear();
        choiceType.setValue("Savings");
    }
}
