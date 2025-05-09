package com.smartbank.model;

import com.smartbank.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Transaction model class.
 */
public class TransactionTest extends BaseTest {

    @Test
    @DisplayName("Transaction constructor should initialize fields correctly")
    public void testTransactionConstructor() {
        // Arrange
        long accountNumber = 1000000000L;
        double amount = 100.0;
        Transaction.Type type = Transaction.Type.DEPOSIT;
        Date timestamp = new Date();
        String description = "Test transaction";
        
        // Act
        Transaction transaction = new Transaction(accountNumber, amount, type, timestamp, description);
        
        // Assert
        assertEquals(accountNumber, transaction.getAccountNumber());
        assertEquals(amount, transaction.getAmount());
        assertEquals(type, transaction.getType());
        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(description, transaction.getDescription());
        assertNull(transaction.getCategory());
        assertFalse(transaction.isCategorized());
        assertFalse(transaction.isCategorizedAutomatically());
        assertFalse(transaction.isRecurring());
    }
    
    @Test
    @DisplayName("Transaction constructor with category should initialize fields correctly")
    public void testTransactionConstructorWithCategory() {
        // Arrange
        long accountNumber = 1000000000L;
        double amount = 100.0;
        Transaction.Type type = Transaction.Type.DEPOSIT;
        Date timestamp = new Date();
        String description = "Test transaction";
        TransactionCategory category = new TransactionCategoryBuilder().build();
        
        // Act
        Transaction transaction = new Transaction(accountNumber, amount, type, timestamp, description, category);
        
        // Assert
        assertEquals(accountNumber, transaction.getAccountNumber());
        assertEquals(amount, transaction.getAmount());
        assertEquals(type, transaction.getType());
        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(description, transaction.getDescription());
        assertEquals(category, transaction.getCategory());
        assertTrue(transaction.isCategorized());
    }
    
    @Test
    @DisplayName("getTransactionDateTime should return the same value as getTimestamp")
    public void testGetTransactionDateTime() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        
        // Act & Assert
        assertEquals(transaction.getTimestamp(), transaction.getTransactionDateTime());
    }
    
    @Test
    @DisplayName("setCategory should update the category")
    public void testSetCategory() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        TransactionCategory category = new TransactionCategoryBuilder().build();
        
        // Act
        transaction.setCategory(category);
        
        // Assert
        assertEquals(category, transaction.getCategory());
        assertTrue(transaction.isCategorized());
    }
    
    @Test
    @DisplayName("setLinkedTransactionId should update the linked transaction ID")
    public void testSetLinkedTransactionId() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        Long linkedId = 12345L;
        
        // Act
        transaction.setLinkedTransactionId(linkedId);
        
        // Assert
        assertEquals(linkedId, transaction.getLinkedTransactionId());
    }
    
    @Test
    @DisplayName("setCategorizedAutomatically should update the categorized automatically flag")
    public void testSetCategorizedAutomatically() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        
        // Act
        transaction.setCategorizedAutomatically(true);
        
        // Assert
        assertTrue(transaction.isCategorizedAutomatically());
    }
    
    @Test
    @DisplayName("setMerchantName should update the merchant name")
    public void testSetMerchantName() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        String merchantName = "Test Merchant";
        
        // Act
        transaction.setMerchantName(merchantName);
        
        // Assert
        assertEquals(merchantName, transaction.getMerchantName());
    }
    
    @Test
    @DisplayName("setRecurring should update the recurring flag")
    public void testSetRecurring() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        
        // Act
        transaction.setRecurring(true);
        
        // Assert
        assertTrue(transaction.isRecurring());
    }
    
    @ParameterizedTest
    @EnumSource(value = Transaction.Type.class, names = {"DEPOSIT", "TRANSFER_IN", "INTEREST", "MARKET_GAIN", "DIVIDEND"})
    @DisplayName("getSignedAmount should return positive amount for income transactions")
    public void testGetSignedAmountPositive(Transaction.Type type) {
        // Arrange
        double amount = 100.0;
        Transaction transaction = new TransactionBuilder()
                .withAmount(amount)
                .withType(type)
                .build();
        
        // Act & Assert
        assertEquals(amount, transaction.getSignedAmount());
    }
    
    @ParameterizedTest
    @EnumSource(value = Transaction.Type.class, names = {"WITHDRAWAL", "TRANSFER_OUT", "PAYMENT", "FEE", 
            "OVERDRAFT_FEE", "MAINTENANCE_FEE", "MANAGEMENT_FEE", "MARKET_LOSS"})
    @DisplayName("getSignedAmount should return negative amount for expense transactions")
    public void testGetSignedAmountNegative(Transaction.Type type) {
        // Arrange
        double amount = 100.0;
        Transaction transaction = new TransactionBuilder()
                .withAmount(amount)
                .withType(type)
                .build();
        
        // Act & Assert
        assertEquals(-amount, transaction.getSignedAmount());
    }
    
    @ParameterizedTest
    @EnumSource(value = Transaction.Type.class, names = {"ADJUSTMENT", "TRADE", "REBALANCE", 
            "ACCOUNT_CONVERSION", "CHECK_PROCESSING"})
    @DisplayName("getSignedAmount should return original amount for neutral transactions")
    public void testGetSignedAmountNeutral(Transaction.Type type) {
        // Arrange
        double amount = 100.0;
        Transaction transaction = new TransactionBuilder()
                .withAmount(amount)
                .withType(type)
                .build();
        
        // Act & Assert
        assertEquals(amount, transaction.getSignedAmount());
    }
    
    @ParameterizedTest
    @EnumSource(value = Transaction.Type.class, names = {"WITHDRAWAL", "TRANSFER_OUT", "PAYMENT", "FEE", 
            "OVERDRAFT_FEE", "MAINTENANCE_FEE", "MANAGEMENT_FEE", "MARKET_LOSS"})
    @DisplayName("isSpending should return true for expense transactions")
    public void testIsSpendingTrue(Transaction.Type type) {
        // Arrange
        Transaction transaction = new TransactionBuilder()
                .withAmount(100.0)
                .withType(type)
                .build();
        
        // Act & Assert
        assertTrue(transaction.isSpending());
    }
    
    @ParameterizedTest
    @EnumSource(value = Transaction.Type.class, names = {"DEPOSIT", "TRANSFER_IN", "INTEREST", "MARKET_GAIN", "DIVIDEND"})
    @DisplayName("isSpending should return false for income transactions")
    public void testIsSpendingFalse(Transaction.Type type) {
        // Arrange
        Transaction transaction = new TransactionBuilder()
                .withAmount(100.0)
                .withType(type)
                .build();
        
        // Act & Assert
        assertFalse(transaction.isSpending());
    }
    
    @Test
    @DisplayName("equals should return true for transactions with same ID")
    public void testEquals() {
        // Arrange
        long transactionId = 12345L;
        
        Transaction transaction1 = new Transaction(1000000000L, 100.0, Transaction.Type.DEPOSIT, new Date(), "Transaction 1");
        Transaction transaction2 = new Transaction(2000000000L, 200.0, Transaction.Type.WITHDRAWAL, new Date(), "Transaction 2");
        
        // Manually set the same transaction ID
        try {
            java.lang.reflect.Field idField = Transaction.class.getDeclaredField("transactionId");
            idField.setAccessible(true);
            idField.set(transaction1, transactionId);
            idField.set(transaction2, transactionId);
        } catch (Exception e) {
            fail("Failed to set transactionId field: " + e.getMessage());
        }
        
        // Act & Assert
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }
    
    @Test
    @DisplayName("toString should return a string representation of the transaction")
    public void testToString() {
        // Arrange
        Transaction transaction = new TransactionBuilder().build();
        
        // Act
        String result = transaction.toString();
        
        // Assert
        assertTrue(result.contains(String.valueOf(transaction.getAccountNumber())));
        assertTrue(result.contains(String.valueOf(transaction.getAmount())));
        assertTrue(result.contains(transaction.getType().toString()));
        assertTrue(result.contains(transaction.getDescription()));
    }
}
