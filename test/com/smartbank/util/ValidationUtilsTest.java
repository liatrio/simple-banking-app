package com.smartbank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ValidationUtils class.
 */
public class ValidationUtilsTest {

    @ParameterizedTest
    @DisplayName("isValidEmail should validate email formats correctly")
    @CsvSource({
            "test@example.com, true",
            "test.email@example.co.uk, true",
            "test+label@example.com, true",
            "test, false",
            "test@, false",
            "test@example, false",
            "@example.com, false",
            "test@.com, false",
            ", true" // Empty email is considered valid (not required)
    })
    public void testIsValidEmail(String email, boolean expected) {
        assertEquals(expected, ValidationUtils.isValidEmail(email));
    }

    @Test
    @DisplayName("isValidEmail should handle null input")
    public void testIsValidEmailWithNull() {
        assertTrue(ValidationUtils.isValidEmail(null));
    }

    @ParameterizedTest
    @DisplayName("isValidUsername should validate username formats correctly")
    @CsvSource({
            "user123, true",
            "john_doe, true",
            "a12, true",
            "abcdefghij1234567890, true", // 20 chars
            "ab, false", // Too short
            "abcdefghij1234567890x, false", // Too long (21 chars)
            "user-name, false", // Contains hyphen
            "user.name, false", // Contains period
            "user name, false", // Contains space
            ", false" // Empty username is invalid
    })
    public void testIsValidUsername(String username, boolean expected) {
        assertEquals(expected, ValidationUtils.isValidUsername(username));
    }

    @Test
    @DisplayName("isValidUsername should handle null input")
    public void testIsValidUsernameWithNull() {
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @ParameterizedTest
    @DisplayName("isValidName should validate name formats correctly")
    @CsvSource({
            "John, true",
            "Mary Jane, true",
            "O'Connor, true",
            "Smith-Jones, true",
            "John123, false",
            "John@Doe, false",
            ", true" // Empty name is considered valid (not required)
    })
    public void testIsValidName(String name, boolean expected) {
        assertEquals(expected, ValidationUtils.isValidName(name));
    }

    @Test
    @DisplayName("isValidName should handle null input")
    public void testIsValidNameWithNull() {
        assertTrue(ValidationUtils.isValidName(null));
    }

    @ParameterizedTest
    @DisplayName("isPasswordStrong should validate password strength correctly")
    @CsvSource({
            "Password123, true", // Has uppercase, lowercase, and digit
            "password123, false", // No uppercase
            "PASSWORD123, false", // No lowercase
            "Password, false", // No digit
            "Pass1, false", // Too short
            ", false" // Empty password is invalid
    })
    public void testIsPasswordStrong(String password, boolean expected) {
        assertEquals(expected, ValidationUtils.isPasswordStrong(password));
    }

    @Test
    @DisplayName("isPasswordStrong should handle null input")
    public void testIsPasswordStrongWithNull() {
        assertFalse(ValidationUtils.isPasswordStrong(null));
    }

    @Test
    @DisplayName("getPasswordRequirements should return the correct requirements text")
    public void testGetPasswordRequirements() {
        String requirements = ValidationUtils.getPasswordRequirements();
        assertNotNull(requirements);
        assertTrue(requirements.contains("8 characters"));
        assertTrue(requirements.contains("uppercase"));
        assertTrue(requirements.contains("lowercase"));
        assertTrue(requirements.contains("number"));
    }

    @ParameterizedTest
    @DisplayName("validateRequired should validate required fields correctly")
    @CsvSource({
            "value, Field, ''",
            ", Field, Field is required",
            "'  ', Field, Field is required"
    })
    public void testValidateRequired(String value, String fieldName, String expected) {
        String result = ValidationUtils.validateRequired(value, fieldName);
        if (expected.isEmpty()) {
            assertNull(result);
        } else {
            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("validateRequired should handle null input")
    public void testValidateRequiredWithNull() {
        assertEquals("Field is required", ValidationUtils.validateRequired(null, "Field"));
    }

    @ParameterizedTest
    @DisplayName("validateLength should validate field length correctly")
    @CsvSource({
            "test, 2, 10, Field, ''", // Valid length
            "test, 5, 10, Field, Field must be at least 5 characters", // Too short
            "test12345678, 2, 10, Field, Field cannot exceed 10 characters", // Too long
            ", 2, 10, Field, ''" // Null value is handled by validateRequired
    })
    public void testValidateLength(String value, int minLength, int maxLength, String fieldName, String expected) {
        String result = ValidationUtils.validateLength(value, minLength, maxLength, fieldName);
        if (expected.isEmpty()) {
            assertNull(result);
        } else {
            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("validateLength should handle null input")
    public void testValidateLengthWithNull() {
        assertNull(ValidationUtils.validateLength(null, 5, 10, "Field"));
    }
}
