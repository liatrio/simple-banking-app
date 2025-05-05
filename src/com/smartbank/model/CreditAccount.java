package com.smartbank.model;

/**
 * Credit account with credit limit
 */
public class CreditAccount extends BankAccount {
    private static final double DEFAULT_CREDIT_LIMIT = 1000.0;
    private final double creditLimit;
    
    /**
     * Constructor for creating a new credit account with default credit limit
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     */
    public CreditAccount(String holderName, double initialDeposit) {
        this(holderName, initialDeposit, DEFAULT_CREDIT_LIMIT);
    }
    
    /**
     * Constructor for creating a new credit account with custom credit limit
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     * @param creditLimit The credit limit
     */
    public CreditAccount(String holderName, double initialDeposit, double creditLimit) {
        super(holderName, initialDeposit);
        this.creditLimit = creditLimit;
    }
    
    /**
     * Withdraws money from the account, allowing overdraft up to the credit limit
     * 
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful
     * @throws IllegalArgumentException if the amount is negative
     * @throws InsufficientBalanceException if the withdrawal would exceed the credit limit
     */
    @Override
    public boolean withdraw(double amount) throws IllegalArgumentException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (balance - amount < -creditLimit) {
            throw new InsufficientBalanceException("Withdrawal would exceed credit limit of $" + creditLimit);
        }
        
        balance -= amount;
        addTransaction(amount, TransactionType.WITHDRAWAL);
        return true;
    }
    
    /**
     * Gets the credit limit
     * 
     * @return The credit limit
     */
    public double getCreditLimit() {
        return creditLimit;
    }
    
    /**
     * Gets the available credit
     * 
     * @return The available credit
     */
    public double getAvailableCredit() {
        return creditLimit + balance;
    }
    
    /**
     * Displays the account details including the credit limit
     * 
     * @return A string containing the account details
     */
    @Override
    public String displayAccountDetails() {
        return super.displayAccountDetails() + 
               String.format("\nCredit Limit: $%.2f\nAvailable Credit: $%.2f", 
                            creditLimit, getAvailableCredit());
    }
}
