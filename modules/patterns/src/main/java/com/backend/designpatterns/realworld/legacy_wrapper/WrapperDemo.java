package com.backend.designpatterns.realworld.legacy_wrapper;

public class WrapperDemo {

    public static void main(String[] args) {
        
        // 1. Raw Legacy Access (Unstable, XML)
        LegacySystem legacy = new LegacySystem();

        // 2. Adapter (Stable Interface, JSON)
        ModernService adapter = new LegacyAdapter(legacy);

        // 3. Decorator (Retry Logic)
        ModernService retrying = new RetryDecorator(adapter);

        // 4. Proxy (Caching) - Wrap the retry logic so we cache successful retried attempts!
        ModernService service = new CachingProxy(retrying);
        
        System.out.println("--- Wrapper Chain Created ---");
        
        // First Call (Cache Miss, might retry)
        try {
            System.out.println("Result 1: " + service.getData("101"));
        } catch (Exception e) {
            System.out.println("Error 1: " + e.getMessage());
        }

        System.out.println();

        // Second Call (Should be Cache Hit)
        System.out.println("Result 2: " + service.getData("101"));
    }
}
