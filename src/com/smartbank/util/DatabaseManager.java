package com.smartbank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    static {
        initializeDatabase();
    }
    private static final String DB_URL = "jdbc:sqlite:smartbank.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Create accounts table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (" +
                    "accountNumber BIGINT PRIMARY KEY, " +
                    "userId VARCHAR(255) NOT NULL, " +
                    "balance DOUBLE PRECISION NOT NULL, " +
                    "creationDate DATETIME NOT NULL, " +
                    "type VARCHAR(31) NOT NULL, " +
                    "interestRate DOUBLE PRECISION, " +
                    "creditLimit DOUBLE PRECISION)" );
            
            // Create transaction categories table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transaction_categories (" +
                    "categoryId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(255) NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "color VARCHAR(10), " +
                    "parent_id INTEGER, " +
                    "keywords TEXT, " +
                    "isSystem BOOLEAN NOT NULL DEFAULT 0, " +
                    "budgetAmount DOUBLE PRECISION DEFAULT 0, " +
                    "FOREIGN KEY(parent_id) REFERENCES transaction_categories(categoryId))" );

            // Create transactions table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transactionId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "accountNumber INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "description TEXT, " +
                    "category_id INTEGER, " +
                    "linkedTransactionId INTEGER, " +
                    "isCategorizedAutomatically BOOLEAN, " +
                    "merchantName TEXT, " +
                    "isRecurring BOOLEAN, " +
                    "FOREIGN KEY(accountNumber) REFERENCES accounts(accountNumber), " +
                    "FOREIGN KEY(category_id) REFERENCES transaction_categories(categoryId))" );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Database verification
    public static boolean verifyConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CRUD for Accounts
    public static boolean insertAccount(long accountNumber, String username, double balance, String creationDate, String type, Double interestRate, Double creditLimit) {
        String sql = "INSERT INTO accounts(accountNumber, userId, balance, creationDate, type, interestRate, creditLimit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountNumber);
            pstmt.setString(2, username);
            pstmt.setDouble(3, balance);
            pstmt.setString(4, creationDate);
            pstmt.setString(5, type);
            if (interestRate != null) pstmt.setDouble(6, interestRate); else pstmt.setNull(6, java.sql.Types.REAL);
            if (creditLimit != null) pstmt.setDouble(7, creditLimit); else pstmt.setNull(7, java.sql.Types.REAL);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateAccountBalance(long accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setLong(2, accountNumber);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAccount(long accountNumber) {
        String sql = "DELETE FROM accounts WHERE accountNumber = ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountNumber);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static java.sql.ResultSet getAccount(long accountNumber) {
        String sql = "SELECT * FROM accounts WHERE accountNumber = ?";
        try {
            Connection conn = getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, accountNumber);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // CRUD for Transactions
    public static boolean insertTransaction(long accountNumber, double amount, String type, String timestamp, String description) {
        String sql = "INSERT INTO transactions(accountNumber, amount, type, timestamp, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountNumber);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, type);
            pstmt.setString(4, timestamp);
            pstmt.setString(5, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static java.sql.ResultSet getTransactionsForAccount(long accountNumber) {
        String sql = "SELECT * FROM transactions WHERE accountNumber = ? ORDER BY timestamp DESC";
        try {
            Connection conn = getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, accountNumber);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
