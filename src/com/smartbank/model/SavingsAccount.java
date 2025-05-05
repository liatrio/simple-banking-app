package com.smartbank.model;

/**
 * Savings account with minimum balance requirement
 */
public class SavingsAccount extends BankAccount {
    private static final double DEFAULT_MINIMUM_BALANCE = 100.0;
    private final double minimumBalance;
    
    /**
     * Constructor for creating a new savings account with default minimum balance
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     * @throws IllegalArgumentException if the initial deposit is less than the minimum balance
     */
    public SavingsAccount(String holderName, double initialDeposit) throws IllegalArgumentException {
        this(holderName, initialDeposit, DEFAULT_MINIMUM_BALANCE);
    }
    
    /**
     * Constructor for creating a new savings account with custom minimum balance
     * 
     * @param holderName The name of the account holder
     * @param initialDeposit The initial deposit amount
     * @param minimumBalance The minimum balance requirement
     * @throws IllegalArgumentException if the initial deposit is less than the minimum balance
     */
    public SavingsAccount(String holderName, double initialDeposit, double minimumBalance) throws IllegalArgumentException {
        super(holderName, initialDeposit);
        this.minimumBalance = minimumBalance;
        
        if (initialDeposit < minimumBalance) {
            throw new IllegalArgumentException("Initial deposit must be at least $" + minimumBalance);
        }
    }
    
    /**
     * Withdraws money from the account, ensuring minimum balance is maintained
     * 
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful
     * @throws IllegalArgumentException if the amount is negative
     * @throws InsufficientBalanceException if the withdrawal would result in a balance below the minimum
     */
    @Override
    public boolean withdraw(double amount) throws IllegalArgumentException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (balance - amount < minimumBalance) {
            throw new InsufficientBalanceException("Withdrawal would break minimum balance requirement of $" + minimumBalance);
        }
        
        balance -= amount;
        addTransaction(amount, TransactionType.WITHDRAWAL);
        return true;
    }
    
    /**
     * Gets the minimum balance requirement
     * 
     * @return The minimum balance requirement
     */
    public double getMinimumBalance() {
        return minimumBalance;
    }
    
    /**
     * Displays the account details including the minimum balance requirement
     * 
     * @return A string containing the account details
     */
    @Override
    public String displayAccountDetails() {
        return super.displayAccountDetails() + String.format("\nMinimum Balance: $%.2f", minimumBalance);
    }
}
