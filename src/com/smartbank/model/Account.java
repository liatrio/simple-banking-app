package com.smartbank.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import javax.persistence.*;

/**
 * Abstract Account class - base for all account types.
 */
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Account {
    @Transient
    private static long nextAccountNumber = 1000000000L;
    
    @Id
    @Column(name = "accountNumber")
    private long accountNumber;
    
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User accountHolder;
    
    @Column(nullable = false)
    protected double balance;
    
    @Column(nullable = false)
    private String creationDate;
    
    @Column
    private String accountName;
    
    // Default constructor required by JPA
    protected Account() {
    }

    public Account(User accountHolder, double initialBalance) {
        this.accountNumber = nextAccountNumber++;
        this.accountHolder = accountHolder;
        this.balance = initialBalance;
        this.creationDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public User getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    public String getCreationDate() {
        return creationDate;
    }
    
    public LocalDateTime getCreationDateTime() {
        return LocalDateTime.parse(creationDate);
    }
    
    public String getAccountName() {
        return accountName != null ? accountName : getClass().getSimpleName() + " " + accountNumber;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public long getUserId() {
        return accountHolder != null ? Long.parseLong(accountHolder.getUserId()) : 0;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
    }

    public abstract void withdraw(double amount) throws Exception;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountNumber == account.accountNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "accountNumber=" + accountNumber +
                ", accountHolder=" + accountHolder.getUsername() +
                ", balance=" + balance +
                ", creationDate=" + creationDate +
                '}';
    }
}
