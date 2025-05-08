package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CreditAccount extends Account and adds credit limit and custom withdrawal logic.
 * Enhanced with credit history tracking for dynamic credit limit adjustments.
 */
@Entity
@DiscriminatorValue("Credit")
public class CreditAccount extends Account {
    @Column(name = "creditLimit")
    private double creditLimit;
    
    @Column(name = "initialCreditLimit")
    private double initialCreditLimit;
    
    @Column(name = "creditScore", nullable = false, columnDefinition = "integer default 700")
    private int creditScore = 700;
    
    @Column(name = "lastCreditLimitChangeDate")
    private String lastCreditLimitChangeDate;
    
    @Column(name = "numberOfOnTimePayments", nullable = false, columnDefinition = "integer default 0")
    private int numberOfOnTimePayments = 0;
    
    @Column(name = "numberOfLatePayments", nullable = false, columnDefinition = "integer default 0")
    private int numberOfLatePayments = 0;
    
    @Column(name = "averageMonthlyBalance")
    private double averageMonthlyBalance;
    
    @Column(name = "lastCreditScoreUpdateDate")
    private String lastCreditScoreUpdateDate;
    
    @Column(name = "numberOfCreditLimitIncreases", nullable = false, columnDefinition = "integer default 0")
    private int numberOfCreditLimitIncreases = 0;
    
    @Column(name = "automaticCreditLimitReviewEnabled", nullable = false, columnDefinition = "boolean default true")
    private boolean automaticCreditLimitReviewEnabled = true;
    
    @OneToMany(mappedBy = "creditAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CreditHistory> creditHistoryEntries = new ArrayList<>();

    // Default constructor required by JPA
    protected CreditAccount() {
        super();
    }

    public CreditAccount(User accountHolder, double initialBalance, double creditLimit) {
        super(accountHolder, initialBalance);
        this.creditLimit = creditLimit;
        this.initialCreditLimit = creditLimit;
        this.creditScore = 700; // Default initial credit score
        this.lastCreditLimitChangeDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.lastCreditScoreUpdateDate = this.lastCreditLimitChangeDate;
        this.numberOfOnTimePayments = 0;
        this.numberOfLatePayments = 0;
        this.averageMonthlyBalance = initialBalance;
        this.numberOfCreditLimitIncreases = 0;
        this.automaticCreditLimitReviewEnabled = true;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.lastCreditLimitChangeDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (creditLimit > this.creditLimit) {
            this.numberOfCreditLimitIncreases++;
        }
        this.creditLimit = creditLimit;
    }
    
    public double getInitialCreditLimit() {
        return initialCreditLimit;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
        this.lastCreditScoreUpdateDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getLastCreditLimitChangeDate() {
        return lastCreditLimitChangeDate;
    }

    public LocalDateTime getLastCreditLimitChangeDateTime() {
        return LocalDateTime.parse(lastCreditLimitChangeDate);
    }

    public int getNumberOfOnTimePayments() {
        return numberOfOnTimePayments;
    }

    public void incrementOnTimePayments() {
        this.numberOfOnTimePayments++;
    }

    public int getNumberOfLatePayments() {
        return numberOfLatePayments;
    }

    public void incrementLatePayments() {
        this.numberOfLatePayments++;
    }

    public double getAverageMonthlyBalance() {
        return averageMonthlyBalance;
    }

    public void updateAverageMonthlyBalance(double newMonthlyBalance) {
        this.averageMonthlyBalance = (this.averageMonthlyBalance + newMonthlyBalance) / 2;
    }

    public String getLastCreditScoreUpdateDate() {
        return lastCreditScoreUpdateDate;
    }

    public LocalDateTime getLastCreditScoreUpdateDateTime() {
        return LocalDateTime.parse(lastCreditScoreUpdateDate);
    }

    public int getNumberOfCreditLimitIncreases() {
        return numberOfCreditLimitIncreases;
    }

    public boolean isAutomaticCreditLimitReviewEnabled() {
        return automaticCreditLimitReviewEnabled;
    }

    public void setAutomaticCreditLimitReviewEnabled(boolean automaticCreditLimitReviewEnabled) {
        this.automaticCreditLimitReviewEnabled = automaticCreditLimitReviewEnabled;
    }
    
    public List<CreditHistory> getCreditHistoryEntries() {
        return creditHistoryEntries;
    }
    
    public void addCreditHistoryEntry(CreditHistory entry) {
        creditHistoryEntries.add(entry);
        entry.setCreditAccount(this);
    }
    
    public void removeCreditHistoryEntry(CreditHistory entry) {
        creditHistoryEntries.remove(entry);
        entry.setCreditAccount(null);
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance + creditLimit) {
            throw new Exception("Withdrawal exceeds credit limit.");
        }
        balance -= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreditAccount that = (CreditAccount) o;
        return Double.compare(that.creditLimit, creditLimit) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), creditLimit);
    }

    @Override
    public String toString() {
        return "CreditAccount{" +
                "accountNumber=" + getAccountNumber() +
                ", accountHolder=" + getAccountHolder().getUsername() +
                ", balance=" + getBalance() +
                ", creditLimit=" + creditLimit +
                ", creditScore=" + creditScore +
                '}';
    }
}
