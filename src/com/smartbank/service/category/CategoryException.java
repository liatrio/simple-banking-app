package com.smartbank.service.category;

/**
 * Exception thrown for category service related errors.
 */
public class CategoryException extends Exception {
    
    public CategoryException(String message) {
        super(message);
    }
    
    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}