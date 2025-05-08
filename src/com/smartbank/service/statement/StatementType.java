package com.smartbank.service.statement;

/**
 * Enum representing different types of account statements.
 */
public enum StatementType {
    
    /**
     * Monthly statement, typically covering a calendar month.
     */
    MONTHLY(30, "Monthly Statement"),
    
    /**
     * Quarterly statement, typically covering a 3-month period.
     */
    QUARTERLY(90, "Quarterly Statement"),
    
    /**
     * Annual statement, covering a full year.
     */
    ANNUAL(365, "Annual Statement"),
    
    /**
     * Custom period statement.
     */
    CUSTOM(0, "Custom Statement");
    
    private final int defaultDays;
    private final String displayName;
    
    StatementType(int defaultDays, String displayName) {
        this.defaultDays = defaultDays;
        this.displayName = displayName;
    }
    
    /**
     * Get the default number of days covered by this statement type.
     * 
     * @return The default number of days
     */
    public int getDefaultDays() {
        return defaultDays;
    }
    
    /**
     * Get the display name for this statement type.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
}