package com.smartbank.service.reporting;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.category.CategorizationRuleService;
import com.smartbank.service.category.CategoryService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the CategoryReportService.
 */
public class CategoryReportServiceImpl implements CategoryReportService {
    private static final Logger LOGGER = Logger.getLogger(CategoryReportServiceImpl.class.getName());
    
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategorizationRuleService categorizationRuleService;
    
    /**
     * Constructor that initializes the repositories and services.
     */
    public CategoryReportServiceImpl() {
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.categoryService = ServiceFactory.getCategoryService();
        this.categorizationRuleService = ServiceFactory.getCategorizationRuleService();
    }
    
    @Override
    public Map<Long, CategorySpending> getSpendingByCategory(long accountNumber, Date startDate, Date endDate) {
        // Get transactions for the account within the date range
        List<Transaction> transactions = getTransactionsInDateRange(accountNumber, startDate, endDate);
        
        // Filter for spending (negative amounts)
        List<Transaction> spendingTransactions = transactions.stream()
                .filter(t -> t.getSignedAmount() < 0 && t.getCategory() != null)
                .collect(Collectors.toList());
        
        // Log for debugging
        Logger.getLogger(CategoryReportServiceImpl.class.getName()).info(
                "Found " + spendingTransactions.size() + " spending transactions for account " + 
                accountNumber + " from " + startDate + " to " + endDate);
        
        return calculateCategorySpending(spendingTransactions, false);
    }
    
    @Override
    public Map<Long, CategorySpending> getIncomeByCategory(long accountNumber, Date startDate, Date endDate) {
        // Get transactions for the account within the date range
        List<Transaction> transactions = getTransactionsInDateRange(accountNumber, startDate, endDate);
        
        // Filter for income (positive amounts)
        List<Transaction> incomeTransactions = transactions.stream()
                .filter(t -> t.getSignedAmount() > 0 && t.getCategory() != null)
                .collect(Collectors.toList());
        
        return calculateCategorySpending(incomeTransactions, true);
    }
    
    @Override
    public List<Transaction> getTransactionsByCategory(long accountNumber, long categoryId, 
                                                    Date startDate, Date endDate) {
        // Get transactions for the account within the date range
        List<Transaction> transactions = getTransactionsInDateRange(accountNumber, startDate, endDate);
        
        // Filter by category
        return transactions.stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> getUncategorizedTransactions(long accountNumber, Date startDate, Date endDate) {
        // Get transactions for the account within the date range
        List<Transaction> transactions = getTransactionsInDateRange(accountNumber, startDate, endDate);
        
        // Filter for uncategorized transactions
        return transactions.stream()
                .filter(t -> t.getCategory() == null)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Map<Long, CategorySpending>> getSpendingTrendsByCategory(
            long accountNumber, int periods, String periodType) {
        // Calculate date ranges for each period
        List<DateRange> dateRanges = calculatePeriodDateRanges(periods, periodType);
        
        // Get spending for each period
        Map<String, Map<Long, CategorySpending>> result = new LinkedHashMap<>();
        
        for (DateRange range : dateRanges) {
            Map<Long, CategorySpending> periodSpending = 
                    getSpendingByCategory(accountNumber, range.startDate, range.endDate);
            
            result.put(range.label, periodSpending);
        }
        
        return result;
    }
    
    @Override
    public Map<Long, BudgetVsActual> getBudgetVsActual(long accountNumber, Date startDate, Date endDate) {
        // Get categories with budget amounts
        List<TransactionCategory> categories = categoryService.getAllCategories().stream()
                .filter(c -> c.getBudgetAmount() > 0)
                .collect(Collectors.toList());
        
        if (categories.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Get spending by category
        Map<Long, CategorySpending> actualSpending = getSpendingByCategory(accountNumber, startDate, endDate);
        
        // Calculate budget vs. actual
        Map<Long, BudgetVsActual> result = new HashMap<>();
        
        for (TransactionCategory category : categories) {
            long categoryId = category.getCategoryId();
            double budgetAmount = category.getBudgetAmount();
            
            // Get actual spending
            double actualAmount = 0;
            if (actualSpending.containsKey(categoryId)) {
                actualAmount = Math.abs(actualSpending.get(categoryId).getAmount());
            }
            
            BudgetVsActual budgetVsActual = new BudgetVsActual(
                    categoryId, category.getName(), budgetAmount, actualAmount);
            
            result.put(categoryId, budgetVsActual);
        }
        
        return result;
    }
    
    @Override
    public Map<Long, List<CategoryRecommendation>> getRecommendedCategories(long accountNumber) {
        // Get uncategorized transactions
        List<Transaction> uncategorizedTransactions = transactionRepository.findByAccountNumber(accountNumber)
                .stream()
                .filter(t -> t.getCategory() == null)
                .collect(Collectors.toList());
        
        if (uncategorizedTransactions.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, List<CategoryRecommendation>> result = new HashMap<>();
        
        for (Transaction transaction : uncategorizedTransactions) {
            // Get potential categories
            List<CategoryRecommendation> recommendations = findCategoryRecommendations(transaction);
            if (!recommendations.isEmpty()) {
                result.put(transaction.getTransactionId(), recommendations);
            }
        }
        
        return result;
    }
    
    @Override
    public byte[] generateSpendingReport(long accountNumber, Date startDate, Date endDate, String format) {
        // Get spending by category
        Map<Long, CategorySpending> spendingByCategory = getSpendingByCategory(accountNumber, startDate, endDate);
        
        if (spendingByCategory.isEmpty()) {
            return new byte[0];
        }
        
        // Generate report based on format
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            switch (format.toLowerCase()) {
                case "csv":
                    generateCsvReport(outputStream, spendingByCategory, startDate, endDate);
                    break;
                case "html":
                    generateHtmlReport(outputStream, spendingByCategory, startDate, endDate);
                    break;
                case "text":
                    generateTextReport(outputStream, spendingByCategory, startDate, endDate);
                    break;
                default:
                    generateCsvReport(outputStream, spendingByCategory, startDate, endDate);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating report: " + e.getMessage(), e);
            return new byte[0];
        }
        
        return outputStream.toByteArray();
    }
    
    @Override
    public Map<Long, List<Transaction>> getRecurringTransactionsByCategory(long accountNumber) {
        // Get all transactions for the account
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        
        // Filter for recurring transactions
        List<Transaction> recurringTransactions = transactions.stream()
                .filter(Transaction::isRecurring)
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.toList());
        
        if (recurringTransactions.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Group by category
        Map<Long, List<Transaction>> result = new HashMap<>();
        
        for (Transaction transaction : recurringTransactions) {
            long categoryId = transaction.getCategory().getCategoryId();
            if (!result.containsKey(categoryId)) {
                result.put(categoryId, new ArrayList<>());
            }
            result.get(categoryId).add(transaction);
        }
        
        return result;
    }
    
    /**
     * Helper method to get transactions in a date range.
     */
    private List<Transaction> getTransactionsInDateRange(long accountNumber, Date startDate, Date endDate) {
        List<Transaction> allTransactions = transactionRepository.findByAccountNumber(accountNumber);
        
        Logger.getLogger(CategoryReportServiceImpl.class.getName()).info(
                "Found " + allTransactions.size() + " total transactions for account " + accountNumber);
        
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> (t.getTimestamp().equals(startDate) || t.getTimestamp().after(startDate)) &&
                            (t.getTimestamp().equals(endDate) || t.getTimestamp().before(endDate)))
                .collect(Collectors.toList());
        
        Logger.getLogger(CategoryReportServiceImpl.class.getName()).info(
                "After date filtering, found " + filteredTransactions.size() + " transactions between " + 
                startDate + " and " + endDate);
                
        return filteredTransactions;
    }
    
    /**
     * Calculate spending by category.
     * 
     * @param transactions The transactions to analyze
     * @param isIncome Whether these are income transactions
     * @return Map of category IDs to spending information
     */
    private Map<Long, CategorySpending> calculateCategorySpending(List<Transaction> transactions, boolean isIncome) {
        Logger logger = Logger.getLogger(CategoryReportServiceImpl.class.getName());
        
        if (transactions.isEmpty()) {
            logger.info("No transactions to analyze for spending by category");
            return Collections.emptyMap();
        }
        
        // Group transactions by category
        Map<Long, List<Transaction>> transactionsByCategory = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            long categoryId = transaction.getCategory().getCategoryId();
            if (!transactionsByCategory.containsKey(categoryId)) {
                transactionsByCategory.put(categoryId, new ArrayList<>());
            }
            transactionsByCategory.get(categoryId).add(transaction);
        }
        
        logger.info("Transactions grouped into " + transactionsByCategory.size() + " categories");
        
        // Calculate total amount
        double totalAmount = transactions.stream()
                .mapToDouble(t -> Math.abs(t.getSignedAmount()))
                .sum();
                
        logger.info("Total spending amount: " + totalAmount);
        
        // Calculate spending for each category
        Map<Long, CategorySpending> result = new HashMap<>();
        
        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            
            // Get category name
            String categoryName = "Unknown";
            Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                categoryName = categoryOpt.get().getName();
            }
            
            // Calculate amount and percentage
            double amount = categoryTransactions.stream()
                    .mapToDouble(t -> Math.abs(t.getSignedAmount()))
                    .sum();
            
            double percentage = (totalAmount > 0) ? (amount / totalAmount) * 100 : 0;
            
            // Create spending object - IMPORTANT: For spending (not income), we store it as a negative amount
            // to be consistent with getSignedAmount
            CategorySpending spending = new CategorySpending(
                    categoryId, categoryName, isIncome ? amount : -amount, 
                    categoryTransactions.size(), percentage);
            
            logger.info("Category: " + categoryName + ", Transactions: " + categoryTransactions.size() + 
                       ", Amount: " + (isIncome ? amount : -amount) + ", Percentage: " + percentage + "%");
            
            result.put(categoryId, spending);
        }
        
        return result;
    }
    
    /**
     * Calculate date ranges for periods.
     * 
     * @param periods Number of periods
     * @param periodType Type of period (e.g., "month", "week", "day")
     * @return List of date ranges
     */
    private List<DateRange> calculatePeriodDateRanges(int periods, String periodType) {
        List<DateRange> ranges = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        Date currentEnd = calendar.getTime();
        
        SimpleDateFormat labelFormat;
        
        switch (periodType.toLowerCase()) {
            case "month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                labelFormat = new SimpleDateFormat("MMM yyyy");
                break;
            case "week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                labelFormat = new SimpleDateFormat("MMM d");
                break;
            case "day":
            default:
                labelFormat = new SimpleDateFormat("MMM d");
                periodType = "day";
                break;
        }
        
        for (int i = 0; i < periods; i++) {
            Date periodEnd = (i == 0) ? currentEnd : calendar.getTime();
            
            // Calculate period start
            int field;
            switch (periodType.toLowerCase()) {
                case "month":
                    field = Calendar.MONTH;
                    break;
                case "week":
                    field = Calendar.WEEK_OF_YEAR;
                    break;
                case "day":
                default:
                    field = Calendar.DAY_OF_YEAR;
                    break;
            }
            
            calendar.add(field, -1);
            Date periodStart = calendar.getTime();
            
            // Create label
            String label = labelFormat.format(periodStart);
            
            ranges.add(new DateRange(periodStart, periodEnd, label));
        }
        
        Collections.reverse(ranges);
        return ranges;
    }
    
    /**
     * Find category recommendations for a transaction.
     * 
     * @param transaction The transaction to analyze
     * @return List of category recommendations
     */
    private List<CategoryRecommendation> findCategoryRecommendations(Transaction transaction) {
        if (transaction == null || transaction.getDescription() == null) {
            return Collections.emptyList();
        }
        
        // Get all categories
        List<TransactionCategory> allCategories = categoryService.getAllCategories();
        
        if (allCategories.isEmpty()) {
            return Collections.emptyList();
        }
        
        String description = transaction.getDescription().toLowerCase();
        List<CategoryRecommendation> recommendations = new ArrayList<>();
        
        // Check for keyword matches
        Map<Long, List<String>> allKeywords = categorizationRuleService.getAllKeywords();
        
        for (TransactionCategory category : allCategories) {
            long categoryId = category.getCategoryId();
            
            // Skip if no keywords
            if (!allKeywords.containsKey(categoryId)) {
                continue;
            }
            
            List<String> keywords = allKeywords.get(categoryId);
            
            // Count matching keywords
            int matchCount = 0;
            for (String keyword : keywords) {
                if (description.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            
            // Calculate confidence based on match count
            if (matchCount > 0) {
                double confidence = Math.min((matchCount * 25.0), 100.0);
                recommendations.add(new CategoryRecommendation(
                        categoryId, category.getName(), confidence));
            }
        }
        
        // Sort by confidence (descending)
        recommendations.sort((r1, r2) -> Double.compare(r2.getConfidence(), r1.getConfidence()));
        
        // Limit to top 3
        return recommendations.stream().limit(3).collect(Collectors.toList());
    }
    
    /**
     * Generate a CSV report.
     */
    private void generateCsvReport(ByteArrayOutputStream outputStream, 
                                 Map<Long, CategorySpending> spendingByCategory,
                                 Date startDate, Date endDate) throws IOException {
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            // Header
            writer.println("Category ID,Category Name,Amount,Transaction Count,Percentage");
            
            // Sort by amount (descending)
            List<CategorySpending> sortedSpending = new ArrayList<>(spendingByCategory.values());
            sortedSpending.sort((s1, s2) -> Double.compare(Math.abs(s2.getAmount()), Math.abs(s1.getAmount())));
            
            // Data rows
            for (CategorySpending spending : sortedSpending) {
                writer.println(
                        spending.getCategoryId() + "," +
                        escapeForCsv(spending.getCategoryName()) + "," +
                        spending.getAmount() + "," +
                        spending.getTransactionCount() + "," +
                        String.format("%.2f", spending.getPercentage()) + "%");
            }
        }
    }
    
    /**
     * Generate an HTML report.
     */
    private void generateHtmlReport(ByteArrayOutputStream outputStream,
                                  Map<Long, CategorySpending> spendingByCategory,
                                  Date startDate, Date endDate) throws IOException {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Category Spending Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1 { color: #333; }");
            writer.println("table { border-collapse: collapse; width: 100%; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println(".negative { color: red; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<h1>Category Spending Report</h1>");
            writer.println("<p>Date Range: " + dateFormat.format(startDate) + 
                          " to " + dateFormat.format(endDate) + "</p>");
            
            writer.println("<table>");
            writer.println("<tr>");
            writer.println("<th>Category</th>");
            writer.println("<th>Amount</th>");
            writer.println("<th>Transaction Count</th>");
            writer.println("<th>Percentage</th>");
            writer.println("</tr>");
            
            // Sort by amount (descending)
            List<CategorySpending> sortedSpending = new ArrayList<>(spendingByCategory.values());
            sortedSpending.sort((s1, s2) -> Double.compare(Math.abs(s2.getAmount()), Math.abs(s1.getAmount())));
            
            // Data rows
            for (CategorySpending spending : sortedSpending) {
                String amountClass = spending.getAmount() < 0 ? "negative" : "";
                
                writer.println("<tr>");
                writer.println("<td>" + spending.getCategoryName() + "</td>");
                writer.println("<td class=\"" + amountClass + "\">" + 
                              String.format("$%.2f", spending.getAmount()) + "</td>");
                writer.println("<td>" + spending.getTransactionCount() + "</td>");
                writer.println("<td>" + String.format("%.2f%%", spending.getPercentage()) + "</td>");
                writer.println("</tr>");
            }
            
            writer.println("</table>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    /**
     * Generate a text report.
     */
    private void generateTextReport(ByteArrayOutputStream outputStream,
                                  Map<Long, CategorySpending> spendingByCategory,
                                  Date startDate, Date endDate) throws IOException {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.println("Category Spending Report");
            writer.println("======================");
            writer.println();
            
            writer.println("Date Range: " + dateFormat.format(startDate) + 
                          " to " + dateFormat.format(endDate));
            writer.println();
            
            // Sort by amount (descending)
            List<CategorySpending> sortedSpending = new ArrayList<>(spendingByCategory.values());
            sortedSpending.sort((s1, s2) -> Double.compare(Math.abs(s2.getAmount()), Math.abs(s1.getAmount())));
            
            // Calculate column widths
            int categoryWidth = 20;
            int amountWidth = 15;
            int countWidth = 10;
            int percentWidth = 10;
            
            // Print header
            writer.printf("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                         countWidth + "s %" + percentWidth + "s%n",
                         "Category", "Amount", "Count", "Percentage");
            
            writer.println(String.format("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                                       countWidth + "s %" + percentWidth + "s",
                                       "--------------------", "---------------", "----------", "----------"));
            
            // Print data rows
            for (CategorySpending spending : sortedSpending) {
                writer.printf("%-" + categoryWidth + "s %" + amountWidth + "s %" + 
                             countWidth + "s %" + percentWidth + "s%n",
                             truncate(spending.getCategoryName(), categoryWidth),
                             String.format("$%.2f", spending.getAmount()),
                             spending.getTransactionCount(),
                             String.format("%.2f%%", spending.getPercentage()));
            }
        }
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
    
    /**
     * Class representing a date range.
     */
    private static class DateRange {
        private final Date startDate;
        private final Date endDate;
        private final String label;
        
        public DateRange(Date startDate, Date endDate, String label) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.label = label;
        }
    }
}