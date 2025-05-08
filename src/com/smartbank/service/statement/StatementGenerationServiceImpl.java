package com.smartbank.service.statement;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.model.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.StatementRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.UserRepository;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.reporting.CategoryReportService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the StatementGenerationService.
 */
public class StatementGenerationServiceImpl implements StatementGenerationService {
    
    private static final Logger LOGGER = Logger.getLogger(StatementGenerationServiceImpl.class.getName());
    
    private final StatementRepository statementRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryReportService categoryReportService;
    
    // Directory to store statements
    private final String statementDirectory;
    
    // Email configuration
    private final Properties emailProperties;
    private final String emailUsername;
    private final String emailPassword;
    private final String emailFrom;
    
    // Fonts for PDF generation
    private final PDFont titleFont = PDType1Font.HELVETICA_BOLD;
    private final PDFont headerFont = PDType1Font.HELVETICA_BOLD;
    private final PDFont normalFont = PDType1Font.HELVETICA;
    
    // Scheduler for periodic statement generation
    private final ScheduledExecutorService scheduler;
    
    /**
     * Constructs a new StatementGenerationServiceImpl.
     */
    public StatementGenerationServiceImpl() {
        this.statementRepository = RepositoryFactory.getStatementRepository();
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.userRepository = RepositoryFactory.getUserRepository();
        this.categoryReportService = ServiceFactory.getCategoryReportService();
        
        // Initialize statement directory
        this.statementDirectory = System.getProperty("user.home") + File.separator + 
                              "SmartBank" + File.separator + "statements";
        
        // Create the directory if it doesn't exist
        File directory = new File(statementDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                LOGGER.warning("Failed to create statement directory: " + statementDirectory);
            }
        }
        
        // Initialize email configuration
        this.emailProperties = new Properties();
        this.emailProperties.put("mail.smtp.auth", "true");
        this.emailProperties.put("mail.smtp.starttls.enable", "true");
        this.emailProperties.put("mail.smtp.host", "smtp.example.com");
        this.emailProperties.put("mail.smtp.port", "587");
        
        // Set these from system properties or configuration in a real application
        this.emailUsername = "smartbank@example.com";
        this.emailPassword = "password";
        this.emailFrom = "statements@smartbank.example.com";
        
        // Initialize scheduler
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Start periodic email delivery task
        scheduler.scheduleAtFixedRate(this::processEmailDeliveryQueue, 1, 30, TimeUnit.MINUTES);
    }
    
    @Override
    public byte[] generateStatement(long accountNumber, Date startDate, Date endDate, 
                                 StatementType statementType) throws StatementGenerationException {
        
        // Validate account existence
        Account account = accountRepository.findById(accountNumber).orElse(null);
        if (account == null) {
            throw new StatementGenerationException("Account not found: " + accountNumber);
        }
        
        // Get user details
        String userId = account.getAccountHolder().getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new StatementGenerationException("User not found for account: " + accountNumber);
        }
        
        return generateStatement(account, user, startDate, endDate, statementType);
    }
    
    @Override
    public byte[] generateStatement(Account account, User user, Date startDate, Date endDate, 
                                 StatementType statementType) throws StatementGenerationException {
        if (account == null) {
            throw new StatementGenerationException("Account cannot be null");
        }
        
        if (user == null) {
            throw new StatementGenerationException("User cannot be null");
        }
        
        // Get transactions for the period
        List<Transaction> transactions = getTransactionsForPeriod(account.getAccountNumber(), startDate, endDate);
        
        // Generate PDF
        byte[] pdfData;
        try {
            pdfData = generatePdf(account, user, transactions, startDate, endDate, statementType);
        } catch (IOException e) {
            throw new StatementGenerationException("Error generating PDF: " + e.getMessage(), e);
        }
        
        return pdfData;
    }
    
    @Override
    public StatementRecord generateAndStoreStatement(long accountNumber, Date startDate, Date endDate,
                                                 StatementType statementType) throws StatementGenerationException {
        
        // Get account details to validate and get user ID
        Account account = accountRepository.findById(accountNumber).orElse(null);
        if (account == null) {
            throw new StatementGenerationException("Account not found: " + accountNumber);
        }
        
        // Get user details
        String userId = account.getAccountHolder().getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new StatementGenerationException("User not found for account: " + accountNumber);
        }
        
        return generateAndStoreStatement(account, user, startDate, endDate, statementType);
    }
    
    @Override
    public StatementRecord generateAndStoreStatement(Account account, User user, Date startDate, Date endDate,
                                                 StatementType statementType) throws StatementGenerationException {
        if (account == null) {
            throw new StatementGenerationException("Account cannot be null");
        }
        
        if (user == null) {
            throw new StatementGenerationException("User cannot be null");
        }
        
        // Generate the statement PDF
        byte[] pdfData = generateStatement(account, user, startDate, endDate, statementType);
        
        // Create a new statement record
        StatementRecord statementRecord = new StatementRecord(
                account.getAccountNumber(), user.getUserId(), startDate, endDate, statementType);
        
        // Set PDF content and details
        statementRecord.setContent(pdfData);
        statementRecord.setPageCount(countPages(pdfData));
        
        // Save the statement record
        statementRepository.save(statementRecord);
        
        // Save the PDF to the file system
        try {
            String filePath = saveStatementToFile(statementRecord, pdfData);
            statementRecord.setFilePath(filePath);
            statementRepository.update(statementRecord);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save statement to file system: " + e.getMessage(), e);
            // Continue without saving to file system, as we still have the content in the database
        }
        
        return statementRecord;
    }
    
    @Override
    public int generateStatementsForPeriod(String period, StatementType statementType) {
        LOGGER.info("Generating " + statementType + " statements for period: " + period);
        
        // Parse the period (format: yyyy-MM)
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat periodFormat = new SimpleDateFormat("yyyy-MM");
            Date periodDate = periodFormat.parse(period);
            calendar.setTime(periodDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Invalid period format: " + period, e);
            return 0;
        }
        
        // Set start and end dates based on statement type
        Date startDate;
        Date endDate;
        
        calendar.set(Calendar.DAY_OF_MONTH, 1); // First day of the month
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        switch (statementType) {
            case MONTHLY:
                startDate = calendar.getTime();
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.MILLISECOND, -1);
                endDate = calendar.getTime();
                break;
                
            case QUARTERLY:
                // Adjust to the start of the quarter
                int month = calendar.get(Calendar.MONTH);
                int quarterStartMonth = (month / 3) * 3; // 0, 3, 6, or 9
                calendar.set(Calendar.MONTH, quarterStartMonth);
                startDate = calendar.getTime();
                
                // End of quarter
                calendar.add(Calendar.MONTH, 3);
                calendar.add(Calendar.MILLISECOND, -1);
                endDate = calendar.getTime();
                break;
                
            case ANNUAL:
                // Adjust to the start of the year
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                startDate = calendar.getTime();
                
                // End of year
                calendar.add(Calendar.YEAR, 1);
                calendar.add(Calendar.MILLISECOND, -1);
                endDate = calendar.getTime();
                break;
                
            default:
                LOGGER.warning("Unsupported statement type for batch generation: " + statementType);
                return 0;
        }
        
        // Get all accounts
        List<Account> accounts = accountRepository.findAll();
        int statementsGenerated = 0;
        
        // Generate statements for each account
        for (Account account : accounts) {
            try {
                // Get corresponding user
                User accountUser = account.getAccountHolder();
                if (accountUser == null) {
                    LOGGER.warning("Account " + account.getAccountNumber() + " has no account holder");
                    continue;
                }
                
                StatementRecord statement = generateAndStoreStatement(
                        account, accountUser, startDate, endDate, statementType);
                
                // Check if email delivery is needed for this user/account
                if (accountUser.getEmail() != null && !accountUser.getEmail().isEmpty()) {
                    statement.setEmailRecipient(accountUser.getEmail());
                    statementRepository.update(statement);
                }
                
                statementsGenerated++;
                LOGGER.info("Generated statement for account " + account.getAccountNumber());
            } catch (StatementGenerationException e) {
                LOGGER.log(Level.WARNING, "Failed to generate statement for account " + 
                         account.getAccountNumber() + ": " + e.getMessage(), e);
            }
        }
        
        return statementsGenerated;
    }
    
    @Override
    public byte[] getStatement(long statementId) {
        StatementRecord statement = statementRepository.findById(statementId).orElse(null);
        if (statement == null) {
            return null;
        }
        
        try {
            return getStatementContent(statement);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to get statement content: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public byte[] getStatementContent(StatementRecord statement) throws IOException {
        if (statement == null) {
            return null;
        }
        
        byte[] content = statement.getContent();
        if (content != null && content.length > 0) {
            return content;
        }
        
        // If content is not stored in the database, try to read from file
        String filePath = statement.getFilePath();
        if (filePath != null && !filePath.isEmpty()) {
            try {
                return java.nio.file.Files.readAllBytes(new File(filePath).toPath());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to read statement file: " + filePath, e);
                throw e;
            }
        }
        
        throw new IOException("Statement content not available for statement ID: " + statement.getStatementId());
    }
    
    @Override
    public boolean viewStatement(StatementRecord statement) throws IOException {
        if (statement == null) {
            return false;
        }
        
        byte[] content = getStatementContent(statement);
        if (content == null || content.length == 0) {
            return false;
        }
        
        // Create a temporary file
        File tempFile = File.createTempFile("statement_", ".pdf");
        tempFile.deleteOnExit();
        
        // Write the content to the file
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content);
        }
        
        // Open the file with the system's default PDF viewer
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
                return true;
            } else {
                LOGGER.warning("Desktop is not supported, cannot open PDF viewer");
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to open PDF viewer: " + e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean exportStatement(long statementId, File outputFile) {
        byte[] statementData = getStatement(statementId);
        if (statementData == null) {
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(statementData);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to export statement: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean emailStatement(StatementRecord statement, String recipientEmail, String... ccEmails) {
        if (statement == null) {
            LOGGER.warning("Statement is null");
            return false;
        }
        
        // Get statement data
        byte[] statementData;
        try {
            statementData = getStatementContent(statement);
            if (statementData == null || statementData.length == 0) {
                LOGGER.warning("Statement data not found for ID: " + statement.getStatementId());
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to get statement content: " + e.getMessage(), e);
            return false;
        }
        
        return sendStatementEmail(statement, statementData, recipientEmail, ccEmails);
    }
    
    @Override
    public boolean emailStatement(long statementId, String recipientEmail, String... ccEmails) {
        // Validate statement
        StatementRecord statement = statementRepository.findById(statementId).orElse(null);
        if (statement == null) {
            LOGGER.warning("Statement not found: " + statementId);
            return false;
        }
        
        return emailStatement(statement, recipientEmail, ccEmails);
    }
    
    /**
     * Helper method to send statement email.
     */
    private boolean sendStatementEmail(StatementRecord statement, byte[] statementData, 
                                    String recipientEmail, String... ccEmails) {
        
        try {
            // Create email session
            Session session = Session.getInstance(emailProperties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsername, emailPassword);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            
            // Add CC recipients if provided
            if (ccEmails != null && ccEmails.length > 0) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(String.join(",", ccEmails)));
            }
            
            // Get account details for the subject
            Account account = accountRepository.findById(statement.getAccountNumber()).orElse(null);
            String accountType = account != null ? account.getClass().getSimpleName() : "Account";
            
            // Set subject and date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            String periodStr = dateFormat.format(statement.getStartDate());
            message.setSubject("Your " + statement.getStatementType().getDisplayName() + 
                             " for " + accountType + " - " + periodStr);
            
            // Create multipart email
            Multipart multipart = new MimeMultipart();
            
            // Add text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Dear Customer,\n\nPlease find attached your " + 
                           statement.getStatementType().getDisplayName().toLowerCase() + 
                           " for the period ending " + dateFormat.format(statement.getEndDate()) + 
                           ".\n\nThank you for banking with SmartBank.\n\nSincerely,\nSmartBank Customer Service");
            multipart.addBodyPart(textPart);
            
            // Add attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName(statement.getFileName());
            attachmentPart.setContent(statementData, "application/pdf");
            multipart.addBodyPart(attachmentPart);
            
            // Set content
            message.setContent(multipart);
            
            // Send message
            Transport.send(message);
            
            // Update statement record
            statement.setEmailDelivered(true);
            statement.setEmailRecipient(recipientEmail);
            statement.setEmailDate(new Date());
            statementRepository.update(statement);
            
            LOGGER.info("Statement " + statement.getStatementId() + " emailed to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.WARNING, "Failed to email statement: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public StatementRecord[] getStatementHistory(long accountNumber) {
        List<StatementRecord> statements = statementRepository.findByAccountNumber(accountNumber);
        return statements.toArray(new StatementRecord[0]);
    }
    
    @Override
    public List<StatementRecord> getStatementHistory(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        
        try {
            // Get all statements for this user
            return statementRepository.findByUserId(user.getUserId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting statement history: " + e.getMessage(), e);
            // Return empty list rather than crashing
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean scheduleStatementGeneration(long accountNumber, StatementType statementType, boolean emailDelivery) {
        // Validate account
        Account account = accountRepository.findById(accountNumber).orElse(null);
        if (account == null) {
            LOGGER.warning("Account not found: " + accountNumber);
            return false;
        }
        
        // In a real implementation, we would store the schedule in a database table
        // For this demo, we'll just log it and assume it's stored
        LOGGER.info("Scheduled " + statementType + " statement generation for account " + 
                 accountNumber + " with email delivery: " + emailDelivery);
        
        return true;
    }
    
    /**
     * Process the queue of statements that need to be emailed.
     */
    private void processEmailDeliveryQueue() {
        LOGGER.info("Processing email delivery queue");
        
        try {
            List<StatementRecord> pendingDelivery = statementRepository.findPendingEmailDelivery();
            
            for (StatementRecord statement : pendingDelivery) {
                if (statement.getEmailRecipient() != null && !statement.getEmailRecipient().isEmpty()) {
                    boolean success = emailStatement(statement, statement.getEmailRecipient());
                    if (success) {
                        LOGGER.info("Successfully delivered statement " + statement.getStatementId() + 
                                 " to " + statement.getEmailRecipient());
                    } else {
                        LOGGER.warning("Failed to deliver statement " + statement.getStatementId() + 
                                    " to " + statement.getEmailRecipient());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing email delivery queue: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get transactions for an account within a date range.
     * 
     * @param accountNumber The account number
     * @param startDate The start date
     * @param endDate The end date
     * @return List of transactions
     */
    private List<Transaction> getTransactionsForPeriod(long accountNumber, Date startDate, Date endDate) {
        List<Transaction> allTransactions = transactionRepository.findByAccountNumber(accountNumber);
        
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            Date transactionDate = transaction.getTimestamp();
            if ((transactionDate.equals(startDate) || transactionDate.after(startDate)) &&
                (transactionDate.equals(endDate) || transactionDate.before(endDate))) {
                filteredTransactions.add(transaction);
            }
        }
        
        // Sort transactions by date, most recent first
        filteredTransactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
        
        return filteredTransactions;
    }
    
    /**
     * Generate a PDF statement.
     * 
     * @param account The account
     * @param user The user
     * @param transactions The transactions
     * @param startDate The start date of the statement period
     * @param endDate The end date of the statement period
     * @param statementType The statement type
     * @return The generated PDF as a byte array
     * @throws IOException if there's an error generating the PDF
     */
    private byte[] generatePdf(Account account, User user, List<Transaction> transactions, 
                           Date startDate, Date endDate, StatementType statementType) throws IOException {
        
        // Create a new PDF document
        PDDocument document = new PDDocument();
        
        try {
            // Add first page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // Get page dimensions
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            
            // Start a new content stream for the first page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Add the header
            float yPosition = height - 50;
            
            // Bank logo and name (placeholder for actual logo)
            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("SMARTBANK");
            contentStream.endText();
            
            // Statement type and period
            yPosition -= 30;
            contentStream.beginText();
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(statementType.getDisplayName());
            contentStream.endText();
            
            // Format dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
            String periodText = dateFormat.format(startDate) + " to " + dateFormat.format(endDate);
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(normalFont, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Period: " + periodText);
            contentStream.endText();
            
            // Add account information
            yPosition -= 40;
            contentStream.beginText();
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Account Information");
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Account Number: " + account.getAccountNumber());
            contentStream.endText();
            
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Account Type: " + account.getClass().getSimpleName());
            contentStream.endText();
            
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Account Holder: " + user.getFirstName() + " " + user.getLastName());
            contentStream.endText();
            
            // Add balance information
            yPosition -= 30;
            contentStream.beginText();
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Balance Summary");
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Opening Balance: $" + String.format("%.2f", calculateOpeningBalance(account, transactions)));
            contentStream.endText();
            
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Closing Balance: $" + String.format("%.2f", account.getBalance()));
            contentStream.endText();
            
            // Add transaction header
            yPosition -= 40;
            contentStream.beginText();
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Transaction History");
            contentStream.endText();
            
            // Draw transaction table header
            yPosition -= 25;
            float tableWidth = width - 100;
            float[] columnWidths = {150, 200, 100, 100};
            
            // Table header
            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Date");
            contentStream.newLineAtOffset(columnWidths[0], 0);
            contentStream.showText("Description");
            contentStream.newLineAtOffset(columnWidths[1], 0);
            contentStream.showText("Type");
            contentStream.newLineAtOffset(columnWidths[2], 0);
            contentStream.showText("Amount");
            contentStream.endText();
            
            // Draw a line under the header
            yPosition -= 5;
            contentStream.setLineWidth(1);
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(50 + tableWidth, yPosition);
            contentStream.stroke();
            
            // Add transaction rows
            SimpleDateFormat transactionDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            yPosition -= 20;
            int rowCount = 0;
            
            for (Transaction transaction : transactions) {
                // Check if we need a new page
                if (yPosition < 100) {
                    // Close current content stream
                    contentStream.close();
                    
                    // Add a new page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    
                    // Reset position for the new page
                    yPosition = height - 50;
                    
                    // Add page header
                    contentStream.beginText();
                    contentStream.setFont(titleFont, 12);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("SMARTBANK - " + statementType.getDisplayName() + " (continued)");
                    contentStream.endText();
                    
                    // Add transaction table header again
                    yPosition -= 30;
                    
                    contentStream.beginText();
                    contentStream.setFont(headerFont, 10);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Date");
                    contentStream.newLineAtOffset(columnWidths[0], 0);
                    contentStream.showText("Description");
                    contentStream.newLineAtOffset(columnWidths[1], 0);
                    contentStream.showText("Type");
                    contentStream.newLineAtOffset(columnWidths[2], 0);
                    contentStream.showText("Amount");
                    contentStream.endText();
                    
                    // Draw a line under the header
                    yPosition -= 5;
                    contentStream.setLineWidth(1);
                    contentStream.moveTo(50, yPosition);
                    contentStream.lineTo(50 + tableWidth, yPosition);
                    contentStream.stroke();
                    
                    yPosition -= 20;
                }
                
                // Determine transaction text color (red for negative amounts)
                double amount = Math.abs(transaction.getSignedAmount());
                boolean isNegative = transaction.getSignedAmount() < 0;
                
                // Add transaction data
                contentStream.beginText();
                contentStream.setFont(normalFont, 9);
                
                // Date
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(transactionDateFormat.format(transaction.getTimestamp()));
                
                // Description
                contentStream.newLineAtOffset(columnWidths[0], 0);
                String description = transaction.getDescription();
                if (description == null || description.isEmpty()) {
                    description = transaction.getType().toString();
                }
                // Truncate if too long
                if (description.length() > 30) {
                    description = description.substring(0, 27) + "...";
                }
                contentStream.showText(description);
                
                // Type
                contentStream.newLineAtOffset(columnWidths[1], 0);
                contentStream.showText(transaction.getType().toString());
                
                // Amount (with color)
                contentStream.newLineAtOffset(columnWidths[2], 0);
                String amountText = isNegative ? "-$" + String.format("%.2f", amount) : 
                                              "$" + String.format("%.2f", amount);
                contentStream.showText(amountText);
                
                contentStream.endText();
                
                // Move to next row
                yPosition -= 15;
                rowCount++;
                
                // Limit to 50 transactions per statement (could add pagination for more)
                if (rowCount >= 50) {
                    break;
                }
            }
            
            // Add footer
            yPosition = 50;
            contentStream.beginText();
            contentStream.setFont(normalFont, 8);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("This statement was generated on " + 
                                 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            contentStream.endText();
            
            // Close the content stream
            contentStream.close();
            
            // Save the document to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } finally {
            // Ensure document is closed even on exception
            document.close();
        }
    }
    
    /**
     * Calculate the opening balance for an account based on transactions.
     * 
     * @param account The account
     * @param transactions Transactions for the period
     * @return The opening balance
     */
    private double calculateOpeningBalance(Account account, List<Transaction> transactions) {
        double currentBalance = account.getBalance();
        
        // If no transactions, return current balance
        if (transactions.isEmpty()) {
            return currentBalance;
        }
        
        // Sum all transaction amounts to determine the net change
        double netChange = 0.0;
        for (Transaction transaction : transactions) {
            netChange += transaction.getSignedAmount();
        }
        
        // Subtract the net change from current balance to get opening balance
        return currentBalance - netChange;
    }
    
    /**
     * Save a statement to a file.
     * 
     * @param statement The statement record
     * @param pdfData The PDF data
     * @return The file path
     * @throws IOException If an I/O error occurs
     */
    private String saveStatementToFile(StatementRecord statement, byte[] pdfData) throws IOException {
        // Create a directory for the account if it doesn't exist
        String accountDir = statementDirectory + File.separator + statement.getAccountNumber();
        File directory = new File(accountDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + accountDir);
            }
        }
        
        // Create the file path
        String filePath = accountDir + File.separator + statement.getFileName();
        
        // Write the file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfData);
        }
        
        return filePath;
    }
    
    /**
     * Count the number of pages in a PDF.
     * 
     * @param pdfData The PDF data
     * @return The number of pages, or 0 if there was an error
     */
    private int countPages(byte[] pdfData) {
        try (PDDocument document = PDDocument.load(pdfData)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error counting PDF pages: " + e.getMessage(), e);
            return 0;
        }
    }
}