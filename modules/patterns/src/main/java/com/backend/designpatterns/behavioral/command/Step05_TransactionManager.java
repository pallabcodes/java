package com.backend.designpatterns.behavioral.command;

import java.util.Stack;

/**
 * Step 5: THE INVOKER
 * 
 * Executes commands and maintains the transaction history stack.
 */
public class Step05_TransactionManager {

    private final Stack<Step02_TransactionCommand> history = new Stack<>();

    public void executeCommand(Step02_TransactionCommand command) {
        System.out.println(">>> Requesting Command: " + command.getCommandName());
        
        if (command.execute()) {
            // Only push successfully executed commands into history
            history.push(command);
        } else {
            System.err.println(">>> Command execution failed, not adding to history.");
        }
        System.out.println();
    }

    public void undoLastTransaction() {
        if (history.isEmpty()) {
            System.out.println("<<< Nothing to Undo.");
            return;
        }

        Step02_TransactionCommand lastCommand = history.pop();
        System.out.println("<<< Initiating Undo for: " + lastCommand.getCommandName());
        lastCommand.undo();
        System.out.println();
    }

    public void printHistorySize() {
        System.out.println("[TransactionManager] Current History Stack Size: " + history.size());
    }
}
