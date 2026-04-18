package com.backend.designpatterns.behavioral.chain_of_responsibility;

public class ChainDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Chain of Responsibility Demo (Middleware Pipeline) ===\n");

        // 1. Build the HTTP pipeline
        Step02_Middleware pipeline = new Step03_RateLimitMiddleware(2);
        
        // Fluent linking
        pipeline
            .linkWith(new Step04_AuthMiddleware())
            .linkWith(new Step05_LoggingMiddleware());

        // 2. Scenario A: Perfectly valid request
        System.out.println("--- Scenario A: Valid Authenticated Request ---");
        Step01_HttpRequest validReq = new Step01_HttpRequest("192.168.1.1", "/api/data", "Bearer VALID_TOKEN_123", "{}");
        boolean isSuccess1 = pipeline.check(validReq);
        System.out.println("Pipeline Result: " + (isSuccess1 ? "ALLOW" : "DENY") + "\n");

        // 3. Scenario B: Invalid/Expired Token (Fails at Node 2)
        System.out.println("--- Scenario B: Expired Token ---");
        Step01_HttpRequest expiredReq = new Step01_HttpRequest("192.168.1.2", "/api/data", "Bearer EXPIRED_TOKEN", "{}");
        boolean isSuccess2 = pipeline.check(expiredReq);
        System.out.println("Pipeline Result: " + (isSuccess2 ? "ALLOW" : "DENY") + "\n");

        // 4. Scenario C: Spamming / DDoS attempt (Fails at Node 1)
        System.out.println("--- Scenario C: Rate Limit Spamming ---");
        Step01_HttpRequest spamReq = new Step01_HttpRequest("192.168.1.99", "/api/spam", "Bearer VALID_TOKEN", "{}");
        
        System.out.println("Attempt 1:");
        pipeline.check(spamReq);
        
        System.out.println("Attempt 2:");
        pipeline.check(spamReq); // Limit is 2, this is the last allowed
        
        System.out.println("Attempt 3 (Should be blocked):");
        boolean isSuccessSpam = pipeline.check(spamReq); // Should be blocked here before hitting Auth
        System.out.println("Pipeline Result: " + (isSuccessSpam ? "ALLOW" : "DENY") + "\n");

        System.out.println("[L5 ACHIEVEMENT]: The pipeline cleanly orchestrates execution and " +
                           "guarantees short-circuiting, isolating Controller logic from cross-cutting concerns.");
    }
}
