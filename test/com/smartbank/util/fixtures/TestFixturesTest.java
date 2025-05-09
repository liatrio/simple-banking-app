package com.smartbank.util.fixtures;

import com.smartbank.BaseTest;
import com.smartbank.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify that test fixtures and builders work correctly.
 */
public class TestFixturesTest extends BaseTest {

    @Test
    @DisplayName("Test UserBuilder creates valid User objects")
    public void testUserBuilder() {
        // Test default builder
        User user = new UserBuilder().build();
        assertNotNull(user);
        assertNotNull(user.getUserId());
        assertNotNull(user.getUsername());
        assertEquals("customer", user.getRole());
        
        // Test custom builder
        String username = "testuser";
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        
        User customUser = new UserBuilder()
                .withUsername(username)
                .withFirstName(firstName)
                .withLastName(lastName)
                .withEmail(email)
                .asAdmin()
                .build();
        
        assertNotNull(customUser);
        assertEquals(username, customUser.getUsername());
        assertEquals(firstName, customUser.getFirstName());
        assertEquals(lastName, customUser.getLastName());
        assertEquals(email, customUser.getEmail());
        assertEquals("admin", customUser.getRole());
    }
    
    @Test
    @DisplayName("Test CheckingAccountBuilder creates valid CheckingAccount objects")
    public void testCheckingAccountBuilder() {
        // Test default builder
        User user = new UserBuilder().build();
        CheckingAccount account = new CheckingAccountBuilder()
                .withAccountHolder(user)
                .build();
        
        assertNotNull(account);
        assertEquals(user, account.getAccountHolder());
        
        // Test custom builder
        double initialBalance = 2000.0;
        double maintenanceFee = 10.0;
        double minimumBalance = 1000.0;
        
        CheckingAccount customAccount = new CheckingAccountBuilder()
                .withAccountHolder(user)
                .withInitialBalance(initialBalance)
                .withMonthlyMaintenanceFee(maintenanceFee)
                .withMinimumBalanceRequired(minimumBalance)
                .withOverdraftProtection(true)
                .withOverdraftProtection(500.0)
                .build();
        
        assertNotNull(customAccount);
        assertEquals(initialBalance, customAccount.getBalance());
        assertEquals(maintenanceFee, customAccount.getMonthlyMaintenanceFee());
        assertEquals(minimumBalance, customAccount.getMinimumBalanceRequired());
        assertTrue(customAccount.hasOverdraftProtection());
        assertEquals(500.0, customAccount.getOverdraftLimit());
    }
    
    @Test
    @DisplayName("Test TransactionBuilder creates valid Transaction objects")
    public void testTransactionBuilder() {
        // Test default builder
        Transaction transaction = new TransactionBuilder().build();
        assertNotNull(transaction);
        assertEquals(Transaction.Type.DEPOSIT, transaction.getType());
        
        // Test custom builder
        User user = new UserBuilder().build();
        CheckingAccount account = new CheckingAccountBuilder()
                .withAccountHolder(user)
                .build();
        
        double amount = 150.0;
        String description = "Test transaction";
        Date timestamp = new Date();
        
        Transaction customTransaction = new TransactionBuilder()
                .withAccount(account)
                .withAmount(amount)
                .withType(Transaction.Type.WITHDRAWAL)
                .withTimestamp(timestamp)
                .withDescription(description)
                .withMerchantName("Test Merchant")
                .build();
        
        assertNotNull(customTransaction);
        assertEquals(account.getAccountNumber(), customTransaction.getAccountNumber());
        assertEquals(amount, customTransaction.getAmount());
        assertEquals(Transaction.Type.WITHDRAWAL, customTransaction.getType());
        assertEquals(timestamp, customTransaction.getTimestamp());
        assertEquals(description, customTransaction.getDescription());
        assertEquals("Test Merchant", customTransaction.getMerchantName());
    }
    
    @Test
    @DisplayName("Test TransactionCategoryBuilder creates valid TransactionCategory objects")
    public void testTransactionCategoryBuilder() {
        // Test default builder
        TransactionCategory category = new TransactionCategoryBuilder().build();
        assertNotNull(category);
        
        // Test custom builder
        String name = "Test Category";
        String description = "Test Description";
        String color = "#FF5733";
        
        TransactionCategory customCategory = new TransactionCategoryBuilder()
                .withName(name)
                .withDescription(description)
                .withColor(color)
                .withKeywords("test,category")
                .asSystemCategory()
                .build();
        
        assertNotNull(customCategory);
        assertEquals(name, customCategory.getName());
        assertEquals(description, customCategory.getDescription());
        assertEquals(color, customCategory.getColor());
        assertEquals("test,category", customCategory.getKeywords());
        assertTrue(customCategory.isSystem());
        
        // Test common categories
        TransactionCategory[] commonCategories = TransactionCategoryBuilder.createCommonCategories();
        assertNotNull(commonCategories);
        assertTrue(commonCategories.length > 0);
        
        // Verify parent-child relationships
        TransactionCategory income = commonCategories[0];
        assertEquals("Income", income.getName());
        assertTrue(income.getSubcategories().size() > 0);
    }
    
    @Test
    @DisplayName("Test TestDataFactory generates valid test data")
    public void testTestDataFactory() {
        // Test random string
        String randomString = TestDataFactory.randomString("test");
        assertNotNull(randomString);
        assertTrue(randomString.startsWith("test-"));
        
        // Test random email
        String randomEmail = TestDataFactory.randomEmail();
        assertNotNull(randomEmail);
        assertTrue(randomEmail.contains("@"));
        
        // Test random amount
        double min = 100.0;
        double max = 200.0;
        double randomAmount = TestDataFactory.randomAmount(min, max).doubleValue();
        assertTrue(randomAmount >= min && randomAmount <= max);
        
        // Test random string list
        int count = 5;
        List<String> stringList = TestDataFactory.randomStringList("item", count);
        assertEquals(count, stringList.size());
        for (String item : stringList) {
            assertTrue(item.startsWith("item-"));
        }
    }
}
