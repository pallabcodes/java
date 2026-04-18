package com.backend.designpatterns.realworld.legacy_wrapper;

// Decorator (Retry Logic)
public class RetryDecorator implements ModernService {
    private final ModernService wrappee;
    private static final int MAX_RETRIES = 3;

    public RetryDecorator(ModernService wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public String getData(String id) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return wrappee.getData(id);
            } catch (Exception e) {
                attempts++;
                System.out.println("Decorator: Retry attempt " + attempts + " for " + id);
            }
        }
        throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts");
    }
}
