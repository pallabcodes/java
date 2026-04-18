package com.backend.designpatterns.behavioral.command;

public class CommandDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Command Pattern Demo (Transactional Undo Engine) ===\n");

        Step05_TransactionManager manager = new Step05_TransactionManager();

        Step01_BankAccount accountA = new Step01_BankAccount("Alice-Checking", 1000.0);
        Step01_BankAccount accountB = new Step01_BankAccount("Bob-Savings", 500.0);

        System.out.println("--- Starting Transactions ---");
        // 1. Withdraw 200 from Alice
        manager.executeCommand(new Step03_WithdrawCommand(accountA, 200.0));

        // 2. Deposit 100 to Bob
        manager.executeCommand(new Step03_DepositCommand(accountB, 100.0));

        // 3. Transfer 300 from Bob to Alice (Macro command)
        manager.executeCommand(new Step04_TransferCommand(accountB, accountA, 300.0));

        // 4. Failed Withdrawal (Insufficient funds)
        manager.executeCommand(new Step03_WithdrawCommand(accountB, 1000.0));

        manager.printHistorySize(); // Should be 3, the failed one isn't tracked

        System.out.println("\n--- Initiating 'Disaster Recovery' (Undo Mechanism) ---");
        // Reverting Transfer
        manager.undoLastTransaction();
        System.out.println("Alice Balance: $" + accountA.getBalance() + " | Bob Balance: $" + accountB.getBalance());

        // Reverting Deposit
        manager.undoLastTransaction();
        System.out.println("Alice Balance: $" + accountA.getBalance() + " | Bob Balance: $" + accountB.getBalance());

        // Reverting Withdrawal
        manager.undoLastTransaction();
        
        System.out.println("\n--- Final Integrity Check ---");
        System.out.println("Alice Balance: $" + accountA.getBalance() + " (Expected: 1000.0)");
        System.out.println("Bob Balance: $" + accountB.getBalance() + " (Expected: 500.0)");

        System.out.println("\n[L5 ACHIEVEMENT]: Actions encapsulated as objects allowed us to " +
                           "build a perfect transaction history stack with flawless undo functionality.");
    }
}
