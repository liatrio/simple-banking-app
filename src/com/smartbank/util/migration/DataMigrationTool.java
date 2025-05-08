package com.smartbank.util.migration;

import com.smartbank.model.*;
import com.smartbank.repository.*;
import com.smartbank.service.*;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility tool to migrate data from in-memory storage to the JPA/SQLite database.
 */
public class DataMigrationTool {
    private static final Logger LOGGER = Logger.getLogger(DataMigrationTool.class.getName());
    
    // Repositories
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    // Migration statistics
    private int totalUsers = 0;
    private int migratedUsers = 0;
    private int totalAccounts = 0;
    private int migratedAccounts = 0;
    private int totalTransactions = 0;
    private int migratedTransactions = 0;
    
    // Error tracking
    private final List<String> errors = new ArrayList<>();
    
    // Progress tracking
    private int currentProgress = 0;
    private int totalSteps = 0;
    
    /**
     * Constructor initializing repositories.
     */
    public DataMigrationTool() {
        this.userRepository = RepositoryFactory.getUserRepository();
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    /**
     * Main migration method to migrate all data.
     * @return true if migration was successful, false otherwise
     */
    public boolean migrateData() {
        LOGGER.info("Starting data migration process...");
        long startTime = System.currentTimeMillis();
        
        boolean success = false;
        try {
            // 1. Read in-memory data
            MigrationData data = readInMemoryData();
            
            // Update statistics
            totalUsers = data.getUserCount();
            totalAccounts = data.getAccountCount();
            totalTransactions = data.getTransactionCount();
            
            // Calculate total steps for progress tracking
            totalSteps = totalUsers + totalAccounts + totalTransactions + 3; // 3 additional steps for preparation, validation, and cleanup
            
            LOGGER.info("Found " + totalUsers + " users, " + totalAccounts + " accounts, and " + 
                        totalTransactions + " transactions in memory.");
            
            // 2. Migrate users
            migrateUsers(data);
            
            // 3. Migrate accounts
            migrateAccounts(data);
            
            // 4. Migrate transactions
            migrateTransactions(data);
            
            // 5. Validate migration
            validateMigration(data);
            
            long endTime = System.currentTimeMillis();
            LOGGER.info("Data migration completed in " + (endTime - startTime) + "ms");
            printMigrationSummary();
            success = errors.isEmpty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Migration failed with error: " + e.getMessage(), e);
            errors.add("Fatal error: " + e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    /**
     * Read all data from in-memory storage.
     * @return A MigrationData object containing all in-memory data
     */
    private MigrationData readInMemoryData() {
        LOGGER.info("Reading in-memory data...");
        MigrationData data = new MigrationData();
        
        try {
            // Get the DataStore instance
            DataStore dataStore = DataStore.getInstance();
            
            // Read data from DataStore
            List<Account> accounts = dataStore.getAllAccounts();
            List<Transaction> transactions = dataStore.getAllTransactions();
            
            // Extract users from accounts to avoid duplicates
            Map<String, User> uniqueUsers = new HashMap<>();
            for (Account account : accounts) {
                User user = account.getAccountHolder();
                if (user != null && !uniqueUsers.containsKey(user.getUsername())) {
                    uniqueUsers.put(user.getUsername(), user);
                }
            }
            
            // Add users to migration data
            for (User user : uniqueUsers.values()) {
                data.addUser(user);
            }
            
            // Add accounts to migration data
            for (Account account : accounts) {
                data.addAccount(account);
            }
            
            // Add transactions to migration data
            for (Transaction transaction : transactions) {
                data.addTransaction(transaction);
            }
            
            LOGGER.info("Successfully read in-memory data: " + 
                       data.getUserCount() + " users, " +
                       data.getAccountCount() + " accounts, " +
                       data.getTransactionCount() + " transactions");
            
            updateProgress("Read in-memory data");
            return data;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to read in-memory data: " + e.getMessage(), e);
            errors.add("Failed to read in-memory data: " + e.getMessage());
            throw new RuntimeException("Failed to read in-memory data", e);
        }
    }
    
    /**
     * Migrate users from in-memory to database.
     * @param data The migration data containing users to migrate
     */
    private void migrateUsers(MigrationData data) {
        LOGGER.info("Migrating users...");
        
        for (User user : data.getUsers()) {
            try {
                // Check if user already exists in database
                Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
                if (existingUser.isPresent()) {
                    LOGGER.info("User " + user.getUsername() + " already exists in database, skipping.");
                    migratedUsers++;
                    updateProgress("Migrated user: " + user.getUsername());
                    continue;
                }
                
                // Save user to database
                userRepository.save(user);
                migratedUsers++;
                LOGGER.info("Migrated user: " + user.getUsername());
                updateProgress("Migrated user: " + user.getUsername());
            } catch (Exception e) {
                String errorMsg = "Failed to migrate user " + user.getUsername() + ": " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg, e);
                errors.add(errorMsg);
            }
        }
        
        LOGGER.info("Completed migrating users: " + migratedUsers + "/" + totalUsers);
    }
    
    /**
     * Migrate accounts from in-memory to database.
     * @param data The migration data containing accounts to migrate
     */
    private void migrateAccounts(MigrationData data) {
        LOGGER.info("Migrating accounts...");
        
        for (Account account : data.getAccounts()) {
            try {
                // Check if account already exists in database
                Optional<Account> existingAccount = accountRepository.findById(account.getAccountNumber());
                if (existingAccount.isPresent()) {
                    LOGGER.info("Account #" + account.getAccountNumber() + " already exists in database, skipping.");
                    migratedAccounts++;
                    updateProgress("Migrated account: " + account.getAccountNumber());
                    continue;
                }
                
                // Save account to database
                accountRepository.save(account);
                migratedAccounts++;
                LOGGER.info("Migrated account: #" + account.getAccountNumber());
                updateProgress("Migrated account: " + account.getAccountNumber());
            } catch (Exception e) {
                String errorMsg = "Failed to migrate account #" + account.getAccountNumber() + ": " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg, e);
                errors.add(errorMsg);
            }
        }
        
        LOGGER.info("Completed migrating accounts: " + migratedAccounts + "/" + totalAccounts);
    }
    
    /**
     * Migrate transactions from in-memory to database.
     * @param data The migration data containing transactions to migrate
     */
    private void migrateTransactions(MigrationData data) {
        LOGGER.info("Migrating transactions...");
        
        for (Transaction transaction : data.getTransactions()) {
            try {
                // Since transaction IDs may be different, we can't easily check if a transaction already exists
                // Instead, we'll just save it and handle any constraints at the database level
                
                // Save transaction to database
                transactionRepository.save(transaction);
                migratedTransactions++;
                updateProgress("Migrated transaction: " + transaction.getTransactionId());
            } catch (Exception e) {
                String errorMsg = "Failed to migrate transaction #" + transaction.getTransactionId() + ": " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg, e);
                errors.add(errorMsg);
            }
        }
        
        LOGGER.info("Completed migrating transactions: " + migratedTransactions + "/" + totalTransactions);
    }
    
    /**
     * Validate that all data was migrated correctly.
     * @param data The original migration data to compare against
     */
    private void validateMigration(MigrationData data) {
        LOGGER.info("Validating migration...");
        
        try {
            // Validate users
            List<User> dbUsers = userRepository.findAll();
            if (dbUsers.size() < migratedUsers) {
                String errorMsg = "User count mismatch: " + dbUsers.size() + " in database, expected at least " + migratedUsers;
                LOGGER.warning(errorMsg);
                errors.add(errorMsg);
            }
            
            // Validate accounts
            List<Account> dbAccounts = accountRepository.findAll();
            if (dbAccounts.size() < migratedAccounts) {
                String errorMsg = "Account count mismatch: " + dbAccounts.size() + " in database, expected at least " + migratedAccounts;
                LOGGER.warning(errorMsg);
                errors.add(errorMsg);
            }
            
            // Validate transactions
            List<Transaction> dbTransactions = transactionRepository.findAll();
            if (dbTransactions.size() < migratedTransactions) {
                String errorMsg = "Transaction count mismatch: " + dbTransactions.size() + " in database, expected at least " + migratedTransactions;
                LOGGER.warning(errorMsg);
                errors.add(errorMsg);
            }
            
            LOGGER.info("Migration validation complete");
            updateProgress("Validated migration");
        } catch (Exception e) {
            String errorMsg = "Failed to validate migration: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            errors.add(errorMsg);
        }
    }
    
    /**
     * Update progress tracking.
     * @param stepDescription Description of the current step
     */
    private void updateProgress(String stepDescription) {
        currentProgress++;
        double percentComplete = (double) currentProgress / totalSteps * 100;
        System.out.printf("[%.1f%%] %s%n", percentComplete, stepDescription);
    }
    
    /**
     * Print migration statistics.
     */
    public void printMigrationSummary() {
        System.out.println("\n=== MIGRATION SUMMARY ===");
        System.out.println("Users: " + migratedUsers + "/" + totalUsers + " migrated");
        System.out.println("Accounts: " + migratedAccounts + "/" + totalAccounts + " migrated");
        System.out.println("Transactions: " + migratedTransactions + "/" + totalTransactions + " migrated");
        
        if (!errors.isEmpty()) {
            System.out.println("\nERRORS ENCOUNTERED:");
            errors.forEach(error -> System.out.println("- " + error));
        } else {
            System.out.println("\nNo errors encountered. Migration completed successfully!");
        }
    }
    
    /**
     * Main method for command-line execution.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        DataMigrationTool migrationTool = new DataMigrationTool();
        boolean success = migrationTool.migrateData();
        
        System.exit(success ? 0 : 1);
    }
}