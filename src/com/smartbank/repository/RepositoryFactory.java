package com.smartbank.repository;

import com.smartbank.repository.impl.InterestCalculationRecordRepositoryImpl;
import com.smartbank.repository.impl.JpaAccountRepository;
import com.smartbank.repository.impl.JpaRecurringTransactionExecutionRepository;
import com.smartbank.repository.impl.JpaRecurringTransactionRepository;
import com.smartbank.repository.impl.JpaTransactionRepository;
import com.smartbank.repository.impl.JpaUserRepository;

/**
 * Factory class for creating repository instances.
 */
public class RepositoryFactory {
    private static AccountRepository accountRepository;
    private static TransactionRepository transactionRepository;
    private static UserRepository userRepository;
    private static TransactionCategoryRepository transactionCategoryRepository;
    private static InterestCalculationRecordRepository interestCalculationRecordRepository;
    private static RecurringTransactionRepository recurringTransactionRepository;
    private static RecurringTransactionExecutionRepository recurringTransactionExecutionRepository;
    
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
}