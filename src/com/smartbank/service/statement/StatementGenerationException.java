package com.smartbank.service.statement;

/**
 * Exception thrown when there is an error generating a statement.
 */
public class StatementGenerationException extends Exception {
    
    /**
     * Constructs a new statement generation exception with the specified detail message.
     * 
     * @param message The detail message
     */
    public StatementGenerationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new statement generation exception with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause
     */
    public StatementGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}