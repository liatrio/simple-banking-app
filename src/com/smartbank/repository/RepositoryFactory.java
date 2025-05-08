package com.smartbank.repository;

import com.smartbank.repository.impl.*;

/**
 * Factory class for creating repository instances.
 */
public class RepositoryFactory {
    private static RepositoryFactory instance;
    private static AccountRepository accountRepository;
    private static TransactionRepository transactionRepository;
    private static UserRepository userRepository;
    private static TransactionCategoryRepository transactionCategoryRepository;
    private static InterestCalculationRecordRepository interestCalculationRecordRepository;
    private static RecurringTransactionRepository recurringTransactionRepository;
    private static RecurringTransactionExecutionRepository recurringTransactionExecutionRepository;
    private static StatementRepository statementRepository;
    private static CreditHistoryRepository creditHistoryRepository;
    private static CreditLimitChangeRequestRepository creditLimitChangeRequestRepository;
    private static SearchHistoryRepository searchHistoryRepository;
    private static TransactionSearchRepository transactionSearchRepository;
    
    /**
     * Get an instance of the AccountRepository.
     * @return The AccountRepository instance
     */
    public static synchronized AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new JpaAccountRepository();
        }
        return accountRepository;
    }
    
    /**
     * Get an instance of the TransactionRepository.
     * @return The TransactionRepository instance
     */
    public static synchronized TransactionRepository getTransactionRepository() {
        if (transactionRepository == null) {
            transactionRepository = new JpaTransactionRepository();
        }
        return transactionRepository;
    }
    
    /**
     * Get an instance of the UserRepository.
     * @return The UserRepository instance
     */
    public static synchronized UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new JpaUserRepository();
        }
        return userRepository;
    }
    
    /**
     * Get an instance of the TransactionCategoryRepository.
     * @return The TransactionCategoryRepository instance
     */
    public static synchronized TransactionCategoryRepository getTransactionCategoryRepository() {
        if (transactionCategoryRepository == null) {
            transactionCategoryRepository = new JpaTransactionCategoryRepository();
        }
        return transactionCategoryRepository;
    }
    
    /**
     * Get an instance of the InterestCalculationRecordRepository.
     * @return The InterestCalculationRecordRepository instance
     */
    public static synchronized InterestCalculationRecordRepository getInterestCalculationRecordRepository() {
        if (interestCalculationRecordRepository == null) {
            interestCalculationRecordRepository = new InterestCalculationRecordRepositoryImpl();
        }
        return interestCalculationRecordRepository;
    }
    
    /**
     * Get an instance of the RecurringTransactionRepository.
     * @return The RecurringTransactionRepository instance
     */
    public static synchronized RecurringTransactionRepository getRecurringTransactionRepository() {
        if (recurringTransactionRepository == null) {
            recurringTransactionRepository = new JpaRecurringTransactionRepository();
        }
        return recurringTransactionRepository;
    }
    
    /**
     * Get an instance of the RecurringTransactionExecutionRepository.
     * @return The RecurringTransactionExecutionRepository instance
     */
    public static synchronized RecurringTransactionExecutionRepository getRecurringTransactionExecutionRepository() {
        if (recurringTransactionExecutionRepository == null) {
            recurringTransactionExecutionRepository = new JpaRecurringTransactionExecutionRepository();
        }
        return recurringTransactionExecutionRepository;
    }
    
    /**
     * Get an instance of the StatementRepository.
     * @return The StatementRepository instance
     */
    public static synchronized StatementRepository getStatementRepository() {
        if (statementRepository == null) {
            statementRepository = new JpaStatementRepository();
        }
        return statementRepository;
    }
    
    /**
     * Get an instance of the CreditHistoryRepository.
     * @return The CreditHistoryRepository instance
     */
    public static synchronized CreditHistoryRepository getCreditHistoryRepository() {
        if (creditHistoryRepository == null) {
            creditHistoryRepository = new JpaCreditHistoryRepository();
        }
        return creditHistoryRepository;
    }
    
    /**
     * Get an instance of the CreditLimitChangeRequestRepository.
     * @return The CreditLimitChangeRequestRepository instance
     */
    public static synchronized CreditLimitChangeRequestRepository getCreditLimitChangeRequestRepository() {
        if (creditLimitChangeRequestRepository == null) {
            creditLimitChangeRequestRepository = new JpaCreditLimitChangeRequestRepository();
        }
        return creditLimitChangeRequestRepository;
    }
    
    /**
     * Get an instance of the SearchHistoryRepository.
     * @return The SearchHistoryRepository instance
     */
    public static synchronized SearchHistoryRepository getSearchHistoryRepository() {
        if (searchHistoryRepository == null) {
            searchHistoryRepository = new JpaSearchHistoryRepository();
        }
        return searchHistoryRepository;
    }
    
    /**
     * Get an instance of the TransactionSearchRepository.
     * @return The TransactionSearchRepository instance
     */
    public static synchronized TransactionSearchRepository getTransactionSearchRepository() {
        if (transactionSearchRepository == null) {
            transactionSearchRepository = new JpaTransactionSearchRepository();
        }
        return transactionSearchRepository;
    }
    
    /**
     * Get the singleton instance of the RepositoryFactory.
     * @return The RepositoryFactory instance
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }
    
    /**
     * Get a repository for a specific entity type.
     * @param entityClass The entity class
     * @param <T> The entity type
     * @param <ID> The ID type
     * @return The repository instance
     */
    @SuppressWarnings("unchecked")
    public <T, ID> Repository<T, ID> getRepository(Class<T> entityClass) {
        if (entityClass.getSimpleName().equals("User")) {
            return (Repository<T, ID>) getUserRepository();
        } else if (entityClass.getSimpleName().equals("Account")) {
            return (Repository<T, ID>) getAccountRepository();
        } else if (entityClass.getSimpleName().equals("Transaction")) {
            return (Repository<T, ID>) getTransactionRepository();
        } else if (entityClass.getSimpleName().equals("TransactionCategory")) {
            return (Repository<T, ID>) getTransactionCategoryRepository();
        } else if (entityClass.getSimpleName().equals("ThemePreference")) {
            return (Repository<T, ID>) getSearchHistoryRepository();
        } else {
            throw new IllegalArgumentException("No repository available for entity: " + entityClass.getSimpleName());
        }
    }
}