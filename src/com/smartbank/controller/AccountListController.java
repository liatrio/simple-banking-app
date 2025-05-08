package com.smartbank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import com.smartbank.model.Account;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AccountListController {
    @FXML private TableView<AccountRow> accountTable;
    @FXML private TableColumn<AccountRow, Long> colAccountNumber;
    @FXML private TableColumn<AccountRow, String> colHolder;
    @FXML private TableColumn<AccountRow, String> colType;
    @FXML private TableColumn<AccountRow, Double> colBalance;

    private ObservableList<AccountRow> accountData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colAccountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colHolder.setCellValueFactory(new PropertyValueFactory<>("holder"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        // Load accounts from DataStore
        refreshAccountList();
        accountTable.setItems(accountData);
    }

    public void refreshAccountList() {
        accountData.clear();
        try {
            // Get the current user from the security context
            com.smartbank.model.User currentUser = com.smartbank.auth.SecurityContext.getCurrentUser();
            
            if (currentUser == null) {
                return;  // No user authenticated, nothing to show
            }
            
            java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
            java.sql.PreparedStatement stmt;
            java.sql.ResultSet rs;
            
            // Check if user is admin - admins can see all accounts
            if (currentUser.hasPermission("SYSTEM_ADMIN") || "admin".equalsIgnoreCase(currentUser.getRole())) {
                // Admin can see all accounts
                stmt = conn.prepareStatement("SELECT * FROM accounts");
                rs = stmt.executeQuery();
            } else {
                // Regular users can only see their own accounts
                stmt = conn.prepareStatement("SELECT * FROM accounts WHERE userId = ?");
                stmt.setString(1, currentUser.getUserId());
                rs = stmt.executeQuery();
            }
            
            while (rs.next()) {
                long accountNumber = rs.getLong("accountNumber");
                String holder = rs.getString("userId");
                String type = rs.getString("type");
                double balance = rs.getDouble("balance");
                accountData.add(new AccountRow(accountNumber, holder, type, balance));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example method to add a new account
    public void addAccount(Account acc) {
        com.smartbank.model.DataStore.getInstance().addAccount(acc);
        refreshAccountList();
    }

    public static class AccountRow {
        private final long accountNumber;
        private final String holder;
        private final String type;
        private final double balance;
        public AccountRow(long accountNumber, String holder, String type, double balance) {
            this.accountNumber = accountNumber;
            this.holder = holder;
            this.type = type;
            this.balance = balance;
        }
        public long getAccountNumber() { return accountNumber; }
        public String getHolder() { return holder; }
        public String getType() { return type; }
        public double getBalance() { return balance; }
    }

    @FXML
    private void accountTableClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && accountTable.getSelectionModel().getSelectedItem() != null) {
            AccountRow selected = accountTable.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartbank/view/AccountDetailView.fxml"));
                Parent detailRoot = loader.load();
                AccountDetailController controller = loader.getController();
                controller.setAccountRow(selected);
                Stage detailStage = new Stage();
                detailStage.setTitle("Account Details");
                detailStage.setScene(new Scene(detailRoot));
                detailStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

