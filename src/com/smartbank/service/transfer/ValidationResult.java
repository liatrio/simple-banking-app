package com.smartbank.service.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of validating a transfer operation.
 */
public class ValidationResult {
    private final boolean valid;
    private final List<String> messages;
    
    /**
     * Create a new ValidationResult.
     * 
     * @param valid Whether the validation is successful
     * @param messages List of validation messages
     */
    public ValidationResult(boolean valid, List<String> messages) {
        this.valid = valid;
        this.messages = new ArrayList<>(messages);
    }
    
    /**
     * Create a successful validation result with no messages.
     * 
     * @return A ValidationResult indicating success
     */
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }
    
    /**
     * Create a successful validation result with a single message.
     * 
     * @param message The validation message
     * @return A ValidationResult indicating success with a message
     */
    public static ValidationResult success(String message) {
        return new ValidationResult(true, Collections.singletonList(message));
    }
    
    /**
     * Create a failed validation result with a single message.
     * 
     * @param message The validation message
     * @return A ValidationResult indicating failure with a message
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, Collections.singletonList(message));
    }
    
    /**
     * Create a failed validation result with multiple messages.
     * 
     * @param messages The validation messages
     * @return A ValidationResult indicating failure with messages
     */
    public static ValidationResult failure(List<String> messages) {
        return new ValidationResult(false, messages);
    }
    
    /**
     * Check if the validation is successful.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Get the validation messages.
     * 
     * @return List of validation messages
     */
    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    /**
     * Get the first validation message or a default message.
     * 
     * @param defaultMessage The default message to return if no messages exist
     * @return The first validation message or the default message
     */
    public String getFirstMessage(String defaultMessage) {
        if (messages.isEmpty()) {
            return defaultMessage;
        }
        return messages.get(0);
    }
}