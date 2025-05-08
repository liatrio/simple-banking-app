package com.smartbank.service.statement;

import java.io.File;
import java.util.Date;

/**
 * Service interface for generating account statements.
 */
public interface StatementGenerationService {
    
    /**
     * Generate a PDF statement for a specific account and time period.
     * 
     * @param accountNumber The account number
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY")
     * @return The generated PDF as a byte array
     * @throws StatementGenerationException if there's an error generating the statement
     */
    byte[] generateStatement(long accountNumber, Date startDate, Date endDate, 
                           StatementType statementType) throws StatementGenerationException;
                           
    /**
     * Generate a PDF statement for a specific account and time period.
     * 
     * @param account The account object
     * @param user The user who owns the account
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY")
     * @return The generated PDF as a byte array
     * @throws StatementGenerationException if there's an error generating the statement
     */
    byte[] generateStatement(com.smartbank.model.Account account, com.smartbank.model.User user, 
                           Date startDate, Date endDate, 
                           StatementType statementType) throws StatementGenerationException;
    
    /**
     * Generate and store a PDF statement for a specific account and time period.
     * 
     * @param accountNumber The account number
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY") 
     * @return The stored statement record
     * @throws StatementGenerationException if there's an error generating or storing the statement
     */
    StatementRecord generateAndStoreStatement(long accountNumber, Date startDate, Date endDate,
                                           StatementType statementType) throws StatementGenerationException;
                                           
    /**
     * Generate and store a PDF statement for a specific account and time period.
     * 
     * @param account The account object
     * @param user The user who owns the account
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY") 
     * @return The stored statement record
     * @throws StatementGenerationException if there's an error generating or storing the statement
     */
    StatementRecord generateAndStoreStatement(com.smartbank.model.Account account, 
                                           com.smartbank.model.User user,
                                           Date startDate, Date endDate,
                                           StatementType statementType) throws StatementGenerationException;
    
    /**
     * Generate statements for all accounts for a specific period.
     * 
     * @param period The period for which to generate statements (e.g., "2025-05")
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY")
     * @return The number of statements generated
     */
    int generateStatementsForPeriod(String period, StatementType statementType);
    
    /**
     * Get a previously generated statement.
     * 
     * @param statementId The ID of the statement to retrieve
     * @return The statement as a byte array, or null if not found
     */
    byte[] getStatement(long statementId);
    
    /**
     * View a statement by opening it with the system's default PDF viewer.
     * 
     * @param statement The statement record to view
     * @return true if successful, false otherwise
     * @throws java.io.IOException if there's an error viewing the statement
     */
    boolean viewStatement(StatementRecord statement) throws java.io.IOException;
    
    /**
     * Get the content of a statement record.
     * 
     * @param statement The statement record
     * @return The statement as a byte array, or null if not found
     * @throws java.io.IOException if there's an error retrieving the statement
     */
    byte[] getStatementContent(StatementRecord statement) throws java.io.IOException;
    
    /**
     * Export a statement to a file.
     * 
     * @param statementId The ID of the statement to export
     * @param outputFile The file to write the statement to
     * @return true if the export was successful, false otherwise
     */
    boolean exportStatement(long statementId, File outputFile);
    
    /**
     * Send a statement by email.
     * 
     * @param statementId The ID of the statement to send
     * @param recipientEmail The email address to send the statement to
     * @param ccEmails Optional CC email addresses
     * @return true if the email was sent successfully, false otherwise
     */
    boolean emailStatement(long statementId, String recipientEmail, String... ccEmails);
    
    /**
     * Send a statement by email.
     * 
     * @param statement The statement record to send
     * @param recipientEmail The email address to send the statement to
     * @param ccEmails Optional CC email addresses
     * @return true if the email was sent successfully, false otherwise
     */
    boolean emailStatement(StatementRecord statement, String recipientEmail, String... ccEmails);
    
    /**
     * Get available statements for an account.
     * 
     * @param accountNumber The account number
     * @return Array of statement records for the account
     */
    StatementRecord[] getStatementHistory(long accountNumber);
    
    /**
     * Get statement history for a specific user.
     * 
     * @param user The user
     * @return List of statement records for the user's accounts
     */
    java.util.List<StatementRecord> getStatementHistory(com.smartbank.model.User user);
    
    /**
     * Schedule automatic statement generation for an account.
     * 
     * @param accountNumber The account number
     * @param statementType The type of statement (e.g., "MONTHLY", "QUARTERLY")
     * @param emailDelivery Whether to deliver the statement by email
     * @return true if the schedule was set successfully, false otherwise
     */
    boolean scheduleStatementGeneration(long accountNumber, StatementType statementType, boolean emailDelivery);
}