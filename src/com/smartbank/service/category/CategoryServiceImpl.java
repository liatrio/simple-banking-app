package com.smartbank.service.category;

import com.smartbank.model.TransactionCategory;
import com.smartbank.repository.RepositoryFactory;
import com.smartbank.repository.TransactionCategoryRepository;
import com.smartbank.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the CategoryService.
 */
public class CategoryServiceImpl implements CategoryService {
    private static final Logger LOGGER = Logger.getLogger(CategoryServiceImpl.class.getName());
    
    private final TransactionCategoryRepository categoryRepository;
    
    /**
     * Constructor that initializes the category repository.
     */
    public CategoryServiceImpl() {
        this.categoryRepository = RepositoryFactory.getTransactionCategoryRepository();
    }
    
    @Override
    public List<TransactionCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Override
    public Optional<TransactionCategory> getCategoryById(long categoryId) {
        return categoryRepository.findById(categoryId);
    }
    
    @Override
    public Optional<TransactionCategory> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    @Override
    public TransactionCategory createCategory(TransactionCategory category) throws CategoryException {
        if (category == null) {
            throw new CategoryException("Category cannot be null");
        }
        
        // Check if category with same name already exists
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new CategoryException("Category with name '" + category.getName() + "' already exists");
        }
        
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating category: " + e.getMessage(), e);
            throw new CategoryException("Failed to create category: " + e.getMessage(), e);
        }
    }
    
    @Override
    public TransactionCategory updateCategory(TransactionCategory category) throws CategoryException {
        if (category == null) {
            throw new CategoryException("Category cannot be null");
        }
        
        // Verify the category exists
        Optional<TransactionCategory> existingCategory = categoryRepository.findById(category.getCategoryId());
        if (!existingCategory.isPresent()) {
            throw new CategoryException("Category with ID " + category.getCategoryId() + " not found");
        }
        
        // Check if it's a system category that shouldn't be modified
        TransactionCategory existing = existingCategory.get();
        if (existing.isSystem() && !category.isSystem()) {
            throw new CategoryException("Cannot change system flag for system category");
        }
        
        // Check if another category with the same name exists
        Optional<TransactionCategory> categoryWithSameName = categoryRepository.findByName(category.getName());
        if (categoryWithSameName.isPresent() && categoryWithSameName.get().getCategoryId() != category.getCategoryId()) {
            throw new CategoryException("Another category with name '" + category.getName() + "' already exists");
        }
        
        // Check for circular dependency
        if (category.getParent() != null && 
            (category.getParent().getCategoryId() == category.getCategoryId() || 
             category.getParent().isDescendantOf(category))) {
            throw new CategoryException("Circular category dependency detected");
        }
        
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating category: " + e.getMessage(), e);
            throw new CategoryException("Failed to update category: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteCategory(long categoryId) throws CategoryException {
        // Verify the category exists
        Optional<TransactionCategory> categoryOpt = categoryRepository.findById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        TransactionCategory category = categoryOpt.get();
        
        // Check if it's a system category
        if (category.isSystem()) {
            throw new CategoryException("Cannot delete system category: " + category.getName());
        }
        
        // Check if it has subcategories
        if (!category.getSubcategories().isEmpty()) {
            throw new CategoryException("Cannot delete category with subcategories. Remove subcategories first.");
        }
        
        // Check if it has transactions
        if (!category.getTransactions().isEmpty()) {
            throw new CategoryException("Cannot delete category that has transactions. Reassign transactions first.");
        }
        
        try {
            categoryRepository.deleteById(categoryId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting category: " + e.getMessage(), e);
            throw new CategoryException("Failed to delete category: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<TransactionCategory> getRootCategories() {
        return categoryRepository.findRootCategories();
    }
    
    @Override
    public List<TransactionCategory> getSubcategories(long parentId) {
        return categoryRepository.findByParentId(parentId);
    }
    
    @Override
    public TransactionCategory addSubcategory(long parentId, TransactionCategory subcategory) 
            throws CategoryException {
        // Verify parent exists
        Optional<TransactionCategory> parentOpt = categoryRepository.findById(parentId);
        if (!parentOpt.isPresent()) {
            throw new CategoryException("Parent category with ID " + parentId + " not found");
        }
        
        TransactionCategory parent = parentOpt.get();
        subcategory.setParent(parent);
        
        return createCategory(subcategory);
    }
    
    @Override
    public List<TransactionCategory> findCategoriesByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return categoryRepository.findByKeyword(keyword);
    }
    
    @Override
    public List<TransactionCategory> initializeDefaultCategories() {
        List<TransactionCategory> systemCategories = categoryRepository.findSystemCategories();
        
        // If we already have system categories, don't recreate them
        if (!systemCategories.isEmpty()) {
            return systemCategories;
        }
        
        List<TransactionCategory> createdCategories = new ArrayList<>();
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // Create default categories
            Map<String, TransactionCategory> topLevelCategories = new HashMap<>();
            
            // Root categories
            String[] rootCategoryNames = {
                "Income", "Housing", "Transportation", "Food", "Entertainment", 
                "Health", "Personal", "Debt", "Savings", "Giving", "Misc"
            };
            
            // Define colors for top-level categories
            Map<String, String> categoryColors = new HashMap<>();
            categoryColors.put("Income", "#4CAF50");     // Green
            categoryColors.put("Housing", "#2196F3");    // Blue
            categoryColors.put("Transportation", "#3F51B5"); // Indigo
            categoryColors.put("Food", "#00BCD4");      // Cyan
            categoryColors.put("Entertainment", "#009688"); // Teal
            categoryColors.put("Health", "#FF9800");    // Orange
            categoryColors.put("Personal", "#FF5722");   // Deep Orange
            categoryColors.put("Debt", "#F44336");      // Red
            categoryColors.put("Savings", "#CDDC39");   // Lime
            categoryColors.put("Giving", "#8BC34A");    // Light Green
            categoryColors.put("Misc", "#9C27B0");      // Purple
            
            // Create uncategorized category
            TransactionCategory uncategorized = new TransactionCategory("Uncategorized", "Transactions not yet categorized");
            uncategorized.setSystem(true);
            uncategorized.setColor("#808080"); // Gray
            uncategorized.setKeywords("uncategorized,misc,unknown");
            em.persist(uncategorized);
            createdCategories.add(uncategorized);
            
            // Create top-level categories
            for (String name : rootCategoryNames) {
                TransactionCategory category = new TransactionCategory(name, name + " related expenses");
                category.setSystem(true);
                category.setKeywords(name.toLowerCase());
                category.setColor(categoryColors.get(name)); // Set the color from our map
                em.persist(category);
                topLevelCategories.put(name, category);
                createdCategories.add(category);
            }
            
            // Add some common subcategories with appropriate keywords
            addSubcategory(em, topLevelCategories.get("Income"), "Salary", "#4CAF50", "salary,paycheck,payment,deposit,wage,compensation,payroll", createdCategories);
            addSubcategory(em, topLevelCategories.get("Income"), "Interest", "#8BC34A", "interest,yield,dividend,investment return", createdCategories);
            addSubcategory(em, topLevelCategories.get("Income"), "Dividends", "#CDDC39", "dividend,stock dividends,equity", createdCategories);
            addSubcategory(em, topLevelCategories.get("Income"), "Gifts", "#FFC107", "gift,present,bonus", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Housing"), "Rent/Mortgage", "#FF5722", "rent,mortgage,lease,housing payment,apartment,home payment", createdCategories);
            addSubcategory(em, topLevelCategories.get("Housing"), "Utilities", "#F44336", "utilities,electric,water,gas,power,energy,sewage,waste,garbage", createdCategories);
            addSubcategory(em, topLevelCategories.get("Housing"), "Maintenance", "#E91E63", "maintenance,repair,home repair,plumber,handyman,contractor", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Transportation"), "Gas", "#9C27B0", "gas,fuel,gasoline,petrol,shell,chevron,exxon,bp,circle k", createdCategories);
            addSubcategory(em, topLevelCategories.get("Transportation"), "Car Payment", "#673AB7", "car payment,auto loan,vehicle finance,lease payment,toyota,honda,ford,bmw", createdCategories);
            addSubcategory(em, topLevelCategories.get("Transportation"), "Public Transit", "#3F51B5", "transit,bus,subway,train,metro,transportation,uber,lyft,taxi,cab", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Food"), "Groceries", "#2196F3", "groceries,supermarket,food,grocery,kroger,safeway,publix,walmart,target,whole foods,trader joe", createdCategories);
            addSubcategory(em, topLevelCategories.get("Food"), "Restaurants", "#03A9F4", "restaurant,dining,eat out,lunch,dinner,food delivery,doordash,grubhub,ubereats,mcdonalds,chipotle,starbucks", createdCategories);
            addSubcategory(em, topLevelCategories.get("Food"), "Coffee", "#00BCD4", "coffee,cafe,starbucks,peets,dunkin,costa,tim hortons", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Entertainment"), "Movies", "#009688", "movies,cinema,theatre,theater,netflix,hulu,disney,hbo,amazon prime", createdCategories);
            addSubcategory(em, topLevelCategories.get("Entertainment"), "Concerts", "#4CAF50", "concert,live music,ticket,festival,live show,eventbrite,ticketmaster", createdCategories);
            addSubcategory(em, topLevelCategories.get("Entertainment"), "Subscriptions", "#8BC34A", "subscription,membership,streaming,amazon,netflix,hulu,spotify,apple music,youtube", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Health"), "Insurance", "#CDDC39", "insurance,health insurance,medical insurance,premium,coverage", createdCategories);
            addSubcategory(em, topLevelCategories.get("Health"), "Doctor", "#FFC107", "doctor,physician,medical,clinic,hospital,appointment,healthcare", createdCategories);
            addSubcategory(em, topLevelCategories.get("Health"), "Pharmacy", "#FF9800", "pharmacy,medication,prescription,medicine,walgreens,cvs,rite aid", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Personal"), "Clothing", "#FF5722", "clothing,clothes,apparel,shoes,fashion,shopping,mall,nordstrom,macys,tj maxx,ross,amazon", createdCategories);
            addSubcategory(em, topLevelCategories.get("Personal"), "Education", "#F44336", "education,tuition,student,university,college,course,class,school,training,udemy,coursera", createdCategories);
            addSubcategory(em, topLevelCategories.get("Personal"), "Hobbies", "#E91E63", "hobby,craft,amazon,book,game,gaming,sport,equipment,hobby lobby,michaels", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Debt"), "Credit Card", "#9C27B0", "credit card,cc payment,visa,mastercard,discover,amex,capital one,chase,citi,bank of america", createdCategories);
            addSubcategory(em, topLevelCategories.get("Debt"), "Student Loans", "#673AB7", "student loan,education loan,sallie mae,navient,nelnet,great lakes,fedloan", createdCategories);
            addSubcategory(em, topLevelCategories.get("Debt"), "Personal Loans", "#3F51B5", "loan,personal loan,finance,lending club,sofi,upstart,prosper", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Savings"), "Emergency Fund", "#2196F3", "emergency,savings,fund,reserve", createdCategories);
            addSubcategory(em, topLevelCategories.get("Savings"), "Retirement", "#03A9F4", "retirement,401k,ira,pension,invest,fidelity,vanguard,charles schwab", createdCategories);
            addSubcategory(em, topLevelCategories.get("Savings"), "Investment", "#00BCD4", "investment,stock,bond,mutual fund,etf,roth,vanguard,fidelity,schwab,robinhood", createdCategories);
            
            addSubcategory(em, topLevelCategories.get("Giving"), "Donations", "#009688", "donation,donate,charity,nonprofit,relief,salvation army,red cross,goodwill", createdCategories);
            addSubcategory(em, topLevelCategories.get("Giving"), "Gifts", "#4CAF50", "gift,present,charity,birthday,holiday,wedding,christmas,amazon", createdCategories);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error initializing default categories: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        
        return createdCategories;
    }
    
    private void addSubcategory(EntityManager em, TransactionCategory parent, String name, String color,
                               String keywords, List<TransactionCategory> createdCategories) {
        TransactionCategory subcategory = new TransactionCategory(name, name + " related expenses", color, parent);
        subcategory.setSystem(true);
        subcategory.setKeywords(keywords);
        em.persist(subcategory);
        createdCategories.add(subcategory);
    }
    
    @Override
    public List<TransactionCategory> getSystemCategories() {
        return categoryRepository.findSystemCategories();
    }
    
    @Override
    public TransactionCategory getUncategorizedCategory() {
        Optional<TransactionCategory> uncategorized = categoryRepository.findByName("Uncategorized");
        if (uncategorized.isPresent()) {
            return uncategorized.get();
        }
        
        // If not found, initialize default categories which includes Uncategorized
        List<TransactionCategory> systemCategories = initializeDefaultCategories();
        return systemCategories.stream()
                .filter(c -> "Uncategorized".equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find or create Uncategorized category"));
    }
    
    @Override
    public TransactionCategory updateBudget(long categoryId, double budgetAmount) throws CategoryException {
        if (budgetAmount < 0) {
            throw new CategoryException("Budget amount cannot be negative");
        }
        
        Optional<TransactionCategory> categoryOpt = categoryRepository.findById(categoryId);
        if (!categoryOpt.isPresent()) {
            throw new CategoryException("Category with ID " + categoryId + " not found");
        }
        
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            TransactionCategory category = em.find(TransactionCategory.class, categoryId);
            category.setBudgetAmount(budgetAmount);
            em.merge(category);
            tx.commit();
            return category;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating category budget: " + e.getMessage(), e);
            throw new CategoryException("Failed to update category budget: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}