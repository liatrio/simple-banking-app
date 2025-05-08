package com.smartbank.service.recurring;

import com.smartbank.model.Account;
import com.smartbank.model.RecurringTransaction;
import com.smartbank.model.Transaction;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RecurringTransactionExecutionRepository;
import com.smartbank.repository.RecurringTransactionRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.AccountService;
import com.smartbank.service.ServiceFactory;
import com.smartbank.service.TransactionService;
import com.smartbank.service.transfer.TransferService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the RecurringTransactionService interface.
 */
public class RecurringTransactionServiceImpl implements RecurringTransactionService {
    private static final Logger LOGGER = Logger.getLogger(RecurringTransactionServiceImpl.class.getName());
    
    // Singleton instance
    private static RecurringTransactionServiceImpl instance;
    
    // Repositories
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionExecutionRepository executionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    // Services
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransferService transferService;
    
    /**
     * Get the singleton instance of the service.
     * 
     * @return The RecurringTransactionServiceImpl instance
     */
    public static synchronized RecurringTransactionServiceImpl getInstance() {
        if (instance == null) {
            instance = new RecurringTransactionServiceImpl();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private RecurringTransactionServiceImpl() {
        this.recurringTransactionRepository = RepositoryFactory.getRecurringTransactionRepository();
        this.executionRepository = RepositoryFactory.getRecurringTransactionExecutionRepository();
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        
        this.accountService = ServiceFactory.getAccountService();
        this.transactionService = ServiceFactory.getTransactionService();
        this.transferService = ServiceFactory.getTransferService();
    }
    
    @Override
    public RecurringTransaction createRecurringTransaction(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            Transaction.Type type, String description, RecurringTransaction.Frequency frequency,
            LocalDate startDate, LocalDate endDate, Integer occurrenceLimit) throws Exception {
        
        // Validate the recurring transaction
        ValidationResult validationResult = validateRecurringTransaction(
                sourceAccountNumber, targetAccountNumber, amount, type, frequency);
        
        if (!validationResult.isValid()) {
            throw new Exception("Invalid recurring transaction: " + validationResult.getMessages().get(0));
        }
        
        // Create the recurring transaction
        RecurringTransaction recurringTransaction = new RecurringTransaction(
                sourceAccountNumber, targetAccountNumber, amount, type, description,
                frequency, 0, 0, startDate, endDate, occurrenceLimit);
        
        // Save and return the recurring transaction
        return recurringTransactionRepository.save(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction createDailyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate) throws Exception {
        return createRecurringTransaction(
                sourceAccountNumber, 0, amount, type, description,
                RecurringTransaction.Frequency.DAILY, startDate, null, null);
    }
    
    @Override
    public RecurringTransaction createWeeklyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate, int dayOfWeek) throws Exception {
        
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        }
        
        // Create the recurring transaction
        RecurringTransaction recurringTransaction = new RecurringTransaction(
                sourceAccountNumber, 0, amount, type, description,
                RecurringTransaction.Frequency.WEEKLY, startDate);
        
        // Set the day of week
        recurringTransaction.setDayOfWeek(dayOfWeek);
        
        // Update the next execution date based on the day of week
        recurringTransaction.updateNextExecutionDate();
        
        // Save and return the recurring transaction
        return recurringTransactionRepository.save(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction createMonthlyRecurringTransaction(
            long sourceAccountNumber, double amount, Transaction.Type type, 
            String description, LocalDate startDate, int dayOfMonth) throws Exception {
        
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("Day of month must be between 1 and 31");
        }
        
        // Create the recurring transaction
        RecurringTransaction recurringTransaction = new RecurringTransaction(
                sourceAccountNumber, 0, amount, type, description,
                RecurringTransaction.Frequency.MONTHLY, startDate);
        
        // Set the day of month
        recurringTransaction.setDayOfMonth(dayOfMonth);
        
        // Update the next execution date based on the day of month
        recurringTransaction.updateNextExecutionDate();
        
        // Save and return the recurring transaction
        return recurringTransactionRepository.save(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction createRecurringTransfer(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            String description, RecurringTransaction.Frequency frequency,
            LocalDate startDate) throws Exception {
        
        // Validate the transfer
        if (targetAccountNumber <= 0) {
            throw new IllegalArgumentException("Target account number is required for transfers");
        }
        
        // Create the recurring transaction as a transfer
        return createRecurringTransaction(
                sourceAccountNumber, targetAccountNumber, amount,
                Transaction.Type.TRANSFER_OUT, description, frequency,
                startDate, null, null);
    }
    
    @Override
    public Optional<RecurringTransaction> getRecurringTransactionById(long recurringTransactionId) {
        return recurringTransactionRepository.findById(recurringTransactionId);
    }
    
    @Override
    public List<RecurringTransaction> getAllRecurringTransactions() {
        return recurringTransactionRepository.findAll();
    }
    
    @Override
    public List<RecurringTransaction> getRecurringTransactionsByAccount(long accountNumber) {
        return recurringTransactionRepository.findByAccountNumber(accountNumber);
    }
    
    @Override
    public List<RecurringTransaction> getRecurringTransactionsByStatus(RecurringTransaction.Status status) {
        return recurringTransactionRepository.findByStatus(status);
    }
    
    @Override
    public List<RecurringTransaction> getDueRecurringTransactions(LocalDate date) {
        return recurringTransactionRepository.findDueTransactions(date);
    }
    
    @Override
    public RecurringTransaction updateRecurringTransaction(
            long recurringTransactionId, Double amount, String description,
            RecurringTransaction.Frequency frequency, LocalDate endDate, 
            Integer occurrenceLimit) throws Exception {
        
        // Find the recurring transaction
        Optional<RecurringTransaction> recurringTransactionOpt = 
                recurringTransactionRepository.findById(recurringTransactionId);
        
        if (!recurringTransactionOpt.isPresent()) {
            throw new Exception("Recurring transaction not found: " + recurringTransactionId);
        }
        
        RecurringTransaction recurringTransaction = recurringTransactionOpt.get();
        
        // Check if active
        if (!recurringTransaction.isActive()) {
            throw new Exception("Cannot update inactive recurring transaction: " + recurringTransactionId);
        }
        
        // Update fields if provided
        if (amount != null) {
            // Validate amount
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            recurringTransaction.setAmount(amount);
        }
        
        if (description != null) {
            recurringTransaction.setDescription(description);
        }
        
        if (frequency != null) {
            recurringTransaction.setFrequency(frequency);
        }
        
        if (endDate != null) {
            if (endDate.equals(LocalDate.MAX)) {
                // Special case: remove end date
                recurringTransaction.setEndDate(null);
            } else {
                // Validate end date
                if (endDate.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("End date cannot be in the past");
                }
                recurringTransaction.setEndDate(endDate);
            }
        }
        
        if (occurrenceLimit != null) {
            if (occurrenceLimit == 0) {
                // Special case: remove occurrence limit
                recurringTransaction.setOccurrenceLimit(null);
            } else {
                // Validate occurrence limit
                if (occurrenceLimit < 0) {
                    throw new IllegalArgumentException("Occurrence limit cannot be negative");
                }
                if (occurrenceLimit <= recurringTransaction.getExecutionCount()) {
                    throw new IllegalArgumentException("Occurrence limit cannot be less than current execution count");
                }
                recurringTransaction.setOccurrenceLimit(occurrenceLimit);
            }
        }
        
        // Save and return the updated recurring transaction
        return recurringTransactionRepository.update(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction pauseRecurringTransaction(long recurringTransactionId) throws Exception {
        // Find the recurring transaction
        Optional<RecurringTransaction> recurringTransactionOpt = 
                recurringTransactionRepository.findById(recurringTransactionId);
        
        if (!recurringTransactionOpt.isPresent()) {
            throw new Exception("Recurring transaction not found: " + recurringTransactionId);
        }
        
        RecurringTransaction recurringTransaction = recurringTransactionOpt.get();
        
        // Check if already paused or inactive
        if (recurringTransaction.getStatus() == RecurringTransaction.Status.PAUSED) {
            LOGGER.info("Recurring transaction is already paused: " + recurringTransactionId);
            return recurringTransaction;
        }
        
        if (recurringTransaction.getStatus() != RecurringTransaction.Status.ACTIVE) {
            throw new Exception("Cannot pause inactive recurring transaction: " + recurringTransactionId);
        }
        
        // Pause the recurring transaction
        recurringTransaction.pause();
        
        // Save and return the updated recurring transaction
        return recurringTransactionRepository.update(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction resumeRecurringTransaction(long recurringTransactionId) throws Exception {
        // Find the recurring transaction
        Optional<RecurringTransaction> recurringTransactionOpt = 
                recurringTransactionRepository.findById(recurringTransactionId);
        
        if (!recurringTransactionOpt.isPresent()) {
            throw new Exception("Recurring transaction not found: " + recurringTransactionId);
        }
        
        RecurringTransaction recurringTransaction = recurringTransactionOpt.get();
        
        // Check if paused
        if (recurringTransaction.getStatus() != RecurringTransaction.Status.PAUSED) {
            throw new Exception("Cannot resume non-paused recurring transaction: " + recurringTransactionId);
        }
        
        // Resume the recurring transaction
        recurringTransaction.resume();
        
        // Save and return the updated recurring transaction
        return recurringTransactionRepository.update(recurringTransaction);
    }
    
    @Override
    public RecurringTransaction cancelRecurringTransaction(long recurringTransactionId) throws Exception {
        // Find the recurring transaction
        Optional<RecurringTransaction> recurringTransactionOpt = 
                recurringTransactionRepository.findById(recurringTransactionId);
        
        if (!recurringTransactionOpt.isPresent()) {
            throw new Exception("Recurring transaction not found: " + recurringTransactionId);
        }
        
        RecurringTransaction recurringTransaction = recurringTransactionOpt.get();
        
        // Check if already cancelled or completed
        if (recurringTransaction.getStatus() == RecurringTransaction.Status.CANCELLED ||
            recurringTransaction.getStatus() == RecurringTransaction.Status.COMPLETED) {
            LOGGER.info("Recurring transaction is already cancelled or completed: " + recurringTransactionId);
            return recurringTransaction;
        }
        
        // Cancel the recurring transaction
        recurringTransaction.cancel();
        
        // Save and return the updated recurring transaction
        return recurringTransactionRepository.update(recurringTransaction);
    }
    
    @Override
    public boolean deleteRecurringTransaction(long recurringTransactionId) {
        // First delete all execution records
        executionRepository.deleteByRecurringTransactionId(recurringTransactionId);
        
        // Then delete the recurring transaction
        return recurringTransactionRepository.deleteById(recurringTransactionId);
    }
    
    @Override
    public RecurringTransactionResult executeRecurringTransaction(long recurringTransactionId) throws Exception {
        // Find the recurring transaction
        Optional<RecurringTransaction> recurringTransactionOpt = 
                recurringTransactionRepository.findById(recurringTransactionId);
        
        if (!recurringTransactionOpt.isPresent()) {
            throw new Exception("Recurring transaction not found: " + recurringTransactionId);
        }
        
        RecurringTransaction recurringTransaction = recurringTransactionOpt.get();
        
        // Check if active
        if (!recurringTransaction.isActive()) {
            return RecurringTransactionResult.Builder.failure(
                    recurringTransactionId,
                    RecurringTransactionResult.Status.VALIDATION_ERROR,
                    "Recurring transaction is not active").build();
        }
        
        // Validation and execution
        try {
            // Create a transaction based on the type
            Transaction transaction = null;
            
            switch (recurringTransaction.getType()) {
                case DEPOSIT:
                    transaction = transactionService.createTransaction(
                            recurringTransaction.getSourceAccountNumber(),
                            recurringTransaction.getAmount(),
                            Transaction.Type.DEPOSIT,
                            recurringTransaction.getDescription() + " (Recurring)");
                    break;
                    
                case WITHDRAWAL:
                    // Check if there are sufficient funds
                    Optional<Account> accountOpt = accountRepository.findById(recurringTransaction.getSourceAccountNumber());
                    if (!accountOpt.isPresent()) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.ACCOUNT_NOT_FOUND,
                                "Source account not found").build();
                    }
                    
                    Account account = accountOpt.get();
                    if (account.getBalance() < recurringTransaction.getAmount()) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.INSUFFICIENT_FUNDS,
                                "Insufficient funds in source account").build();
                    }
                    
                    transaction = transactionService.createTransaction(
                            recurringTransaction.getSourceAccountNumber(),
                            recurringTransaction.getAmount(),
                            Transaction.Type.WITHDRAWAL,
                            recurringTransaction.getDescription() + " (Recurring)");
                    break;
                    
                case TRANSFER_OUT:
                    if (recurringTransaction.getTargetAccountNumber() <= 0) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.VALIDATION_ERROR,
                                "Target account number is required for transfers").build();
                    }
                    
                    // Execute the transfer through the transfer service
                    try {
                        transferService.transfer(
                                recurringTransaction.getSourceAccountNumber(),
                                recurringTransaction.getTargetAccountNumber(),
                                recurringTransaction.getAmount(),
                                recurringTransaction.getDescription() + " (Recurring)");
                        
                        // Get the transfer-out transaction
                        List<Transaction> recentTransactions = transactionRepository.findRecentByAccountAndType(
                                recurringTransaction.getSourceAccountNumber(),
                                Transaction.Type.TRANSFER_OUT,
                                1);
                        
                        if (!recentTransactions.isEmpty()) {
                            transaction = recentTransactions.get(0);
                        }
                    } catch (Exception e) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.SYSTEM_ERROR,
                                "Transfer failed: " + e.getMessage()).build();
                    }
                    break;
                    
                case PAYMENT:
                    // For payments, check if there are sufficient funds
                    Optional<Account> paymentAccountOpt = accountRepository.findById(recurringTransaction.getSourceAccountNumber());
                    if (!paymentAccountOpt.isPresent()) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.ACCOUNT_NOT_FOUND,
                                "Source account not found").build();
                    }
                    
                    Account paymentAccount = paymentAccountOpt.get();
                    if (paymentAccount.getBalance() < recurringTransaction.getAmount()) {
                        return RecurringTransactionResult.Builder.failure(
                                recurringTransactionId,
                                RecurringTransactionResult.Status.INSUFFICIENT_FUNDS,
                                "Insufficient funds in source account").build();
                    }
                    
                    transaction = transactionService.createTransaction(
                            recurringTransaction.getSourceAccountNumber(),
                            recurringTransaction.getAmount(),
                            Transaction.Type.PAYMENT,
                            recurringTransaction.getDescription() + " (Recurring)");
                    break;
                    
                default:
                    return RecurringTransactionResult.Builder.failure(
                            recurringTransactionId,
                            RecurringTransactionResult.Status.VALIDATION_ERROR,
                            "Unsupported transaction type: " + recurringTransaction.getType()).build();
            }
            
            // Update the recurring transaction with execution details
            recurringTransaction.recordExecution();
            recurringTransactionRepository.update(recurringTransaction);
            
            // Create the result
            RecurringTransactionResult result = RecurringTransactionResult.Builder.success(
                    recurringTransactionId,
                    transaction,
                    recurringTransaction.getNextExecutionDate(),
                    recurringTransaction.getExecutionCount()).build();
            
            // Record the execution
            executionRepository.createFromResult(result);
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing recurring transaction: " + recurringTransactionId, e);
            
            RecurringTransactionResult result = RecurringTransactionResult.Builder.failure(
                    recurringTransactionId,
                    RecurringTransactionResult.Status.SYSTEM_ERROR,
                    "Error executing recurring transaction: " + e.getMessage()).build();
            
            // Record the failure
            executionRepository.createFromResult(result);
            
            return result;
        }
    }
    
    @Override
    public int processDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = getDueRecurringTransactions(today);
        
        int successCount = 0;
        
        for (RecurringTransaction recurringTransaction : dueTransactions) {
            try {
                RecurringTransactionResult result = executeRecurringTransaction(recurringTransaction.getRecurringTransactionId());
                if (result.isSuccessful()) {
                    successCount++;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing due recurring transaction: " + 
                        recurringTransaction.getRecurringTransactionId(), e);
            }
        }
        
        return successCount;
    }
    
    @Override
    public List<RecurringTransactionExecution> getExecutionHistory(long recurringTransactionId) {
        return executionRepository.findByRecurringTransactionId(recurringTransactionId);
    }
    
    @Override
    public ValidationResult validateRecurringTransaction(
            long sourceAccountNumber, long targetAccountNumber, double amount,
            Transaction.Type type, RecurringTransaction.Frequency frequency) {
        
        List<String> errors = new ArrayList<>();
        
        // Validate amount
        if (amount <= 0) {
            errors.add("Amount must be positive");
        }
        
        // Validate source account
        Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountNumber);
        if (!sourceAccountOpt.isPresent()) {
            errors.add("Source account not found: " + sourceAccountNumber);
        }
        
        // Validate target account if it's a transfer
        if (type == Transaction.Type.TRANSFER_OUT) {
            if (targetAccountNumber <= 0) {
                errors.add("Target account number is required for transfers");
            } else {
                Optional<Account> targetAccountOpt = accountRepository.findById(targetAccountNumber);
                if (!targetAccountOpt.isPresent()) {
                    errors.add("Target account not found: " + targetAccountNumber);
                }
            }
        }
        
        // Validate transaction type
        switch (type) {
            case DEPOSIT:
            case WITHDRAWAL:
            case TRANSFER_OUT:
            case PAYMENT:
                // These types are supported
                break;
            default:
                errors.add("Unsupported transaction type: " + type);
        }
        
        // Return validation result
        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors);
        }
        
        return ValidationResult.success();
    }
}