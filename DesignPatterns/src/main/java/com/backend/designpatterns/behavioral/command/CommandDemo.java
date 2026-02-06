package com.backend.designpatterns.behavioral.command;

public class CommandDemo {

    public static void main(String[] args) {
        System.out.println("--- Command Pattern Demo ---");

        // Use Case: Use Command to encapsulate a request as an object, allowing you to parameterize 
        // objects with different requests, queue or log requests, and support undoable operations.

        UserService service = new UserService();
        CommandInvoker invoker = new CommandInvoker();

        // 1. Execute Command
        Command createAlice = new CreateUserCommand(service, "Alice");
        invoker.execute(createAlice);

        // 2. Undo
        invoker.undo();
        
        // 3. Queue / Retry (Simple Loop)
        System.out.println("\n[Batch Execution]");
        invoker.execute(new CreateUserCommand(service, "Bob"));
        invoker.execute(new CreateUserCommand(service, "Charlie"));
        
        invoker.undo(); // Undoes Charlie
        invoker.undo(); // Undoes Bob
    }
}
