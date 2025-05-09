package com.smartbank.model;

import com.smartbank.util.fixtures.TestDataFactory;
import com.smartbank.util.fixtures.TestObjectBuilder;

/**
 * Abstract builder for creating Account instances for testing.
 * This builder allows creating Account objects with default or custom values.
 * 
 * @param <T> The specific Account type
 * @param <B> The specific builder type
 */
public abstract class AccountBuilder<T extends Account, B extends AccountBuilder<T, B>> 
        extends TestObjectBuilder<T, B> {
    
    protected User accountHolder;
    protected double initialBalance;
    protected String accountName;
    
    /**
     * Create a new AccountBuilder with default values.
     */
    public AccountBuilder() {
        this.accountHolder = new UserBuilder().build();
        this.initialBalance = 1000.0;
        this.accountName = TestDataFactory.randomString("Account");
    }
    
    /**
     * Set the account holder.
     * 
     * @param accountHolder The account holder
     * @return This builder
     */
    public B withAccountHolder(User accountHolder) {
        this.accountHolder = accountHolder;
        return self();
    }
    
    /**
     * Set the initial balance.
     * 
     * @param initialBalance The initial balance
     * @return This builder
     */
    public B withInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
        return self();
    }
    
    /**
     * Set the account name.
     * 
     * @param accountName The account name
     * @return This builder
     */
    public B withAccountName(String accountName) {
        this.accountName = accountName;
        return self();
    }
    
    /**
     * Build an Account instance with the current builder state.
     * 
     * @return A new Account instance
     */
    @Override
    public abstract T build();
}
