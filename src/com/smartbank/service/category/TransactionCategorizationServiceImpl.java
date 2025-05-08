package com.smartbank.service.category;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.service.ServiceFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the TransactionCategorizationService.
 */
public class TransactionCategorizationServiceImpl implements TransactionCategorizationService {
    private static final Logger LOGGER = Logger.getLogger(TransactionCategorizationServiceImpl.class.getName());
    
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategorizationRuleService categorizationRuleService;
    
    /**
     * Constructor that initializes the repositories and services.
     */
    public TransactionCategorizationServiceImpl() {
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
        this.categoryService = ServiceFactory.getCategoryService();
        this.categorizationRuleService = ServiceFactory.getCategorizationRuleService();
    }
    
    @Override
    public Transaction assignCategory(long transactionId, long categoryId, boolean isAutomatic) 
            throws CategoryException {
        
        // Find the transaction
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (!transactionOpt.isPresent()) {
            throw new CategoryException("Transaction with ID " + transactionId + " not found");
        }
        
        // Find the category
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        // Assign the category
        Transaction transaction = transactionOpt.get();
        transaction.setCategory(categoryOpt.get());
        transaction.setCategorizedAutomatically(isAutomatic);
        
        // Save the updated transaction
        try {
            return transactionRepository.save(transaction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning category: " + e.getMessage(), e);
            throw new CategoryException("Failed to assign category: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Transaction removeCategory(long transactionId) throws CategoryException {
        // Find the transaction
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (!transactionOpt.isPresent()) {
            throw new CategoryException("Transaction with ID " + transactionId + " not found");
        }
        
        // Remove the category
        Transaction transaction = transactionOpt.get();
        transaction.setCategory(null);
        transaction.setCategorizedAutomatically(false);
        
        // Save the updated transaction
        try {
            return transactionRepository.save(transaction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing category: " + e.getMessage(), e);
            throw new CategoryException("Failed to remove category: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int categorizeAll() {
        // Get all uncategorized transactions
        List<Transaction> uncategorizedTransactions = transactionRepository.findAll()
                .stream()
                .filter(t -> !t.isCategorized())
                .collect(Collectors.toList());
        
        if (uncategorizedTransactions.isEmpty()) {
            return 0;
        }
        
        // Apply categorization rules
        List<Transaction> categorizedTransactions = categorizationRuleService.categorizeTransactions(uncategorizedTransactions);
        
        // Count how many were actually categorized
        int categorizedCount = 0;
        for (Transaction transaction : categorizedTransactions) {
            if (transaction.isCategorized() && transaction.isCategorizedAutomatically()) {
                categorizedCount++;
            }
        }
        
        return categorizedCount;
    }
    
    @Override
    public int reassignCategory(long fromCategoryId, long toCategoryId) throws CategoryException {
        // Verify both categories exist
        Optional<TransactionCategory> fromCategoryOpt = categoryService.getCategoryById(fromCategoryId);
        if (!fromCategoryOpt.isPresent()) {
            throw new CategoryException("Source category with ID " + fromCategoryId + " not found");
        }
        
        Optional<TransactionCategory> toCategoryOpt = categoryService.getCategoryById(toCategoryId);
        if (!toCategoryOpt.isPresent()) {
            throw new CategoryException("Target category with ID " + toCategoryId + " not found");
        }
        
        // Get all transactions with the source category
        List<Transaction> transactions = getTransactionsByCategory(fromCategoryId);
        
        if (transactions.isEmpty()) {
            return 0;
        }
        
        TransactionCategory toCategory = toCategoryOpt.get();
        
        // Update each transaction
        int reassignedCount = 0;
        for (Transaction transaction : transactions) {
            transaction.setCategory(toCategory);
            transaction.setCategorizedAutomatically(false); // Manual reassignment
            
            try {
                transactionRepository.save(transaction);
                reassignedCount++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error reassigning category for transaction " + 
                        transaction.getTransactionId() + ": " + e.getMessage(), e);
            }
        }
        
        return reassignedCount;
    }
    
    @Override
    public List<Transaction> getTransactionsByCategory(long categoryId) {
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        return allTransactions.stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    
    @Override
    public int applyCategoryByPattern(String pattern, long categoryId) throws CategoryException {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new CategoryException("Pattern cannot be empty");
        }
        
        // Verify category exists
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        TransactionCategory category = categoryOpt.get();
        
        // Compile the pattern
        Pattern regex;
        try {
            regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            throw new CategoryException("Invalid regex pattern: " + e.getMessage());
        }
        
        // Get all transactions
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // Apply category to matching transactions
        int appliedCount = 0;
        for (Transaction transaction : allTransactions) {
            if (transaction.getDescription() != null && 
                regex.matcher(transaction.getDescription()).find()) {
                
                transaction.setCategory(category);
                transaction.setCategorizedAutomatically(false); // Manual assignment
                
                try {
                    transactionRepository.save(transaction);
                    appliedCount++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error applying category for transaction " + 
                            transaction.getTransactionId() + ": " + e.getMessage(), e);
                }
            }
        }
        
        // If any transactions were categorized, add pattern as a keyword
        if (appliedCount > 0) {
            try {
                categorizationRuleService.addKeyword(categoryId, pattern);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error adding keyword to category: " + e.getMessage(), e);
            }
        }
        
        return appliedCount;
    }
    
    @Override
    public int applyCategoryByMerchant(String merchantName, long categoryId) throws CategoryException {
        if (merchantName == null || merchantName.trim().isEmpty()) {
            throw new CategoryException("Merchant name cannot be empty");
        }
        
        // Verify category exists
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        TransactionCategory category = categoryOpt.get();
        
        // Get all transactions
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // Apply category to matching transactions
        int appliedCount = 0;
        for (Transaction transaction : allTransactions) {
            if ((transaction.getMerchantName() != null && 
                 transaction.getMerchantName().equalsIgnoreCase(merchantName)) ||
                (transaction.getDescription() != null && 
                 transaction.getDescription().toLowerCase().contains(merchantName.toLowerCase()))) {
                
                transaction.setCategory(category);
                transaction.setCategorizedAutomatically(false); // Manual assignment
                
                try {
                    transactionRepository.save(transaction);
                    appliedCount++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error applying category for transaction " + 
                            transaction.getTransactionId() + ": " + e.getMessage(), e);
                }
            }
        }
        
        // If any transactions were categorized, add merchant name as a keyword
        if (appliedCount > 0) {
            try {
                categorizationRuleService.addKeyword(categoryId, merchantName);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error adding keyword to category: " + e.getMessage(), e);
            }
        }
        
        return appliedCount;
    }
    
    @Override
    public Map<Long, CategorySummary> getTransactionSummaryByCategory() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // Group transactions by category
        Map<Long, List<Transaction>> transactionsByCategory = new HashMap<>();
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getCategory() != null) {
                long categoryId = transaction.getCategory().getCategoryId();
                if (!transactionsByCategory.containsKey(categoryId)) {
                    transactionsByCategory.put(categoryId, new ArrayList<>());
                }
                transactionsByCategory.get(categoryId).add(transaction);
            }
        }
        
        // Calculate summary for each category
        Map<Long, CategorySummary> summaries = new HashMap<>();
        
        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            
            // Get category name
            String categoryName = "Unknown";
            Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                categoryName = categoryOpt.get().getName();
            }
            
            // Calculate total amount
            double totalAmount = categoryTransactions.stream()
                    .mapToDouble(Transaction::getSignedAmount)
                    .sum();
            
            // Create summary
            CategorySummary summary = new CategorySummary(
                    categoryId, categoryName, totalAmount, categoryTransactions.size());
            
            summaries.put(categoryId, summary);
        }
        
        return summaries;
    }
    
    @Override
    public boolean createRuleFromTransaction(long transactionId, long categoryId) throws CategoryException {
        // Verify transaction exists
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (!transactionOpt.isPresent()) {
            throw new CategoryException("Transaction with ID " + transactionId + " not found");
        }
        
        // Verify category exists
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Extract potential keywords from transaction
        Set<String> potentialKeywords = new HashSet<>();
        
        // Add merchant name if available
        if (transaction.getMerchantName() != null && !transaction.getMerchantName().trim().isEmpty()) {
            potentialKeywords.add(transaction.getMerchantName().trim().toLowerCase());
        }
        
        // Extract words from description
        if (transaction.getDescription() != null) {
            String description = transaction.getDescription().toLowerCase();
            
            // Skip common words
            Set<String> stopWords = new HashSet<>(Arrays.asList(
                    "the", "and", "that", "have", "for", "not", "with", "you", "this", "but"));
            
            // Split description into words
            String[] words = description.split("\\s+");
            
            // Add significant words
            for (String word : words) {
                // Clean up the word (remove non-alphanumeric)
                word = word.replaceAll("[^a-zA-Z0-9]", "").trim();
                
                if (word.isEmpty() || stopWords.contains(word) || word.length() <= 3) {
                    continue;
                }
                
                potentialKeywords.add(word);
            }
        }
        
        if (potentialKeywords.isEmpty()) {
            return false;
        }
        
        // Add keywords to category
        boolean success = false;
        for (String keyword : potentialKeywords) {
            try {
                categorizationRuleService.addKeyword(categoryId, keyword);
                success = true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error adding keyword '" + keyword + 
                        "' to category: " + e.getMessage(), e);
            }
        }
        
        return success;
    }
}