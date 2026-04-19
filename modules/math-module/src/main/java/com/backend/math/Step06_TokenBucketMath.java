package com.backend.math;

/**
 * Step 06: Rate Limiting Math (Token Bucket Core)
 * 
 * L5 Principles:
 * 1. Distributed Fairness: Ensuring API callers stay within quotas.
 * 2. Mathematical Bursting: Allowing temporary higher volume (tokens) without sustained overload.
 * 3. Atomic Math: Refilling tokens based on time elapsed since last request.
 */
public class Step06_TokenBucketMath {

    private final long capacity;
    private final double refillRatePerSec;
    private double tokens;
    private long lastRefillTimestamp;

    public Step06_TokenBucketMath(long capacity, double refillRatePerSec) {
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double elapsedSec = (now - lastRefillTimestamp) / 1000.0;
        tokens = Math.min(capacity, tokens + (elapsedSec * refillRatePerSec));
        lastRefillTimestamp = now;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Step 06: Token Bucket Math (System Math) ===");

        // Rate: 1 request/sec, Burst: 5 requests
        Step06_TokenBucketMath limiter = new Step06_TokenBucketMath(5, 1.0);

        System.out.println("Consuming 5 burst requests...");
        for (int i = 1; i <= 6; i++) {
            System.out.println("Request " + i + ": " + (limiter.tryConsume() ? "ALLOWED" : "RATE_LIMITED"));
        }

        System.out.println("\nWaiting 2 seconds for refill...");
        Thread.sleep(2000);

        System.out.println("Request 7 (refilled): " + (limiter.tryConsume() ? "ALLOWED" : "RATE_LIMITED"));
        System.out.println("Request 8 (empty again): " + (limiter.tryConsume() ? "ALLOWED" : "RATE_LIMITED"));
    }
}
