package com.smartbank.scripts;

import com.smartbank.model.*;
import com.smartbank.repository.*;
import com.smartbank.service.category.*;
import com.smartbank.service.transfer.*;
import com.smartbank.util.JPAUtil;
import com.smartbank.auth.AuthenticationServiceImpl;

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
    
    private static UserRepository userRepository;
    private static AccountRepository accountRepository;
    private static TransactionRepository transactionRepository;
    private static TransactionCategoryRepository categoryRepository;
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting data generation...");
            
            // Initialize repositories
            userRepository = RepositoryFactory.getUserRepository();
            accountRepository = RepositoryFactory.getAccountRepository();
            transactionRepository = RepositoryFactory.getTransactionRepository();
            categoryRepository = RepositoryFactory.getTransactionCategoryRepository();
            
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
            TransactionCategory category = new TransactionCategory(CATEGORY_NAMES[i], "Category for " + CATEGORY_NAMES[i].toLowerCase() + " transactions");
            category.setColor(CATEGORY_COLORS[i]);
            category.setSystem(true);
            
            categoryRepository.save(category);
            System.out.println("Created category: " + CATEGORY_NAMES[i]);
        }
    }
    
    private static void createAdminUser() {
        System.out.println("Creating admin user...");
        
        // Use proper BCrypt hashed password
        AuthenticationServiceImpl authService = new AuthenticationServiceImpl();
        String hashedPassword = authService.hashPassword(ADMIN_PASSWORD);
        
        User adminUser = new User(
            ADMIN_USER,
            hashedPassword,
            "admin",
            "Admin",
            "User",
            "admin@smartbank.com"
        );
        
        userRepository.save(adminUser);
        System.out.println("Admin user created successfully.");
    }
    
    private static void createRegularUsers() {
        System.out.println("Creating regular users...");
        
        // Use proper BCrypt hashed password
        AuthenticationServiceImpl authService = new AuthenticationServiceImpl();
        String hashedPassword = authService.hashPassword(DEFAULT_PASSWORD);
        
        for (int i = 1; i <= NUM_REGULAR_USERS; i++) {
            String firstName = getRandomElement(FIRST_NAMES);
            String lastName = getRandomElement(LAST_NAMES);
            String username = (firstName + lastName + i).toLowerCase();
            String email = username + "@example.com";
            
            User user = new User(
                username,
                hashedPassword,
                "customer",
                firstName,
                lastName,
                email
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
        // Increase initial balance to be in the same range as savings accounts
        double initialBalance = getRandomDouble(MIN_ACCOUNT_BALANCE, MAX_ACCOUNT_BALANCE);
        double creditLimit = getRandomDouble(MIN_CREDIT_LIMIT, MAX_CREDIT_LIMIT);
        
        CreditAccount account = new CreditAccount(user, initialBalance, creditLimit);
        accountRepository.save(account);
        
        System.out.println("Created credit account for user " + user.getUsername() + 
                           " with balance " + initialBalance + " and credit limit " + 
                           creditLimit);
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