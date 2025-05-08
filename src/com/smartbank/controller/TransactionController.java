package com.smartbank.controller;

import com.smartbank.model.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.List;

public class TransactionController {
    private final DataStore dataStore = DataStore.getInstance();

    public boolean processDeposit(long accountNumber, double amount, String description) {
        if (amount <= 0) {
            showAlert("Error", "Amount must be positive.");
            return false;
        }
        Account acc = dataStore.getAccountByNumber(accountNumber);
        if (acc == null) {
            showAlert("Error", "Account not found.");
            return false;
        }
        acc.deposit(amount);
        dataStore.recordTransaction(accountNumber, amount, Transaction.Type.DEPOSIT, description);
        showAlert("Success", "Deposit successful.");
        return true;
    }

    public boolean processWithdrawal(long accountNumber, double amount, String description) {
        if (amount <= 0) {
            showAlert("Error", "Amount must be positive.");
            return false;
        }
        Account acc = dataStore.getAccountByNumber(accountNumber);
        if (acc == null) {
            showAlert("Error", "Account not found.");
            return false;
        }
        try {
            acc.withdraw(amount);
            dataStore.recordTransaction(accountNumber, amount, Transaction.Type.WITHDRAWAL, description);
            showAlert("Success", "Withdrawal successful.");
            return true;
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
            return false;
        }
    }

    public List<Transaction> getTransactionHistory(long accountNumber) {
        return dataStore.getTransactionsForAccount(accountNumber);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(title.equals("Error") ? AlertType.ERROR : AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
