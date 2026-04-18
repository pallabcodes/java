package com.backend.designpatterns.behavioral.command;

/**
 * Step 3: CONCRETE COMMAND (Primitive)
 */
public class Step03_WithdrawCommand implements Step02_TransactionCommand {

    private final Step01_BankAccount account;
    private final double amount;

    public Step03_WithdrawCommand(Step01_BankAccount account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public boolean execute() {
        return account.removeFunds(amount);
    }

    @Override
    public void undo() {
        // The perfect reverse of executing a withdrawal
        System.out.println("[Undo] Reversing Withdrawal of $" + amount);
        account.addFunds(amount);
    }

    @Override
    public String getCommandName() {
        return "WITHDRAW_" + amount + "_FROM_" + account.getAccountId();
    }
}
