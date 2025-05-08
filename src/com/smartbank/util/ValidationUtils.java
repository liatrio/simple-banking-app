package com.smartbank.util;

import java.util.regex.Pattern;

/**
 * Utility class for validation functions.
 */
public class ValidationUtils {
    
    // Regular expression for email validation
    private static final String EMAIL_REGEX = 
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    // Regular expression for username validation (alphanumeric and underscores, 3-20 chars)
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
    
    // Regular expression for name validation (letters, spaces, hyphens, apostrophes)
    private static final String NAME_REGEX = "^[a-zA-Z\\s'-]{1,50}$";
    
    // Pre-compiled patterns for performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    
    /**
     * Validate email format.
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Empty email is considered valid (not required)
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate username format.
     * @param username The username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false; // Username cannot be empty
        }
        
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate name format.
     * @param name The name to validate
     * @return true if the name is valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return true; // Empty name is considered valid (not required)
        }
        
        return NAME_PATTERN.matcher(name).matches();
    }
    
    /**
     * Validate password strength.
     * @param password The password to validate
     * @return true if the password is strong enough, false otherwise
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUppercase && hasLowercase && hasDigit;
    }
    
    /**
     * Get a description of password requirements.
     * @return A string describing password requirements
     */
    public static String getPasswordRequirements() {
        return "Password must be at least 8 characters long and contain at least "
               + "one uppercase letter, one lowercase letter, and one number.";
    }
    
    /**
     * Validate that a text field is not empty.
     * @param value The value to validate
     * @param fieldName The name of the field for error message
     * @return null if valid, error message if invalid
     */
    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required";
        }
        return null;
    }
    
    /**
     * Validate that a value is within a specified length range.
     * @param value The value to validate
     * @param minLength The minimum allowed length
     * @param maxLength The maximum allowed length
     * @param fieldName The name of the field for error message
     * @return null if valid, error message if invalid
     */
    public static String validateLength(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            return null; // Null is handled by validateRequired if needed
        }
        
        int length = value.trim().length();
        
        if (length < minLength) {
            return fieldName + " must be at least " + minLength + " characters";
        }
        
        if (length > maxLength) {
            return fieldName + " cannot exceed " + maxLength + " characters";
        }
        
        return null;
    }
}