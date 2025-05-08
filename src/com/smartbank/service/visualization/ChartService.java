package com.smartbank.service.visualization;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import javafx.scene.chart.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for chart and visualization related functionality.
 */
public interface ChartService {

    /**
     * Create a line chart showing balance history over time for a specific account.
     * 
     * @param accountNumber The account number to show balance history for
     * @param startDate The start date for the period to display
     * @param endDate The end date for the period to display
     * @return LineChart with balance history data
     */
    LineChart<String, Number> createBalanceHistoryChart(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Create a pie chart showing transaction distribution by category.
     * 
     * @param accountNumber The account number to show category distribution for
     * @param startDate The start date for the period to display
     * @param endDate The end date for the period to display
     * @return PieChart with category distribution data
     */
    PieChart createCategoryDistributionChart(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Create a bar chart showing income vs expenses by period.
     * 
     * @param accountNumber The account number to show income/expense data for
     * @param startDate The start date for the period to display
     * @param endDate The end date for the period to display
     * @param periodType The type of period to group by (day, week, month, quarter, year)
     * @return BarChart with income/expense data
     */
    BarChart<String, Number> createIncomeExpenseChart(long accountNumber, Date startDate, Date endDate, PeriodType periodType);
    
    /**
     * Create a bar chart comparing monthly activity across different months.
     * 
     * @param accountNumber The account number to show monthly comparison for
     * @param startDate The start date for the period to display
     * @param endDate The end date for the period to display
     * @return BarChart with monthly comparison data
     */
    BarChart<String, Number> createMonthlyComparisonChart(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Apply custom styling to a chart.
     * 
     * @param chart The chart to apply styling to
     * @param title The chart title
     * @param styleClass The style class to apply
     * @return The styled chart
     */
    <T extends Chart> T applyChartStyling(T chart, String title, String styleClass);
    
    /**
     * Export a chart to an image file.
     * 
     * @param chart The chart to export
     * @param filePath The path to save the exported image to
     * @param format The image format (PNG, JPG, SVG)
     * @return True if export was successful, false otherwise
     */
    boolean exportChartToImage(Chart chart, String filePath, ExportFormat format);
    
    /**
     * Get data for balance history visualization.
     * 
     * @param accountNumber The account number to get data for
     * @param startDate The start date for the data range
     * @param endDate The end date for the data range
     * @return A map of dates to balance values
     */
    Map<Date, Double> getBalanceHistoryData(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get data for category distribution visualization.
     * 
     * @param accountNumber The account number to get data for
     * @param startDate The start date for the data range
     * @param endDate The end date for the data range
     * @return A map of category names to transaction amounts
     */
    Map<String, Double> getCategoryDistributionData(long accountNumber, Date startDate, Date endDate);
    
    /**
     * Get data for income vs expense visualization.
     * 
     * @param accountNumber The account number to get data for
     * @param startDate The start date for the data range
     * @param endDate The end date for the data range
     * @param periodType The type of period to group by
     * @return A map of period labels to income/expense data
     */
    Map<String, Map<String, Double>> getIncomeExpenseData(long accountNumber, Date startDate, Date endDate, PeriodType periodType);
    
    /**
     * Period types for grouping transaction data.
     */
    enum PeriodType {
        DAY, WEEK, MONTH, QUARTER, YEAR
    }
    
    /**
     * Export formats for chart images.
     */
    enum ExportFormat {
        PNG, JPG, SVG, PDF
    }
}