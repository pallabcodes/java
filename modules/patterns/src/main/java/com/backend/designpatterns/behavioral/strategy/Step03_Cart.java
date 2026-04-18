package com.backend.designpatterns.behavioral.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 3: THE CONTEXT
 * 
 * The Cart doesn't know anything about how discounts are calculated.
 * It strictly delegates the calculation to whatever `Step01_DiscountStrategy` was injected.
 */
public class Step03_Cart {

    private final List<BigDecimal> items = new ArrayList<>();
    private Step01_DiscountStrategy discountStrategy;

    public Step03_Cart() {
        // Default to no discount
        this.discountStrategy = Step02_DiscountStrategies.noDiscount();
    }

    public void addItem(double price) {
        items.add(BigDecimal.valueOf(price));
    }

    /**
     * DYNAMIC INJECTION
     */
    public void setDiscountStrategy(Step01_DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public BigDecimal calculateTotal() {
        BigDecimal subTotal = items.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Delegate arithmetic purely to the Strategy
        return discountStrategy.apply(subTotal);
    }
}
