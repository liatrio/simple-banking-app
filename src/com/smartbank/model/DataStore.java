package com.smartbank.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DataStore {
    private static DataStore instance;
    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private static final AtomicLong transactionIdGenerator = new AtomicLong(1);

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // Account operations
    public synchronized void addAccount(Account account) {
        accounts.add(account);
    }

    public synchronized List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    public synchronized Account getAccountByNumber(long accountNumber) {
        return accounts.stream().filter(a -> a.getAccountNumber() == accountNumber).findFirst().orElse(null);
    }

    public synchronized List<Account> getAccountsByOwner(String username) {
        return accounts.stream()
                .filter(a -> a.getAccountHolder().getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    // Transaction operations
    public synchronized void recordTransaction(long accountNumber, double amount, Transaction.Type type, String description) {
        Transaction tx = new Transaction(transactionIdGenerator.getAndIncrement(), accountNumber, amount, type, new Date(), description);
        transactions.add(tx);
    }

    public synchronized List<Transaction> getTransactionsForAccount(long accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber() == accountNumber)
                .collect(Collectors.toList());
    }

    public synchronized List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }
}
