package com.backend.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Step 01: Precision & Financial Math
 * 
 * L5 Principles:
 * 1. Financial Accuracy: Never use float/double for money or ad-bids.
 * 2. Determinism: Controlled rounding modes ensure consistent results across platforms.
 * 3. Precision: Maintaining scale is critical for fractional calculations like bid overrides.
 */
public class Step01_BigDecimalPrecision {

    public record AdBid(String id, BigDecimal amount, String currency) {}

    /**
     * Google Ads Style: Appends a surcharge and rounds to nearest cent.
     * Uses HALF_EVEN (Banker's rounding) to minimize cumulative error.
     */
    public static AdBid applySurcharge(AdBid bid, double surchargePercent) {
        BigDecimal surcharge = BigDecimal.valueOf(surchargePercent)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        BigDecimal multiplier = BigDecimal.ONE.add(surcharge);
        BigDecimal finalAmount = bid.amount().multiply(multiplier)
                .setScale(2, RoundingMode.HALF_EVEN);
        
        return new AdBid(bid.id(), finalAmount, bid.currency());
    }

    public static void main(String[] args) {
        System.out.println("=== Step 01: BigDecimal Precision (Ads Math) ===");

        AdBid originalBid = new AdBid("AD-777", new BigDecimal("10.00"), "USD");

        // The danger of double
        double doubleAmount = 10.00;
        double doubleSurcharged = doubleAmount * 1.055;
        System.out.println("Double Result: " + doubleSurcharged); // Might lose precision

        // The safety of BigDecimal
        AdBid finalBid = applySurcharge(originalBid, 5.5);
        System.out.println("BigDecimal Result: " + finalBid.amount() + " " + finalBid.currency());

        // Demonstrating rounding
        AdBid tinyBid = new AdBid("AD-001", new BigDecimal("0.01"), "USD");
        AdBid surchargedTiny = applySurcharge(tinyBid, 4.5);
        System.out.println("Tiny Bid Surcharged: " + surchargedTiny.amount());
    }
}
