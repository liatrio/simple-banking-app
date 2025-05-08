package com.smartbank.service.recurring;

import com.smartbank.model.RecurringTransaction;
import com.smartbank.service.ServiceFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler for executing recurring transactions.
 */
public class RecurringTransactionScheduler {
    private static final Logger LOGGER = Logger.getLogger(RecurringTransactionScheduler.class.getName());
    
    // Singleton instance
    private static RecurringTransactionScheduler instance;
    
    // Scheduler components
    private final ScheduledExecutorService scheduler;
    private final RecurringTransactionService recurringTransactionService;
    
    // Configuration
    private final LocalTime dailyProcessingTime;
    private final int maxRetries;
    private boolean notificationsEnabled;
    
    // Current state
    private boolean isRunning = false;
    
    /**
     * Get the singleton instance of the scheduler.
     * 
     * @return The RecurringTransactionScheduler instance
     */
    public static synchronized RecurringTransactionScheduler getInstance() {
        if (instance == null) {
            instance = new RecurringTransactionScheduler();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private RecurringTransactionScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.recurringTransactionService = ServiceFactory.getRecurringTransactionService();
        
        // Set default configuration
        this.dailyProcessingTime = LocalTime.of(1, 0); // 1:00 AM
        this.maxRetries = 3;
        this.notificationsEnabled = false;
    }
    
    /**
     * Start the scheduler.
     */
    public synchronized void start() {
        if (isRunning) {
            LOGGER.info("Scheduler is already running");
            return;
        }
        
        LOGGER.info("Starting recurring transaction scheduler");
        
        // Schedule daily processing
        scheduleDailyProcessing();
        
        isRunning = true;
    }
    
    /**
     * Stop the scheduler.
     */
    public synchronized void stop() {
        if (!isRunning) {
            LOGGER.info("Scheduler is not running");
            return;
        }
        
        LOGGER.info("Stopping recurring transaction scheduler");
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
    }
    
    /**
     * Check if the scheduler is running.
     * 
     * @return true if the scheduler is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Enable or disable notifications.
     * 
     * @param enabled true to enable notifications, false to disable
     */
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }
    
    /**
     * Check if notifications are enabled.
     * 
     * @return true if notifications are enabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    /**
     * Schedule the daily processing of recurring transactions.
     */
    private void scheduleDailyProcessing() {
        // Calculate delay until next run
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.with(dailyProcessingTime);
        
        if (now.isAfter(nextRun)) {
            // If we're already past today's time, schedule for tomorrow
            nextRun = nextRun.plusDays(1);
        }
        
        long initialDelay = Duration.between(now, nextRun).getSeconds();
        
        // Schedule the task
        scheduler.scheduleAtFixedRate(
                this::processDueRecurringTransactions,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1), // 24 hours
                TimeUnit.SECONDS
        );
        
        LOGGER.info("Scheduled recurring transaction processing at " + dailyProcessingTime + 
                   ", next run in " + formatDuration(initialDelay));
    }
    
    /**
     * Process all due recurring transactions.
     */
    private void processDueRecurringTransactions() {
        try {
            LOGGER.info("Processing due recurring transactions");
            
            LocalDate today = LocalDate.now();
            List<RecurringTransaction> dueTransactions = 
                    recurringTransactionService.getDueRecurringTransactions(today);
            
            LOGGER.info("Found " + dueTransactions.size() + " due recurring transactions");
            
            int successCount = 0;
            int failureCount = 0;
            
            for (RecurringTransaction recurringTransaction : dueTransactions) {
                LOGGER.info("Processing recurring transaction: " + recurringTransaction.getRecurringTransactionId());
                
                // Process with retries
                boolean success = processWithRetries(recurringTransaction.getRecurringTransactionId());
                
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
            
            LOGGER.info("Recurring transaction processing complete: " + 
                       successCount + " successful, " + failureCount + " failed");
            
            // Send notifications if enabled
            if (notificationsEnabled) {
                sendFailureNotifications(failureCount);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing due recurring transactions", e);
        }
    }
    
    /**
     * Process a recurring transaction with retries.
     * 
     * @param recurringTransactionId The recurring transaction ID
     * @return true if the transaction was executed successfully, false otherwise
     */
    private boolean processWithRetries(long recurringTransactionId) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                RecurringTransactionResult result = 
                        recurringTransactionService.executeRecurringTransaction(recurringTransactionId);
                
                if (result.isSuccessful()) {
                    return true;
                }
                
                // If it's a temporary error, retry
                if (result.getStatus() == RecurringTransactionResult.Status.SYSTEM_ERROR ||
                    result.getStatus() == RecurringTransactionResult.Status.INSUFFICIENT_FUNDS) {
                    LOGGER.warning("Recurring transaction failed, retrying (" + attempt + "/" + maxRetries + "): " + 
                                 recurringTransactionId + " - " + result.getMessage());
                    
                    // Wait before retrying
                    try {
                        Thread.sleep(5000); // 5 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else {
                    // If it's a permanent error, don't retry
                    LOGGER.warning("Recurring transaction failed with non-retriable error: " + 
                                 recurringTransactionId + " - " + result.getMessage());
                    return false;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error executing recurring transaction: " + recurringTransactionId, e);
                
                // Wait before retrying
                try {
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        // All retries failed
        LOGGER.severe("All retries failed for recurring transaction: " + recurringTransactionId);
        return false;
    }
    
    /**
     * Send notifications for failed transactions.
     * 
     * @param failureCount The number of failed transactions
     */
    private void sendFailureNotifications(int failureCount) {
        // This is a placeholder for a notification system
        if (failureCount > 0) {
            LOGGER.info("Would send notification about " + failureCount + " failed transactions");
            // TODO: Implement actual notification mechanism
        }
    }
    
    /**
     * Run the recurring transaction processing immediately.
     * 
     * @return The number of transactions processed successfully
     */
    public int runNow() {
        try {
            LOGGER.info("Running recurring transaction processing now");
            return recurringTransactionService.processDueRecurringTransactions();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running recurring transaction processing", e);
            return 0;
        }
    }
    
    /**
     * Format a duration in seconds to a human-readable string.
     * 
     * @param seconds The duration in seconds
     * @return A formatted string representation
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        
        if (seconds < 3600) {
            return seconds / 60 + " minutes";
        }
        
        if (seconds < 86400) {
            return seconds / 3600 + " hours";
        }
        
        return seconds / 86400 + " days";
    }
}