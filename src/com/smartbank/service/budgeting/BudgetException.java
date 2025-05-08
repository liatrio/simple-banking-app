package com.smartbank.service.budgeting;

/**
 * Exception thrown for budget-related errors.
 */
public class BudgetException extends Exception {
    
    public BudgetException(String message) {
        super(message);
    }
    
    public BudgetException(String message, Throwable cause) {
        super(message, cause);
    }
}