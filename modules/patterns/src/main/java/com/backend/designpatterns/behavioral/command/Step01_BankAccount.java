package com.backend.designpatterns.behavioral.command;

/**
 * Step 1: THE RECEIVER
 * 
 * The domain entity that actually holds the business logic.
 * Notice that it knows absolutely nothing about Commands, History, or Undo.
 */
public class Step01_BankAccount {
    
    private final String accountId;
    private double balance;

    public Step01_BankAccount(String accountId, double initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance;
    }

    public void addFunds(double amount) {
        this.balance += amount;
        System.out.println(String.format("[Bank] Added $%.2f to %s. New Balance: $%.2f", amount, accountId, balance));
    }

    public boolean removeFunds(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            System.out.println(String.format("[Bank] Withdrew $%.2f from %s. New Balance: $%.2f", amount, accountId, balance));
            return true;
        }
        System.out.println(String.format("[Bank] Insufficient funds in %s to withdraw $%.2f. Current Balance: $%.2f", accountId, amount, balance));
        return false;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }
}
