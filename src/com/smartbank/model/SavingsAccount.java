package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * SavingsAccount extends Account and adds interest rate and withdrawal validation.
 * Includes enhanced interest calculation properties.
 */
@Entity
@DiscriminatorValue("Savings")
public class SavingsAccount extends Account {
    @Column(name = "interestRate")
    private double interestRate;
    
    @Column(name = "dailyInterestRate")
    private double dailyInterestRate;
    
    @Column(name = "compoundingMethod")
    @Enumerated(EnumType.STRING)
    private CompoundingMethod compoundingMethod = CompoundingMethod.DAILY;
    
    @Column(name = "lastInterestAccrualDate")
    private String lastInterestAccrualDate;
    
    @Column(name = "lastInterestPostingDate")
    private String lastInterestPostingDate;
    
    @Column(name = "accruedInterest")
    private double accruedInterest;
    
    @Column(name = "minimumBalanceForInterest")
    private double minimumBalanceForInterest;
    
    @Column(name = "interestTierType")
    @Enumerated(EnumType.STRING)
    private InterestTierType interestTierType = InterestTierType.FLAT;

    /**
     * Method used to compound interest.
     */
    public enum CompoundingMethod {
        DAILY,      // Interest compounded daily
        MONTHLY,    // Interest compounded monthly
        QUARTERLY,  // Interest compounded quarterly
        ANNUALLY    // Interest compounded annually
    }
    
    /**
     * Type of interest tier calculation.
     */
    public enum InterestTierType {
        FLAT,       // Same interest rate regardless of balance
        TIERED,     // Different interest rates for different balance tiers
        BLENDED     // Blended interest rates based on balance tiers
    }

    // Default constructor required by JPA
    protected SavingsAccount() {
        super();
    }

    public SavingsAccount(User accountHolder, double initialBalance, double interestRate) {
        super(accountHolder, initialBalance);
        this.interestRate = interestRate;
        this.dailyInterestRate = calculateDailyInterestRate(interestRate);
        this.lastInterestAccrualDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.lastInterestPostingDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.accruedInterest = 0.0;
        this.minimumBalanceForInterest = 0.0; // Default to no minimum balance requirement
    }
    
    /**
     * Calculate the daily interest rate from the annual interest rate.
     * @param annualInterestRate The annual interest rate (e.g., 0.05 for 5%)
     * @return The daily interest rate
     */
    private double calculateDailyInterestRate(double annualInterestRate) {
        return annualInterestRate / 365.0;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
        this.dailyInterestRate = calculateDailyInterestRate(interestRate);
    }
    
    public double getDailyInterestRate() {
        return dailyInterestRate;
    }
    
    public CompoundingMethod getCompoundingMethod() {
        return compoundingMethod;
    }
    
    public void setCompoundingMethod(CompoundingMethod compoundingMethod) {
        this.compoundingMethod = compoundingMethod;
    }
    
    public String getLastInterestAccrualDate() {
        return lastInterestAccrualDate;
    }
    
    public LocalDate getLastInterestAccrualLocalDate() {
        if (lastInterestAccrualDate == null) return null;
        return LocalDate.parse(lastInterestAccrualDate);
    }
    
    public void setLastInterestAccrualDate(String lastInterestAccrualDate) {
        this.lastInterestAccrualDate = lastInterestAccrualDate;
    }
    
    public void setLastInterestAccrualDate(LocalDate lastInterestAccrualDate) {
        this.lastInterestAccrualDate = lastInterestAccrualDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public String getLastInterestPostingDate() {
        return lastInterestPostingDate;
    }
    
    public LocalDate getLastInterestPostingLocalDate() {
        if (lastInterestPostingDate == null) return null;
        return LocalDate.parse(lastInterestPostingDate);
    }
    
    public void setLastInterestPostingDate(String lastInterestPostingDate) {
        this.lastInterestPostingDate = lastInterestPostingDate;
    }
    
    public void setLastInterestPostingDate(LocalDate lastInterestPostingDate) {
        this.lastInterestPostingDate = lastInterestPostingDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public double getAccruedInterest() {
        return accruedInterest;
    }
    
    public void setAccruedInterest(double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }
    
    public double getMinimumBalanceForInterest() {
        return minimumBalanceForInterest;
    }
    
    public void setMinimumBalanceForInterest(double minimumBalanceForInterest) {
        this.minimumBalanceForInterest = minimumBalanceForInterest;
    }
    
    public InterestTierType getInterestTierType() {
        return interestTierType;
    }
    
    public void setInterestTierType(InterestTierType interestTierType) {
        this.interestTierType = interestTierType;
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new Exception("Insufficient funds in savings account.");
        }
        balance -= amount;
    }
    
    /**
     * Accrue daily interest based on the current balance.
     * This adds to the accrued interest amount but does not modify the account balance.
     * @return The amount of interest accrued
     */
    public double accrueInterest() {
        // Check if balance meets the minimum requirement
        if (balance < minimumBalanceForInterest) {
            return 0.0;
        }
        
        // Calculate daily interest based on the current balance
        double dailyInterest = balance * dailyInterestRate;
        
        // Add to accrued interest
        accruedInterest += dailyInterest;
        
        // Update the last accrual date
        lastInterestAccrualDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        
        return dailyInterest;
    }
    
    /**
     * Post accrued interest to the account balance.
     * This applies all accrued interest to the account balance and resets the accrued interest to zero.
     * @return The amount of interest posted
     */
    public double postInterest() {
        if (accruedInterest <= 0) {
            return 0.0;
        }
        
        // Add accrued interest to the balance
        balance += accruedInterest;
        
        // Store the amount for return
        double postedAmount = accruedInterest;
        
        // Reset accrued interest
        accruedInterest = 0.0;
        
        // Update last posting date
        lastInterestPostingDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        
        return postedAmount;
    }
    
    /**
     * Legacy method for backward compatibility.
     * Applies interest directly to the balance without accrual.
     */
    public void applyInterest() {
        double interestAmount = balance * interestRate;
        balance += interestAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SavingsAccount that = (SavingsAccount) o;
        return Double.compare(that.interestRate, interestRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interestRate);
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "accountNumber=" + getAccountNumber() +
                ", accountHolder=" + getAccountHolder().getUsername() +
                ", balance=" + getBalance() +
                ", interestRate=" + interestRate +
                ", accruedInterest=" + accruedInterest +
                ", compoundingMethod=" + compoundingMethod +
                ", lastInterestAccrualDate=" + lastInterestAccrualDate +
                ", lastInterestPostingDate=" + lastInterestPostingDate +
                '}';
    }
}
