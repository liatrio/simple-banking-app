package com.smartbank.model;

import com.smartbank.util.fixtures.TestDataFactory;
import com.smartbank.util.fixtures.TestObjectBuilder;

import java.util.Date;

/**
 * Builder for creating Transaction instances for testing.
 * This builder allows creating Transaction objects with default or custom values.
 */
public class TransactionBuilder extends TestObjectBuilder<Transaction, TransactionBuilder> {
    
    private long accountNumber;
    private double amount;
    private Transaction.Type type;
    private Date timestamp;
    private String description;
    private TransactionCategory category;
    private Long linkedTransactionId;
    private boolean isCategorizedAutomatically;
    private String merchantName;
    private boolean isRecurring;
    
    /**
     * Create a new TransactionBuilder with default values.
     */
    public TransactionBuilder() {
        this.accountNumber = 1000000000L;
        this.amount = 100.0;
        this.type = Transaction.Type.DEPOSIT;
        this.timestamp = new Date();
        this.description = TestDataFactory.randomString("Transaction");
        this.isCategorizedAutomatically = false;
        this.merchantName = null;
        this.isRecurring = false;
    }
    
    /**
     * Set the account number.
     * 
     * @param accountNumber The account number
     * @return This builder
     */
    public TransactionBuilder withAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
        return self();
    }
    
    /**
     * Set the account from an Account object.
     * 
     * @param account The account
     * @return This builder
     */
    public TransactionBuilder withAccount(Account account) {
        this.accountNumber = account.getAccountNumber();
        return self();
    }
    
    /**
     * Set the amount.
     * 
     * @param amount The amount
     * @return This builder
     */
    public TransactionBuilder withAmount(double amount) {
        this.amount = amount;
        return self();
    }
    
    /**
     * Set the transaction type.
     * 
     * @param type The transaction type
     * @return This builder
     */
    public TransactionBuilder withType(Transaction.Type type) {
        this.type = type;
        return self();
    }
    
    /**
     * Set the timestamp.
     * 
     * @param timestamp The timestamp
     * @return This builder
     */
    public TransactionBuilder withTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return self();
    }
    
    /**
     * Set the description.
     * 
     * @param description The description
     * @return This builder
     */
    public TransactionBuilder withDescription(String description) {
        this.description = description;
        return self();
    }
    
    /**
     * Set the category.
     * 
     * @param category The category
     * @return This builder
     */
    public TransactionBuilder withCategory(TransactionCategory category) {
        this.category = category;
        return self();
    }
    
    /**
     * Set the linked transaction ID.
     * 
     * @param linkedTransactionId The linked transaction ID
     * @return This builder
     */
    public TransactionBuilder withLinkedTransactionId(Long linkedTransactionId) {
        this.linkedTransactionId = linkedTransactionId;
        return self();
    }
    
    /**
     * Set whether the transaction is categorized automatically.
     * 
     * @param categorizedAutomatically Whether the transaction is categorized automatically
     * @return This builder
     */
    public TransactionBuilder withCategorizedAutomatically(boolean categorizedAutomatically) {
        this.isCategorizedAutomatically = categorizedAutomatically;
        return self();
    }
    
    /**
     * Set the merchant name.
     * 
     * @param merchantName The merchant name
     * @return This builder
     */
    public TransactionBuilder withMerchantName(String merchantName) {
        this.merchantName = merchantName;
        return self();
    }
    
    /**
     * Set whether the transaction is recurring.
     * 
     * @param recurring Whether the transaction is recurring
     * @return This builder
     */
    public TransactionBuilder withRecurring(boolean recurring) {
        this.isRecurring = recurring;
        return self();
    }
    
    /**
     * Build a Transaction instance with the current builder state.
     * 
     * @return A new Transaction instance
     */
    @Override
    public Transaction build() {
        Transaction transaction = new Transaction(accountNumber, amount, type, timestamp, description, category);
        
        if (linkedTransactionId != null) {
            transaction.setLinkedTransactionId(linkedTransactionId);
        }
        
        transaction.setCategorizedAutomatically(isCategorizedAutomatically);
        
        if (merchantName != null) {
            transaction.setMerchantName(merchantName);
        }
        
        transaction.setRecurring(isRecurring);
        
        return transaction;
    }
    
    /**
     * Create a deposit transaction.
     * 
     * @return This builder with type set to DEPOSIT
     */
    public TransactionBuilder asDeposit() {
        this.type = Transaction.Type.DEPOSIT;
        return self();
    }
    
    /**
     * Create a withdrawal transaction.
     * 
     * @return This builder with type set to WITHDRAWAL
     */
    public TransactionBuilder asWithdrawal() {
        this.type = Transaction.Type.WITHDRAWAL;
        return self();
    }
    
    /**
     * Create a transfer in transaction.
     * 
     * @return This builder with type set to TRANSFER_IN
     */
    public TransactionBuilder asTransferIn() {
        this.type = Transaction.Type.TRANSFER_IN;
        return self();
    }
    
    /**
     * Create a transfer out transaction.
     * 
     * @return This builder with type set to TRANSFER_OUT
     */
    public TransactionBuilder asTransferOut() {
        this.type = Transaction.Type.TRANSFER_OUT;
        return self();
    }
    
    /**
     * Create a payment transaction.
     * 
     * @return This builder with type set to PAYMENT
     */
    public TransactionBuilder asPayment() {
        this.type = Transaction.Type.PAYMENT;
        return self();
    }
    
    /**
     * Create a fee transaction.
     * 
     * @return This builder with type set to FEE
     */
    public TransactionBuilder asFee() {
        this.type = Transaction.Type.FEE;
        return self();
    }
}
