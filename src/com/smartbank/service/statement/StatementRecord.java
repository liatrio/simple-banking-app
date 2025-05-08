package com.smartbank.service.statement;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity representing a stored account statement.
 */
@Entity
@Table(name = "statement_records")
public class StatementRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long statementId;
    
    @Column(nullable = false)
    private long accountNumber;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date generationDate;
    
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatementType statementType;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private int pageCount;
    
    @Column
    private String description;
    
    @Column(columnDefinition = "BLOB")
    private byte[] content;
    
    @Column
    private String filePath;
    
    @Column
    private boolean emailDelivered;
    
    @Column
    private String emailRecipient;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailDate;
    
    // Default constructor required by JPA
    protected StatementRecord() {
    }
    
    /**
     * Constructs a new statement record.
     * 
     * @param accountNumber The account number
     * @param userId The user ID
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The type of statement
     */
    public StatementRecord(long accountNumber, String userId, Date startDate, Date endDate, 
                         StatementType statementType) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.generationDate = new Date();
        this.startDate = startDate;
        this.endDate = endDate;
        this.statementType = statementType;
        this.fileName = generateFileName(accountNumber, startDate, endDate);
        this.pageCount = 0;
        this.emailDelivered = false;
    }
    
    /**
     * Generate a file name for the statement.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return The generated file name
     */
    private String generateFileName(long accountNumber, Date startDate, Date endDate) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
        return String.format("statement_%d_%s_%s.pdf", 
                           accountNumber, 
                           dateFormat.format(startDate), 
                           dateFormat.format(endDate));
    }
    
    // Getters and setters
    
    public long getStatementId() {
        return statementId;
    }
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Date getGenerationDate() {
        return generationDate;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public StatementType getStatementType() {
        return statementType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public int getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public boolean isEmailDelivered() {
        return emailDelivered;
    }
    
    public void setEmailDelivered(boolean emailDelivered) {
        this.emailDelivered = emailDelivered;
    }
    
    public String getEmailRecipient() {
        return emailRecipient;
    }
    
    public void setEmailRecipient(String emailRecipient) {
        this.emailRecipient = emailRecipient;
    }
    
    public Date getEmailDate() {
        return emailDate;
    }
    
    public void setEmailDate(Date emailDate) {
        this.emailDate = emailDate;
    }
    
    /**
     * Gets a default file name for downloading the statement.
     * @return The default file name
     */
    public String getDefaultFileName() {
        return fileName;
    }
    
    /**
     * Gets a description of the period covered by this statement.
     * @return A string describing the period
     */
    public String getPeriodDescription() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(startDate) + " - " + dateFormat.format(endDate);
    }
    
    @Override
    public String toString() {
        return String.format("StatementRecord{statementId=%d, accountNumber=%d, period=%s to %s, type=%s}",
                           statementId, accountNumber, startDate, endDate, statementType);
    }
}