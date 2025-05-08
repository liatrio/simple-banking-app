#!/bin/bash
echo "Checking credit accounts in the database..."
sqlite3 smartbank.db "SELECT accountNumber, balance, creditLimit, creditScore, automaticCreditLimitReviewEnabled, averageMonthlyBalance FROM accounts WHERE type = 'Credit';"