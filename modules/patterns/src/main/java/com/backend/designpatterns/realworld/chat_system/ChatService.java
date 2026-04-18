package com.backend.designpatterns.realworld.chat_system;

// Facade
public class ChatService {
    private final ChatMediator chatRoom;

    public ChatService() {
        this.chatRoom = new ChatRoom();
    }

    public void join(User user) {
        chatRoom.addUser(user);
        System.out.println(user.getName() + " joined the chat.");
    }

    public ChatMediator getMediator() {
        return chatRoom;
    }
}
