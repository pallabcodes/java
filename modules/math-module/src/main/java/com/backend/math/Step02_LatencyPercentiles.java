package com.backend.math;

import java.util.Arrays;

/**
 * Step 02: Observability & Latency Stats (P99 Percentiles)
 * 
 * L5 Principles:
 * 1. Tail Latency Analysis: Mean/Average hides the "bad" user experiences.
 * 2. Outlier Awareness: P99.9 and P99.99 are critical for reliability at Google scale.
 * 3. Quantile Math: Choosing the right percentile for SLOs.
 */
public class Step02_LatencyPercentiles {

    /**
     * Calculates the value at a specific percentile in a dataset.
     * @param latencies array of latency values (ms)
     * @param percentile value between 0 and 100
     */
    public static double getPercentile(double[] latencies, double percentile) {
        if (latencies == null || latencies.length == 0) return 0;
        
        double[] sorted = Arrays.copyOf(latencies, latencies.length);
        Arrays.sort(sorted);
        
        int index = (int) Math.ceil(percentile / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, index)];
    }

    public static void main(String[] args) {
        System.out.println("=== Step 02: Latency Percentiles (SRE Math) ===");

        // Simulating Google Load Balancer latencies
        double[] gfeLatencies = {
            12.5, 15.2, 11.8, 14.1, 13.9, 12.1, 500.2, 140.5, 12.8, 13.2,
            11.9, 15.6, 14.8, 12.3, 13.7, 12.9, 14.1, 15.0, 12.5, 13.0
        };

        System.out.println("Dataset size: " + gfeLatencies.length);

        // Average (Misleading)
        double average = Arrays.stream(gfeLatencies).average().orElse(0);
        System.out.printf("Average Latency: %.2f ms (Looks good!)\n", average);

        // P50 (Median)
        System.out.printf("P50 (Median): %.2f ms\n", getPercentile(gfeLatencies, 50));

        // P95 (Most users)
        System.out.printf("P95 Latency: %.2f ms\n", getPercentile(gfeLatencies, 95));

        // P99 (Tail latency - The real problem)
        System.out.printf("P99 Latency: %.2f ms\n", getPercentile(gfeLatencies, 99));
        
        System.out.println("\nL5 Insight: The P99 is ~38x slower than the average.");
    }
}
