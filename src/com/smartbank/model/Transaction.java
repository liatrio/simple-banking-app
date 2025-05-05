package com.smartbank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class representing a bank transaction
 */
public class Transaction {
    private final double amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor for creating a new transaction
     * 
     * @param amount The transaction amount
     * @param type The transaction type
     * @param timestamp The transaction timestamp
     */
    public Transaction(double amount, TransactionType type, LocalDateTime timestamp) {
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the transaction amount
     * 
     * @return The transaction amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Gets the transaction type
     * 
     * @return The transaction type
     */
    public TransactionType getType() {
        return type;
    }
    
    /**
     * Gets the transaction timestamp
     * 
     * @return The transaction timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns a string representation of the transaction
     * 
     * @return A string representation of the transaction
     */
    @Override
    public String toString() {
        String action = type == TransactionType.DEPOSIT ? "Deposited" : "Withdrew";
        return String.format("%s $%.2f on %s", action, amount, timestamp.format(formatter));
    }
}
