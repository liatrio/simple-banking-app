package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * CheckingAccount extends Account with features specific to checking accounts.
 * Includes overdraft protection, monthly maintenance fee, and check processing.
 */
@Entity
@DiscriminatorValue("Checking")
public class CheckingAccount extends Account {
    @Column(name = "monthlyMaintenanceFee")
    private double monthlyMaintenanceFee;
    
    @Column(name = "minimumBalanceRequired")
    private double minimumBalanceRequired;
    
    @Column(name = "overdraftProtection")
    private boolean overdraftProtection;
    
    @Column(name = "overdraftLimit")
    private double overdraftLimit;
    
    @Column(name = "overdraftFee")
    private double overdraftFee;
    
    @Column(name = "lastMaintenanceFeeDate")
    private String lastMaintenanceFeeDate;
    
    @Column(name = "hasPaperCheckFeature")
    private boolean hasPaperCheckFeature;
    
    @Column(name = "checkOrderingEnabled")
    private boolean checkOrderingEnabled;
    
    @Column(name = "directDepositEnabled")
    private boolean directDepositEnabled;
    
    @Column(name = "numberOfMonthlyTransactions")
    private int numberOfMonthlyTransactions;
    
    @Column(name = "freeTransactionsPerMonth")
    private int freeTransactionsPerMonth;
    
    @Column(name = "perTransactionFee")
    private double perTransactionFee;

    // Default constructor required by JPA
    protected CheckingAccount() {
        super();
    }

    public CheckingAccount(User accountHolder, double initialBalance) {
        super(accountHolder, initialBalance);
        this.monthlyMaintenanceFee = 5.0; // Default $5 monthly fee
        this.minimumBalanceRequired = 500.0; // Default $500 minimum balance
        this.overdraftProtection = false; // Default: no overdraft protection
        this.overdraftLimit = 0.0; // Default: no overdraft limit
        this.overdraftFee = 35.0; // Default $35 overdraft fee
        this.lastMaintenanceFeeDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.hasPaperCheckFeature = true; // Default: has paper check feature
        this.checkOrderingEnabled = true; // Default: check ordering enabled
        this.directDepositEnabled = true; // Default: direct deposit enabled
        this.numberOfMonthlyTransactions = 0;
        this.freeTransactionsPerMonth = 25; // Default: 25 free transactions per month
        this.perTransactionFee = 0.25; // Default $0.25 per transaction fee after free limit
    }

    public CheckingAccount(User accountHolder, double initialBalance, double monthlyMaintenanceFee, 
                          double minimumBalanceRequired, boolean overdraftProtection, double overdraftLimit) {
        super(accountHolder, initialBalance);
        this.monthlyMaintenanceFee = monthlyMaintenanceFee;
        this.minimumBalanceRequired = minimumBalanceRequired;
        this.overdraftProtection = overdraftProtection;
        this.overdraftLimit = overdraftLimit;
        this.overdraftFee = 35.0; // Default $35 overdraft fee
        this.lastMaintenanceFeeDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.hasPaperCheckFeature = true;
        this.checkOrderingEnabled = true;
        this.directDepositEnabled = true;
        this.numberOfMonthlyTransactions = 0;
        this.freeTransactionsPerMonth = 25;
        this.perTransactionFee = 0.25;
    }

    public double getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    public void setMonthlyMaintenanceFee(double monthlyMaintenanceFee) {
        this.monthlyMaintenanceFee = monthlyMaintenanceFee;
    }

    public double getMinimumBalanceRequired() {
        return minimumBalanceRequired;
    }

    public void setMinimumBalanceRequired(double minimumBalanceRequired) {
        this.minimumBalanceRequired = minimumBalanceRequired;
    }

    public boolean hasOverdraftProtection() {
        return overdraftProtection;
    }

    public void setOverdraftProtection(boolean overdraftProtection) {
        this.overdraftProtection = overdraftProtection;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftFee() {
        return overdraftFee;
    }

    public void setOverdraftFee(double overdraftFee) {
        this.overdraftFee = overdraftFee;
    }

    public String getLastMaintenanceFeeDate() {
        return lastMaintenanceFeeDate;
    }

    public void setLastMaintenanceFeeDate(String lastMaintenanceFeeDate) {
        this.lastMaintenanceFeeDate = lastMaintenanceFeeDate;
    }
    
    public LocalDate getLastMaintenanceFeeLocalDate() {
        if (lastMaintenanceFeeDate == null) return null;
        return LocalDate.parse(lastMaintenanceFeeDate);
    }
    
    public void setLastMaintenanceFeeDate(LocalDate lastMaintenanceFeeDate) {
        this.lastMaintenanceFeeDate = lastMaintenanceFeeDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public boolean hasPaperCheckFeature() {
        return hasPaperCheckFeature;
    }

    public void setHasPaperCheckFeature(boolean hasPaperCheckFeature) {
        this.hasPaperCheckFeature = hasPaperCheckFeature;
    }

    public boolean isCheckOrderingEnabled() {
        return checkOrderingEnabled;
    }

    public void setCheckOrderingEnabled(boolean checkOrderingEnabled) {
        this.checkOrderingEnabled = checkOrderingEnabled;
    }

    public boolean isDirectDepositEnabled() {
        return directDepositEnabled;
    }

    public void setDirectDepositEnabled(boolean directDepositEnabled) {
        this.directDepositEnabled = directDepositEnabled;
    }

    public int getNumberOfMonthlyTransactions() {
        return numberOfMonthlyTransactions;
    }

    public void setNumberOfMonthlyTransactions(int numberOfMonthlyTransactions) {
        this.numberOfMonthlyTransactions = numberOfMonthlyTransactions;
    }

    public int getFreeTransactionsPerMonth() {
        return freeTransactionsPerMonth;
    }

    public void setFreeTransactionsPerMonth(int freeTransactionsPerMonth) {
        this.freeTransactionsPerMonth = freeTransactionsPerMonth;
    }

    public double getPerTransactionFee() {
        return perTransactionFee;
    }

    public void setPerTransactionFee(double perTransactionFee) {
        this.perTransactionFee = perTransactionFee;
    }

    /**
     * Increments the number of monthly transactions.
     * @return The current number of transactions after incrementing
     */
    public int incrementMonthlyTransactions() {
        return ++numberOfMonthlyTransactions;
    }

    /**
     * Resets the number of monthly transactions to zero.
     * Used at the end of each billing cycle.
     */
    public void resetMonthlyTransactions() {
        this.numberOfMonthlyTransactions = 0;
    }

    /**
     * Applies the monthly maintenance fee if the minimum balance requirement is not met.
     * @return The amount of the fee applied, or 0 if no fee was applied
     */
    public double applyMaintenanceFee() {
        if (balance < minimumBalanceRequired) {
            balance -= monthlyMaintenanceFee;
            lastMaintenanceFeeDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return monthlyMaintenanceFee;
        }
        return 0.0;
    }

    /**
     * Calculate transaction fees based on the number of transactions beyond the free limit.
     * @return The amount of transaction fees
     */
    public double calculateTransactionFees() {
        if (numberOfMonthlyTransactions <= freeTransactionsPerMonth) {
            return 0.0;
        }
        return (numberOfMonthlyTransactions - freeTransactionsPerMonth) * perTransactionFee;
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        
        if (amount <= balance) {
            balance -= amount;
            incrementMonthlyTransactions();
            return;
        }
        
        // Handle overdraft
        if (overdraftProtection && amount <= (balance + overdraftLimit)) {
            balance -= amount;
            // Apply overdraft fee if balance goes negative
            if (balance < 0) {
                balance -= overdraftFee;
            }
            incrementMonthlyTransactions();
        } else {
            throw new Exception("Insufficient funds and overdraft limit exceeded.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CheckingAccount that = (CheckingAccount) o;
        return Double.compare(that.monthlyMaintenanceFee, monthlyMaintenanceFee) == 0 &&
               Double.compare(that.minimumBalanceRequired, minimumBalanceRequired) == 0 &&
               overdraftProtection == that.overdraftProtection &&
               Double.compare(that.overdraftLimit, overdraftLimit) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), monthlyMaintenanceFee, minimumBalanceRequired, 
                           overdraftProtection, overdraftLimit);
    }

    @Override
    public String toString() {
        return "CheckingAccount{" +
                "accountNumber=" + getAccountNumber() +
                ", accountHolder=" + getAccountHolder().getUsername() +
                ", balance=" + getBalance() +
                ", monthlyMaintenanceFee=" + monthlyMaintenanceFee +
                ", minimumBalanceRequired=" + minimumBalanceRequired +
                ", overdraftProtection=" + overdraftProtection +
                ", overdraftLimit=" + overdraftLimit +
                ", transactions=" + numberOfMonthlyTransactions + "/" + freeTransactionsPerMonth +
                '}';
    }
}