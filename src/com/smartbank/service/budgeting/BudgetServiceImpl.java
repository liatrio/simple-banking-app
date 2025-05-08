package com.smartbank.service.budgeting;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.category.CategoryException;
import com.smartbank.service.category.CategoryService;
import com.smartbank.service.reporting.CategoryReportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the BudgetService.
 */
public class BudgetServiceImpl implements BudgetService {
    private static final Logger LOGGER = Logger.getLogger(BudgetServiceImpl.class.getName());
    
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategoryReportService categoryReportService;
    
    /**
     * Constructor that initializes the repositories and services.
     */
    public BudgetServiceImpl() {
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.categoryService = ServiceFactory.getCategoryService();
        this.categoryReportService = ServiceFactory.getCategoryReportService();
    }
    
    @Override
    public boolean setBudgetAmount(long categoryId, double amount) throws BudgetException {
        if (amount < 0) {
            throw new BudgetException("Budget amount cannot be negative");
        }
        
        try {
            TransactionCategory category = categoryService.updateBudget(categoryId, amount);
            return category != null;
        } catch (CategoryException e) {
            throw new BudgetException("Failed to set budget amount: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<Long, BudgetStatus> getBudgetStatus(long accountNumber, String period) {
        Logger logger = Logger.getLogger(BudgetServiceImpl.class.getName());
        
        // Get budget period date range
        DateRange dateRange = getBudgetPeriodRange(period, null);
        logger.info("Getting budget status for account " + accountNumber + 
                   " for period " + period + " (" + dateRange.getStartDate() + " to " + dateRange.getEndDate() + ")");
        
        // Get categories with budget amounts
        List<TransactionCategory> categories = categoryService.getAllCategories().stream()
                .filter(c -> c.getBudgetAmount() > 0)
                .collect(Collectors.toList());
        
        logger.info("Found " + categories.size() + " categories with budget amounts");
        
        if (categories.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Get spending by category for the period
        Map<Long, CategoryReportService.CategorySpending> spending = 
                categoryReportService.getSpendingByCategory(accountNumber, dateRange.getStartDate(), dateRange.getEndDate());
        
        logger.info("Found spending data for " + spending.size() + " categories");
        
        // Calculate budget status for each category
        Map<Long, BudgetStatus> result = new HashMap<>();
        
        for (TransactionCategory category : categories) {
            long categoryId = category.getCategoryId();
            double budgetAmount = category.getBudgetAmount();
            
            // Get actual spending
            double spentAmount = 0;
            if (spending.containsKey(categoryId)) {
                // IMPORTANT: The amount from spending will be negative for expenses
                // We need to take the absolute value
                spentAmount = Math.abs(spending.get(categoryId).getAmount());
                logger.info("For category " + category.getName() + " with budget $" + budgetAmount + 
                           ", found spent amount: $" + spentAmount);
            } else {
                logger.info("No spending found for category " + category.getName() + 
                           " (ID: " + categoryId + ") with budget $" + budgetAmount);
            }
            
            BudgetStatus status = new BudgetStatus(
                    categoryId, category.getName(), budgetAmount, spentAmount);
            
            logger.info("Budget status for " + category.getName() + ": Budget=$" + budgetAmount + 
                       ", Spent=$" + spentAmount + ", Remaining=$" + status.getRemainingAmount() + 
                       ", Percentage=" + status.getPercentageUsed() + "%, OverBudget=" + status.isOverBudget());
            
            result.put(categoryId, status);
        }
        
        return result;
    }
    
    @Override
    public BudgetStatus getCategoryBudgetStatus(long accountNumber, long categoryId, String period) {
        // Get budget status for all categories
        Map<Long, BudgetStatus> allStatus = getBudgetStatus(accountNumber, period);
        
        // Return status for the specified category
        return allStatus.getOrDefault(categoryId, null);
    }
    
    @Override
    public List<BudgetStatus> getOverBudgetCategories(long accountNumber, String period) {
        // Get budget status for all categories
        Map<Long, BudgetStatus> allStatus = getBudgetStatus(accountNumber, period);
        
        // Filter to over-budget categories
        return allStatus.values().stream()
                .filter(BudgetStatus::isOverBudget)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Double> getCategorySpendingTrend(long accountNumber, long categoryId, 
                                                     int periods, String periodType) {
        // Calculate date ranges for each period
        List<DateRange> dateRanges = calculatePeriodDateRanges(periods, periodType);
        
        // Get spending for each period
        Map<String, Double> result = new LinkedHashMap<>();
        
        for (DateRange range : dateRanges) {
            // Get transactions for the account in the category for the date range
            List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber)
                    .stream()
                    .filter(t -> t.getCategory() != null && 
                                t.getCategory().getCategoryId() == categoryId &&
                                isInDateRange(t.getTimestamp(), range.getStartDate(), range.getEndDate()))
                    .collect(Collectors.toList());
            
            // Calculate total spending
            double totalSpending = transactions.stream()
                    .filter(t -> t.getSignedAmount() < 0) // Only count outgoing transactions
                    .mapToDouble(t -> Math.abs(t.getSignedAmount()))
                    .sum();
            
            result.put(range.getLabel(), totalSpending);
        }
        
        return result;
    }
    
    @Override
    public Map<Long, BudgetForecast> getSpendingForecasts(long accountNumber, String period) {
        // Get budget period date range
        DateRange currentPeriod = getBudgetPeriodRange(period, null);
        
        // Get categories with budget amounts
        List<TransactionCategory> categories = categoryService.getAllCategories().stream()
                .filter(c -> c.getBudgetAmount() > 0)
                .collect(Collectors.toList());
        
        if (categories.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Get current budget status
        Map<Long, BudgetStatus> currentStatus = getBudgetStatus(accountNumber, period);
        
        // Calculate forecast for each category
        Map<Long, BudgetForecast> forecasts = new HashMap<>();
        
        for (TransactionCategory category : categories) {
            long categoryId = category.getCategoryId();
            
            if (currentStatus.containsKey(categoryId)) {
                BudgetStatus status = currentStatus.get(categoryId);
                
                // Calculate days passed and remaining
                int daysPassed = currentPeriod.getDaysInPeriod() - currentPeriod.getDaysRemaining();
                int daysRemaining = currentPeriod.getDaysRemaining();
                
                if (daysPassed > 0 && daysRemaining > 0) {
                    // Calculate daily rate based on current spending
                    double dailyRate = status.getSpentAmount() / daysPassed;
                    
                    // Forecast total spending by end of period
                    double forecastAmount = status.getSpentAmount() + (dailyRate * daysRemaining);
                    
                    // Calculate confidence level based on spending regularity
                    double confidenceLevel = calculateConfidenceLevel(accountNumber, categoryId, 
                                                                     currentPeriod.getStartDate());
                    
                    BudgetForecast forecast = new BudgetForecast(
                            categoryId, category.getName(), category.getBudgetAmount(), 
                            forecastAmount, confidenceLevel);
                    
                    forecasts.put(categoryId, forecast);
                }
            }
        }
        
        return forecasts;
    }
    
    @Override
    public byte[] generateBudgetReport(long accountNumber, String period, String format) {
        // Get budget status for all categories
        Map<Long, BudgetStatus> budgetStatus = getBudgetStatus(accountNumber, period);
        
        if (budgetStatus.isEmpty()) {
            return new byte[0];
        }
        
        // Get budget period date range
        DateRange dateRange = getBudgetPeriodRange(period, null);
        
        // Generate report based on format
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            switch (format.toLowerCase()) {
                case "csv":
                    generateCsvBudgetReport(outputStream, budgetStatus, dateRange);
                    break;
                case "html":
                    generateHtmlBudgetReport(outputStream, budgetStatus, dateRange);
                    break;
                case "text":
                    generateTextBudgetReport(outputStream, budgetStatus, dateRange);
                    break;
                default:
                    generateCsvBudgetReport(outputStream, budgetStatus, dateRange);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating budget report: " + e.getMessage(), e);
            return new byte[0];
        }
        
        return outputStream.toByteArray();
    }
    
    @Override
    public double calculateDailyAllowance(long accountNumber, long categoryId, String period) {
        // Get budget status for the category
        BudgetStatus status = getCategoryBudgetStatus(accountNumber, categoryId, period);
        
        if (status == null || status.isOverBudget()) {
            return 0; // No allowance if over budget
        }
        
        // Get budget period date range
        DateRange dateRange = getBudgetPeriodRange(period, null);
        
        // Calculate daily allowance based on remaining budget and days
        int daysRemaining = dateRange.getDaysRemaining();
        
        if (daysRemaining <= 0) {
            return 0;
        }
        
        return status.getRemainingAmount() / daysRemaining;
    }
    
    @Override
    public DateRange getBudgetPeriodRange(String period, Date referenceDate) {
        if (referenceDate == null) {
            referenceDate = new Date();
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        
        // Set to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        
        Date endDate;
        Date startDate;
        String label;
        int daysInPeriod;
        
        SimpleDateFormat labelFormat;
        
        switch (period.toLowerCase()) {
            case "month":
                // Set to the end of the month
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = calendar.getTime();
                
                // Set to the beginning of the month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                labelFormat = new SimpleDateFormat("MMMM yyyy");
                label = labelFormat.format(startDate);
                
                daysInPeriod = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
                
            case "week":
                // Set to the end of the week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() + 6);
                endDate = calendar.getTime();
                
                // Set to the beginning of the week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                SimpleDateFormat weekFormat = new SimpleDateFormat("MMM d");
                label = "Week of " + weekFormat.format(startDate);
                
                daysInPeriod = 7;
                break;
                
            case "day":
            default:
                endDate = calendar.getTime();
                
                // Set to the beginning of the day
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                labelFormat = new SimpleDateFormat("MMM d, yyyy");
                label = labelFormat.format(startDate);
                
                daysInPeriod = 1;
                period = "day";
                break;
        }
        
        // Calculate days remaining
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        
        long diffMillis = end.getTimeInMillis() - today.getTimeInMillis();
        int daysRemaining = (int) (diffMillis / (24 * 60 * 60 * 1000)) + 1;
        
        return new DateRange(startDate, endDate, label, daysInPeriod, daysRemaining);
    }
    
    /**
     * Generate CSV budget report.
     */
    private void generateCsvBudgetReport(ByteArrayOutputStream outputStream, 
                                      Map<Long, BudgetStatus> budgetStatus,
                                      DateRange dateRange) throws IOException {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            // Header
            writer.println("Budget Report for " + dateRange.getLabel());
            writer.println("Generated on " + dateFormat.format(new Date()));
            writer.println();
            
            writer.println("Category ID,Category Name,Budget Amount,Spent Amount," +
                          "Remaining Amount,Percentage Used,Over Budget");
            
            // Sort by percentage used (descending)
            List<BudgetStatus> sortedStatus = new ArrayList<>(budgetStatus.values());
            sortedStatus.sort((s1, s2) -> Double.compare(s2.getPercentageUsed(), s1.getPercentageUsed()));
            
            // Data rows
            for (BudgetStatus status : sortedStatus) {
                writer.println(
                        status.getCategoryId() + "," +
                        escapeForCsv(status.getCategoryName()) + "," +
                        String.format("%.2f", status.getBudgetAmount()) + "," +
                        String.format("%.2f", status.getSpentAmount()) + "," +
                        String.format("%.2f", status.getRemainingAmount()) + "," +
                        String.format("%.2f", status.getPercentageUsed()) + "%," +
                        (status.isOverBudget() ? "Yes" : "No"));
            }
        }
    }
    
    /**
     * Generate HTML budget report.
     */
    private void generateHtmlBudgetReport(ByteArrayOutputStream outputStream,
                                       Map<Long, BudgetStatus> budgetStatus,
                                       DateRange dateRange) throws IOException {
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Budget Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1, h2 { color: #333; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println(".over-budget { color: red; font-weight: bold; }");
            writer.println(".under-budget { color: green; }");
            writer.println(".progress-container { width: 100%; background-color: #e0e0e0; height: 20px; }");
            writer.println(".progress-bar { height: 100%; text-align: center; color: white; }");
            writer.println(".progress-bar-under { background-color: #4CAF50; }");
            writer.println(".progress-bar-near { background-color: #FFC107; }");
            writer.println(".progress-bar-over { background-color: #F44336; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<h1>Budget Report</h1>");
            writer.println("<h2>" + dateRange.getLabel() + "</h2>");
            
            writer.println("<p>Days remaining in period: " + dateRange.getDaysRemaining() + 
                          " of " + dateRange.getDaysInPeriod() + "</p>");
            
            writer.println("<table>");
            writer.println("<tr>");
            writer.println("<th>Category</th>");
            writer.println("<th>Budget</th>");
            writer.println("<th>Spent</th>");
            writer.println("<th>Remaining</th>");
            writer.println("<th>% Used</th>");
            writer.println("<th>Status</th>");
            writer.println("</tr>");
            
            // Sort by percentage used (descending)
            List<BudgetStatus> sortedStatus = new ArrayList<>(budgetStatus.values());
            sortedStatus.sort((s1, s2) -> Double.compare(s2.getPercentageUsed(), s1.getPercentageUsed()));
            
            // Data rows
            for (BudgetStatus status : sortedStatus) {
                String rowClass = status.isOverBudget() ? "over-budget" : "under-budget";
                
                writer.println("<tr>");
                writer.println("<td>" + status.getCategoryName() + "</td>");
                writer.println("<td>$" + String.format("%.2f", status.getBudgetAmount()) + "</td>");
                writer.println("<td>$" + String.format("%.2f", status.getSpentAmount()) + "</td>");
                writer.println("<td class=\"" + rowClass + "\">$" + 
                              String.format("%.2f", status.getRemainingAmount()) + "</td>");
                
                // Progress bar color
                String barClass = "progress-bar-under";
                if (status.getPercentageUsed() >= 100) {
                    barClass = "progress-bar-over";
                } else if (status.getPercentageUsed() >= 85) {
                    barClass = "progress-bar-near";
                }
                
                // Progress bar width, capped at 100%
                int barWidth = (int) Math.min(status.getPercentageUsed(), 100);
                
                writer.println("<td>");
                writer.println("<div class=\"progress-container\">");
                writer.println("<div class=\"progress-bar " + barClass + "\" style=\"width: " + 
                              barWidth + "%;\">" + String.format("%.1f%%", status.getPercentageUsed()) + "</div>");
                writer.println("</div>");
                writer.println("</td>");
                
                writer.println("<td class=\"" + rowClass + "\">" + 
                              (status.isOverBudget() ? "Over Budget" : "Under Budget") + "</td>");
                
                writer.println("</tr>");
            }
            
            writer.println("</table>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    /**
     * Generate text budget report.
     */
    private void generateTextBudgetReport(ByteArrayOutputStream outputStream,
                                       Map<Long, BudgetStatus> budgetStatus,
                                       DateRange dateRange) throws IOException {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.println("Budget Report");
            writer.println("=============");
            writer.println();
            
            writer.println("Period: " + dateRange.getLabel());
            writer.println("Generated on: " + dateFormat.format(new Date()));
            writer.println("Days remaining: " + dateRange.getDaysRemaining() + 
                          " of " + dateRange.getDaysInPeriod());
            writer.println();
            
            // Calculate column widths
            int categoryWidth = 20;
            int amountWidth = 12;
            int percentWidth = 8;
            int statusWidth = 15;
            
            // Print header
            writer.printf("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                         amountWidth + "s %" + amountWidth + "s %" + 
                         percentWidth + "s %" + statusWidth + "s%n",
                         "Category", "Budget", "Spent", "Remaining", "% Used", "Status");
            
            writer.println(String.format("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                                       amountWidth + "s %" + amountWidth + "s %" + 
                                       percentWidth + "s %" + statusWidth + "s",
                                       "--------------------", "------------", 
                                       "------------", "------------", "--------", "---------------"));
            
            // Sort by percentage used (descending)
            List<BudgetStatus> sortedStatus = new ArrayList<>(budgetStatus.values());
            sortedStatus.sort((s1, s2) -> Double.compare(s2.getPercentageUsed(), s1.getPercentageUsed()));
            
            // Print data rows
            for (BudgetStatus status : sortedStatus) {
                writer.printf("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                             amountWidth + "s %" + amountWidth + "s %" + 
                             percentWidth + "s %" + statusWidth + "s%n",
                             truncate(status.getCategoryName(), categoryWidth),
                             "$" + String.format("%.2f", status.getBudgetAmount()),
                             "$" + String.format("%.2f", status.getSpentAmount()),
                             "$" + String.format("%.2f", status.getRemainingAmount()),
                             String.format("%.1f%%", status.getPercentageUsed()),
                             status.isOverBudget() ? "OVER BUDGET" : "Under Budget");
            }
            
            writer.println();
            writer.println("Note: Categories are sorted by percentage of budget used.");
        }
    }
    
    /**
     * Calculate confidence level for a forecast based on spending regularity.
     */
    private double calculateConfidenceLevel(long accountNumber, long categoryId, Date startDate) {
        // Get transactions for this category in the past few months
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, -3);
        Date threeMonthsAgo = calendar.getTime();
        
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber)
                .stream()
                .filter(t -> t.getCategory() != null && 
                            t.getCategory().getCategoryId() == categoryId &&
                            t.getTimestamp().after(threeMonthsAgo))
                .collect(Collectors.toList());
        
        if (transactions.isEmpty()) {
            return 50.0; // Neutral confidence if no history
        }
        
        // Group transactions by week to check regularity
        Map<Integer, Double> weeklyTotals = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            Calendar transDate = Calendar.getInstance();
            transDate.setTime(transaction.getTimestamp());
            
            // Get week of year
            int weekOfYear = transDate.get(Calendar.WEEK_OF_YEAR);
            
            // Add transaction amount
            double amount = Math.abs(transaction.getSignedAmount());
            weeklyTotals.put(weekOfYear, weeklyTotals.getOrDefault(weekOfYear, 0.0) + amount);
        }
        
        // Calculate standard deviation of weekly amounts
        double mean = weeklyTotals.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = weeklyTotals.values().stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // Calculate coefficient of variation (normalized standard deviation)
        double cv = mean > 0 ? stdDev / mean : 1.0;
        
        // Convert to confidence level (inverse relationship to cv)
        // Lower cv means higher regularity and higher confidence
        double confidence = 90.0 - (cv * 40.0);
        
        // Clamp to [20, 90] range
        return Math.max(20.0, Math.min(90.0, confidence));
    }
    
    /**
     * Check if a date is within a date range.
     */
    private boolean isInDateRange(Date date, Date startDate, Date endDate) {
        return (date.equals(startDate) || date.after(startDate)) &&
               (date.equals(endDate) || date.before(endDate));
    }
    
    /**
     * Calculate date ranges for periods.
     */
    private List<DateRange> calculatePeriodDateRanges(int periods, String periodType) {
        List<DateRange> ranges = new ArrayList<>();
        
        for (int i = 0; i < periods; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -i);
            
            Date referenceDate = calendar.getTime();
            
            DateRange range = getBudgetPeriodRange(periodType, referenceDate);
            ranges.add(range);
        }
        
        Collections.reverse(ranges);
        return ranges;
    }
    
    /**
     * Escape a string for CSV format.
     */
    private String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains quotes, commas, or newlines, escape it
        if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
            // Double up quotes
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return value;
    }
    
    /**
     * Truncate a string to a maximum length.
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}