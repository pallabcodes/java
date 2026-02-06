package com.backend.designpatterns.realworld.chat_system;

public class ChatSystemDemo {

    public static void main(String[] args) {
        
        // 1. Facade setup
        ChatService service = new ChatService();
        ChatMediator room = service.getMediator();

        // 2. Users (Observers)
        User user1 = new ChatUser(room, "Alice");
        User user2 = new ChatUser(room, "Bob");
        User user3 = new ChatUser(room, "Charlie");

        // 3. Facade Action (Join)
        service.join(user1);
        service.join(user2);
        service.join(user3);

        System.out.println("--- Chat Started ---");

        // 4. Interaction (Mediator handles routing)
        user1.send("Hello everyone!");
        
        System.out.println();
        
        user2.send("Hi Alice!");
    }
}
