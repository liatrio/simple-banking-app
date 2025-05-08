package com.smartbank.service.recurring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of validating a recurring transaction.
 */
public class ValidationResult {
    private final boolean valid;
    private final List<String> messages;
    
    /**
     * Create a new validation result.
     * 
     * @param valid Whether the validation passed
     * @param messages The validation messages
     */
    private ValidationResult(boolean valid, List<String> messages) {
        this.valid = valid;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }
    
    /**
     * Create a successful validation result.
     * 
     * @return A successful validation result
     */
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }
    
    /**
     * Create a successful validation result with a message.
     * 
     * @param message The success message
     * @return A successful validation result
     */
    public static ValidationResult success(String message) {
        List<String> messages = new ArrayList<>();
        messages.add(message);
        return new ValidationResult(true, messages);
    }
    
    /**
     * Create a failed validation result.
     * 
     * @param message The error message
     * @return A failed validation result
     */
    public static ValidationResult failure(String message) {
        List<String> messages = new ArrayList<>();
        messages.add(message);
        return new ValidationResult(false, messages);
    }
    
    /**
     * Create a failed validation result with multiple messages.
     * 
     * @param messages The error messages
     * @return A failed validation result
     */
    public static ValidationResult failure(List<String> messages) {
        return new ValidationResult(false, messages);
    }
    
    /**
     * Check if the validation passed.
     * 
     * @return true if the validation passed
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Get the validation messages.
     * 
     * @return The validation messages
     */
    public List<String> getMessages() {
        return messages;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult: ").append(valid ? "VALID" : "INVALID");
        
        if (!messages.isEmpty()) {
            sb.append(" - ");
            if (messages.size() == 1) {
                sb.append(messages.get(0));
            } else {
                sb.append("Multiple messages: ").append(messages);
            }
        }
        
        return sb.toString();
    }
}