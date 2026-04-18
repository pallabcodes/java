package com.backend.designpatterns.behavioral.command;

// Role: Receiver
public class UserService {
    public void createUser(String name) {
        System.out.println("DB: Insert User '" + name + "'");
    }

    public void deleteUser(String name) {
        System.out.println("DB: Delete User '" + name + "'");
    }
}
