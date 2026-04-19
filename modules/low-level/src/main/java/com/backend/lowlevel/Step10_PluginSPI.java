package com.backend.lowlevel;

import java.util.ServiceLoader;

/**
 * Step 10: Plugin Architecture with SPI (Service Provider Interface)
 * 
 * L7 Principles:
 * 1. Modular Evolution: Adding functionality at runtime without modifying core code.
 * 2. Inversion of Control: The core defines the service; the plugin provides the implementation.
 * 3. Discovery: Using 'ServiceLoader' to dynamically find implementations in the classpath.
 */
public class Step10_PluginSPI {

    // The Service Interface (Contract)
    public interface PayloadProcessor {
        String process(byte[] data);
        String getProcessorName();
    }

    // A concrete implementation (Mock Plugin)
    public static class JsonProcessor implements PayloadProcessor {
        @Override public String process(byte[] data) { return "{ \"status\": \"processed\" }"; }
        @Override public String getProcessorName() { return "JSON_PLUGIN"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 10: Plugin Architecture (SPI Mastery) ===");

        // In a real L7 system, these would be loaded from external JARs via ServiceLoader.
        // For the demo, we use the ServiceLoader API simulation.
        ServiceLoader<PayloadProcessor> loader = ServiceLoader.load(PayloadProcessor.class);

        System.out.println("Scanning for Payload Processors...");
        
        // Manual simulation of registry discovery
        PayloadProcessor defaultProcessor = new JsonProcessor();
        System.out.println("Found Processor: " + defaultProcessor.getProcessorName());
        System.out.println("Sample Output: " + defaultProcessor.process(new byte[0]));

        System.out.println("\nL5 Insight: ServiceLoader is the mechanism behind JDBC driver discovery and advanced JAR-based plugin systems.");
    }
}
