package com.backend.lowlevel;

import jdk.jfr.Recording;
import jdk.jfr.Configuration;

/**
 * Step 05: Observability & Profiling (JFR)
 * 
 * L7 Mastery:
 * 1. Continuous Profiling: Capturing low-level telemetry with <1% overhead.
 * 2. On-demand Diagnostics: Programmatically starting a recording when an anomaly occurs.
 * 3. Event Customization: Defining application-specific events for deep traceability.
 */
public class Step05_Observability {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 05: Observability (Java Flight Recorder - JFR) ===");

        // Start a JFR recording programmatically
        try (Recording recording = new Recording(Configuration.getConfiguration("default"))) {
            
            recording.start();
            System.out.println("JFR Recording Started...");

            // Simulate some high-throughput work
            performWork();

            recording.stop();
            System.out.println("JFR Recording Stopped.");
            
            // Note: In production, we would save 'recording.dump(path)' for analysis in JMC or Grafana.
            System.out.println("Analysis: L7 Engineers use JFR to find allocation hotspots and TLAB contention.");
        }
    }

    private static void performWork() {
        for (int i = 0; i < 1_000_000; i++) {
            String.valueOf(i).hashCode(); // Dummy allocation-heavy work
        }
    }
}
