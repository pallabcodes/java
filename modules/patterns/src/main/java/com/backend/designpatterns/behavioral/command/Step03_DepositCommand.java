package com.backend.designpatterns.behavioral.command;

/**
 * Step 3: CONCRETE COMMAND (Primitive)
 */
public class Step03_DepositCommand implements Step02_TransactionCommand {

    private final Step01_BankAccount account;
    private final double amount;

    public Step03_DepositCommand(Step01_BankAccount account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public boolean execute() {
        account.addFunds(amount);
        return true;
    }

    @Override
    public void undo() {
        // The perfect reverse of executing a deposit
        System.out.println("[Undo] Reversing Deposit of $" + amount);
        account.removeFunds(amount);
    }

    @Override
    public String getCommandName() {
        return "DEPOSIT_" + amount + "_TO_" + account.getAccountId();
    }
}
