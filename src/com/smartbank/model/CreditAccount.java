package com.smartbank.model;

import javax.persistence.*;
import java.util.Objects;

/**
 * CreditAccount extends Account and adds credit limit and custom withdrawal logic.
 */
@Entity
@DiscriminatorValue("Credit")
public class CreditAccount extends Account {
    @Column(name = "creditLimit")
    private double creditLimit;

    // Default constructor required by JPA
    protected CreditAccount() {
        super();
    }

    public CreditAccount(User accountHolder, double initialBalance, double creditLimit) {
        super(accountHolder, initialBalance);
        this.creditLimit = creditLimit;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance + creditLimit) {
            throw new Exception("Withdrawal exceeds credit limit.");
        }
        balance -= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreditAccount that = (CreditAccount) o;
        return Double.compare(that.creditLimit, creditLimit) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), creditLimit);
    }

    @Override
    public String toString() {
        return "CreditAccount{" +
                "accountNumber=" + getAccountNumber() +
                ", accountHolder=" + getAccountHolder().getUsername() +
                ", balance=" + getBalance() +
                ", creditLimit=" + creditLimit +
                '}';
    }
}
