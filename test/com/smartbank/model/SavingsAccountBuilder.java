package com.smartbank.model;

import java.time.LocalDate;

/**
 * Builder for creating SavingsAccount instances for testing.
 * This builder allows creating SavingsAccount objects with default or custom values.
 */
public class SavingsAccountBuilder extends AccountBuilder<SavingsAccount, SavingsAccountBuilder> {
    
    private double interestRate;
    private SavingsAccount.CompoundingMethod compoundingMethod;
    private String lastInterestAccrualDate;
    private String lastInterestPostingDate;
    private double accruedInterest;
    private double minimumBalanceForInterest;
    private SavingsAccount.InterestTierType interestTierType;
    
    /**
     * Create a new SavingsAccountBuilder with default values.
     */
    public SavingsAccountBuilder() {
        super();
        this.interestRate = 0.05; // 5% default interest rate
        this.compoundingMethod = SavingsAccount.CompoundingMethod.DAILY;
        this.lastInterestAccrualDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.lastInterestPostingDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.accruedInterest = 0.0;
        this.minimumBalanceForInterest = 0.0;
        this.interestTierType = SavingsAccount.InterestTierType.FLAT;
    }
    
    /**
     * Set the interest rate.
     * 
     * @param interestRate The interest rate (e.g., 0.05 for 5%)
     * @return This builder
     */
    public SavingsAccountBuilder withInterestRate(double interestRate) {
        this.interestRate = interestRate;
        return self();
    }
    
    /**
     * Set the compounding method.
     * 
     * @param compoundingMethod The compounding method
     * @return This builder
     */
    public SavingsAccountBuilder withCompoundingMethod(SavingsAccount.CompoundingMethod compoundingMethod) {
        this.compoundingMethod = compoundingMethod;
        return self();
    }
    
    /**
     * Set the last interest accrual date.
     * 
     * @param lastInterestAccrualDate The last interest accrual date
     * @return This builder
     */
    public SavingsAccountBuilder withLastInterestAccrualDate(String lastInterestAccrualDate) {
        this.lastInterestAccrualDate = lastInterestAccrualDate;
        return self();
    }
    
    /**
     * Set the last interest accrual date.
     * 
     * @param lastInterestAccrualDate The last interest accrual date
     * @return This builder
     */
    public SavingsAccountBuilder withLastInterestAccrualDate(LocalDate lastInterestAccrualDate) {
        this.lastInterestAccrualDate = lastInterestAccrualDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        return self();
    }
    
    /**
     * Set the last interest posting date.
     * 
     * @param lastInterestPostingDate The last interest posting date
     * @return This builder
     */
    public SavingsAccountBuilder withLastInterestPostingDate(String lastInterestPostingDate) {
        this.lastInterestPostingDate = lastInterestPostingDate;
        return self();
    }
    
    /**
     * Set the last interest posting date.
     * 
     * @param lastInterestPostingDate The last interest posting date
     * @return This builder
     */
    public SavingsAccountBuilder withLastInterestPostingDate(LocalDate lastInterestPostingDate) {
        this.lastInterestPostingDate = lastInterestPostingDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        return self();
    }
    
    /**
     * Set the accrued interest.
     * 
     * @param accruedInterest The accrued interest
     * @return This builder
     */
    public SavingsAccountBuilder withAccruedInterest(double accruedInterest) {
        this.accruedInterest = accruedInterest;
        return self();
    }
    
    /**
     * Set the minimum balance for interest.
     * 
     * @param minimumBalanceForInterest The minimum balance for interest
     * @return This builder
     */
    public SavingsAccountBuilder withMinimumBalanceForInterest(double minimumBalanceForInterest) {
        this.minimumBalanceForInterest = minimumBalanceForInterest;
        return self();
    }
    
    /**
     * Set the interest tier type.
     * 
     * @param interestTierType The interest tier type
     * @return This builder
     */
    public SavingsAccountBuilder withInterestTierType(SavingsAccount.InterestTierType interestTierType) {
        this.interestTierType = interestTierType;
        return self();
    }
    
    /**
     * Build a SavingsAccount instance with the current builder state.
     * 
     * @return A new SavingsAccount instance
     */
    @Override
    public SavingsAccount build() {
        SavingsAccount account = new SavingsAccount(accountHolder, initialBalance, interestRate);
        
        account.setCompoundingMethod(compoundingMethod);
        account.setLastInterestAccrualDate(lastInterestAccrualDate);
        account.setLastInterestPostingDate(lastInterestPostingDate);
        account.setAccruedInterest(accruedInterest);
        account.setMinimumBalanceForInterest(minimumBalanceForInterest);
        account.setInterestTierType(interestTierType);
        
        if (accountName != null) {
            account.setAccountName(accountName);
        }
        
        return account;
    }
}
