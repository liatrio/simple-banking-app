package com.smartbank.service.interest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler for running interest calculation operations.
 */
public class InterestCalculationScheduler {
    private static final Logger LOGGER = Logger.getLogger(InterestCalculationScheduler.class.getName());
    
    // Singleton instance
    private static InterestCalculationScheduler instance;
    
    // Scheduler components
    private final ScheduledExecutorService scheduler;
    private final InterestCalculationService interestCalculationService;
    
    // Configuration
    private final LocalTime dailyProcessingTime;
    private final LocalTime monthlyProcessingTime;
    
    // Current state
    private boolean isRunning = false;
    
    /**
     * Get the singleton instance of the scheduler.
     * 
     * @return The InterestCalculationScheduler instance
     */
    public static synchronized InterestCalculationScheduler getInstance() {
        if (instance == null) {
            instance = new InterestCalculationScheduler();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private InterestCalculationScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.interestCalculationService = InterestCalculationServiceImpl.getInstance();
        
        // Set default processing times
        this.dailyProcessingTime = LocalTime.of(23, 0); // 11:00 PM
        this.monthlyProcessingTime = LocalTime.of(23, 30); // 11:30 PM
    }
    
    /**
     * Start the scheduler.
     */
    public synchronized void start() {
        if (isRunning) {
            LOGGER.info("Scheduler is already running");
            return;
        }
        
        LOGGER.info("Starting interest calculation scheduler");
        
        // Schedule daily processing
        scheduleDailyProcessing();
        
        // Schedule monthly processing
        scheduleMonthlyProcessing();
        
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
        
        LOGGER.info("Stopping interest calculation scheduler");
        
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
     * @return true if the scheduler is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Schedule the daily interest accrual processing.
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
                this::runDailyProcessing,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1), // 24 hours
                TimeUnit.SECONDS
        );
        
        LOGGER.info("Scheduled daily interest processing at " + dailyProcessingTime + 
                   ", next run in " + formatDuration(initialDelay));
    }
    
    /**
     * Schedule the monthly interest posting processing.
     */
    private void scheduleMonthlyProcessing() {
        // Calculate delay until next run
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.with(monthlyProcessingTime);
        
        if (now.isAfter(nextRun)) {
            // If we're already past today's time, schedule for tomorrow
            nextRun = nextRun.plusDays(1);
        }
        
        long initialDelay = Duration.between(now, nextRun).getSeconds();
        
        // Schedule the task
        scheduler.scheduleAtFixedRate(
                this::runMonthlyProcessing,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1), // Check daily
                TimeUnit.SECONDS
        );
        
        LOGGER.info("Scheduled monthly interest processing at " + monthlyProcessingTime + 
                   ", next run in " + formatDuration(initialDelay));
    }
    
    /**
     * Run the daily interest accrual processing.
     */
    private void runDailyProcessing() {
        try {
            LOGGER.info("Running daily interest accrual processing");
            int accountsProcessed = interestCalculationService.runEndOfDayProcessing();
            LOGGER.info("Completed daily interest accrual for " + accountsProcessed + " accounts");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during daily interest processing", e);
        }
    }
    
    /**
     * Run the monthly interest posting processing.
     */
    private void runMonthlyProcessing() {
        try {
            LOGGER.info("Running monthly interest posting check");
            int accountsProcessed = interestCalculationService.runEndOfMonthProcessing();
            
            if (accountsProcessed > 0) {
                LOGGER.info("Completed monthly interest posting for " + accountsProcessed + " accounts");
            } else {
                LOGGER.info("No accounts processed for monthly interest posting (not end of month)");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during monthly interest processing", e);
        }
    }
    
    /**
     * Force run the daily interest accrual processing immediately.
     * 
     * @return The number of accounts processed
     */
    public int forceRunDailyProcessing() {
        try {
            LOGGER.info("Force running daily interest accrual processing");
            int accountsProcessed = interestCalculationService.runEndOfDayProcessing();
            LOGGER.info("Completed daily interest accrual for " + accountsProcessed + " accounts");
            return accountsProcessed;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during forced daily interest processing", e);
            return 0;
        }
    }
    
    /**
     * Force run the monthly interest posting processing immediately.
     * 
     * @param ignoreDate If true, force posting regardless of the date
     * @return The number of accounts processed
     */
    public int forceRunMonthlyProcessing(boolean ignoreDate) {
        try {
            LOGGER.info("Force running monthly interest posting processing");
            
            int accountsProcessed = 0;
            if (ignoreDate) {
                // Force posting for all accounts with accrued interest
                accountsProcessed = interestCalculationService.postInterestForAllAccounts().size();
            } else {
                // Only post if it's the end of the month
                accountsProcessed = interestCalculationService.runEndOfMonthProcessing();
            }
            
            LOGGER.info("Completed monthly interest posting for " + accountsProcessed + " accounts");
            return accountsProcessed;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during forced monthly interest processing", e);
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