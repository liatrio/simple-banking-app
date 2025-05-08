package com.smartbank.scripts;

import com.smartbank.model.*;
import com.smartbank.repository.*;
import com.smartbank.service.category.*;
import com.smartbank.service.transfer.*;
import com.smartbank.util.JPAUtil;
import com.smartbank.auth.AuthenticationServiceImpl;
import com.smartbank.model.CreditHistory.EventType;
import com.smartbank.model.CreditLimitChangeRequest.Status;
import com.smartbank.model.CreditLimitChangeRequest.Source;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Data generator script to create test data for the SmartBank application.
 * Run this using:
 * java -cp build/libs/simple-banking-app.jar:src:lib/* com.smartbank.scripts.DataGenerator
 */
public class DataGenerator {
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_PASSWORD = "password123";
    
    private static final int NUM_REGULAR_USERS = 10;
    private static final int NUM_ACCOUNTS_PER_USER_MIN = 1;
    private static final int NUM_ACCOUNTS_PER_USER_MAX = 3;
    private static final int NUM_TRANSACTIONS_PER_ACCOUNT_MIN = 5;
    private static final int NUM_TRANSACTIONS_PER_ACCOUNT_MAX = 20;
    
    private static final double MIN_ACCOUNT_BALANCE = 500.0;
    private static final double MAX_ACCOUNT_BALANCE = 10000.0;
    private static final double MIN_CREDIT_LIMIT = 1000.0;
    private static final double MAX_CREDIT_LIMIT = 5000.0;
    private static final double MIN_INTEREST_RATE = 0.01; // 1%
    private static final double MAX_INTEREST_RATE = 0.05; // 5%
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Emily", "David", "Sarah", "James", "Emma", "Robert", "Olivia",
        "William", "Sophia", "Joseph", "Isabella", "Daniel", "Mia", "Matthew", "Charlotte", "Andrew", "Amelia"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    };
    
    private static final String[] TRANSACTION_DESCRIPTIONS = {
        "Grocery shopping", "Gas station", "Online purchase", "Restaurant bill", "Utility payment",
        "Subscription fee", "Movie tickets", "Coffee shop", "Clothing store", "Electronics store",
        "Pharmacy", "Haircut", "Gym membership", "Book store", "Music streaming", "Mobile phone bill",
        "Internet service", "Public transport", "Taxi ride", "Office supplies", "Home improvement",
        "Furniture store", "Pet supplies", "Gift purchase", "Charity donation"
    };
    
    private static final String[] MERCHANTS = {
        "Walmart", "Amazon", "Target", "Kroger", "Costco", "Home Depot", "Best Buy", "Starbucks",
        "McDonald's", "Subway", "Shell", "Exxon", "CVS", "Walgreens", "Apple", "Netflix", "Spotify",
        "AT&T", "Verizon", "Uber", "Lyft", "Staples", "Ikea", "PetSmart", "Macy's"
    };
    
    private static final String[] CATEGORY_NAMES = {
        "Groceries", "Dining", "Transportation", "Entertainment", "Utilities", "Shopping",
        "Healthcare", "Education", "Travel", "Subscriptions", "Housing", "Personal Care",
        "Gifts", "Charity", "Investments"
    };
    
    private static final String[] CATEGORY_COLORS = {
        "#FF5733", "#33FF57", "#3357FF", "#F3FF33", "#FF33F3", "#33FFF3", "#FF5733",
        "#33FF57", "#3357FF", "#F3FF33", "#FF33F3", "#33FFF3", "#FF5733", "#33FF57", "#3357FF"
    };
    
    private static final UserRepository userRepository = RepositoryFactory.getUserRepository();
    private static final AccountRepository accountRepository = RepositoryFactory.getAccountRepository();
    private static final TransactionRepository transactionRepository = RepositoryFactory.getTransactionRepository();
    private static final TransactionCategoryRepository categoryRepository = RepositoryFactory.getTransactionCategoryRepository();
    private static final CreditHistoryRepository creditHistoryRepository = RepositoryFactory.getCreditHistoryRepository();
    private static final CreditLimitChangeRequestRepository creditLimitRequestRepository = RepositoryFactory.getCreditLimitChangeRequestRepository();
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting data generation...");
            
            clearExistingData();
            createTransactionCategories();
            createAdminUser();
            createRegularUsers();
            createAccountsForUsers();
            createTransactionsForAccounts();
            
            System.out.println("Data generation completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during data generation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            JPAUtil.closeEntityManagerFactory();
        }
    }
    
    private static void clearExistingData() {
        System.out.println("Clearing existing data...");
        // We'll skip this part as it's safer to just delete the database file and start fresh
    }
    
    private static void createTransactionCategories() {
        System.out.println("Creating transaction categories...");
        
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            TransactionCategory category = new TransactionCategory();
            category.setName(CATEGORY_NAMES[i]);
            category.setDescription("Category for " + CATEGORY_NAMES[i].toLowerCase() + " transactions");
            category.setColor(CATEGORY_COLORS[i]);
            category.setSystem(true);
            
            categoryRepository.save(category);
            System.out.println("Created category: " + CATEGORY_NAMES[i]);
        }
    }
    
    private static void createAdminUser() {
        System.out.println("Creating admin user...");
        
        // Use proper BCrypt hashed password
        String hashedPassword = AuthenticationServiceImpl.hashPassword(ADMIN_PASSWORD);
        
        User adminUser = new User(
            ADMIN_USER,
            hashedPassword,
            "Admin",
            "User",
            "admin@smartbank.com",
            "ADMIN"
        );
        
        userRepository.save(adminUser);
        System.out.println("Admin user created successfully.");
    }
    
    private static void createRegularUsers() {
        System.out.println("Creating regular users...");
        
        // Use proper BCrypt hashed password
        String hashedPassword = AuthenticationServiceImpl.hashPassword(DEFAULT_PASSWORD);
        
        for (int i = 1; i <= NUM_REGULAR_USERS; i++) {
            String firstName = getRandomElement(FIRST_NAMES);
            String lastName = getRandomElement(LAST_NAMES);
            String username = (firstName + lastName + i).toLowerCase();
            String email = username + "@example.com";
            
            User user = new User(
                username,
                hashedPassword,
                firstName,
                lastName,
                email,
                "USER"
            );
            
            userRepository.save(user);
            System.out.println("Created user: " + username);
        }
    }
    
    private static void createAccountsForUsers() {
        System.out.println("Creating accounts for users...");
        
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            int numAccounts = ThreadLocalRandom.current().nextInt(
                NUM_ACCOUNTS_PER_USER_MIN, 
                NUM_ACCOUNTS_PER_USER_MAX + 1
            );
            
            for (int i = 0; i < numAccounts; i++) {
                // 70% chance of Savings account, 30% chance of Credit account
                boolean isSavings = random.nextDouble() < 0.7;
                
                if (isSavings) {
                    createSavingsAccount(user);
                } else {
                    createCreditAccount(user);
                }
            }
        }
    }
    
    private static void createSavingsAccount(User user) {
        double initialBalance = getRandomDouble(MIN_ACCOUNT_BALANCE, MAX_ACCOUNT_BALANCE);
        double interestRate = getRandomDouble(MIN_INTEREST_RATE, MAX_INTEREST_RATE);
        
        SavingsAccount account = new SavingsAccount(user, initialBalance, interestRate);
        accountRepository.save(account);
        
        System.out.println("Created savings account for user " + user.getUsername() + 
                           " with balance " + initialBalance + " and interest rate " + 
                           (interestRate * 100) + "%");
    }
    
    private static void createCreditAccount(User user) {
        double initialBalance = getRandomDouble(0, MAX_ACCOUNT_BALANCE / 2);
        double creditLimit = getRandomDouble(MIN_CREDIT_LIMIT, MAX_CREDIT_LIMIT);
        
        CreditAccount account = new CreditAccount(user, initialBalance, creditLimit);
        
        // Set additional credit-related fields to avoid null values in primitives
        account.setAutomaticCreditLimitReviewEnabled(random.nextBoolean());
        account.setCreditScore(random.nextInt(300) + 550); // Random score between 550-850
        int onTimePayments = random.nextInt(20);
        int latePayments = random.nextInt(3);  // Keep late payments low
        
        for (int i = 0; i < onTimePayments; i++) {
            account.incrementOnTimePayments();
        }
        
        for (int i = 0; i < latePayments; i++) {
            account.incrementLatePayments();
        }
        
        account.updateAverageMonthlyBalance(initialBalance);
        
        // Save the account to get the account number
        accountRepository.save(account);
        
        // Create credit history entries
        createCreditHistoryEntries(account);
        
        // Create some credit limit change requests
        createCreditLimitChangeRequests(account, user.getUsername());
        
        System.out.println("Created credit account for user " + user.getUsername() + 
                           " with balance " + initialBalance + " and credit limit " + 
                           creditLimit + ", credit score: " + account.getCreditScore());
    }
    
    private static void createTransactionsForAccounts() {
        System.out.println("Creating transactions for accounts...");
        
        List<Account> accounts = accountRepository.findAll();
        List<TransactionCategory> categories = categoryRepository.findAll();
        
        for (Account account : accounts) {
            int numTransactions = ThreadLocalRandom.current().nextInt(
                NUM_TRANSACTIONS_PER_ACCOUNT_MIN, 
                NUM_TRANSACTIONS_PER_ACCOUNT_MAX + 1
            );
            
            for (int i = 0; i < numTransactions; i++) {
                createRandomTransaction(account, categories);
            }
        }
    }
    
    private static void createRandomTransaction(Account account, List<TransactionCategory> categories) {
        // Generate random transaction amount (between $1 and $500)
        double amount = getRandomDouble(1.0, 500.0);
        
        // 60% chance of withdrawal, 40% chance of deposit
        boolean isWithdrawal = random.nextDouble() < 0.6;
        
        Transaction.Type type = isWithdrawal ? Transaction.Type.WITHDRAWAL : Transaction.Type.DEPOSIT;
        String description = getRandomElement(TRANSACTION_DESCRIPTIONS);
        String merchantName = getRandomElement(MERCHANTS);
        
        // Generate a random date within the last 3 months
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime transactionDate = now.minusDays(random.nextInt(90));
        
        // Get random category
        TransactionCategory category = getRandomElement(categories);
        
        Transaction transaction = new Transaction(
            account.getAccountNumber(),
            isWithdrawal ? -amount : amount,
            type,
            new Date(), // Use current date
            description
        );
        
        transaction.setMerchantName(merchantName);
        transaction.setCategory(category);
        transaction.setCategorizedAutomatically(true);
        
        transactionRepository.save(transaction);
        
        System.out.println("Created " + type + " transaction of " + amount + 
                           " for account " + account.getAccountNumber());
    }
    
    /**
     * Create credit history entries for a credit account
     */
    private static void createCreditHistoryEntries(CreditAccount account) {
        System.out.println("Creating credit history entries for account " + account.getAccountNumber());
        
        // Create initial credit score update entry
        CreditHistory initialScoreEntry = new CreditHistory(
            account, 
            EventType.CREDIT_SCORE_UPDATE, 
            "Initial credit score assessment",
            700,  // Default initial score
            account.getCreditScore()
        );
        account.addCreditHistoryEntry(initialScoreEntry);
        creditHistoryRepository.save(initialScoreEntry);
        
        // Create on-time payment entries
        for (int i = 0; i < account.getNumberOfOnTimePayments(); i++) {
            double amount = getRandomDouble(50, 200);
            CreditHistory paymentEntry = new CreditHistory(
                account,
                EventType.PAYMENT_ON_TIME,
                "On-time payment of $" + amount
            );
            paymentEntry.setOldValue(amount);
            paymentEntry.setNewValue(0);
            account.addCreditHistoryEntry(paymentEntry);
            creditHistoryRepository.save(paymentEntry);
        }
        
        // Create late payment entries if any
        for (int i = 0; i < account.getNumberOfLatePayments(); i++) {
            double amount = getRandomDouble(50, 200);
            CreditHistory latePaymentEntry = new CreditHistory(
                account,
                EventType.PAYMENT_LATE,
                "Late payment of $" + amount
            );
            latePaymentEntry.setOldValue(amount);
            latePaymentEntry.setNewValue(0);
            account.addCreditHistoryEntry(latePaymentEntry);
            creditHistoryRepository.save(latePaymentEntry);
        }
        
        // Add credit limit change event if applicable
        if (account.getNumberOfCreditLimitIncreases() > 0) {
            double oldLimit = account.getInitialCreditLimit();
            double newLimit = account.getCreditLimit();
            CreditHistory limitIncreaseEntry = new CreditHistory(
                account,
                EventType.CREDIT_LIMIT_INCREASE,
                "Credit limit increase based on account performance",
                oldLimit,
                newLimit
            );
            account.addCreditHistoryEntry(limitIncreaseEntry);
            creditHistoryRepository.save(limitIncreaseEntry);
        }
        
        // Add automatic review entry
        if (account.isAutomaticCreditLimitReviewEnabled()) {
            CreditHistory reviewEntry = new CreditHistory(
                account,
                EventType.AUTOMATIC_REVIEW,
                "Scheduled automatic review of account performance"
            );
            reviewEntry.setOldCreditScore(account.getCreditScore() - random.nextInt(20));
            reviewEntry.setNewCreditScore(account.getCreditScore());
            account.addCreditHistoryEntry(reviewEntry);
            creditHistoryRepository.save(reviewEntry);
        }
        
        // Update the account with the history entries
        accountRepository.save(account);
    }
    
    /**
     * Create credit limit change requests for an account
     */
    private static void createCreditLimitChangeRequests(CreditAccount account, String username) {
        System.out.println("Creating credit limit change requests for account " + account.getAccountNumber());
        
        // Determine number of requests (0-3)
        int numRequests = random.nextInt(4);
        
        for (int i = 0; i < numRequests; i++) {
            // Determine request type (increase or decrease)
            boolean isIncrease = random.nextDouble() < 0.8;  // 80% chance of increase
            
            double currentLimit = account.getCreditLimit();
            double requestedLimit;
            
            if (isIncrease) {
                // Request 10-30% increase
                double increasePercent = 0.1 + (random.nextDouble() * 0.2);
                requestedLimit = currentLimit * (1 + increasePercent);
            } else {
                // Request 5-15% decrease
                double decreasePercent = 0.05 + (random.nextDouble() * 0.1);
                requestedLimit = currentLimit * (1 - decreasePercent);
            }
            
            // Round to nearest $100
            requestedLimit = Math.round(requestedLimit / 100.0) * 100.0;
            
            // Determine source
            Source source;
            double rand = random.nextDouble();
            if (rand < 0.6) {
                source = Source.USER_REQUESTED;
            } else if (rand < 0.9) {
                source = Source.SYSTEM_AUTOMATIC;
            } else {
                source = Source.ADMIN_INITIATED;
            }
            
            // Create the request
            String reason = isIncrease 
                ? "Request for credit limit increase based on payment history"
                : "Request to reduce credit limit to manage spending";
                
            CreditLimitChangeRequest request = new CreditLimitChangeRequest(
                account.getAccountNumber(),
                currentLimit,
                requestedLimit,
                username,
                source,
                reason,
                account.getCreditScore(),
                source == Source.SYSTEM_AUTOMATIC
            );
            
            // Determine status
            double statusRand = random.nextDouble();
            if (statusRand < 0.2) {
                // Leave as PENDING
            } else if (statusRand < 0.7) {
                // Approve
                request.approve("admin", "Approved based on account history and credit score");
            } else {
                // Reject
                request.reject("admin", "Rejected due to recent account activity");
            }
            
            creditLimitRequestRepository.save(request);
        }
    }

    // Utility methods
    
    private static <T> T getRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }
    
    private static <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    private static double getRandomDouble(double min, double max) {
        double value = min + (max - min) * random.nextDouble();
        // Round to 2 decimal places
        return Math.round(value * 100.0) / 100.0;
    }
}