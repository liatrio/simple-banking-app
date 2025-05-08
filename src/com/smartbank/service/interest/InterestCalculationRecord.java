package com.smartbank.service.interest;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity for tracking interest calculation history.
 */
@Entity
@Table(name = "interest_calculation_records")
public class InterestCalculationRecord {
    
    /**
     * Type of interest operation.
     */
    public enum OperationType {
        ACCRUAL,     // Daily interest accrual
        POSTING,     // Interest posting to account
        RATE_CHANGE  // Interest rate change
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recordId;
    
    @Column(nullable = false)
    private long accountNumber;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private double balance;
    
    @Column(nullable = false)
    private double interestRate;
    
    @Column(nullable = false)
    private double amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;
    
    @Column
    private String description;
    
    // Default constructor required by JPA
    protected InterestCalculationRecord() {
    }
    
    /**
     * Create a new interest calculation record.
     * 
     * @param accountNumber The account number
     * @param date The operation date
     * @param balance The account balance at the time of operation
     * @param interestRate The interest rate at the time of operation
     * @param amount The interest amount
     * @param operationType The operation type
     * @param description Optional description
     */
    public InterestCalculationRecord(long accountNumber, LocalDate date, double balance, 
                                    double interestRate, double amount, OperationType operationType,
                                    String description) {
        this.accountNumber = accountNumber;
        this.date = date;
        this.balance = balance;
        this.interestRate = interestRate;
        this.amount = amount;
        this.operationType = operationType;
        this.description = description;
    }
    
    // Getters
    
    public long getRecordId() {
        return recordId;
    }
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Setters
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Object methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterestCalculationRecord that = (InterestCalculationRecord) o;
        return recordId == that.recordId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
    
    @Override
    public String toString() {
        return "InterestCalculationRecord{" +
                "recordId=" + recordId +
                ", accountNumber=" + accountNumber +
                ", date=" + date +
                ", balance=" + balance +
                ", interestRate=" + interestRate +
                ", amount=" + amount +
                ", operationType=" + operationType +
                ", description='" + description + '\'' +
                '}';
    }
}