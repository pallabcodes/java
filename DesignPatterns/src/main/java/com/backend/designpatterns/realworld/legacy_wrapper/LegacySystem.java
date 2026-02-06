package com.backend.designpatterns.realworld.legacy_wrapper;

// Adaptee (Legacy System)
public class LegacySystem {
    public String fetchXML(String id) {
        System.out.println("Legacy: Fetching XML for ID " + id);
        // Simulate occasional failure
        if (Math.random() < 0.2) {
             throw new RuntimeException("Connection timeout");
        }
        return "<data><id>" + id + "</id><value>LegacyContent</value></data>";
    }
}
