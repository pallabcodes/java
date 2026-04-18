package com.backend.designpatterns.behavioral.command;

/**
 * Step 4: MACRO COMMAND
 * 
 * Demonstrates combining multiple primitive commands into a single transactional unit.
 */
public class Step04_TransferCommand implements Step02_TransactionCommand {

    private final Step03_WithdrawCommand withdraw;
    private final Step03_DepositCommand deposit;
    private final double amount;

    public Step04_TransferCommand(Step01_BankAccount fromAccount, Step01_BankAccount toAccount, double amount) {
        this.withdraw = new Step03_WithdrawCommand(fromAccount, amount);
        this.deposit = new Step03_DepositCommand(toAccount, amount);
        this.amount = amount;
    }

    @Override
    public boolean execute() {
        System.out.println("[Execute] Initiating transfer of $" + amount);
        // If withdrawal is successful, we deposit.
        if (withdraw.execute()) {
            deposit.execute();
            return true;
        }
        System.out.println("[Execute] Transfer aborted due to insufficient funds.");
        return false;
    }

    @Override
    public void undo() {
        System.out.println("[Undo] Reversing transfer of $" + amount);
        // Notice the reverse order: Undo deposit, then undo withdrawal
        deposit.undo();
        withdraw.undo();
    }

    @Override
    public String getCommandName() {
        return "TRANSFER_" + amount;
    }
}
