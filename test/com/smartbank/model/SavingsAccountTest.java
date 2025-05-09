package com.smartbank.model;

import com.smartbank.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SavingsAccount model class.
 */
public class SavingsAccountTest extends BaseTest {

    @Test
    @DisplayName("SavingsAccount constructor should initialize fields correctly")
    public void testSavingsAccountConstructor() {
        // Arrange
        User user = new UserBuilder().build();
        double initialBalance = 1000.0;
        double interestRate = 0.05; // 5%
        
        // Act
        SavingsAccount account = new SavingsAccount(user, initialBalance, interestRate);
        
        // Assert
        assertEquals(user, account.getAccountHolder());
        assertEquals(initialBalance, account.getBalance());
        assertEquals(interestRate, account.getInterestRate());
        assertEquals(interestRate / 365.0, account.getDailyInterestRate(), 0.0000001);
        assertEquals(SavingsAccount.CompoundingMethod.DAILY, account.getCompoundingMethod());
        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestAccrualDate());
        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestPostingDate());
        assertEquals(0.0, account.getAccruedInterest());
        assertEquals(0.0, account.getMinimumBalanceForInterest());
        assertEquals(SavingsAccount.InterestTierType.FLAT, account.getInterestTierType());
    }
    
    @Test
    @DisplayName("setInterestRate should update interest rate and daily interest rate")
    public void testSetInterestRate() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        double newInterestRate = 0.06; // 6%
        
        // Act
        account.setInterestRate(newInterestRate);
        
        // Assert
        assertEquals(newInterestRate, account.getInterestRate());
        assertEquals(newInterestRate / 365.0, account.getDailyInterestRate(), 0.0000001);
    }
    
    @Test
    @DisplayName("setCompoundingMethod should update the compounding method")
    public void testSetCompoundingMethod() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        // Act
        account.setCompoundingMethod(SavingsAccount.CompoundingMethod.MONTHLY);
        
        // Assert
        assertEquals(SavingsAccount.CompoundingMethod.MONTHLY, account.getCompoundingMethod());
    }
    
    @Test
    @DisplayName("setLastInterestAccrualDate should update the last interest accrual date")
    public void testSetLastInterestAccrualDate() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        String newDate = "2025-01-01";
        
        // Act
        account.setLastInterestAccrualDate(newDate);
        
        // Assert
        assertEquals(newDate, account.getLastInterestAccrualDate());
        assertEquals(LocalDate.parse(newDate), account.getLastInterestAccrualLocalDate());
    }
    
    @Test
    @DisplayName("setLastInterestAccrualDate with LocalDate should update the last interest accrual date")
    public void testSetLastInterestAccrualDateWithLocalDate() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        LocalDate newDate = LocalDate.of(2025, 1, 1);
        
        // Act
        account.setLastInterestAccrualDate(newDate);
        
        // Assert
        assertEquals(newDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestAccrualDate());
        assertEquals(newDate, account.getLastInterestAccrualLocalDate());
    }
    
    @Test
    @DisplayName("setLastInterestPostingDate should update the last interest posting date")
    public void testSetLastInterestPostingDate() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        String newDate = "2025-01-01";
        
        // Act
        account.setLastInterestPostingDate(newDate);
        
        // Assert
        assertEquals(newDate, account.getLastInterestPostingDate());
        assertEquals(LocalDate.parse(newDate), account.getLastInterestPostingLocalDate());
    }
    
    @Test
    @DisplayName("setLastInterestPostingDate with LocalDate should update the last interest posting date")
    public void testSetLastInterestPostingDateWithLocalDate() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        LocalDate newDate = LocalDate.of(2025, 1, 1);
        
        // Act
        account.setLastInterestPostingDate(newDate);
        
        // Assert
        assertEquals(newDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestPostingDate());
        assertEquals(newDate, account.getLastInterestPostingLocalDate());
    }
    
    @Test
    @DisplayName("setAccruedInterest should update the accrued interest")
    public void testSetAccruedInterest() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        double newAccruedInterest = 10.0;
        
        // Act
        account.setAccruedInterest(newAccruedInterest);
        
        // Assert
        assertEquals(newAccruedInterest, account.getAccruedInterest());
    }
    
    @Test
    @DisplayName("setMinimumBalanceForInterest should update the minimum balance for interest")
    public void testSetMinimumBalanceForInterest() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        double newMinimumBalance = 500.0;
        
        // Act
        account.setMinimumBalanceForInterest(newMinimumBalance);
        
        // Assert
        assertEquals(newMinimumBalance, account.getMinimumBalanceForInterest());
    }
    
    @ParameterizedTest
    @EnumSource(SavingsAccount.InterestTierType.class)
    @DisplayName("setInterestTierType should update the interest tier type")
    public void testSetInterestTierType(SavingsAccount.InterestTierType tierType) {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        // Act
        account.setInterestTierType(tierType);
        
        // Assert
        assertEquals(tierType, account.getInterestTierType());
    }
    
    @Test
    @DisplayName("withdraw should decrease balance by the specified amount")
    public void testWithdraw() throws Exception {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        double withdrawalAmount = 500.0;
        
        // Act
        account.withdraw(withdrawalAmount);
        
        // Assert
        assertEquals(500.0, account.getBalance());
    }
    
    @Test
    @DisplayName("withdraw should throw IllegalArgumentException for non-positive amounts")
    public void testWithdrawNonPositiveAmount() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(0.0));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(-100.0));
    }
    
    @Test
    @DisplayName("withdraw should throw Exception for insufficient funds")
    public void testWithdrawInsufficientFunds() {
        // Arrange
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        // Act & Assert
        assertThrows(Exception.class, () -> account.withdraw(1500.0));
    }
    
    @Test
    @DisplayName("accrueInterest should add daily interest to accrued interest")
    public void testAccrueInterest() {
        // Arrange
        double balance = 1000.0;
        double interestRate = 0.05; // 5%
        double dailyInterestRate = interestRate / 365.0;
        double expectedDailyInterest = balance * dailyInterestRate;
        
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                balance, 
                interestRate
        );
        
        // Act
        double accrued = account.accrueInterest();
        
        // Assert
        assertEquals(expectedDailyInterest, accrued, 0.0001);
        assertEquals(expectedDailyInterest, account.getAccruedInterest(), 0.0001);
        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestAccrualDate());
    }
    
    @Test
    @DisplayName("accrueInterest should return 0 if balance is below minimum")
    public void testAccrueInterestBelowMinimum() {
        // Arrange
        double balance = 1000.0;
        double interestRate = 0.05; // 5%
        double minimumBalance = 2000.0;
        
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                balance, 
                interestRate
        );
        account.setMinimumBalanceForInterest(minimumBalance);
        
        // Act
        double accrued = account.accrueInterest();
        
        // Assert
        assertEquals(0.0, accrued);
        assertEquals(0.0, account.getAccruedInterest());
    }
    
    @Test
    @DisplayName("postInterest should add accrued interest to balance and reset accrued interest")
    public void testPostInterest() {
        // Arrange
        double balance = 1000.0;
        double interestRate = 0.05; // 5%
        double accruedInterest = 10.0;
        
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                balance, 
                interestRate
        );
        account.setAccruedInterest(accruedInterest);
        
        // Act
        double posted = account.postInterest();
        
        // Assert
        assertEquals(accruedInterest, posted);
        assertEquals(balance + accruedInterest, account.getBalance());
        assertEquals(0.0, account.getAccruedInterest());
        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE), 
                account.getLastInterestPostingDate());
    }
    
    @Test
    @DisplayName("postInterest should return 0 if accrued interest is 0")
    public void testPostInterestWithZeroAccrued() {
        // Arrange
        double balance = 1000.0;
        double interestRate = 0.05; // 5%
        
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                balance, 
                interestRate
        );
        
        // Act
        double posted = account.postInterest();
        
        // Assert
        assertEquals(0.0, posted);
        assertEquals(balance, account.getBalance());
    }
    
    @Test
    @DisplayName("applyInterest should add interest directly to balance")
    public void testApplyInterest() {
        // Arrange
        double balance = 1000.0;
        double interestRate = 0.05; // 5%
        double expectedInterest = balance * interestRate;
        
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                balance, 
                interestRate
        );
        
        // Act
        account.applyInterest();
        
        // Assert
        assertEquals(balance + expectedInterest, account.getBalance());
    }
    
    @Test
    @DisplayName("equals should return true for accounts with same account number and interest rate")
    public void testEquals() {
        // Arrange
        User user = new UserBuilder().build();
        SavingsAccount account1 = new SavingsAccount(user, 1000.0, 0.05);
        SavingsAccount account2 = new SavingsAccount(user, 2000.0, 0.05);
        
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
        SavingsAccount account = new SavingsAccount(
                new UserBuilder().build(), 
                1000.0, 
                0.05 // 5%
        );
        
        // Act
        String result = account.toString();
        
        // Assert
        assertTrue(result.contains(String.valueOf(account.getAccountNumber())));
        assertTrue(result.contains(account.getAccountHolder().getUsername()));
        assertTrue(result.contains(String.valueOf(account.getBalance())));
        assertTrue(result.contains(String.valueOf(account.getInterestRate())));
    }
}
