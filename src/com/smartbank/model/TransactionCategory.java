package com.smartbank.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a transaction category.
 * Categories can be hierarchical with parent-child relationships.
 */
@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long categoryId;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    // Color in hexadecimal format (e.g. #FF5733)
    @Column
    private String color;
    
    // For category hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TransactionCategory parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TransactionCategory> subcategories = new HashSet<>();
    
    // Keywords for automatic categorization
    @Column
    private String keywords;
    
    // If this is a system default category that shouldn't be deleted
    @Column(nullable = false)
    private boolean isSystem = false;
    
    // Default budget amount for this category
    @Column
    private double budgetAmount;
    
    // Bidirectional relationship with Transaction
    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions = new HashSet<>();
    
    // Default constructor required by JPA
    protected TransactionCategory() {
    }
    
    public TransactionCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public TransactionCategory(String name, String description, String color, TransactionCategory parent) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.parent = parent;
    }

    // Getters and setters
    public long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TransactionCategory getParent() {
        return parent;
    }

    public void setParent(TransactionCategory parent) {
        this.parent = parent;
    }

    public Set<TransactionCategory> getSubcategories() {
        return subcategories;
    }

    public void addSubcategory(TransactionCategory subcategory) {
        subcategories.add(subcategory);
        subcategory.setParent(this);
    }

    public void removeSubcategory(TransactionCategory subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParent(null);
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
    
    public Set<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Get the full hierarchical name of this category (e.g., "Bills > Utilities > Electricity")
     * @return The full hierarchical name
     */
    @Transient
    public String getFullName() {
        if (parent == null) {
            return name;
        }
        return parent.getFullName() + " > " + name;
    }
    
    /**
     * Get the root category in this category's hierarchy
     * @return The root category
     */
    @Transient
    public TransactionCategory getRootCategory() {
        if (parent == null) {
            return this;
        }
        return parent.getRootCategory();
    }
    
    /**
     * Check if this category is a descendant of the specified category
     * @param ancestor The potential ancestor category
     * @return true if this category is a descendant of the specified category
     */
    public boolean isDescendantOf(TransactionCategory ancestor) {
        if (parent == null) {
            return false;
        }
        if (parent.equals(ancestor)) {
            return true;
        }
        return parent.isDescendantOf(ancestor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategory category = (TransactionCategory) o;
        return categoryId == category.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    @Override
    public String toString() {
        return "TransactionCategory{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}