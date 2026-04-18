package com.backend.designpatterns.behavioral.command;

import java.util.Stack;

// Role: Invoker
public class CommandInvoker {

    private final Stack<Command> history = new Stack<>();

    public void execute(Command command) {
        command.execute();
        history.push(command);
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            System.out.println("Undoing...");
            command.undo();
        } else {
            System.out.println("Nothing to undo.");
        }
    }
}
