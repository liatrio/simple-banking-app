package com.smartbank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.List;
import java.util.ArrayList;

public class TransactionFormController {
    @FXML private TextField txtAccountNumber;
    @FXML private TextField txtAmount;
    @FXML private ChoiceBox<String> choiceType;
    @FXML private Button btnSubmit;

    // For demo: simple in-memory account list
    private static List<Account> accounts = new ArrayList<>();
    static {
        User user1 = new User("alice", "password", "customer");
        User user2 = new User("bob", "password", "customer");
        accounts.add(new SavingsAccount(user1, 1200.0, 0.02));
        accounts.add(new CreditAccount(user2, 500.0, 1000.0));
    }

    @FXML
    public void initialize() {
        choiceType.getItems().addAll("Deposit", "Withdraw");
        choiceType.setValue("Deposit");
        btnSubmit.setOnAction(e -> handleTransaction());
    }

    private void handleTransaction() {
        String accNumStr = txtAccountNumber.getText();
        String amountStr = txtAmount.getText();
        String type = choiceType.getValue();
        if (accNumStr.isEmpty() || amountStr.isEmpty()) {
            showAlert("Error", "Account number and amount are required.");
            return;
        }
        long accNum;
        double amount;
        try {
            accNum = Long.parseLong(accNumStr);
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Invalid account number or amount.");
            return;
        }
        // Fetch account from DB
        try {
            java.sql.ResultSet rs = com.smartbank.util.DatabaseManager.getAccount(accNum);
            if (rs == null || !rs.next()) {
                showAlert("Error", "Account not found.");
                if (rs != null) rs.close();
                return;
            }
            double balance = rs.getDouble("balance");
            rs.close();
            if (type.equals("Deposit")) {
                double newBalance = balance + amount;
                boolean updated = com.smartbank.util.DatabaseManager.updateAccountBalance(accNum, newBalance);
                boolean txLogged = com.smartbank.util.DatabaseManager.insertTransaction(
                    accNum, amount, "Deposit", java.time.LocalDateTime.now().toString(), "Deposit via UI");
                if (updated && txLogged) {
                    showAlert("Success", "Deposited $" + amount + " to account " + accNum);
                } else {
                    showAlert("Error", "Failed to process deposit.");
                }
            } else {
                if (balance < amount) {
                    showAlert("Error", "Insufficient funds.");
                    return;
                }
                double newBalance = balance - amount;
                boolean updated = com.smartbank.util.DatabaseManager.updateAccountBalance(accNum, newBalance);
                boolean txLogged = com.smartbank.util.DatabaseManager.insertTransaction(
                    accNum, amount, "Withdraw", java.time.LocalDateTime.now().toString(), "Withdrawal via UI");
                if (updated && txLogged) {
                    showAlert("Success", "Withdrew $" + amount + " from account " + accNum);
                } else {
                    showAlert("Error", "Failed to process withdrawal.");
                }
            }
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(title.equals("Error") ? AlertType.ERROR : AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

