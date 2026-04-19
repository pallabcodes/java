package com.backend.math;

/**
 * L5 Google-Grade Math & Algorithms Demo
 */
public class MathDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("\n--- STARTING L5 MATH & ALGORITHMS DEMO ---\n");

        Step01_BigDecimalPrecision.main(null);
        System.out.println();

        Step02_LatencyPercentiles.main(null);
        System.out.println();

        Step03_HyperLogLogEstimation.main(null);
        System.out.println();

        Step04_BloomFilter.main(null);
        System.out.println();

        Step05_HaversineDistance.main(null);
        System.out.println();

        Step06_TokenBucketMath.main(null);

        System.out.println("\n--- L5 MATH & ALGORITHMS DEMO COMPLETE ---");
    }
}
