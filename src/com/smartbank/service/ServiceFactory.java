package com.smartbank.service;

import com.smartbank.service.budgeting.BudgetService;
import com.smartbank.service.budgeting.BudgetServiceImpl;
import com.smartbank.service.category.*;
import com.smartbank.service.credit.CreditLimitService;
import com.smartbank.service.credit.CreditLimitServiceImpl;
import com.smartbank.service.impl.AccountServiceImpl;
import com.smartbank.service.impl.TransactionServiceImpl;
import com.smartbank.service.impl.UserServiceImpl;
import com.smartbank.service.interest.InterestCalculationService;
import com.smartbank.service.interest.InterestCalculationServiceImpl;
import com.smartbank.service.interest.InterestCalculationScheduler;
import com.smartbank.service.recurring.RecurringTransactionService;
import com.smartbank.service.recurring.RecurringTransactionServiceImpl;
import com.smartbank.service.recurring.RecurringTransactionScheduler;
import com.smartbank.service.recurring.RecurringTransactionNotifier;
import com.smartbank.service.reporting.CategoryReportService;
import com.smartbank.service.reporting.CategoryReportServiceImpl;
import com.smartbank.service.search.TransactionSearchService;
import com.smartbank.service.search.TransactionSearchServiceImpl;
import com.smartbank.service.statement.StatementGenerationService;
import com.smartbank.service.statement.StatementGenerationServiceImpl;
import com.smartbank.service.transfer.TransferService;
import com.smartbank.service.transfer.TransferServiceImpl;
import com.smartbank.service.visualization.ChartService;
import com.smartbank.service.visualization.ChartServiceImpl;

/**
 * Factory class for creating service instances.
 */
public class ServiceFactory {
    private static AccountService accountService;
    private static TransactionService transactionService;
    private static UserService userService;
    private static CategoryService categoryService;
    private static CategorizationRuleService categorizationRuleService;
    private static TransactionCategorizationService transactionCategorizationService;
    private static CategoryReportService categoryReportService;
    private static BudgetService budgetService;
    private static InterestCalculationService interestCalculationService;
    private static RecurringTransactionService recurringTransactionService;
    private static StatementGenerationService statementGenerationService;
    private static CreditLimitService creditLimitService;
    private static ChartService chartService;
    private static TransactionSearchService transactionSearchService;
    private static ServiceFactory instance;
    
    /**
     * Get an instance of the AccountService.
     * @return The AccountService instance
     */
    public static synchronized AccountService getAccountService() {
        if (accountService == null) {
            accountService = new AccountServiceImpl();
        }
        return accountService;
    }
    
    /**
     * Get an instance of the TransactionService.
     * @return The TransactionService instance
     */
    public static synchronized TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionServiceImpl();
        }
        return transactionService;
    }
    
    /**
     * Get an instance of the UserService.
     * @return The UserService instance
     */
    public static synchronized UserService getUserService() {
        if (userService == null) {
            userService = new UserServiceImpl();
        }
        return userService;
    }
    
    /**
     * Get an instance of the CategoryService.
     * @return The CategoryService instance
     */
    public static synchronized CategoryService getCategoryService() {
        if (categoryService == null) {
            categoryService = new CategoryServiceImpl();
        }
        return categoryService;
    }
    
    /**
     * Get an instance of the CategorizationRuleService.
     * @return The CategorizationRuleService instance
     */
    public static synchronized CategorizationRuleService getCategorizationRuleService() {
        if (categorizationRuleService == null) {
            categorizationRuleService = new CategorizationRuleServiceImpl();
        }
        return categorizationRuleService;
    }
    
    /**
     * Get an instance of the TransactionCategorizationService.
     * @return The TransactionCategorizationService instance
     */
    public static synchronized TransactionCategorizationService getTransactionCategorizationService() {
        if (transactionCategorizationService == null) {
            transactionCategorizationService = new TransactionCategorizationServiceImpl();
        }
        return transactionCategorizationService;
    }
    
    /**
     * Get an instance of the CategoryReportService.
     * @return The CategoryReportService instance
     */
    public static synchronized CategoryReportService getCategoryReportService() {
        if (categoryReportService == null) {
            categoryReportService = new CategoryReportServiceImpl();
        }
        return categoryReportService;
    }
    
    /**
     * Get an instance of the BudgetService.
     * @return The BudgetService instance
     */
    public static synchronized BudgetService getBudgetService() {
        if (budgetService == null) {
            budgetService = new BudgetServiceImpl();
        }
        return budgetService;
    }
    
    /**
     * Get an instance of the InterestCalculationService.
     * @return The InterestCalculationService instance
     */
    public static synchronized InterestCalculationService getInterestCalculationService() {
        if (interestCalculationService == null) {
            interestCalculationService = InterestCalculationServiceImpl.getInstance();
        }
        return interestCalculationService;
    }
    
    /**
     * Get an instance of the InterestCalculationScheduler.
     * @return The InterestCalculationScheduler instance
     */
    public static synchronized InterestCalculationScheduler getInterestCalculationScheduler() {
        return InterestCalculationScheduler.getInstance();
    }
    
    /**
     * Get an instance of the RecurringTransactionService.
     * @return The RecurringTransactionService instance
     */
    public static synchronized RecurringTransactionService getRecurringTransactionService() {
        if (recurringTransactionService == null) {
            recurringTransactionService = RecurringTransactionServiceImpl.getInstance();
        }
        return recurringTransactionService;
    }
    
    /**
     * Get an instance of the RecurringTransactionScheduler.
     * @return The RecurringTransactionScheduler instance
     */
    public static synchronized RecurringTransactionScheduler getRecurringTransactionScheduler() {
        return RecurringTransactionScheduler.getInstance();
    }
    
    /**
     * Get an instance of the RecurringTransactionNotifier.
     * @return The RecurringTransactionNotifier instance
     */
    public static synchronized RecurringTransactionNotifier getRecurringTransactionNotifier() {
        return RecurringTransactionNotifier.getInstance();
    }
    
    /**
     * Get an instance of the TransferService.
     * @return The TransferService instance
     */
    public static synchronized TransferService getTransferService() {
        return new TransferServiceImpl();
    }
    
    /**
     * Get an instance of the StatementGenerationService.
     * @return The StatementGenerationService instance
     */
    public static synchronized StatementGenerationService getStatementGenerationService() {
        if (statementGenerationService == null) {
            statementGenerationService = new StatementGenerationServiceImpl();
        }
        return statementGenerationService;
    }
    
    /**
     * Get an instance of the CreditLimitService.
     * @return The CreditLimitService instance
     */
    public static synchronized CreditLimitService getCreditLimitService() {
        if (creditLimitService == null) {
            creditLimitService = new CreditLimitServiceImpl();
        }
        return creditLimitService;
    }
    
    /**
     * Get an instance of the ChartService.
     * @return The ChartService instance
     */
    public static synchronized ChartService getChartService() {
        if (chartService == null) {
            chartService = new ChartServiceImpl();
        }
        return chartService;
    }
    
    /**
     * Get an instance of the TransactionSearchService.
     * @return The TransactionSearchService instance
     */
    public static synchronized TransactionSearchService getTransactionSearchService() {
        if (transactionSearchService == null) {
            transactionSearchService = new TransactionSearchServiceImpl();
        }
        return transactionSearchService;
    }
    
    /**
     * Get the singleton instance of the ServiceFactory.
     * @return The ServiceFactory instance
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }
}