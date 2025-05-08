package com.smartbank.service.search;

import com.smartbank.model.Transaction;
import com.smartbank.model.TransactionCategory;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Model class to represent search criteria for Transaction searches.
 * This class encapsulates all possible search parameters for the advanced transaction search feature.
 */
public class TransactionSearchCriteria {
    // Basic search criteria
    private Long accountNumber;
    private Transaction.Type type;
    private Date startDate;
    private Date endDate;
    private Double minAmount;
    private Double maxAmount;
    private String description;
    private String merchantName;
    private TransactionCategory category;
    private Long categoryId;
    private Boolean isRecurring;
    
    // Extended search criteria
    private Boolean isCategorizedAutomatically;
    private List<Long> excludedAccounts = new ArrayList<>();
    private List<Transaction.Type> excludedTypes = new ArrayList<>();
    private List<Long> excludedCategories = new ArrayList<>();
    private Boolean includeChildCategories = false;
    
    // Pagination and sorting
    private Integer pageNumber = 0;
    private Integer pageSize = 20;
    private String sortBy = "timestamp";
    private SortDirection sortDirection = SortDirection.DESCENDING;

    public enum SortDirection {
        ASCENDING,
        DESCENDING
    }
    
    // Default constructor
    public TransactionSearchCriteria() {
    }
    
    // Builder pattern constructor
    private TransactionSearchCriteria(Builder builder) {
        this.accountNumber = builder.accountNumber;
        this.type = builder.type;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
        this.description = builder.description;
        this.merchantName = builder.merchantName;
        this.category = builder.category;
        this.categoryId = builder.categoryId;
        this.isRecurring = builder.isRecurring;
        this.isCategorizedAutomatically = builder.isCategorizedAutomatically;
        this.excludedAccounts = builder.excludedAccounts;
        this.excludedTypes = builder.excludedTypes;
        this.excludedCategories = builder.excludedCategories;
        this.includeChildCategories = builder.includeChildCategories;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
        this.sortBy = builder.sortBy;
        this.sortDirection = builder.sortDirection;
    }
    
    // Getters and setters
    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Transaction.Type getType() {
        return type;
    }

    public void setType(Transaction.Type type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getCategoryId();
        }
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public Boolean isCategorizedAutomatically() {
        return isCategorizedAutomatically;
    }

    public void setCategorizedAutomatically(Boolean categorizedAutomatically) {
        isCategorizedAutomatically = categorizedAutomatically;
    }

    public List<Long> getExcludedAccounts() {
        return excludedAccounts;
    }

    public void setExcludedAccounts(List<Long> excludedAccounts) {
        this.excludedAccounts = excludedAccounts;
    }

    public List<Transaction.Type> getExcludedTypes() {
        return excludedTypes;
    }

    public void setExcludedTypes(List<Transaction.Type> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    public List<Long> getExcludedCategories() {
        return excludedCategories;
    }

    public void setExcludedCategories(List<Long> excludedCategories) {
        this.excludedCategories = excludedCategories;
    }

    public Boolean getIncludeChildCategories() {
        return includeChildCategories;
    }

    public void setIncludeChildCategories(Boolean includeChildCategories) {
        this.includeChildCategories = includeChildCategories;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * Checks if this search criteria has any filters set.
     * @return true if any search filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return accountNumber != null || 
               type != null || 
               startDate != null || 
               endDate != null || 
               minAmount != null || 
               maxAmount != null || 
               (description != null && !description.isEmpty()) || 
               (merchantName != null && !merchantName.isEmpty()) || 
               category != null || 
               categoryId != null || 
               isRecurring != null || 
               isCategorizedAutomatically != null || 
               !excludedAccounts.isEmpty() || 
               !excludedTypes.isEmpty() || 
               !excludedCategories.isEmpty();
    }
    
    /**
     * Builder class for creating TransactionSearchCriteria objects.
     */
    public static class Builder {
        private Long accountNumber;
        private Transaction.Type type;
        private Date startDate;
        private Date endDate;
        private Double minAmount;
        private Double maxAmount;
        private String description;
        private String merchantName;
        private TransactionCategory category;
        private Long categoryId;
        private Boolean isRecurring;
        private Boolean isCategorizedAutomatically;
        private List<Long> excludedAccounts = new ArrayList<>();
        private List<Transaction.Type> excludedTypes = new ArrayList<>();
        private List<Long> excludedCategories = new ArrayList<>();
        private Boolean includeChildCategories = false;
        private Integer pageNumber = 0;
        private Integer pageSize = 20;
        private String sortBy = "timestamp";
        private SortDirection sortDirection = SortDirection.DESCENDING;
        
        public Builder() {
        }
        
        public Builder accountNumber(Long accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }
        
        public Builder type(Transaction.Type type) {
            this.type = type;
            return this;
        }
        
        public Builder dateRange(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }
        
        public Builder amountRange(Double minAmount, Double maxAmount) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }
        
        public Builder category(TransactionCategory category) {
            this.category = category;
            if (category != null) {
                this.categoryId = category.getCategoryId();
            }
            return this;
        }
        
        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public Builder recurring(Boolean isRecurring) {
            this.isRecurring = isRecurring;
            return this;
        }
        
        public Builder categorizedAutomatically(Boolean isCategorizedAutomatically) {
            this.isCategorizedAutomatically = isCategorizedAutomatically;
            return this;
        }
        
        public Builder excludeAccounts(List<Long> excludedAccounts) {
            this.excludedAccounts = excludedAccounts;
            return this;
        }
        
        public Builder excludeTypes(List<Transaction.Type> excludedTypes) {
            this.excludedTypes = excludedTypes;
            return this;
        }
        
        public Builder excludeCategories(List<Long> excludedCategories) {
            this.excludedCategories = excludedCategories;
            return this;
        }
        
        public Builder includeChildCategories(Boolean includeChildCategories) {
            this.includeChildCategories = includeChildCategories;
            return this;
        }
        
        public Builder pagination(Integer pageNumber, Integer pageSize) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            return this;
        }
        
        public Builder sorting(String sortBy, SortDirection sortDirection) {
            this.sortBy = sortBy;
            this.sortDirection = sortDirection;
            return this;
        }
        
        public TransactionSearchCriteria build() {
            return new TransactionSearchCriteria(this);
        }
    }
}