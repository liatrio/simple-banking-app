package com.smartbank.service.impl;

import com.smartbank.model.Transaction;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.TransactionService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of the TransactionService interface.
 */
public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());
    
    private final TransactionRepository transactionRepository;
    
    public TransactionServiceImpl() {
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    @Override
    public Transaction createTransaction(long accountNumber, double amount, Transaction.Type type, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        Transaction transaction = new Transaction(
                0, // Will be auto-generated
                accountNumber,
                amount,
                type,
                new Date(),
                description);
        
        transaction = transactionRepository.save(transaction);
        LOGGER.info("Created " + type + " transaction #" + transaction.getTransactionId() + " for account #" + accountNumber);
        return transaction;
    }
    
    @Override
    public Transaction updateTransaction(Transaction transaction) {
        transaction = transactionRepository.save(transaction);
        LOGGER.info("Updated transaction #" + transaction.getTransactionId());
        return transaction;
    }
    
    @Override
    public Optional<Transaction> getTransactionById(long transactionId) {
        return transactionRepository.findById(transactionId);
    }
    
    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    @Override
    public List<Transaction> getTransactionsByAccount(long accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber);
    }
    
    @Override
    public List<Transaction> getTransactionsByType(Transaction.Type type) {
        return transactionRepository.findByType(type);
    }
    
    @Override
    public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }
    
    @Override
    public List<Transaction> getTransactionsByAccountAndDateRange(long accountNumber, Date startDate, Date endDate) {
        return transactionRepository.findByAccountNumberAndDateRange(accountNumber, startDate, endDate);
    }
    
    @Override
    public List<Transaction> getTransactionsByAccountAndType(long accountNumber, Transaction.Type type) {
        return transactionRepository.findByAccountNumberAndType(accountNumber, type);
    }
    
    @Override
    public double calculateTotalDeposits(long accountNumber) {
        List<Transaction> deposits = transactionRepository.findByAccountNumberAndType(accountNumber, Transaction.Type.DEPOSIT);
        return deposits.stream().mapToDouble(Transaction::getAmount).sum();
    }
    
    @Override
    public double calculateTotalWithdrawals(long accountNumber) {
        List<Transaction> withdrawals = transactionRepository.findByAccountNumberAndType(accountNumber, Transaction.Type.WITHDRAWAL);
        return withdrawals.stream().mapToDouble(Transaction::getAmount).sum();
    }
}