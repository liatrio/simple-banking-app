package com.smartbank.controller;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.category.CategoryService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility controller for generating sample transaction data.
 * Primarily used during development and testing.
 */
public class SampleDataController {
    private static final Logger LOGGER = Logger.getLogger(SampleDataController.class.getName());
    
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final Random random = new Random();
    
    /**
     * Constructor.
     */
    public SampleDataController() {
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.categoryService = ServiceFactory.getCategoryService();
    }
    
    /**
     * Generate sample transactions for testing.
     * @param accountNumber The account number to generate transactions for
     * @param numberOfTransactions The number of transactions to generate
     * @return True if transactions were successfully generated
     */
    public boolean generateSampleTransactions(long accountNumber, int numberOfTransactions) {
        try {
            LOGGER.info("Generating " + numberOfTransactions + " sample transactions for account " + accountNumber);
            
            // Get all categories
            List<TransactionCategory> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                LOGGER.warning("No categories found to assign to sample transactions");
                return false;
            }
            
            // Generate transactions
            Calendar calendar = Calendar.getInstance();
            
            // Start from 30 days ago
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            
            for (int i = 0; i < numberOfTransactions; i++) {
                // Generate a random transaction
                Transaction.Type type = getRandomTransactionType();
                
                // Amount between $5 and $200
                double amount = 5 + (random.nextDouble() * 195);
                amount = Math.round(amount * 100) / 100.0; // Round to 2 decimal places
                
                // Random date within the last 30 days
                calendar.add(Calendar.HOUR, random.nextInt(24));
                Date timestamp = calendar.getTime();
                
                // Generate description
                String description = generateDescription(type);
                
                // Create the transaction
                Transaction transaction = new Transaction(
                        accountNumber, amount, type, timestamp, description);
                
                // Assign a random category
                TransactionCategory category = categories.get(random.nextInt(categories.size()));
                transaction.setCategory(category);
                
                // Random merchant name for certain transaction types
                if (type == Transaction.Type.PAYMENT || type == Transaction.Type.WITHDRAWAL) {
                    transaction.setMerchantName(generateMerchantName(category));
                }
                
                // Save transaction
                transactionRepository.save(transaction);
                
                LOGGER.fine("Generated transaction: " + transaction);
            }
            
            LOGGER.info("Successfully generated " + numberOfTransactions + " sample transactions");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating sample transactions: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generate more realistic sample transactions for budget testing.
     * These transactions will be distributed across categories based on budget amounts.
     * 
     * @param accountNumber The account number
     * @return True if successful
     */
    public boolean generateBudgetedTransactions(long accountNumber) {
        try {
            LOGGER.info("Generating budgeted transactions for account " + accountNumber);
            
            // Get categories with budget amounts
            List<TransactionCategory> categories = categoryService.getAllCategories();
            categories.removeIf(c -> c.getBudgetAmount() <= 0);
            
            if (categories.isEmpty()) {
                LOGGER.warning("No categories with budget amounts found");
                return false;
            }
            
            LOGGER.info("Found " + categories.size() + " categories with budget amounts");
            
            // Generate transactions for each category
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1); // Start from beginning of current month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            Date startDate = calendar.getTime();
            
            // End date is current date
            Date endDate = new Date();
            
            for (TransactionCategory category : categories) {
                double budgetAmount = category.getBudgetAmount();
                
                // Determine how much of the budget to "use" (between 30% and 110%)
                double spendingFactor = 0.3 + (random.nextDouble() * 0.8);
                double totalToSpend = budgetAmount * spendingFactor;
                
                LOGGER.info("Category: " + category.getName() + ", Budget: $" + budgetAmount + 
                           ", Will spend: $" + totalToSpend + " (" + (spendingFactor * 100) + "%)");
                
                // Determine number of transactions (1-8)
                int numTransactions = 1 + random.nextInt(7);
                
                // Generate transactions to approximately reach the target spending
                double remainingToSpend = totalToSpend;
                for (int i = 0; i < numTransactions; i++) {
                    // Last transaction gets remainder, others get random portion
                    double amount;
                    if (i == numTransactions - 1) {
                        amount = remainingToSpend;
                    } else {
                        // Random portion of remaining amount, but at least $5
                        double portion = 0.1 + (random.nextDouble() * 0.4); // 10-50% of remaining
                        amount = Math.max(5.0, remainingToSpend * portion);
                        remainingToSpend -= amount;
                    }
                    
                    // Round to 2 decimal places
                    amount = Math.round(amount * 100) / 100.0;
                    
                    // Random date between start and end
                    Date timestamp = getRandomDateBetween(startDate, endDate);
                    
                    // Transaction type based on category
                    Transaction.Type type = getTransactionTypeForCategory(category);
                    
                    // Description based on category
                    String description = generateDescriptionForCategory(category);
                    
                    // Create transaction
                    Transaction transaction = new Transaction(
                            accountNumber, amount, type, timestamp, description, category);
                    
                    // Set merchant name for certain transaction types
                    if (type == Transaction.Type.PAYMENT || type == Transaction.Type.WITHDRAWAL) {
                        transaction.setMerchantName(generateMerchantName(category));
                    }
                    
                    // Save transaction
                    transactionRepository.save(transaction);
                    
                    LOGGER.fine("Generated transaction for category " + category.getName() + 
                               ": " + transaction);
                }
            }
            
            LOGGER.info("Successfully generated budgeted transactions");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating budgeted transactions: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get a random date between two dates.
     */
    private Date getRandomDateBetween(Date startDate, Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long randomTime = startTime + (long) (random.nextDouble() * (endTime - startTime));
        return new Date(randomTime);
    }
    
    /**
     * Get a transaction type appropriate for the category.
     */
    private Transaction.Type getTransactionTypeForCategory(TransactionCategory category) {
        String categoryName = category.getName().toLowerCase();
        
        if (categoryName.contains("income") || 
            categoryName.contains("salary") || 
            categoryName.contains("deposit")) {
            return Transaction.Type.DEPOSIT;
        }
        else if (categoryName.contains("fee") || 
                 categoryName.contains("charge")) {
            return Transaction.Type.FEE;
        }
        else if (categoryName.contains("interest")) {
            return Transaction.Type.INTEREST;
        }
        else if (categoryName.contains("transfer")) {
            return random.nextBoolean() ? Transaction.Type.TRANSFER_OUT : Transaction.Type.TRANSFER_IN;
        }
        else {
            // Most categories are payments
            return random.nextDouble() < 0.8 ? Transaction.Type.PAYMENT : Transaction.Type.WITHDRAWAL;
        }
    }
    
    /**
     * Generate a description appropriate for the category.
     */
    private String generateDescriptionForCategory(TransactionCategory category) {
        String categoryName = category.getName().toLowerCase();
        
        // Common prefixes for transactions
        String[] prefixes = {"Payment to ", "Purchase at ", "Transaction with ", "Service from "};
        
        if (categoryName.contains("grocery") || categoryName.contains("food")) {
            String[] merchants = {"Whole Foods", "Kroger", "Safeway", "Trader Joe's", "Publix", "Albertsons"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("dining") || categoryName.contains("restaurant")) {
            String[] merchants = {"Chipotle", "Olive Garden", "Cheesecake Factory", "Panera Bread", "Starbucks", "McDonald's"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("utilities") || categoryName.contains("bill")) {
            String[] merchants = {"Electric Company", "Water Utility", "Gas Company", "Internet Provider", "Phone Company"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("entertainment")) {
            String[] merchants = {"Netflix", "Movie Theater", "Concert Tickets", "Spotify", "Apple Music", "YouTube Premium"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("shopping")) {
            String[] merchants = {"Amazon", "Walmart", "Target", "Best Buy", "Macy's", "REI", "IKEA"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("travel")) {
            String[] merchants = {"Airline Tickets", "Hotel Stay", "Car Rental", "AirBnB", "Travel Agency", "Expedia"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("health") || categoryName.contains("medical")) {
            String[] merchants = {"Pharmacy", "Doctor's Office", "Hospital", "Medical Lab", "Dental Clinic"};
            return prefixes[random.nextInt(prefixes.length)] + merchants[random.nextInt(merchants.length)];
        }
        else {
            // Default for other categories
            return "Transaction for " + category.getName();
        }
    }
    
    /**
     * Generate a random transaction type.
     */
    private Transaction.Type getRandomTransactionType() {
        Transaction.Type[] types = Transaction.Type.values();
        
        // Higher weight for payment and withdrawal
        if (random.nextDouble() < 0.7) {
            return random.nextBoolean() ? Transaction.Type.PAYMENT : Transaction.Type.WITHDRAWAL;
        } else {
            return types[random.nextInt(types.length)];
        }
    }
    
    /**
     * Generate a description for a transaction.
     */
    private String generateDescription(Transaction.Type type) {
        switch (type) {
            case DEPOSIT:
                String[] depositSources = {"Direct Deposit", "Check Deposit", "Transfer from External", "Cash Deposit"};
                return depositSources[random.nextInt(depositSources.length)];
                
            case WITHDRAWAL:
                String[] withdrawalTypes = {"ATM Withdrawal", "Cash Withdrawal", "Bank Withdrawal"};
                return withdrawalTypes[random.nextInt(withdrawalTypes.length)];
                
            case PAYMENT:
                String[] merchants = {"Amazon", "Walmart", "Target", "Starbucks", "Whole Foods", "Netflix", 
                                    "Utility Company", "Insurance Payment", "Rent Payment", "Online Purchase"};
                return "Payment to " + merchants[random.nextInt(merchants.length)];
                
            case TRANSFER_IN:
                return "Transfer from Account";
                
            case TRANSFER_OUT:
                return "Transfer to Account";
                
            case FEE:
                String[] feeTypes = {"Monthly Service Fee", "Overdraft Fee", "ATM Fee", "Wire Transfer Fee"};
                return feeTypes[random.nextInt(feeTypes.length)];
                
            case INTEREST:
                return "Interest Payment";
                
            case ADJUSTMENT:
                String[] adjustmentTypes = {"Balance Adjustment", "Refund", "Correction", "Dispute Resolution"};
                return adjustmentTypes[random.nextInt(adjustmentTypes.length)];
                
            default:
                return "Transaction";
        }
    }
    
    /**
     * Generate a merchant name based on category.
     */
    private String generateMerchantName(TransactionCategory category) {
        String categoryName = category.getName().toLowerCase();
        
        if (categoryName.contains("grocery") || categoryName.contains("food")) {
            String[] merchants = {"Whole Foods", "Kroger", "Safeway", "Trader Joe's", "Publix", "Albertsons"};
            return merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("dining") || categoryName.contains("restaurant")) {
            String[] merchants = {"Chipotle", "Olive Garden", "Cheesecake Factory", "Panera Bread", "Starbucks", "McDonald's"};
            return merchants[random.nextInt(merchants.length)];
        }
        else if (categoryName.contains("utilities") || categoryName.contains("bill")) {
            String[] merchants = {"Electric Company", "Water Utility", "Gas Company", "Internet Provider", "Phone Company"};
            return merchants[random.nextInt(merchants.length)];
        }
        else {
            // Default merchants for other categories
            String[] merchants = {"Amazon", "Walmart", "Target", "Best Buy", "Local Store", "Online Merchant"};
            return merchants[random.nextInt(merchants.length)];
        }
    }
    
    /**
     * Gets the first account number for a user.
     * @param username The username
     * @return The account number or -1 if not found
     */
    public long getFirstAccountForUser(String username) {
        try {
            // Get userId from username
            String userId = null;
            try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                 java.sql.PreparedStatement userStmt = conn.prepareStatement("SELECT userId FROM users WHERE username = ?")) {
                userStmt.setString(1, username);
                java.sql.ResultSet userRs = userStmt.executeQuery();
                if (userRs.next()) {
                    userId = userRs.getString("userId");
                } else {
                    LOGGER.warning("Could not find userId for username: " + username);
                    return -1;
                }
            }
            
            if (userId != null) {
                // Get first account for this user
                try (java.sql.Connection conn = com.smartbank.util.DatabaseManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(
                             "SELECT accountNumber FROM accounts WHERE userId = ? LIMIT 1")) {
                    stmt.setString(1, userId);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return rs.getLong("accountNumber");
                    }
                }
            }
            
            return -1;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting account for user: " + e.getMessage(), e);
            return -1;
        }
    }
}