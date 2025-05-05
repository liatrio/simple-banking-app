package com.smartbank.model;

/**
 * Custom exception for insufficient balance in an account
 */
public class InsufficientBalanceException extends Exception {
    /**
     * Constructor for creating a new InsufficientBalanceException
     * 
     * @param message The exception message
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
