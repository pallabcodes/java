package com.backend.designpatterns.realworld.chat_system;

import java.util.ArrayList;
import java.util.List;

// Concrete Mediator
public class ChatRoom implements ChatMediator {
    private List<User> users;

    public ChatRoom() {
        this.users = new ArrayList<>();
    }

    @Override
    public void addUser(User user) {
        this.users.add(user);
    }

    @Override
    public void sendMessage(String message, User sender) {
        for (User user : this.users) {
            // Mediator logic: Don't send to self
            if (user != sender) {
                user.receive(message);
            }
        }
    }
}
