package com.backend.designpatterns.structural.proxy;

/**
 * Step 1: DOMAIN ENTITY
 */
public class Step01_User {
    private final String id;
    private final String name;

    public Step01_User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "'}";
    }
}
