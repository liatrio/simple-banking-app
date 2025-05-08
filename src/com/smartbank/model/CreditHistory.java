package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entity for tracking credit-related events and history.
 * Used for credit score calculation and limit adjustment.
 */
@Entity
@Table(name = "credit_history")
public class CreditHistory {
    
    // Event types for credit history entries
    public enum EventType {
        PAYMENT_ON_TIME,
        PAYMENT_LATE,
        CREDIT_LIMIT_INCREASE,
        CREDIT_LIMIT_DECREASE,
        CREDIT_SCORE_UPDATE,
        ACCOUNT_OVERDRAWN,
        BALANCE_PAID_IN_FULL,
        AUTOMATIC_REVIEW,
        MANUAL_REVIEW
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", nullable = false)
    private CreditAccount creditAccount;
    
    @Column(nullable = false)
    private long accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    @Column(nullable = false)
    private String eventDate;
    
    @Column(nullable = false)
    private String description;
    
    @Column
    private double oldValue;
    
    @Column
    private double newValue;
    
    @Column
    private int oldCreditScore;
    
    @Column
    private int newCreditScore;
    
    @Column
    private String additionalInfo;
    
    @Column
    private String userId;
    
    // Default constructor required by JPA
    protected CreditHistory() {
    }
    
    public CreditHistory(CreditAccount creditAccount, EventType eventType, String description) {
        this.creditAccount = creditAccount;
        this.accountNumber = creditAccount.getAccountNumber();
        this.eventType = eventType;
        this.eventDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.description = description;
    }
    
    public CreditHistory(CreditAccount creditAccount, EventType eventType, String description, 
                        double oldValue, double newValue) {
        this(creditAccount, eventType, description);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public CreditHistory(CreditAccount creditAccount, EventType eventType, String description, 
                        int oldCreditScore, int newCreditScore) {
        this(creditAccount, eventType, description);
        this.oldCreditScore = oldCreditScore;
        this.newCreditScore = newCreditScore;
    }

    // Getters and setters
    public long getId() {
        return id;
    }
    
    public CreditAccount getCreditAccount() {
        return creditAccount;
    }
    
    void setCreditAccount(CreditAccount creditAccount) {
        this.creditAccount = creditAccount;
        if (creditAccount != null) {
            this.accountNumber = creditAccount.getAccountNumber();
        }
    }
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getEventDate() {
        return eventDate;
    }
    
    public LocalDateTime getEventDateTime() {
        return LocalDateTime.parse(eventDate);
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(double oldValue) {
        this.oldValue = oldValue;
    }
    
    public double getNewValue() {
        return newValue;
    }
    
    public void setNewValue(double newValue) {
        this.newValue = newValue;
    }
    
    public int getOldCreditScore() {
        return oldCreditScore;
    }
    
    public void setOldCreditScore(int oldCreditScore) {
        this.oldCreditScore = oldCreditScore;
    }
    
    public int getNewCreditScore() {
        return newCreditScore;
    }
    
    public void setNewCreditScore(int newCreditScore) {
        this.newCreditScore = newCreditScore;
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditHistory that = (CreditHistory) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CreditHistory{" +
                "id=" + id +
                ", accountNumber=" + accountNumber +
                ", eventType=" + eventType +
                ", eventDate='" + eventDate + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}