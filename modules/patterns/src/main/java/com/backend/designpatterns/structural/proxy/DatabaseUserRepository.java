package com.backend.designpatterns.structural.proxy;

// Role: Real Subject
public class DatabaseUserRepository implements UserRepository {

    @Override
    public User findById(String id) {
        System.out.println("DB: Fetching user " + id + " from database...");
        simulateSlowQuery();
        return new User(id, "User-" + id);
    }

    private void simulateSlowQuery() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
