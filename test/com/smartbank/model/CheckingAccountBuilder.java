package com.smartbank.model;

/**
 * Builder for creating CheckingAccount instances for testing.
 * This builder allows creating CheckingAccount objects with default or custom values.
 */
public class CheckingAccountBuilder extends AccountBuilder<CheckingAccount, CheckingAccountBuilder> {
    
    private double monthlyMaintenanceFee;
    private double minimumBalanceRequired;
    private boolean overdraftProtection;
    private double overdraftLimit;
    private double overdraftFee;
    private boolean hasPaperCheckFeature;
    private boolean checkOrderingEnabled;
    private boolean directDepositEnabled;
    private int freeTransactionsPerMonth;
    private double perTransactionFee;
    
    /**
     * Create a new CheckingAccountBuilder with default values.
     */
    public CheckingAccountBuilder() {
        super();
        this.monthlyMaintenanceFee = 5.0;
        this.minimumBalanceRequired = 500.0;
        this.overdraftProtection = false;
        this.overdraftLimit = 0.0;
        this.overdraftFee = 35.0;
        this.hasPaperCheckFeature = true;
        this.checkOrderingEnabled = true;
        this.directDepositEnabled = true;
        this.freeTransactionsPerMonth = 25;
        this.perTransactionFee = 0.25;
    }
    
    /**
     * Set the monthly maintenance fee.
     * 
     * @param monthlyMaintenanceFee The monthly maintenance fee
     * @return This builder
     */
    public CheckingAccountBuilder withMonthlyMaintenanceFee(double monthlyMaintenanceFee) {
        this.monthlyMaintenanceFee = monthlyMaintenanceFee;
        return self();
    }
    
    /**
     * Set the minimum balance required.
     * 
     * @param minimumBalanceRequired The minimum balance required
     * @return This builder
     */
    public CheckingAccountBuilder withMinimumBalanceRequired(double minimumBalanceRequired) {
        this.minimumBalanceRequired = minimumBalanceRequired;
        return self();
    }
    
    /**
     * Set overdraft protection.
     * 
     * @param overdraftProtection Whether overdraft protection is enabled
     * @return This builder
     */
    public CheckingAccountBuilder withOverdraftProtection(boolean overdraftProtection) {
        this.overdraftProtection = overdraftProtection;
        return self();
    }
    
    /**
     * Enable overdraft protection with the specified limit.
     * 
     * @param overdraftLimit The overdraft limit
     * @return This builder
     */
    public CheckingAccountBuilder withOverdraftProtection(double overdraftLimit) {
        this.overdraftProtection = true;
        this.overdraftLimit = overdraftLimit;
        return self();
    }
    
    /**
     * Set the overdraft fee.
     * 
     * @param overdraftFee The overdraft fee
     * @return This builder
     */
    public CheckingAccountBuilder withOverdraftFee(double overdraftFee) {
        this.overdraftFee = overdraftFee;
        return self();
    }
    
    /**
     * Set whether the account has paper check feature.
     * 
     * @param hasPaperCheckFeature Whether the account has paper check feature
     * @return This builder
     */
    public CheckingAccountBuilder withPaperCheckFeature(boolean hasPaperCheckFeature) {
        this.hasPaperCheckFeature = hasPaperCheckFeature;
        return self();
    }
    
    /**
     * Set whether check ordering is enabled.
     * 
     * @param checkOrderingEnabled Whether check ordering is enabled
     * @return This builder
     */
    public CheckingAccountBuilder withCheckOrderingEnabled(boolean checkOrderingEnabled) {
        this.checkOrderingEnabled = checkOrderingEnabled;
        return self();
    }
    
    /**
     * Set whether direct deposit is enabled.
     * 
     * @param directDepositEnabled Whether direct deposit is enabled
     * @return This builder
     */
    public CheckingAccountBuilder withDirectDepositEnabled(boolean directDepositEnabled) {
        this.directDepositEnabled = directDepositEnabled;
        return self();
    }
    
    /**
     * Set the number of free transactions per month.
     * 
     * @param freeTransactionsPerMonth The number of free transactions per month
     * @return This builder
     */
    public CheckingAccountBuilder withFreeTransactionsPerMonth(int freeTransactionsPerMonth) {
        this.freeTransactionsPerMonth = freeTransactionsPerMonth;
        return self();
    }
    
    /**
     * Set the per-transaction fee.
     * 
     * @param perTransactionFee The per-transaction fee
     * @return This builder
     */
    public CheckingAccountBuilder withPerTransactionFee(double perTransactionFee) {
        this.perTransactionFee = perTransactionFee;
        return self();
    }
    
    /**
     * Build a CheckingAccount instance with the current builder state.
     * 
     * @return A new CheckingAccount instance
     */
    @Override
    public CheckingAccount build() {
        CheckingAccount account = new CheckingAccount(accountHolder, initialBalance, 
                monthlyMaintenanceFee, minimumBalanceRequired, overdraftProtection, overdraftLimit);
        
        account.setOverdraftFee(overdraftFee);
        account.setHasPaperCheckFeature(hasPaperCheckFeature);
        account.setCheckOrderingEnabled(checkOrderingEnabled);
        account.setDirectDepositEnabled(directDepositEnabled);
        account.setFreeTransactionsPerMonth(freeTransactionsPerMonth);
        account.setPerTransactionFee(perTransactionFee);
        
        if (accountName != null) {
            account.setAccountName(accountName);
        }
        
        return account;
    }
}
