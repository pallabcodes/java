package com.backend.designpatterns.behavioral.chain_of_responsibility;

import java.util.HashMap;
import java.util.Map;

/**
 * Step 3: CONCRETE LINK
 * 
 * Intercepts the request and checks for SPAM/DDoS behavior.
 */
public class Step03_RateLimitMiddleware extends Step02_Middleware {

    private final int requestPerMinute;
    private final Map<String, Integer> requestCounts = new HashMap<>();

    public Step03_RateLimitMiddleware(int requestPerMinute) {
        this.requestPerMinute = requestPerMinute;
    }

    @Override
    public boolean check(Step01_HttpRequest request) {
        System.out.println("[Middleware: RateLimit] Checking IP: " + request.ipAddress());
        
        int count = requestCounts.getOrDefault(request.ipAddress(), 0) + 1;
        requestCounts.put(request.ipAddress(), count);

        if (count > requestPerMinute) {
            System.err.println("  ❌ 429 TOO MANY REQUESTS: Request limit exceeded for " + request.ipAddress());
            return false;
        }

        System.out.println("  ✅ Rate limit passed.");
        return checkNext(request);
    }
}
