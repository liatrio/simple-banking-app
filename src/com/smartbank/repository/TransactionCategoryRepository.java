package com.smartbank.repository;

import com.smartbank.model.TransactionCategory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for transaction category operations.
 */
public interface TransactionCategoryRepository extends Repository<TransactionCategory, Long> {
    
    /**
     * Find a category by its name.
     * 
     * @param name The category name
     * @return The category, if found
     */
    Optional<TransactionCategory> findByName(String name);
    
    /**
     * Find top-level categories (categories with no parent).
     * 
     * @return List of root categories
     */
    List<TransactionCategory> findRootCategories();
    
    /**
     * Find subcategories of a given parent category.
     * 
     * @param parentId The parent category ID
     * @return List of subcategories
     */
    List<TransactionCategory> findByParentId(Long parentId);
    
    /**
     * Find all system categories.
     * 
     * @return List of system categories
     */
    List<TransactionCategory> findSystemCategories();
    
    /**
     * Find categories containing specific keywords in their keywords field.
     * 
     * @param keyword The keyword to match
     * @return List of matching categories
     */
    List<TransactionCategory> findByKeyword(String keyword);
}