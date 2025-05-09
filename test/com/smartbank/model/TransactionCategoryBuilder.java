package com.smartbank.model;

import com.smartbank.util.fixtures.TestDataFactory;
import com.smartbank.util.fixtures.TestObjectBuilder;

/**
 * Builder for creating TransactionCategory instances for testing.
 * This builder allows creating TransactionCategory objects with default or custom values.
 */
public class TransactionCategoryBuilder extends TestObjectBuilder<TransactionCategory, TransactionCategoryBuilder> {
    
    private String name;
    private String description;
    private String color;
    private TransactionCategory parent;
    private String keywords;
    private boolean isSystem;
    private double budgetAmount;
    
    /**
     * Create a new TransactionCategoryBuilder with default values.
     */
    public TransactionCategoryBuilder() {
        this.name = TestDataFactory.randomString("Category");
        this.description = "Test category description";
        this.color = "#" + Integer.toHexString((int)(Math.random() * 0xFFFFFF));
        this.parent = null;
        this.keywords = "";
        this.isSystem = false;
        this.budgetAmount = 0.0;
    }
    
    /**
     * Set the name.
     * 
     * @param name The name
     * @return This builder
     */
    public TransactionCategoryBuilder withName(String name) {
        this.name = name;
        return self();
    }
    
    /**
     * Set the description.
     * 
     * @param description The description
     * @return This builder
     */
    public TransactionCategoryBuilder withDescription(String description) {
        this.description = description;
        return self();
    }
    
    /**
     * Set the color.
     * 
     * @param color The color in hexadecimal format (e.g. #FF5733)
     * @return This builder
     */
    public TransactionCategoryBuilder withColor(String color) {
        this.color = color;
        return self();
    }
    
    /**
     * Set the parent category.
     * 
     * @param parent The parent category
     * @return This builder
     */
    public TransactionCategoryBuilder withParent(TransactionCategory parent) {
        this.parent = parent;
        return self();
    }
    
    /**
     * Set the keywords for automatic categorization.
     * 
     * @param keywords The keywords
     * @return This builder
     */
    public TransactionCategoryBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return self();
    }
    
    /**
     * Set whether this is a system category.
     * 
     * @param isSystem Whether this is a system category
     * @return This builder
     */
    public TransactionCategoryBuilder withSystem(boolean isSystem) {
        this.isSystem = isSystem;
        return self();
    }
    
    /**
     * Set as a system category.
     * 
     * @return This builder
     */
    public TransactionCategoryBuilder asSystemCategory() {
        this.isSystem = true;
        return self();
    }
    
    /**
     * Set the budget amount.
     * 
     * @param budgetAmount The budget amount
     * @return This builder
     */
    public TransactionCategoryBuilder withBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
        return self();
    }
    
    /**
     * Build a TransactionCategory instance with the current builder state.
     * 
     * @return A new TransactionCategory instance
     */
    @Override
    public TransactionCategory build() {
        TransactionCategory category = new TransactionCategory(name, description, color, parent);
        
        category.setKeywords(keywords);
        category.setSystem(isSystem);
        category.setBudgetAmount(budgetAmount);
        
        return category;
    }
    
    /**
     * Create a set of common transaction categories for testing.
     * 
     * @return An array of common transaction categories
     */
    public static TransactionCategory[] createCommonCategories() {
        // Create parent categories
        TransactionCategory income = new TransactionCategoryBuilder()
                .withName("Income")
                .withDescription("Money received")
                .withColor("#4CAF50")
                .asSystemCategory()
                .build();
        
        TransactionCategory expenses = new TransactionCategoryBuilder()
                .withName("Expenses")
                .withDescription("Money spent")
                .withColor("#F44336")
                .asSystemCategory()
                .build();
        
        TransactionCategory transfers = new TransactionCategoryBuilder()
                .withName("Transfers")
                .withDescription("Money moved between accounts")
                .withColor("#2196F3")
                .asSystemCategory()
                .build();
        
        // Create subcategories for Income
        TransactionCategory salary = new TransactionCategoryBuilder()
                .withName("Salary")
                .withDescription("Regular employment income")
                .withColor("#66BB6A")
                .withParent(income)
                .withKeywords("payroll,salary,wage,deposit")
                .build();
        
        TransactionCategory interest = new TransactionCategoryBuilder()
                .withName("Interest")
                .withDescription("Interest earned on accounts")
                .withColor("#81C784")
                .withParent(income)
                .withKeywords("interest,dividend")
                .build();
        
        // Create subcategories for Expenses
        TransactionCategory bills = new TransactionCategoryBuilder()
                .withName("Bills")
                .withDescription("Regular monthly bills")
                .withColor("#E57373")
                .withParent(expenses)
                .withKeywords("bill,utility,payment")
                .build();
        
        TransactionCategory groceries = new TransactionCategoryBuilder()
                .withName("Groceries")
                .withDescription("Food and household items")
                .withColor("#EF5350")
                .withParent(expenses)
                .withKeywords("grocery,supermarket,food")
                .build();
        
        TransactionCategory dining = new TransactionCategoryBuilder()
                .withName("Dining")
                .withDescription("Restaurants and takeout")
                .withColor("#F44336")
                .withParent(expenses)
                .withKeywords("restaurant,cafe,dining")
                .build();
        
        // Add subcategories to parent categories
        income.addSubcategory(salary);
        income.addSubcategory(interest);
        
        expenses.addSubcategory(bills);
        expenses.addSubcategory(groceries);
        expenses.addSubcategory(dining);
        
        return new TransactionCategory[] {
            income, expenses, transfers, 
            salary, interest, 
            bills, groceries, dining
        };
    }
}
