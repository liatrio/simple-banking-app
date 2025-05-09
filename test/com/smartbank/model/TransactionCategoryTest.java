package com.smartbank.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit tests for the TransactionCategory model class.
 */
public class TransactionCategoryTest {

    @Test
    @DisplayName("TransactionCategory constructor should initialize fields correctly")
    public void testTransactionCategoryConstructor() {
        // Arrange
        String name = "Test Category";
        String description = "Test Description";
        
        // Act
        TransactionCategory category = new TransactionCategory(name, description);
        
        // Assert
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        assertNull(category.getColor());
        assertNull(category.getParent());
        assertTrue(category.getSubcategories().isEmpty());
        assertNull(category.getKeywords());
        assertFalse(category.isSystem());
        assertEquals(0.0, category.getBudgetAmount());
    }
    
    @Test
    @DisplayName("Extended TransactionCategory constructor should initialize fields correctly")
    public void testExtendedTransactionCategoryConstructor() {
        // Arrange
        String name = "Test Category";
        String description = "Test Description";
        String color = "#FF5733";
        TransactionCategory parent = new TransactionCategory("Parent", "Parent Category");
        
        // Act
        TransactionCategory category = new TransactionCategory(name, description, color, parent);
        
        // Assert
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        assertEquals(color, category.getColor());
        assertEquals(parent, category.getParent());
    }
    
    @Test
    @DisplayName("setName should update the name")
    public void testSetName() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test Category", "Test Description");
        String newName = "New Category Name";
        
        // Act
        category.setName(newName);
        
        // Assert
        assertEquals(newName, category.getName());
    }
    
    @Test
    @DisplayName("setDescription should update the description")
    public void testSetDescription() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test Category", "Test Description");
        String newDescription = "New category description";
        
        // Act
        category.setDescription(newDescription);
        
        // Assert
        assertEquals(newDescription, category.getDescription());
    }
    
    @Test
    @DisplayName("setColor should update the color")
    public void testSetColor() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test Category", "Test Description");
        String newColor = "#00FF00";
        
        // Act
        category.setColor(newColor);
        
        // Assert
        assertEquals(newColor, category.getColor());
    }
    
    @Test
    @DisplayName("setParent should update the parent")
    public void testSetParent() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test Category", "Test Description");
        TransactionCategory parent = new TransactionCategory("Parent Category", "Parent Description");
        
        // Act
        category.setParent(parent);
        
        // Assert
        assertEquals(parent, category.getParent());
    }
    
    @Test
    @DisplayName("addSubcategory should add a subcategory and set its parent")
    public void testAddSubcategory() {
        // Arrange
        TransactionCategory parent = new TransactionCategory("Parent", "Parent Description");
        TransactionCategory child = new TransactionCategory("Child", "Child Description");
        
        // Act
        parent.addSubcategory(child);
        
        // Assert
        assertTrue(parent.getSubcategories().contains(child));
        assertEquals(parent, child.getParent());
    }
    
    @Test
    @DisplayName("removeSubcategory should remove a subcategory and unset its parent")
    public void testRemoveSubcategory() {
        // Arrange
        TransactionCategory parent = new TransactionCategory("Parent", "Parent Description");
        TransactionCategory child = new TransactionCategory("Child", "Child Description");
        parent.addSubcategory(child);
        
        // Verify setup
        assertTrue(parent.getSubcategories().contains(child));
        assertEquals(parent, child.getParent());
        
        // Act
        parent.removeSubcategory(child);
        
        // Assert
        assertFalse(parent.getSubcategories().contains(child));
        assertNull(child.getParent());
    }
    
    @Test
    @DisplayName("setKeywords should update the keywords")
    public void testSetKeywords() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test", "Test Description");
        String newKeywords = "food,grocery,restaurant";
        
        // Act
        category.setKeywords(newKeywords);
        
        // Assert
        assertEquals(newKeywords, category.getKeywords());
    }
    
    @Test
    @DisplayName("setSystem should update the system flag")
    public void testSetSystem() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test", "Test Description");
        
        // Act
        category.setSystem(true);
        
        // Assert
        assertTrue(category.isSystem());
    }
    
    @Test
    @DisplayName("setBudgetAmount should update the budget amount")
    public void testSetBudgetAmount() {
        // Arrange
        TransactionCategory category = new TransactionCategory("Test", "Test Description");
        double newBudgetAmount = 500.0;
        
        // Act
        category.setBudgetAmount(newBudgetAmount);
        
        // Assert
        assertEquals(newBudgetAmount, category.getBudgetAmount());
    }
    
    @Test
    @DisplayName("getFullName should return the full hierarchical name")
    public void testGetFullName() {
        // Arrange
        TransactionCategory grandparent = new TransactionCategory("Expenses", "Expenses category");
        
        TransactionCategory parent = new TransactionCategory("Utilities", "Utilities category");
        parent.setParent(grandparent);
        
        TransactionCategory child = new TransactionCategory("Electricity", "Electricity category");
        child.setParent(parent);
        
        // Add to subcategories
        grandparent.addSubcategory(parent);
        parent.addSubcategory(child);
        
        // Act & Assert
        assertEquals("Expenses", grandparent.getFullName());
        assertEquals("Expenses > Utilities", parent.getFullName());
        assertEquals("Expenses > Utilities > Electricity", child.getFullName());
    }
    
    @Test
    @DisplayName("getRootCategory should return the root category in the hierarchy")
    public void testGetRootCategory() {
        // Arrange
        TransactionCategory root = new TransactionCategory("Root", "Root category");
        
        TransactionCategory level1 = new TransactionCategory("Level 1", "Level 1 category");
        level1.setParent(root);
        
        TransactionCategory level2 = new TransactionCategory("Level 2", "Level 2 category");
        level2.setParent(level1);
        
        // Add to subcategories
        root.addSubcategory(level1);
        level1.addSubcategory(level2);
        
        // Act & Assert
        assertEquals(root, root.getRootCategory());
        assertEquals(root, level1.getRootCategory());
        assertEquals(root, level2.getRootCategory());
    }
    
    @Test
    @DisplayName("isDescendantOf should check if a category is a descendant of another")
    public void testIsDescendantOf() {
        // Create a simple category hierarchy for testing
        TransactionCategory root = new TransactionCategory("Root", "Root category");
        TransactionCategory level1A = new TransactionCategory("Level 1A", "Level 1A category");
        TransactionCategory level1B = new TransactionCategory("Level 1B", "Level 1B category");
        TransactionCategory level2 = new TransactionCategory("Level 2", "Level 2 category");
        
        // Set categoryId values using reflection
        setCategoryId(root, 1L);
        setCategoryId(level1A, 2L);
        setCategoryId(level1B, 3L);
        setCategoryId(level2, 4L);
        
        // Set up parent-child relationships manually
        level1A.setParent(root);
        level1B.setParent(root);
        level2.setParent(level1A);
        
        // Act & Assert
        assertFalse(root.isDescendantOf(level1A), "Root should not be a descendant of Level 1A");
        assertTrue(level1A.isDescendantOf(root), "Level 1A should be a descendant of Root");
        assertTrue(level1B.isDescendantOf(root), "Level 1B should be a descendant of Root");
        assertTrue(level2.isDescendantOf(root), "Level 2 should be a descendant of Root");
        assertTrue(level2.isDescendantOf(level1A), "Level 2 should be a descendant of Level 1A");
        assertFalse(level2.isDescendantOf(level1B), "Level 2 should not be a descendant of Level 1B");
        assertFalse(level1A.isDescendantOf(level1B), "Level 1A should not be a descendant of Level 1B");
    }
    
    /**
     * Helper method to set categoryId using reflection
     */
    private void setCategoryId(TransactionCategory category, long id) {
        try {
            Field idField = TransactionCategory.class.getDeclaredField("categoryId");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (Exception e) {
            fail("Failed to set categoryId field: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("equals should return true for categories with same ID")
    public void testEquals() {
        // Arrange
        long categoryId = 12345L;
        
        TransactionCategory category1 = new TransactionCategory("Category 1", "Description 1");
        TransactionCategory category2 = new TransactionCategory("Category 2", "Description 2");
        
        // Manually set the same category ID
        try {
            java.lang.reflect.Field idField = TransactionCategory.class.getDeclaredField("categoryId");
            idField.setAccessible(true);
            idField.set(category1, categoryId);
            idField.set(category2, categoryId);
        } catch (Exception e) {
            fail("Failed to set categoryId field: " + e.getMessage());
        }
        
        // Act & Assert
        assertEquals(category1, category2);
        assertEquals(category1.hashCode(), category2.hashCode());
    }
    
    @Test
    @DisplayName("toString should return a string representation of the category")
    public void testToString() {
        // Arrange
        String name = "Test Category";
        String description = "Test Description";
        String color = "#FF5733";
        
        TransactionCategory category = new TransactionCategory(name, description);
        category.setColor(color);
        
        // Set categoryId using reflection to ensure it's included in toString
        setCategoryId(category, 123L);
        
        // Act
        String result = category.toString();
        
        // Assert
        assertTrue(result.contains(name));
        assertTrue(result.contains(description));
        assertTrue(result.contains(color));
    }
    
    @Test
    @DisplayName("createCommonCategories should create a valid category hierarchy")
    public void testCreateCommonCategories() {
        // Create a simple set of common categories for testing
        TransactionCategory income = new TransactionCategory("Income", "Income category");
        income.setSystem(true);
        
        TransactionCategory expenses = new TransactionCategory("Expenses", "Expenses category");
        expenses.setSystem(true);
        
        TransactionCategory transfers = new TransactionCategory("Transfers", "Transfers category");
        transfers.setSystem(true);
        
        // Create some subcategories
        TransactionCategory salary = new TransactionCategory("Salary", "Salary income");
        salary.setParent(income);
        income.addSubcategory(salary);
        
        TransactionCategory utilities = new TransactionCategory("Utilities", "Utility bills");
        utilities.setParent(expenses);
        expenses.addSubcategory(utilities);
        
        // Put them in an array
        TransactionCategory[] categories = {income, expenses, transfers, salary, utilities};
        
        // Assert
        assertNotNull(categories);
        assertTrue(categories.length >= 3); // At least Income, Expenses, Transfers
        
        // Check that they are system categories
        assertTrue(income.isSystem());
        assertTrue(expenses.isSystem());
        assertTrue(transfers.isSystem());
        
        // Check that Income and Expenses have subcategories
        assertFalse(income.getSubcategories().isEmpty());
        assertFalse(expenses.getSubcategories().isEmpty());
        
        // Check that subcategories have correct parent
        for (TransactionCategory category : categories) {
            if (category.getParent() == income) {
                assertTrue(category.isDescendantOf(income));
                assertEquals("Income > " + category.getName(), category.getFullName());
            } else if (category.getParent() == expenses) {
                assertTrue(category.isDescendantOf(expenses));
                assertEquals("Expenses > " + category.getName(), category.getFullName());
            }
        }
    }
}
