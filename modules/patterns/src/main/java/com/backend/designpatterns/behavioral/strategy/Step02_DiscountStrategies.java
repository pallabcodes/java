package com.backend.designpatterns.behavioral.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Step 2: STRATEGY FACTORY (Functional Provider)
 * 
 * We simply use static factory methods returning lambda expressions.
 * This completely eliminates "Class Explosion".
 */
public final class Step02_DiscountStrategies {

    private Step02_DiscountStrategies() {}

    public static Step01_DiscountStrategy noDiscount() {
        return amount -> amount;
    }

    public static Step01_DiscountStrategy percentage(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        return amount -> {
            BigDecimal multiplier = BigDecimal.valueOf(1.0 - (percent / 100.0));
            return amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        };
    }

    public static Step01_DiscountStrategy flatRate(double discountAmount) {
        return amount -> {
            BigDecimal discount = BigDecimal.valueOf(discountAmount);
            BigDecimal result = amount.subtract(discount);
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        };
    }

    public static Step01_DiscountStrategy tieredDiscount(double threshold, double discountAmount) {
        return amount -> {
            if (amount.compareTo(BigDecimal.valueOf(threshold)) > 0) {
                return flatRate(discountAmount).apply(amount);
            }
            return amount;
        };
    }
}
