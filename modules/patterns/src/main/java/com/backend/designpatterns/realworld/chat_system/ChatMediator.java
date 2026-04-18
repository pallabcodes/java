package com.backend.designpatterns.realworld.chat_system;

// Mediator Interface
public interface ChatMediator {
    void sendMessage(String message, User user);
    void addUser(User user);
}
