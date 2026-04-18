package com.backend.designpatterns.behavioral.command;

/**
 * Step 2: THE COMMAND CONTRACT
 * 
 * Defines the ability to encapsulate an executable action,
 * and crucially for L5 systems, the ability to perfectly undo it.
 */
public interface Step02_TransactionCommand {

    /**
     * Executes the command.
     * @return true if successful, false otherwise.
     */
    boolean execute();

    /**
     * Reverts the effects of the command.
     */
    void undo();

    /**
     * Required for logging and visualization.
     */
    String getCommandName();
}
