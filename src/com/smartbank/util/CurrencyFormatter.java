package com.smartbank.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting currency values
 */
public class CurrencyFormatter {
    
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    
    /**
     * Formats a double value as currency
     * 
     * @param amount The amount to format
     * @return The formatted currency string
     */
    public static String format(double amount) {
        return currencyFormatter.format(amount);
    }
    
    /**
     * Parses a currency string to a double value
     * 
     * @param currencyString The currency string to parse
     * @return The parsed double value
     * @throws java.text.ParseException if the string cannot be parsed
     */
    public static double parse(String currencyString) throws java.text.ParseException {
        return currencyFormatter.parse(currencyString).doubleValue();
    }
}
