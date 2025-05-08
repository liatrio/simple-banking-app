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
            java.sql.ResultSet rs = com.smartbank.util.DatabaseManager.getAccount(currentAccountNumber);
            if (rs != null && rs.next()) {
                lblAccountNumber.setText(String.valueOf(rs.getLong("accountNumber")));
                lblHolder.setText(rs.getString("username"));
                lblType.setText(rs.getString("type"));
                lblBalance.setText(String.format("%.2f", rs.getDouble("balance")));
            } else {
                lblAccountNumber.setText("N/A");
                lblHolder.setText("N/A");
                lblType.setText("N/A");
                lblBalance.setText("N/A");
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTransactionHistory() {
        try {
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
