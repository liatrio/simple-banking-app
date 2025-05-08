package com.smartbank.service.transfer;

import com.smartbank.model.Account;
import com.smartbank.model.CreditAccount;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.transfer.ScheduledTransfer.ScheduledTransferStatus;
import com.smartbank.service.transfer.TransferException.TransferErrorCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the TransferService interface.
 */
public class TransferServiceImpl implements TransferService {
    private static final Logger LOGGER = Logger.getLogger(TransferServiceImpl.class.getName());
    
    // Repositories
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    // Transfer ID generator
    private static final AtomicLong transferIdGenerator = new AtomicLong(100000);
    
    // Transfer records (in-memory for now, should be persisted in a database)
    private final Map<Long, List<TransferRecord>> transferRecordsByAccount = new ConcurrentHashMap<>();
    
    // Scheduled transfers (in-memory for now, should be persisted in a database)
    private final Map<Long, ScheduledTransfer> scheduledTransfers = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> scheduledTransfersByAccount = new ConcurrentHashMap<>();
    
    // Daily transfer tracking (in-memory for now, should be persisted in a database)
    private final Map<Long, Map<String, Double>> dailyTransferAmounts = new ConcurrentHashMap<>();
    
    // Constants
    private static final double BASE_TRANSFER_FEE = 0.0;
    private static final double EXTERNAL_TRANSFER_FEE_PERCENTAGE = 0.01; // 1%
    private static final double MAX_TRANSFER_FEE = 25.0;
    private static final double DEFAULT_DAILY_TRANSFER_LIMIT = 10000.0;
    
    /**
     * Constructor.
     */
    public TransferServiceImpl() {
        this.accountRepository = RepositoryFactory.getAccountRepository();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    @Override
    public TransferResult transfer(long sourceAccountNumber, long targetAccountNumber, double amount, String description)
            throws TransferException {
        LOGGER.info(String.format("Initiating transfer of %.2f from account %d to account %d",
                amount, sourceAccountNumber, targetAccountNumber));
        
        // Validate the transfer
        ValidationResult validationResult = validateTransfer(sourceAccountNumber, targetAccountNumber, amount);
        if (!validationResult.isValid()) {
            String errorMessage = validationResult.getFirstMessage("Invalid transfer");
            LOGGER.warning("Transfer validation failed: " + errorMessage);
            throw new TransferException(errorMessage, TransferErrorCode.INVALID_AMOUNT);
        }
        
        try {
            // Get source and target accounts
            Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountNumber);
            Optional<Account> targetAccountOpt = accountRepository.findById(targetAccountNumber);
            
            if (!sourceAccountOpt.isPresent()) {
                throw new TransferException("Source account not found: " + sourceAccountNumber, 
                        TransferErrorCode.ACCOUNT_NOT_FOUND);
            }
            
            if (!targetAccountOpt.isPresent()) {
                throw new TransferException("Target account not found: " + targetAccountNumber,
                        TransferErrorCode.ACCOUNT_NOT_FOUND);
            }
            
            Account sourceAccount = sourceAccountOpt.get();
            Account targetAccount = targetAccountOpt.get();
            
            // Calculate fee
            double fee = calculateTransferFee(sourceAccountNumber, targetAccountNumber, amount);
            double totalAmount = amount + fee;
            
            // Check if it exceeds daily limit
            double currentDailyTotal = getDailyTransferTotal(sourceAccountNumber);
            double dailyLimit = getDailyTransferLimit(sourceAccountNumber);
            
            if (currentDailyTotal + totalAmount > dailyLimit) {
                throw new TransferException(
                        String.format("Transfer would exceed daily limit of %.2f", dailyLimit),
                        TransferErrorCode.EXCEEDS_TRANSFER_LIMIT);
            }
            
            // Perform withdrawal from source account
            double sourceBalanceBefore = sourceAccount.getBalance();
            try {
                sourceAccount.withdraw(totalAmount);
            } catch (Exception e) {
                throw new TransferException("Insufficient funds for transfer: " + e.getMessage(), 
                        e, TransferErrorCode.INSUFFICIENT_FUNDS);
            }
            
            // Perform deposit to target account
            double targetBalanceBefore = targetAccount.getBalance();
            targetAccount.deposit(amount);
            
            // Update accounts in database
            sourceAccount = accountRepository.update(sourceAccount);
            targetAccount = accountRepository.update(targetAccount);
            
            // Record transfer in daily total
            recordDailyTransfer(sourceAccountNumber, totalAmount);
            
            // Create withdrawal transaction
            String withdrawalDescription = description != null && !description.isEmpty() 
                    ? description : "Transfer to account " + targetAccountNumber;
            Transaction sourceTransaction = new Transaction(
                    0, // Will be auto-generated
                    sourceAccountNumber,
                    totalAmount,
                    Transaction.Type.WITHDRAWAL,
                    new Date(),
                    withdrawalDescription + (fee > 0 ? String.format(" (includes fee: %.2f)", fee) : ""));
            sourceTransaction = transactionRepository.save(sourceTransaction);
            
            // Create deposit transaction
            String depositDescription = description != null && !description.isEmpty()
                    ? description : "Transfer from account " + sourceAccountNumber;
            Transaction targetTransaction = new Transaction(
                    0, // Will be auto-generated
                    targetAccountNumber,
                    amount,
                    Transaction.Type.DEPOSIT,
                    new Date(),
                    depositDescription);
            targetTransaction = transactionRepository.save(targetTransaction);
            
            // Generate transfer ID
            long transferId = transferIdGenerator.incrementAndGet();
            
            // Record the transfer
            recordTransfer(transferId, sourceAccountNumber, targetAccountNumber, amount, fee, 
                    new Date(), description != null ? description : "");
            
            // Build and return the result
            TransferResult result = new TransferResult.Builder()
                    .sourceAccountNumber(sourceAccountNumber)
                    .targetAccountNumber(targetAccountNumber)
                    .transferAmount(amount)
                    .feeAmount(fee)
                    .sourceAccountBalanceAfter(sourceAccount.getBalance())
                    .targetAccountBalanceAfter(targetAccount.getBalance())
                    .timestamp(new Date())
                    .description(description != null ? description : "")
                    .sourceTransaction(sourceTransaction)
                    .targetTransaction(targetTransaction)
                    .transferId(transferId)
                    .build();
            
            LOGGER.info(String.format("Transfer completed: %.2f from account %d to account %d (ID: %d)",
                    amount, sourceAccountNumber, targetAccountNumber, transferId));
            
            return result;
        } catch (TransferException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Transfer failed: " + e.getMessage(), e);
            throw new TransferException("Transfer failed: " + e.getMessage(), 
                    e, TransferErrorCode.TRANSACTION_FAILED);
        }
    }
    
    @Override
    public ValidationResult validateTransfer(long sourceAccountNumber, long targetAccountNumber, double amount) {
        List<String> errors = new ArrayList<>();
        
        // Validate source and target accounts
        if (sourceAccountNumber == targetAccountNumber) {
            errors.add("Source and target accounts cannot be the same");
        }
        
        // Validate amount
        if (amount <= 0) {
            errors.add("Transfer amount must be positive");
        }
        
        // Get source and target accounts
        Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountNumber);
        Optional<Account> targetAccountOpt = accountRepository.findById(targetAccountNumber);
        
        // Validate source account exists
        if (!sourceAccountOpt.isPresent()) {
            errors.add("Source account not found: " + sourceAccountNumber);
        }
        
        // Validate target account exists
        if (!targetAccountOpt.isPresent()) {
            errors.add("Target account not found: " + targetAccountNumber);
        }
        
        // If both accounts exist, perform additional validation
        if (sourceAccountOpt.isPresent() && targetAccountOpt.isPresent()) {
            Account sourceAccount = sourceAccountOpt.get();
            Account targetAccount = targetAccountOpt.get();
            
            // Calculate fee
            double fee = calculateTransferFee(sourceAccountNumber, targetAccountNumber, amount);
            double totalAmount = amount + fee;
            
            // Check if source account has sufficient funds
            if (sourceAccount.getBalance() < totalAmount) {
                // For credit accounts, check if transaction exceeds credit limit
                if (sourceAccount instanceof CreditAccount) {
                    CreditAccount creditAccount = (CreditAccount) sourceAccount;
                    if (totalAmount > sourceAccount.getBalance() + creditAccount.getCreditLimit()) {
                        errors.add(String.format(
                                "Insufficient funds and exceeds credit limit (%.2f available + %.2f credit)",
                                sourceAccount.getBalance(), creditAccount.getCreditLimit()));
                    }
                } else {
                    errors.add(String.format("Insufficient funds (%.2f available, %.2f needed)",
                            sourceAccount.getBalance(), totalAmount));
                }
            }
            
            // Check if it exceeds daily limit
            double currentDailyTotal = getDailyTransferTotal(sourceAccountNumber);
            double dailyLimit = getDailyTransferLimit(sourceAccountNumber);
            
            if (currentDailyTotal + totalAmount > dailyLimit) {
                errors.add(String.format(
                        "Transfer would exceed daily limit (%.2f used, %.2f limit)",
                        currentDailyTotal, dailyLimit));
            }
        }
        
        // Return validation result
        if (errors.isEmpty()) {
            double fee = calculateTransferFee(sourceAccountNumber, targetAccountNumber, amount);
            String message = String.format("Transfer validated successfully. Fee: %.2f", fee);
            return ValidationResult.success(message);
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    @Override
    public double calculateTransferFee(long sourceAccountNumber, long targetAccountNumber, double amount) {
        // For now, we use a simple fee calculation:
        // - No fee for transfers between accounts with the same owner
        // - Small percentage fee for external transfers, capped at a maximum amount
        
        // Check if both accounts belong to the same owner
        try {
            Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountNumber);
            Optional<Account> targetAccountOpt = accountRepository.findById(targetAccountNumber);
            
            if (sourceAccountOpt.isPresent() && targetAccountOpt.isPresent()) {
                Account sourceAccount = sourceAccountOpt.get();
                Account targetAccount = targetAccountOpt.get();
                
                // If same owner, no fee
                if (sourceAccount.getAccountHolder().getUserId().equals(
                        targetAccount.getAccountHolder().getUserId())) {
                    return BASE_TRANSFER_FEE;
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error determining transfer fee: " + e.getMessage());
            // Fall back to external transfer fee calculation
        }
        
        // Calculate fee based on percentage, capped at maximum
        double fee = amount * EXTERNAL_TRANSFER_FEE_PERCENTAGE;
        return Math.min(fee, MAX_TRANSFER_FEE);
    }
    
    @Override
    public double getDailyTransferLimit(long accountNumber) {
        // For now, return a default limit
        // In a real application, this would depend on the account type, customer status, etc.
        return DEFAULT_DAILY_TRANSFER_LIMIT;
    }
    
    @Override
    public double getRemainingDailyTransferAmount(long accountNumber) {
        double dailyLimit = getDailyTransferLimit(accountNumber);
        double used = getDailyTransferTotal(accountNumber);
        return Math.max(0, dailyLimit - used);
    }
    
    /**
     * Get the total amount transferred today from an account.
     * 
     * @param accountNumber The account number
     * @return The total amount transferred today
     */
    private double getDailyTransferTotal(long accountNumber) {
        String today = getDateKey(new Date());
        Map<String, Double> accountDailyAmounts = dailyTransferAmounts.get(accountNumber);
        
        if (accountDailyAmounts == null || !accountDailyAmounts.containsKey(today)) {
            return 0.0;
        }
        
        return accountDailyAmounts.get(today);
    }
    
    /**
     * Record a transfer in the daily total for an account.
     * 
     * @param accountNumber The account number
     * @param amount The transfer amount
     */
    private void recordDailyTransfer(long accountNumber, double amount) {
        String today = getDateKey(new Date());
        Map<String, Double> accountDailyAmounts = dailyTransferAmounts.computeIfAbsent(
                accountNumber, k -> new HashMap<>());
        
        double currentTotal = accountDailyAmounts.getOrDefault(today, 0.0);
        accountDailyAmounts.put(today, currentTotal + amount);
    }
    
    /**
     * Get a date key in the format 'YYYY-MM-DD'.
     * 
     * @param date The date
     * @return The date key
     */
    private String getDateKey(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }
    
    /**
     * Record a transfer between accounts.
     * 
     * @param transferId The transfer ID
     * @param sourceAccountNumber The source account number
     * @param targetAccountNumber The target account number
     * @param amount The transfer amount
     * @param fee The fee amount
     * @param timestamp The timestamp
     * @param description The description
     */
    private void recordTransfer(long transferId, long sourceAccountNumber, long targetAccountNumber,
                              double amount, double fee, Date timestamp, String description) {
        // Create transfer records for both accounts
        TransferRecord sourceRecord = new TransferRecord(
                transferId, sourceAccountNumber, targetAccountNumber, amount, fee, timestamp, description, true);
        TransferRecord targetRecord = new TransferRecord(
                transferId, sourceAccountNumber, targetAccountNumber, amount, fee, timestamp, description, false);
        
        // Add to source account's transfer history
        List<TransferRecord> sourceTransfers = transferRecordsByAccount.computeIfAbsent(
                sourceAccountNumber, k -> new ArrayList<>());
        sourceTransfers.add(sourceRecord);
        
        // Add to target account's transfer history
        List<TransferRecord> targetTransfers = transferRecordsByAccount.computeIfAbsent(
                targetAccountNumber, k -> new ArrayList<>());
        targetTransfers.add(targetRecord);
    }
    
    @Override
    public List<TransferRecord> getTransferHistory(long accountNumber) {
        List<TransferRecord> records = transferRecordsByAccount.getOrDefault(accountNumber, Collections.emptyList());
        
        // Sort by timestamp, newest first
        return records.stream()
                .sorted(Comparator.comparing(TransferRecord::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransferRecord> getTransferHistory(long accountNumber, Date startDate, Date endDate) {
        List<TransferRecord> allRecords = getTransferHistory(accountNumber);
        
        // Filter by date range
        return allRecords.stream()
                .filter(record -> !record.getTimestamp().before(startDate) && !record.getTimestamp().after(endDate))
                .collect(Collectors.toList());
    }
    
    @Override
    public ScheduledTransfer scheduleTransfer(long sourceAccountNumber, long targetAccountNumber,
                                          double amount, String description, Date scheduledDate)
            throws TransferException {
        LOGGER.info(String.format("Scheduling transfer of %.2f from account %d to account %d for %s",
                amount, sourceAccountNumber, targetAccountNumber, scheduledDate));
        
        // Validate the transfer (except for balance which may change by the scheduled date)
        List<String> errors = new ArrayList<>();
        
        // Validate source and target accounts
        if (sourceAccountNumber == targetAccountNumber) {
            errors.add("Source and target accounts cannot be the same");
        }
        
        // Validate amount
        if (amount <= 0) {
            errors.add("Transfer amount must be positive");
        }
        
        // Validate scheduled date (must be in the future)
        if (scheduledDate == null || scheduledDate.before(new Date())) {
            errors.add("Scheduled date must be in the future");
        }
        
        // Get source and target accounts
        Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountNumber);
        Optional<Account> targetAccountOpt = accountRepository.findById(targetAccountNumber);
        
        // Validate source account exists
        if (!sourceAccountOpt.isPresent()) {
            errors.add("Source account not found: " + sourceAccountNumber);
        }
        
        // Validate target account exists
        if (!targetAccountOpt.isPresent()) {
            errors.add("Target account not found: " + targetAccountNumber);
        }
        
        if (!errors.isEmpty()) {
            throw new TransferException(String.join("; ", errors));
        }
        
        try {
            // Generate scheduled transfer ID
            long scheduledTransferId = transferIdGenerator.incrementAndGet();
            
            // Create scheduled transfer
            ScheduledTransfer scheduledTransfer = new ScheduledTransfer(
                    scheduledTransferId,
                    sourceAccountNumber,
                    targetAccountNumber,
                    amount,
                    description != null ? description : "",
                    scheduledDate,
                    new Date(),
                    ScheduledTransferStatus.PENDING);
            
            // Store scheduled transfer
            scheduledTransfers.put(scheduledTransferId, scheduledTransfer);
            
            // Add to source account's scheduled transfers
            List<Long> sourceScheduled = scheduledTransfersByAccount.computeIfAbsent(
                    sourceAccountNumber, k -> new ArrayList<>());
            sourceScheduled.add(scheduledTransferId);
            
            // Add to target account's scheduled transfers
            List<Long> targetScheduled = scheduledTransfersByAccount.computeIfAbsent(
                    targetAccountNumber, k -> new ArrayList<>());
            targetScheduled.add(scheduledTransferId);
            
            LOGGER.info(String.format("Scheduled transfer created: ID %d from account %d to account %d for %s",
                    scheduledTransferId, sourceAccountNumber, targetAccountNumber, scheduledDate));
            
            return scheduledTransfer;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling transfer: " + e.getMessage(), e);
            throw new TransferException("Error scheduling transfer: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ScheduledTransfer> getScheduledTransfers(long accountNumber) {
        List<Long> transferIds = scheduledTransfersByAccount.getOrDefault(accountNumber, Collections.emptyList());
        
        // Get scheduled transfers and filter out non-pending ones
        return transferIds.stream()
                .map(scheduledTransfers::get)
                .filter(Objects::nonNull)
                .filter(ScheduledTransfer::isPending)
                .sorted(Comparator.comparing(ScheduledTransfer::getScheduledDate))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean cancelScheduledTransfer(long transferId) {
        ScheduledTransfer scheduledTransfer = scheduledTransfers.get(transferId);
        
        if (scheduledTransfer != null && scheduledTransfer.isPending()) {
            // Update status to cancelled
            scheduledTransfer.setStatus(ScheduledTransferStatus.CANCELLED);
            
            LOGGER.info("Cancelled scheduled transfer: " + transferId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Process due scheduled transfers.
     * This method should be called periodically, e.g., by a scheduler.
     * 
     * @return The number of transfers processed
     */
    public int processDueScheduledTransfers() {
        LOGGER.info("Processing due scheduled transfers");
        
        List<ScheduledTransfer> dueTransfers = scheduledTransfers.values().stream()
                .filter(ScheduledTransfer::isDue)
                .collect(Collectors.toList());
        
        int processed = 0;
        
        for (ScheduledTransfer scheduledTransfer : dueTransfers) {
            try {
                // Execute the transfer
                TransferResult result = transfer(
                        scheduledTransfer.getSourceAccountNumber(),
                        scheduledTransfer.getTargetAccountNumber(),
                        scheduledTransfer.getAmount(),
                        scheduledTransfer.getDescription() + " (scheduled)");
                
                // Update status to completed
                scheduledTransfer.setStatus(ScheduledTransferStatus.COMPLETED);
                
                LOGGER.info(String.format("Processed scheduled transfer %d: %.2f from %d to %d",
                        scheduledTransfer.getScheduledTransferId(),
                        scheduledTransfer.getAmount(),
                        scheduledTransfer.getSourceAccountNumber(),
                        scheduledTransfer.getTargetAccountNumber()));
                
                processed++;
            } catch (Exception e) {
                // Update status to failed
                scheduledTransfer.setStatus(ScheduledTransferStatus.FAILED);
                
                LOGGER.log(Level.SEVERE, String.format(
                        "Failed to process scheduled transfer %d: %s",
                        scheduledTransfer.getScheduledTransferId(), e.getMessage()), e);
            }
        }
        
        LOGGER.info("Processed " + processed + " scheduled transfers");
        return processed;
    }
}