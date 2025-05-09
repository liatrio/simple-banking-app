package com.smartbank.model;

import com.smartbank.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CheckingAccount model class.
 */
public class CheckingAccountTest extends BaseTest {

    @Test
    @DisplayName("CheckingAccount constructor should initialize fields correctly")
    public void testCheckingAccountConstructor() {
        // Arrange
        User user = new UserBuilder().build();
        double initialBalance = 1000.0;
        
        // Act
        CheckingAccount account = new CheckingAccount(user, initialBalance);
        
        // Assert
        assertEquals(user, account.getAccountHolder());
        assertEquals(initialBalance, account.getBalance());
        assertEquals(5.0, account.getMonthlyMaintenanceFee()); // Default value
        assertEquals(500.0, account.getMinimumBalanceRequired()); // Default value
        assertFalse(account.hasOverdraftProtection()); // Default value
        assertEquals(0.0, account.getOverdraftLimit()); // Default value
        assertEquals(35.0, account.getOverdraftFee()); // Default value
        assertTrue(account.hasPaperCheckFeature()); // Default value
        assertTrue(account.isCheckOrderingEnabled()); // Default value
        assertTrue(account.isDirectDepositEnabled()); // Default value
        assertEquals(0, account.getNumberOfMonthlyTransactions()); // Default value
        assertEquals(25, account.getFreeTransactionsPerMonth()); // Default value
        assertEquals(0.25, account.getPerTransactionFee()); // Default value
    }
    
    @Test
    @DisplayName("Extended CheckingAccount constructor should initialize fields correctly")
    public void testExtendedCheckingAccountConstructor() {
        // Arrange
        User user = new UserBuilder().build();
        double initialBalance = 1000.0;
        double monthlyMaintenanceFee = 10.0;
        double minimumBalanceRequired = 1500.0;
        boolean overdraftProtection = true;
        double overdraftLimit = 500.0;
        
        // Act
        CheckingAccount account = new CheckingAccount(user, initialBalance, 
                monthlyMaintenanceFee, minimumBalanceRequired, overdraftProtection, overdraftLimit);
        
        // Assert
        assertEquals(user, account.getAccountHolder());
        assertEquals(initialBalance, account.getBalance());
        assertEquals(monthlyMaintenanceFee, account.getMonthlyMaintenanceFee());
        assertEquals(minimumBalanceRequired, account.getMinimumBalanceRequired());
        assertEquals(overdraftProtection, account.hasOverdraftProtection());
        assertEquals(overdraftLimit, account.getOverdraftLimit());
    }
    
    @Test
    @DisplayName("deposit should increase balance by the specified amount")
    public void testDeposit() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(1000.0)
                .build();
        double depositAmount = 500.0;
        
        // Act
        account.deposit(depositAmount);
        
        // Assert
        assertEquals(1500.0, account.getBalance());
    }
    
    @Test
    @DisplayName("deposit should throw IllegalArgumentException for non-positive amounts")
    public void testDepositNonPositiveAmount() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder().build();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> account.deposit(0.0));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-100.0));
    }
    
    @Test
    @DisplayName("withdraw should decrease balance by the specified amount")
    public void testWithdraw() throws Exception {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(1000.0)
                .build();
        double withdrawalAmount = 500.0;
        
        // Act
        account.withdraw(withdrawalAmount);
        
        // Assert
        assertEquals(500.0, account.getBalance());
        assertEquals(1, account.getNumberOfMonthlyTransactions());
    }
    
    @Test
    @DisplayName("withdraw should throw IllegalArgumentException for non-positive amounts")
    public void testWithdrawNonPositiveAmount() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder().build();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(0.0));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(-100.0));
    }
    
    @Test
    @DisplayName("withdraw should throw Exception for insufficient funds without overdraft protection")
    public void testWithdrawInsufficientFundsNoOverdraft() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(1000.0)
                .withOverdraftProtection(false)
                .build();
        
        // Act & Assert
        assertThrows(Exception.class, () -> account.withdraw(1500.0));
    }
    
    @Test
    @DisplayName("withdraw should allow overdraft within limit and apply fee")
    public void testWithdrawWithOverdraft() throws Exception {
        // Arrange
        double initialBalance = 1000.0;
        double overdraftLimit = 500.0;
        double overdraftFee = 35.0;
        
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(initialBalance)
                .withOverdraftProtection(true)
                .withOverdraftProtection(overdraftLimit)
                .withOverdraftFee(overdraftFee)
                .build();
        
        // Act
        account.withdraw(1200.0);
        
        // Assert
        assertEquals(-235.0, account.getBalance()); // 1000 - 1200 - 35 (overdraft fee)
    }
    
    @Test
    @DisplayName("withdraw should throw Exception for amount exceeding balance plus overdraft limit")
    public void testWithdrawExceedingOverdraftLimit() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(1000.0)
                .withOverdraftProtection(true)
                .withOverdraftProtection(500.0)
                .build();
        
        // Act & Assert
        assertThrows(Exception.class, () -> account.withdraw(2000.0));
    }
    
    @Test
    @DisplayName("incrementMonthlyTransactions should increment the transaction count")
    public void testIncrementMonthlyTransactions() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder().build();
        assertEquals(0, account.getNumberOfMonthlyTransactions());
        
        // Act
        int result = account.incrementMonthlyTransactions();
        
        // Assert
        assertEquals(1, result);
        assertEquals(1, account.getNumberOfMonthlyTransactions());
    }
    
    @Test
    @DisplayName("resetMonthlyTransactions should reset the transaction count to zero")
    public void testResetMonthlyTransactions() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder().build();
        account.incrementMonthlyTransactions();
        account.incrementMonthlyTransactions();
        assertEquals(2, account.getNumberOfMonthlyTransactions());
        
        // Act
        account.resetMonthlyTransactions();
        
        // Assert
        assertEquals(0, account.getNumberOfMonthlyTransactions());
    }
    
    @ParameterizedTest
    @CsvSource({
        "400.0, 500.0, 5.0", // Below minimum balance, fee should be applied
        "500.0, 500.0, 0.0", // At minimum balance, no fee
        "600.0, 500.0, 0.0"  // Above minimum balance, no fee
    })
    @DisplayName("applyMaintenanceFee should apply fee when balance is below minimum")
    public void testApplyMaintenanceFee(double balance, double minimumBalance, double expectedFee) {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withInitialBalance(balance)
                .withMinimumBalanceRequired(minimumBalance)
                .withMonthlyMaintenanceFee(5.0)
                .build();
        
        // Act
        double appliedFee = account.applyMaintenanceFee();
        
        // Assert
        assertEquals(expectedFee, appliedFee);
        assertEquals(balance - expectedFee, account.getBalance());
        
        if (expectedFee > 0) {
            assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                    account.getLastMaintenanceFeeDate());
        }
    }
    
    @ParameterizedTest
    @CsvSource({
        "20, 25, 0.0",      // Below free limit, no fee
        "25, 25, 0.0",      // At free limit, no fee
        "30, 25, 1.25",     // 5 transactions over limit at $0.25 each
        "35, 25, 2.5"       // 10 transactions over limit at $0.25 each
    })
    @DisplayName("calculateTransactionFees should calculate fees correctly")
    public void testCalculateTransactionFees(int transactions, int freeLimit, double expectedFee) {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder()
                .withFreeTransactionsPerMonth(freeLimit)
                .withPerTransactionFee(0.25)
                .build();
        
        // Set the number of transactions
        for (int i = 0; i < transactions; i++) {
            account.incrementMonthlyTransactions();
        }
        
        // Act
        double fee = account.calculateTransactionFees();
        
        // Assert
        assertEquals(expectedFee, fee);
    }
    
    @Test
    @DisplayName("equals should return true for accounts with same account number")
    public void testEquals() {
        // Arrange
        User user = new UserBuilder().build();
        CheckingAccount account1 = new CheckingAccount(user, 1000.0);
        CheckingAccount account2 = new CheckingAccount(user, 2000.0);
        
        // Manually set the same account number
        try {
            java.lang.reflect.Field accountNumberField = Account.class.getDeclaredField("accountNumber");
            accountNumberField.setAccessible(true);
            long accountNumber = (long) accountNumberField.get(account1);
            accountNumberField.set(account2, accountNumber);
        } catch (Exception e) {
            fail("Failed to set accountNumber field: " + e.getMessage());
        }
        
        // Act & Assert
        assertEquals(account1, account2);
        assertEquals(account1.hashCode(), account2.hashCode());
    }
    
    @Test
    @DisplayName("toString should return a string representation of the account")
    public void testToString() {
        // Arrange
        CheckingAccount account = new CheckingAccountBuilder().build();
        
        // Act
        String result = account.toString();
        
        // Assert
        assertTrue(result.contains(String.valueOf(account.getAccountNumber())));
        assertTrue(result.contains(account.getAccountHolder().getUsername()));
        assertTrue(result.contains(String.valueOf(account.getBalance())));
    }
}
