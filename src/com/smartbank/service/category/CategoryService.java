package com.smartbank.service.category;

import com.smartbank.model.TransactionCategory;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing transaction categories.
 */
public interface CategoryService {
    
    /**
     * Get all categories.
     * 
     * @return List of all categories
     */
    List<TransactionCategory> getAllCategories();
    
    /**
     * Get category by ID.
     * 
     * @param categoryId The category ID
     * @return The category, if found
     */
    Optional<TransactionCategory> getCategoryById(long categoryId);
    
    /**
     * Get category by name.
     * 
     * @param name The category name
     * @return The category, if found
     */
    Optional<TransactionCategory> getCategoryByName(String name);
    
    /**
     * Create a new category.
     * 
     * @param category The category to create
     * @return The created category
     * @throws CategoryException if there's an issue creating the category
     */
    TransactionCategory createCategory(TransactionCategory category) throws CategoryException;
    
    /**
     * Update an existing category.
     * 
     * @param category The category to update
     * @return The updated category
     * @throws CategoryException if there's an issue updating the category
     */
    TransactionCategory updateCategory(TransactionCategory category) throws CategoryException;
    
    /**
     * Delete a category.
     * 
     * @param categoryId The ID of the category to delete
     * @throws CategoryException if there's an issue deleting the category
     */
    void deleteCategory(long categoryId) throws CategoryException;
    
    /**
     * Get all top-level categories (categories with no parent).
     * 
     * @return List of root categories
     */
    List<TransactionCategory> getRootCategories();
    
    /**
     * Get subcategories of a category.
     * 
     * @param parentId The parent category ID
     * @return List of subcategories
     */
    List<TransactionCategory> getSubcategories(long parentId);
    
    /**
     * Add a subcategory to a parent category.
     * 
     * @param parentId The parent category ID
     * @param subcategory The subcategory to add
     * @return The created subcategory
     * @throws CategoryException if there's an issue adding the subcategory
     */
    TransactionCategory addSubcategory(long parentId, TransactionCategory subcategory) throws CategoryException;
    
    /**
     * Find categories by keyword.
     * 
     * @param keyword The keyword to search for
     * @return List of matching categories
     */
    List<TransactionCategory> findCategoriesByKeyword(String keyword);
    
    /**
     * Initialize system default categories if they don't exist.
     * 
     * @return List of created system categories
     */
    List<TransactionCategory> initializeDefaultCategories();
    
    /**
     * Get all system categories.
     * 
     * @return List of system categories
     */
    List<TransactionCategory> getSystemCategories();
    
    /**
     * Get the "Uncategorized" category.
     * 
     * @return The "Uncategorized" category
     */
    TransactionCategory getUncategorizedCategory();
    
    /**
     * Update budget amount for a category.
     * 
     * @param categoryId The category ID
     * @param budgetAmount The budget amount to set
     * @return The updated category
     * @throws CategoryException if there's an issue updating the budget
     */
    TransactionCategory updateBudget(long categoryId, double budgetAmount) throws CategoryException;
}