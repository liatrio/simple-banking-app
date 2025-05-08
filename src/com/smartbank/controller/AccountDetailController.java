package com.smartbank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import com.smartbank.controller.AccountListController.AccountRow;

public class AccountDetailController {
    @FXML private Label lblAccountNumber;
    @FXML private Label lblHolder;
    @FXML private Label lblType;
    @FXML private Label lblBalance;

    private long currentAccountNumber;

    public void setAccountRow(AccountListController.AccountRow row) {
        currentAccountNumber = row.getAccountNumber();
        try {
            // First check if the current user has permission to view this account
            com.smartbank.model.User currentUser = com.smartbank.auth.SecurityContext.getCurrentUser();
            
            if (currentUser == null) {
                setErrorView("Not authenticated");
                return;
            }
            
            // Retrieve account details
            java.sql.ResultSet rs = com.smartbank.util.DatabaseManager.getAccount(currentAccountNumber);
            
            if (rs != null && rs.next()) {
                // For security, verify this account belongs to the current user unless they're an admin
                String accountUserId = rs.getString("userId");
                boolean isAdmin = currentUser.hasPermission("SYSTEM_ADMIN") || "admin".equalsIgnoreCase(currentUser.getRole());
                
                if (isAdmin || accountUserId.equals(currentUser.getUserId())) {
                    // User is authorized to view this account
                    lblAccountNumber.setText(String.valueOf(rs.getLong("accountNumber")));
                    lblHolder.setText(rs.getString("userId")); // This should be joined with username in a real app
                    lblType.setText(rs.getString("type"));
                    lblBalance.setText(String.format("%.2f", rs.getDouble("balance")));
                } else {
                    // User is not authorized to view this account
                    setErrorView("Not authorized to view this account");
                }
            } else {
                setErrorView("Account not found");
            }
            
            if (rs != null) rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            setErrorView("Error loading account details: " + e.getMessage());
        }
    }
    
    private void setErrorView(String message) {
        lblAccountNumber.setText("N/A");
        lblHolder.setText("N/A");
        lblType.setText("N/A");
        lblBalance.setText("N/A");
        
        // In a real app, show an error dialog or message
        System.err.println("Account detail error: " + message);
    }

    @FXML
    private void showTransactionHistory() {
        try {
            // First verify the user has access to this account
            com.smartbank.model.User currentUser = com.smartbank.auth.SecurityContext.getCurrentUser();
            
            if (currentUser == null) {
                System.err.println("Not authenticated");
                return;
            }
            
            // Check if the account belongs to the current user or if they're an admin
            boolean isAuthorized = false;
            boolean isAdmin = currentUser.hasPermission("SYSTEM_ADMIN") || "admin".equalsIgnoreCase(currentUser.getRole());
            
            if (isAdmin) {
                isAuthorized = true;
            } else {
                // Get the account details to check ownership
                java.sql.ResultSet rs = com.smartbank.util.DatabaseManager.getAccount(currentAccountNumber);
                if (rs != null && rs.next()) {
                    String accountUserId = rs.getString("userId");
                    isAuthorized = accountUserId.equals(currentUser.getUserId());
                    rs.close();
                }
            }
            
            if (!isAuthorized) {
                System.err.println("Not authorized to view transactions for this account");
                return;
            }
            
            // User is authorized, show transaction history
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/smartbank/view/TransactionHistoryView.fxml"));
            javafx.scene.Parent root = loader.load();
            TransactionHistoryController controller = loader.getController();
            controller.showTransactionsForAccount(currentAccountNumber);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Transaction History");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
