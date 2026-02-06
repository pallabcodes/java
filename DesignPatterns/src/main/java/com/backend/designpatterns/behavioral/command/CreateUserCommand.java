package com.backend.designpatterns.behavioral.command;

// Role: Concrete Command
public class CreateUserCommand implements Command {

    private final UserService service;
    private final String name;

    public CreateUserCommand(UserService service, String name) {
        this.service = service;
        this.name = name;
    }

    @Override
    public void execute() {
        service.createUser(name);
    }

    @Override
    public void undo() {
        service.deleteUser(name);
    }
}
