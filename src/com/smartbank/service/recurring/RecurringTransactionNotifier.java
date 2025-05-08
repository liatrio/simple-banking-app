package com.smartbank.service.recurring;

import com.smartbank.model.Account;
import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for generating notifications about upcoming recurring transactions.
 */
public class RecurringTransactionNotifier {
    private static final Logger LOGGER = Logger.getLogger(RecurringTransactionNotifier.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    
    // Singleton instance
    private static RecurringTransactionNotifier instance;
    
    // Services and repositories
    private final RecurringTransactionService recurringTransactionService;
    private final AccountRepository accountRepository;
    private final UserService userService;
    
    // Current state
    private boolean isEnabled = false;
    
    /**
     * Get the singleton instance of the notifier.
     * 
     * @return The RecurringTransactionNotifier instance
     */
    public static synchronized RecurringTransactionNotifier getInstance() {
        if (instance == null) {
            instance = new RecurringTransactionNotifier();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private RecurringTransactionNotifier() {
        this.recurringTransactionService = ServiceFactory.getRecurringTransactionService();
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.userService = ServiceFactory.getUserService();
    }
    
    /**
     * Enable notifications.
     */
    public void enable() {
        isEnabled = true;
        LOGGER.info("Recurring transaction notifications enabled");
    }
    
    /**
     * Disable notifications.
     */
    public void disable() {
        isEnabled = false;
        LOGGER.info("Recurring transaction notifications disabled");
    }
    
    /**
     * Check if notifications are enabled.
     * 
     * @return true if notifications are enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Check for upcoming transactions and generate notifications.
     * 
     * @param daysInAdvance The number of days to look ahead
     * @return Map of user IDs to notification messages
     */
    public Map<String, List<String>> generateUpcomingTransactionNotifications(int daysInAdvance) {
        if (!isEnabled) {
            LOGGER.info("Notifications are disabled, skipping notification generation");
            return new HashMap<>();
        }
        
        Map<String, List<String>> notifications = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate notificationDate = today.plusDays(daysInAdvance);
        
        try {
            // Get all active recurring transactions
            List<RecurringTransaction> activeTransactions = 
                    recurringTransactionService.getRecurringTransactionsByStatus(RecurringTransaction.Status.ACTIVE);
            
            for (RecurringTransaction transaction : activeTransactions) {
                // Check if the transaction is due on the notification date
                if (transaction.getNextExecutionDate() != null && 
                    transaction.getNextExecutionDate().equals(notificationDate)) {
                    
                    // Get the source account
                    Optional<Account> accountOpt = accountRepository.findById(transaction.getSourceAccountNumber());
                    if (!accountOpt.isPresent()) {
                        continue;
                    }
                    
                    Account account = accountOpt.get();
                    User user = account.getAccountHolder();
                    String userId = user.getUserId();
                    
                    // Create the notification message
                    String message = createNotificationMessage(transaction, account, notificationDate);
                    
                    // Add the message to the user's notifications
                    if (!notifications.containsKey(userId)) {
                        notifications.put(userId, new ArrayList<>());
                    }
                    notifications.get(userId).add(message);
                    
                    LOGGER.info("Generated notification for user " + userId + 
                               " about upcoming transaction on " + notificationDate);
                }
            }
            
            return notifications;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating notifications for upcoming transactions", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Create a notification message for an upcoming transaction.
     * 
     * @param transaction The recurring transaction
     * @param account The source account
     * @param executionDate The execution date
     * @return The notification message
     */
    private String createNotificationMessage(RecurringTransaction transaction, 
                                           Account account, LocalDate executionDate) {
        StringBuilder message = new StringBuilder();
        
        // Format the message based on transaction type
        switch (transaction.getType()) {
            case DEPOSIT:
                message.append("Upcoming deposit of $")
                       .append(String.format("%.2f", transaction.getAmount()))
                       .append(" to account #")
                       .append(account.getAccountNumber())
                       .append(" on ")
                       .append(executionDate.format(DATE_FORMATTER))
                       .append(": ")
                       .append(transaction.getDescription());
                break;
                
            case WITHDRAWAL:
                message.append("Upcoming withdrawal of $")
                       .append(String.format("%.2f", transaction.getAmount()))
                       .append(" from account #")
                       .append(account.getAccountNumber())
                       .append(" on ")
                       .append(executionDate.format(DATE_FORMATTER))
                       .append(": ")
                       .append(transaction.getDescription());
                break;
                
            case TRANSFER_OUT:
                // Get the target account name
                String targetAccountInfo = "account #" + transaction.getTargetAccountNumber();
                Optional<Account> targetAccountOpt = accountRepository.findById(transaction.getTargetAccountNumber());
                if (targetAccountOpt.isPresent()) {
                    Account targetAccount = targetAccountOpt.get();
                    targetAccountInfo = targetAccount.getAccountHolder().getUsername() + 
                                       "'s account #" + targetAccount.getAccountNumber();
                }
                
                message.append("Upcoming transfer of $")
                       .append(String.format("%.2f", transaction.getAmount()))
                       .append(" from account #")
                       .append(account.getAccountNumber())
                       .append(" to ")
                       .append(targetAccountInfo)
                       .append(" on ")
                       .append(executionDate.format(DATE_FORMATTER))
                       .append(": ")
                       .append(transaction.getDescription());
                break;
                
            case PAYMENT:
                message.append("Upcoming payment of $")
                       .append(String.format("%.2f", transaction.getAmount()))
                       .append(" from account #")
                       .append(account.getAccountNumber())
                       .append(" on ")
                       .append(executionDate.format(DATE_FORMATTER))
                       .append(": ")
                       .append(transaction.getDescription());
                break;
                
            default:
                message.append("Upcoming transaction of $")
                       .append(String.format("%.2f", transaction.getAmount()))
                       .append(" for account #")
                       .append(account.getAccountNumber())
                       .append(" on ")
                       .append(executionDate.format(DATE_FORMATTER))
                       .append(": ")
                       .append(transaction.getDescription());
        }
        
        return message.toString();
    }
    
    /**
     * Send notifications to users.
     * 
     * @param notifications Map of user IDs to notification messages
     */
    public void sendNotifications(Map<String, List<String>> notifications) {
        if (!isEnabled) {
            LOGGER.info("Notifications are disabled, skipping notification sending");
            return;
        }
        
        // This is a placeholder for a notification delivery system
        for (Map.Entry<String, List<String>> entry : notifications.entrySet()) {
            String userId = entry.getKey();
            List<String> messages = entry.getValue();
            
            for (String message : messages) {
                // In a real implementation, this would send the notification via email, SMS, app notification, etc.
                LOGGER.info("Sending notification to user " + userId + ": " + message);
            }
        }
    }
    
    /**
     * Check for upcoming transactions and send notifications.
     * 
     * @param daysInAdvance The number of days to look ahead
     * @return The number of notifications sent
     */
    public int notifyUpcomingTransactions(int daysInAdvance) {
        if (!isEnabled) {
            LOGGER.info("Notifications are disabled, skipping notification processing");
            return 0;
        }
        
        Map<String, List<String>> notifications = generateUpcomingTransactionNotifications(daysInAdvance);
        
        // Count total notifications
        int notificationCount = 0;
        for (List<String> userNotifications : notifications.values()) {
            notificationCount += userNotifications.size();
        }
        
        // Send the notifications
        sendNotifications(notifications);
        
        LOGGER.info("Sent " + notificationCount + " notifications for upcoming transactions");
        return notificationCount;
    }
}