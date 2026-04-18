package com.backend.designpatterns.structural.proxy;

/**
 * Step 3: REAL SUBJECT (User)
 */
public class Step03_DatabaseUserRepository implements Step02_UserRepository {

    @Override
    public Step01_User findById(String id) {
        System.out.println("DB: Fetching user " + id + " from database...");
        simulateSlowQuery();
        return new Step01_User(id, "User-" + id);
    }

    private void simulateSlowQuery() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
