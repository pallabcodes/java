package com.backend.designpatterns.behavioral.chain_of_responsibility;

// Role: Request Object
public class Request {
    private final String user;
    private final boolean authenticated;
    private final String payload;

    public Request(String user, boolean authenticated, String payload) {
        this.user = user;
        this.authenticated = authenticated;
        this.payload = payload;
    }

    public String getUser() { return user; }
    public boolean isAuthenticated() { return authenticated; }
    public String getPayload() { return payload; }
}
