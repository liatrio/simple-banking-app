package com.smartbank.model;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a recurring transaction that is automatically executed
 * on a predefined schedule.
 */
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    
    /**
     * Type of frequency for recurring transactions.
     */
    public enum Frequency {
        DAILY,          // Every day
        WEEKLY,         // Once a week
        BIWEEKLY,       // Every two weeks
        MONTHLY,        // Once a month
        QUARTERLY,      // Every three months
        SEMIANNUALLY,   // Every six months
        ANNUALLY        // Once a year
    }
    
    /**
     * Status of the recurring transaction.
     */
    public enum Status {
        ACTIVE,         // Currently active and will be executed on schedule
        PAUSED,         // Temporarily paused
        COMPLETED,      // All occurrences have been executed
        CANCELLED       // Cancelled by user or system
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recurringTransactionId;
    
    @Column(nullable = false)
    private long sourceAccountNumber;
    
    // For transfers, this is the target account number. For deposits/withdrawals, this is 0.
    @Column
    private long targetAccountNumber;
    
    @Column(nullable = false)
    private double amount;
    
    // Use same transaction types as regular transactions
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.Type type;
    
    @Column(nullable = false)
    private String description;
    
    // Frequency settings
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;
    
    // For WEEKLY frequency, specify day of week (1=Monday, 7=Sunday)
    @Column
    private int dayOfWeek;
    
    // For MONTHLY frequency, specify day of month (1-31)
    @Column
    private int dayOfMonth;
    
    // Date of first execution
    @Column(nullable = false)
    private LocalDate startDate;
    
    // Optional end date (null for indefinite)
    @Column
    private LocalDate endDate;
    
    // Optional limit on number of occurrences (null for indefinite)
    @Column
    private Integer occurrenceLimit;
    
    // Current number of successful executions
    @Column(nullable = false)
    private int executionCount;
    
    // Date of last successful execution
    @Column
    private LocalDate lastExecutionDate;
    
    // Date of next scheduled execution
    @Column
    private LocalDate nextExecutionDate;
    
    // Current status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    // Creation and modification timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime modifiedAt;
    
    // Default constructor required by JPA
    protected RecurringTransaction() {
    }
    
    /**
     * Create a new RecurringTransaction.
     */
    public RecurringTransaction(long sourceAccountNumber, long targetAccountNumber, 
                              double amount, Transaction.Type type, String description,
                              Frequency frequency, LocalDate startDate) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.executionCount = 0;
        this.nextExecutionDate = calculateNextExecutionDate(startDate, frequency, 0, dayOfWeek, dayOfMonth);
        this.status = Status.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * Create a new RecurringTransaction with full parameters.
     */
    public RecurringTransaction(long sourceAccountNumber, long targetAccountNumber, 
                              double amount, Transaction.Type type, String description,
                              Frequency frequency, int dayOfWeek, int dayOfMonth,
                              LocalDate startDate, LocalDate endDate, Integer occurrenceLimit) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequency = frequency;
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
        this.occurrenceLimit = occurrenceLimit;
        this.executionCount = 0;
        this.nextExecutionDate = calculateNextExecutionDate(startDate, frequency, 0, dayOfWeek, dayOfMonth);
        this.status = Status.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate the next execution date based on frequency and other parameters.
     */
    public static LocalDate calculateNextExecutionDate(LocalDate baseDate, Frequency frequency, 
                                                    int executionCount, int dayOfWeek, int dayOfMonth) {
        LocalDate nextDate;
        
        switch (frequency) {
            case DAILY:
                nextDate = baseDate.plusDays(executionCount > 0 ? 1 : 0);
                break;
                
            case WEEKLY:
                if (executionCount == 0) {
                    // First execution - if dayOfWeek is specified, find the next occurrence of that day
                    if (dayOfWeek > 0) {
                        DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
                        nextDate = baseDate;
                        while (nextDate.getDayOfWeek() != targetDay) {
                            nextDate = nextDate.plusDays(1);
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 7 days
                    nextDate = baseDate.plusWeeks(1);
                }
                break;
                
            case BIWEEKLY:
                if (executionCount == 0) {
                    // First execution - if dayOfWeek is specified, find the next occurrence of that day
                    if (dayOfWeek > 0) {
                        DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
                        nextDate = baseDate;
                        while (nextDate.getDayOfWeek() != targetDay) {
                            nextDate = nextDate.plusDays(1);
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 14 days
                    nextDate = baseDate.plusWeeks(2);
                }
                break;
                
            case MONTHLY:
                if (executionCount == 0) {
                    // First execution - if dayOfMonth is specified, find the correct day
                    if (dayOfMonth > 0) {
                        // Adjust to the specified day of month
                        int targetDay = Math.min(dayOfMonth, baseDate.getMonth().maxLength());
                        if (baseDate.getDayOfMonth() <= targetDay) {
                            // Target day is in the current month
                            nextDate = baseDate.withDayOfMonth(targetDay);
                        } else {
                            // Target day is in the next month
                            nextDate = baseDate.plusMonths(1).withDayOfMonth(
                                    Math.min(dayOfMonth, baseDate.plusMonths(1).getMonth().maxLength()));
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 1 month and adjust day if needed
                    if (dayOfMonth > 0) {
                        // Use specified day of month
                        LocalDate nextMonth = baseDate.plusMonths(1);
                        nextDate = nextMonth.withDayOfMonth(
                                Math.min(dayOfMonth, nextMonth.getMonth().maxLength()));
                    } else {
                        // Keep same day of month
                        nextDate = baseDate.plusMonths(1);
                    }
                }
                break;
                
            case QUARTERLY:
                if (executionCount == 0) {
                    // First execution - if dayOfMonth is specified, find the correct day
                    if (dayOfMonth > 0) {
                        // Adjust to the specified day of month
                        int targetDay = Math.min(dayOfMonth, baseDate.getMonth().maxLength());
                        if (baseDate.getDayOfMonth() <= targetDay) {
                            // Target day is in the current month
                            nextDate = baseDate.withDayOfMonth(targetDay);
                        } else {
                            // Target day is in the next month
                            nextDate = baseDate.plusMonths(1).withDayOfMonth(
                                    Math.min(dayOfMonth, baseDate.plusMonths(1).getMonth().maxLength()));
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 3 months and adjust day if needed
                    if (dayOfMonth > 0) {
                        // Use specified day of month
                        LocalDate nextQuarter = baseDate.plusMonths(3);
                        nextDate = nextQuarter.withDayOfMonth(
                                Math.min(dayOfMonth, nextQuarter.getMonth().maxLength()));
                    } else {
                        // Keep same day of month
                        nextDate = baseDate.plusMonths(3);
                    }
                }
                break;
                
            case SEMIANNUALLY:
                if (executionCount == 0) {
                    // First execution - if dayOfMonth is specified, find the correct day
                    if (dayOfMonth > 0) {
                        // Adjust to the specified day of month
                        int targetDay = Math.min(dayOfMonth, baseDate.getMonth().maxLength());
                        if (baseDate.getDayOfMonth() <= targetDay) {
                            // Target day is in the current month
                            nextDate = baseDate.withDayOfMonth(targetDay);
                        } else {
                            // Target day is in the next month
                            nextDate = baseDate.plusMonths(1).withDayOfMonth(
                                    Math.min(dayOfMonth, baseDate.plusMonths(1).getMonth().maxLength()));
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 6 months and adjust day if needed
                    if (dayOfMonth > 0) {
                        // Use specified day of month
                        LocalDate nextHalfYear = baseDate.plusMonths(6);
                        nextDate = nextHalfYear.withDayOfMonth(
                                Math.min(dayOfMonth, nextHalfYear.getMonth().maxLength()));
                    } else {
                        // Keep same day of month
                        nextDate = baseDate.plusMonths(6);
                    }
                }
                break;
                
            case ANNUALLY:
                if (executionCount == 0) {
                    // First execution - if dayOfMonth is specified, find the correct day
                    if (dayOfMonth > 0) {
                        // Adjust to the specified day of month
                        int targetDay = Math.min(dayOfMonth, baseDate.getMonth().maxLength());
                        if (baseDate.getDayOfMonth() <= targetDay) {
                            // Target day is in the current month
                            nextDate = baseDate.withDayOfMonth(targetDay);
                        } else {
                            // Target day is in the next month
                            nextDate = baseDate.plusMonths(1).withDayOfMonth(
                                    Math.min(dayOfMonth, baseDate.plusMonths(1).getMonth().maxLength()));
                        }
                    } else {
                        nextDate = baseDate;
                    }
                } else {
                    // Subsequent executions - add 1 year and adjust day if needed
                    if (dayOfMonth > 0) {
                        // Use specified day of month
                        LocalDate nextYear = baseDate.plusYears(1);
                        nextDate = nextYear.withDayOfMonth(
                                Math.min(dayOfMonth, nextYear.getMonth().maxLength()));
                    } else {
                        // Keep same day of month
                        nextDate = baseDate.plusYears(1);
                    }
                }
                break;
                
            default:
                // Default to daily
                nextDate = baseDate.plusDays(executionCount > 0 ? 1 : 0);
        }
        
        return nextDate;
    }
    
    /**
     * Calculate the next execution date for this recurring transaction.
     */
    public LocalDate calculateNextExecutionDate() {
        if (lastExecutionDate == null) {
            // First execution
            return calculateNextExecutionDate(startDate, frequency, 0, dayOfWeek, dayOfMonth);
        } else {
            // Subsequent execution
            return calculateNextExecutionDate(lastExecutionDate, frequency, executionCount, dayOfWeek, dayOfMonth);
        }
    }
    
    /**
     * Update the next execution date.
     */
    public void updateNextExecutionDate() {
        this.nextExecutionDate = calculateNextExecutionDate();
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * Record a successful execution.
     */
    public void recordExecution() {
        this.executionCount++;
        this.lastExecutionDate = LocalDate.now();
        updateNextExecutionDate();
        
        // Check if we've reached the limit or end date
        if ((occurrenceLimit != null && executionCount >= occurrenceLimit) ||
            (endDate != null && nextExecutionDate.isAfter(endDate))) {
            this.status = Status.COMPLETED;
        }
    }
    
    /**
     * Check if this recurring transaction is active.
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
    
    /**
     * Check if this recurring transaction is due for execution.
     */
    public boolean isDue() {
        LocalDate today = LocalDate.now();
        return isActive() && nextExecutionDate != null && 
               (nextExecutionDate.equals(today) || nextExecutionDate.isBefore(today));
    }
    
    // Getters and Setters
    
    public long getRecurringTransactionId() {
        return recurringTransactionId;
    }
    
    public long getSourceAccountNumber() {
        return sourceAccountNumber;
    }
    
    public long getTargetAccountNumber() {
        return targetAccountNumber;
    }
    
    public void setTargetAccountNumber(long targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public Transaction.Type getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public Frequency getFrequency() {
        return frequency;
    }
    
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        updateNextExecutionDate();
    }
    
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        updateNextExecutionDate();
    }
    
    public int getDayOfMonth() {
        return dayOfMonth;
    }
    
    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        updateNextExecutionDate();
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        updateNextExecutionDate();
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public Integer getOccurrenceLimit() {
        return occurrenceLimit;
    }
    
    public void setOccurrenceLimit(Integer occurrenceLimit) {
        this.occurrenceLimit = occurrenceLimit;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public int getExecutionCount() {
        return executionCount;
    }
    
    public LocalDate getLastExecutionDate() {
        return lastExecutionDate;
    }
    
    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.modifiedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
    
    /**
     * Pause this recurring transaction.
     */
    public void pause() {
        this.status = Status.PAUSED;
        this.modifiedAt = LocalDateTime.now();
    }
    
    /**
     * Resume this recurring transaction.
     */
    public void resume() {
        if (status == Status.PAUSED) {
            this.status = Status.ACTIVE;
            updateNextExecutionDate();
        }
    }
    
    /**
     * Cancel this recurring transaction.
     */
    public void cancel() {
        this.status = Status.CANCELLED;
        this.modifiedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecurringTransaction that = (RecurringTransaction) o;
        return recurringTransactionId == that.recurringTransactionId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recurringTransactionId);
    }
    
    @Override
    public String toString() {
        return "RecurringTransaction{" +
                "id=" + recurringTransactionId +
                ", source=" + sourceAccountNumber +
                ", target=" + targetAccountNumber +
                ", amount=" + amount +
                ", type=" + type +
                ", frequency=" + frequency +
                ", nextExecution=" + nextExecutionDate +
                ", status=" + status +
                '}';
    }
}