package com.smartbank.util.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 * This class provides methods to create test instances of domain objects
 * with predefined or random values.
 */
public class TestDataFactory {

    /**
     * Generate a random string with the given prefix.
     * 
     * @param prefix Prefix for the string
     * @return A random string with the given prefix
     */
    public static String randomString(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generate a random email address.
     * 
     * @return A random email address
     */
    public static String randomEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
    
    /**
     * Generate a random phone number.
     * 
     * @return A random phone number
     */
    public static String randomPhoneNumber() {
        StringBuilder sb = new StringBuilder("555");
        for (int i = 0; i < 7; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
    
    /**
     * Generate a random amount between min and max.
     * 
     * @param min Minimum amount
     * @param max Maximum amount
     * @return A random amount between min and max
     */
    public static BigDecimal randomAmount(double min, double max) {
        double amount = min + Math.random() * (max - min);
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Generate a random date within the given range.
     * 
     * @param startDaysAgo Start of range in days ago
     * @param endDaysAgo End of range in days ago
     * @return A random date within the range
     */
    public static LocalDate randomDate(int startDaysAgo, int endDaysAgo) {
        int daysAgo = endDaysAgo + (int) (Math.random() * (startDaysAgo - endDaysAgo));
        return LocalDate.now().minusDays(daysAgo);
    }
    
    /**
     * Generate a random date-time within the given range.
     * 
     * @param startHoursAgo Start of range in hours ago
     * @param endHoursAgo End of range in hours ago
     * @return A random date-time within the range
     */
    public static LocalDateTime randomDateTime(int startHoursAgo, int endHoursAgo) {
        int hoursAgo = endHoursAgo + (int) (Math.random() * (startHoursAgo - endHoursAgo));
        return LocalDateTime.now().minusHours(hoursAgo);
    }
    
    /**
     * Generate a list of random strings.
     * 
     * @param prefix Prefix for the strings
     * @param count Number of strings to generate
     * @return A list of random strings
     */
    public static List<String> randomStringList(String prefix, int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(randomString(prefix));
        }
        return result;
    }
}
