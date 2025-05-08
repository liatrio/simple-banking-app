package com.smartbank.model;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Entity representing a financial transaction.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, PAYMENT, FEE, INTEREST, ADJUSTMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;
    
    @Column(nullable = false)
    private long accountNumber;
    
    @Column(nullable = false)
    private double amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date timestamp;
    
    @Column
    private String description;
    
    // Category relationship
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private TransactionCategory category;
    
    // For linked transactions (like transfers)
    @Column
    private Long linkedTransactionId;
    
    // Fields for automatic categorization
    @Column
    private boolean isCategorizedAutomatically;
    
    // For merchant information
    @Column
    private String merchantName;
    
    // For tracking recurring transactions
    @Column
    private boolean isRecurring;
    
    // Default constructor required by JPA
    protected Transaction() {
    }

    public Transaction(long transactionId, long accountNumber, double amount, Type type, Date timestamp, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Transaction(long accountNumber, double amount, Type type, Date timestamp, String description) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.description = description;
    }
    
    public Transaction(long accountNumber, double amount, Type type, Date timestamp, String description, 
                      TransactionCategory category) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
    }

    // Getters and setters
    public long getTransactionId() { return transactionId; }
    
    public long getAccountNumber() { return accountNumber; }
    
    public double getAmount() { return amount; }
    
    public Type getType() { return type; }
    
    public Date getTimestamp() { return timestamp; }
    
    public String getDescription() { return description; }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TransactionCategory getCategory() {
        return category;
    }
    
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }
    
    public Long getLinkedTransactionId() {
        return linkedTransactionId;
    }
    
    public void setLinkedTransactionId(Long linkedTransactionId) {
        this.linkedTransactionId = linkedTransactionId;
    }
    
    public boolean isCategorizedAutomatically() {
        return isCategorizedAutomatically;
    }
    
    public void setCategorizedAutomatically(boolean categorizedAutomatically) {
        isCategorizedAutomatically = categorizedAutomatically;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public boolean isRecurring() {
        return isRecurring;
    }
    
    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
    
    /**
     * Check if this transaction is categorized.
     * @return true if this transaction has a category
     */
    @Transient
    public boolean isCategorized() {
        return category != null;
    }
    
    /**
     * Get the sign of the transaction amount based on transaction type.
     * @return amount with appropriate sign
     */
    @Transient
    public double getSignedAmount() {
        switch (type) {
            case DEPOSIT:
            case TRANSFER_IN:
            case INTEREST:
                return amount;
            case WITHDRAWAL:
            case TRANSFER_OUT:
            case PAYMENT:
            case FEE:
                return -amount;
            case ADJUSTMENT:
                return amount; // Adjustment can be positive or negative
            default:
                return amount;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return transactionId == that.transactionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountNumber=" + accountNumber +
                ", amount=" + amount +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", category=" + (category != null ? category.getName() : "uncategorized") +
                '}';
    }
}
