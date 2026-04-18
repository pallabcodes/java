package com.backend.designpatterns.behavioral.strategy;

import java.math.BigDecimal;

/**
 * Step 1: THE STRATEGY CONTRACT (L5 Functional Standard)
 * 
 * Instead of creating an interface that 10 different classes must implement,
 * we use a @FunctionalInterface.
 */
@FunctionalInterface
public interface Step01_DiscountStrategy {
    
    /**
     * Applies a discount algorithm to the given amount.
     */
    BigDecimal apply(BigDecimal originalAmount);
}
