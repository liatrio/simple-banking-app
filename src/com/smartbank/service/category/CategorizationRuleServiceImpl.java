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
 * Implementation of the CategorizationRuleService.
 */
public class CategorizationRuleServiceImpl implements CategorizationRuleService {
    private static final Logger LOGGER = Logger.getLogger(CategorizationRuleServiceImpl.class.getName());
    
    private final CategoryService categoryService;
    private final TransactionRepository transactionRepository;
    
    // Cache for keywords to avoid repeated parsing
    private Map<Long, List<String>> keywordCache = new HashMap<>();
    
    /**
     * Constructor that initializes the services and repositories.
     */
    public CategorizationRuleServiceImpl() {
        this.categoryService = ServiceFactory.getCategoryService();
        this.transactionRepository = RepositoryFactory.getTransactionRepository();
    }
    
    @Override
    public Transaction categorizeTransaction(Transaction transaction) {
        if (transaction == null || transaction.isCategorized()) {
            return transaction;
        }
        
        TransactionCategory matchingCategory = findBestMatchingCategory(transaction);
        if (matchingCategory != null) {
            transaction.setCategory(matchingCategory);
            transaction.setCategorizedAutomatically(true);
            
            // Try to extract merchant name from description
            extractMerchantName(transaction);
            
            // Check if this might be a recurring transaction
            detectRecurringPattern(transaction);
            
            try {
                // Save the categorized transaction
                return transactionRepository.save(transaction);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to save categorized transaction: " + e.getMessage(), e);
            }
        }
        
        return transaction;
    }
    
    @Override
    public List<Transaction> categorizeTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Filter transactions that need categorization
        List<Transaction> uncategorizedTransactions = transactions.stream()
                .filter(t -> !t.isCategorized())
                .collect(Collectors.toList());
        
        // Categorize each transaction
        for (Transaction transaction : uncategorizedTransactions) {
            categorizeTransaction(transaction);
        }
        
        return transactions;
    }
    
    @Override
    public TransactionCategory findBestMatchingCategory(Transaction transaction) {
        if (transaction == null || transaction.getDescription() == null) {
            return null;
        }
        
        String description = transaction.getDescription().toLowerCase();
        String merchantName = transaction.getMerchantName() != null ? 
                              transaction.getMerchantName().toLowerCase() : "";
        
        // Load all categories if keyword cache is empty
        if (keywordCache.isEmpty()) {
            loadKeywordCache();
        }
        
        // If we still have no keywords, try to ensure categories are properly initialized
        if (keywordCache.isEmpty()) {
            categoryService.initializeDefaultCategories();
            loadKeywordCache();
        }
        
        // If after initialization we still have no keywords, use a fallback category approach
        if (keywordCache.isEmpty()) {
            // Try to assign a default category based on transaction type
            List<TransactionCategory> allCategories = categoryService.getAllCategories();
            if (!allCategories.isEmpty()) {
                // Find a reasonable default category
                for (TransactionCategory category : allCategories) {
                    if (category.getName().equalsIgnoreCase("Miscellaneous") || 
                        category.getName().equalsIgnoreCase("Other")) {
                        return category;
                    }
                }
                // If no "Miscellaneous" category, just return the first one
                return allCategories.get(0);
            }
        }
        
        // Calculate match scores for each category
        Map<Long, Integer> matchScores = new HashMap<>();
        
        // Check each category's keywords for matches
        for (Map.Entry<Long, List<String>> entry : keywordCache.entrySet()) {
            Long categoryId = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                // Check both description and merchant name for matches
                if (description.contains(keyword.toLowerCase()) || 
                    (!merchantName.isEmpty() && merchantName.contains(keyword.toLowerCase()))) {
                    // Increment score for this category
                    matchScores.put(categoryId, matchScores.getOrDefault(categoryId, 0) + 1);
                }
            }
        }
        
        // Find category with highest match score
        Long bestMatchCategoryId = null;
        int highestScore = 0;
        
        for (Map.Entry<Long, Integer> entry : matchScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestMatchCategoryId = entry.getKey();
            }
        }
        
        // Return best matching category, or null if no matches
        if (bestMatchCategoryId != null && highestScore > 0) {
            Optional<TransactionCategory> category = categoryService.getCategoryById(bestMatchCategoryId);
            return category.orElse(null);
        }
        
        return null;
    }
    
    @Override
    public int trainCategorizationSystem() {
        int updatedCount = 0;
        
        try {
            // Get all transactions that have been manually categorized
            List<Transaction> manuallyCategorizeCategorized = transactionRepository.findAll().stream()
                    .filter(t -> t.isCategorized() && !t.isCategorizedAutomatically())
                    .collect(Collectors.toList());
            
            if (manuallyCategorizeCategorized.isEmpty()) {
                return 0;
            }
            
            // Group transactions by category
            Map<Long, List<Transaction>> transactionsByCategory = new HashMap<>();
            
            for (Transaction transaction : manuallyCategorizeCategorized) {
                long categoryId = transaction.getCategory().getCategoryId();
                if (!transactionsByCategory.containsKey(categoryId)) {
                    transactionsByCategory.put(categoryId, new ArrayList<>());
                }
                transactionsByCategory.get(categoryId).add(transaction);
            }
            
            // Extract common words from descriptions for each category
            for (Map.Entry<Long, List<Transaction>> entry : transactionsByCategory.entrySet()) {
                Long categoryId = entry.getKey();
                List<Transaction> categoryTransactions = entry.getValue();
                
                Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
                if (!categoryOpt.isPresent()) {
                    continue;
                }
                
                TransactionCategory category = categoryOpt.get();
                
                // Get existing keywords
                Set<String> existingKeywords = parseKeywords(category.getKeywords());
                
                // Extract common words from transaction descriptions
                Set<String> commonWords = extractCommonWords(categoryTransactions);
                
                // Add new keywords that aren't already in the category keywords
                boolean updated = false;
                for (String word : commonWords) {
                    if (!existingKeywords.contains(word) && word.length() > 3) {
                        existingKeywords.add(word);
                        updated = true;
                    }
                }
                
                // Update category keywords if new ones were added
                if (updated) {
                    try {
                        String newKeywords = String.join(",", existingKeywords);
                        category.setKeywords(newKeywords);
                        categoryService.updateCategory(category);
                        updatedCount++;
                    } catch (CategoryException e) {
                        LOGGER.log(Level.WARNING, "Failed to update category keywords: " + e.getMessage(), e);
                    }
                }
            }
            
            // Clear keyword cache so it will be reloaded with new keywords
            keywordCache.clear();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in training categorization system: " + e.getMessage(), e);
        }
        
        return updatedCount;
    }
    
    @Override
    public TransactionCategory addKeyword(long categoryId, String keyword) throws CategoryException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CategoryException("Keyword cannot be empty");
        }
        
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        TransactionCategory category = categoryOpt.get();
        Set<String> keywords = parseKeywords(category.getKeywords());
        
        // Add the new keyword if it doesn't already exist
        if (keywords.add(keyword.toLowerCase().trim())) {
            category.setKeywords(String.join(",", keywords));
            
            // Update the category
            TransactionCategory updatedCategory = categoryService.updateCategory(category);
            
            // Update the keyword cache
            if (keywordCache.containsKey(categoryId)) {
                keywordCache.put(categoryId, new ArrayList<>(keywords));
            }
            
            return updatedCategory;
        }
        
        // Keyword already exists
        return category;
    }
    
    @Override
    public TransactionCategory removeKeyword(long categoryId, String keyword) throws CategoryException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CategoryException("Keyword cannot be empty");
        }
        
        Optional<TransactionCategory> categoryOpt = categoryService.getCategoryById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        TransactionCategory category = categoryOpt.get();
        Set<String> keywords = parseKeywords(category.getKeywords());
        
        // Remove the keyword if it exists
        if (keywords.remove(keyword.toLowerCase().trim())) {
            category.setKeywords(String.join(",", keywords));
            
            // Update the category
            TransactionCategory updatedCategory = categoryService.updateCategory(category);
            
            // Update the keyword cache
            if (keywordCache.containsKey(categoryId)) {
                keywordCache.put(categoryId, new ArrayList<>(keywords));
            }
            
            return updatedCategory;
        }
        
        // Keyword doesn't exist
        return category;
    }
    
    @Override
    public Map<Long, List<String>> getAllKeywords() {
        // Load from cache or database
        if (keywordCache.isEmpty()) {
            loadKeywordCache();
        }
        
        // Create a copy to avoid modifying the cache
        Map<Long, List<String>> result = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : keywordCache.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        
        return result;
    }
    
    /**
     * Load all category keywords into the cache.
     */
    private void loadKeywordCache() {
        List<TransactionCategory> allCategories = categoryService.getAllCategories();
        
        for (TransactionCategory category : allCategories) {
            Set<String> keywords = parseKeywords(category.getKeywords());
            if (!keywords.isEmpty()) {
                keywordCache.put(category.getCategoryId(), new ArrayList<>(keywords));
            }
        }
    }
    
    /**
     * Parse a comma-separated keyword string into a set of keywords.
     * 
     * @param keywordsString The comma-separated keywords
     * @return Set of individual keywords
     */
    private Set<String> parseKeywords(String keywordsString) {
        Set<String> result = new HashSet<>();
        
        if (keywordsString == null || keywordsString.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = keywordsString.split(",");
        for (String part : parts) {
            String trimmed = part.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }
    
    /**
     * Extract common words from transaction descriptions.
     * 
     * @param transactions The transactions to analyze
     * @return Set of common words
     */
    private Set<String> extractCommonWords(List<Transaction> transactions) {
        // Skip common words like articles, prepositions, etc.
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
                "his", "from", "they", "she", "her", "will", "has", "been", "their", "what"));
        
        // Count frequency of each word across all descriptions
        Map<String, Integer> wordCounts = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            if (transaction.getDescription() == null) {
                continue;
            }
            
            String description = transaction.getDescription().toLowerCase();
            
            // Split description into words
            String[] words = description.split("\\s+");
            
            // Count each word
            for (String word : words) {
                // Clean up the word (remove non-alphanumeric)
                word = word.replaceAll("[^a-zA-Z0-9]", "").trim();
                
                if (word.isEmpty() || stopWords.contains(word) || word.length() <= 2) {
                    continue;
                }
                
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        
        // Get words that appear in at least 20% of transactions for this category
        int minOccurrences = Math.max(1, transactions.size() / 5);
        
        return wordCounts.entrySet().stream()
                .filter(e -> e.getValue() >= minOccurrences)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    /**
     * Attempt to extract merchant name from transaction description.
     * 
     * @param transaction The transaction to process
     */
    private void extractMerchantName(Transaction transaction) {
        if (transaction.getDescription() == null) {
            return;
        }
        
        String description = transaction.getDescription();
        
        // Common patterns for merchant names in transaction descriptions
        String[] patterns = {
            // "PURCHASE AUTHORIZED ON .* ([A-Z\\s]+)",
            "PURCHASE (?:.*) ([A-Za-z0-9\\s.]+)",
            "POS PURCHASE (?:.*) ([A-Za-z0-9\\s.]+)",
            "PAYMENT TO ([A-Za-z0-9\\s.]+)",
            "PYMT TO ([A-Za-z0-9\\s.]+)",
            "(?:DEBIT CARD PURCHASE) (.+)"
        };
        
        for (String patternStr : patterns) {
            java.util.regex.Pattern pattern = Pattern.compile(patternStr);
            java.util.regex.Matcher matcher = pattern.matcher(description);
            
            if (matcher.find() && matcher.groupCount() >= 1) {
                String merchantName = matcher.group(1).trim();
                if (!merchantName.isEmpty()) {
                    transaction.setMerchantName(merchantName);
                    return;
                }
            }
        }
        
        // If no pattern matches, try to use the first part of the description as merchant name
        String[] parts = description.split("\\s+");
        if (parts.length > 0) {
            String potentialMerchant = parts[0];
            if (potentialMerchant.length() > 3) {
                transaction.setMerchantName(potentialMerchant);
            }
        }
    }
    
    /**
     * Check if this transaction might be part of a recurring pattern.
     * 
     * @param transaction The transaction to analyze
     */
    private void detectRecurringPattern(Transaction transaction) {
        if (transaction.getDescription() == null || transaction.getMerchantName() == null) {
            return;
        }
        
        // Look for similar transactions in the past
        String merchantName = transaction.getMerchantName();
        Long accountNumber = transaction.getAccountNumber();
        Date currentDate = transaction.getTimestamp();
        
        // Get last 6 months of transactions for this account
        Calendar sixMonthsAgo = Calendar.getInstance();
        sixMonthsAgo.setTime(currentDate);
        sixMonthsAgo.add(Calendar.MONTH, -6);
        
        List<Transaction> accountTransactions = transactionRepository.findByAccountNumber(accountNumber);
        
        // Filter to transactions with same merchant and similar amount
        double amount = transaction.getAmount();
        double amountThreshold = amount * 0.1; // 10% threshold
        
        List<Transaction> similarTransactions = accountTransactions.stream()
                .filter(t -> 
                    t.getTransactionId() != transaction.getTransactionId() && 
                    t.getTimestamp().after(sixMonthsAgo.getTime()) &&
                    t.getType() == transaction.getType() &&
                    Math.abs(t.getAmount() - amount) <= amountThreshold &&
                    (t.getMerchantName() != null && t.getMerchantName().equals(merchantName) ||
                     t.getDescription() != null && t.getDescription().contains(merchantName))
                )
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
        
        // If we find at least 2 similar transactions, consider it recurring
        if (similarTransactions.size() >= 2) {
            transaction.setRecurring(true);
        }
    }
}