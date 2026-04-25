package com.backend.lowlevel;

import java.util.ServiceLoader;

/**
 * Step 10: Plugin Architecture with SPI (Service Provider Interface)
 * 
 * L7 Principles:
 * 1. Modular Evolution: Adding functionality at runtime without modifying core code.
 * 2. Inversion of Control: The core defines the service; the plugin provides the implementation.
 * 3. Dynamic Discovery: Using 'ServiceLoader' and 'META-INF/services'.
 */
public class Step10_PluginSPI {

    // The Service Interface (Contract)
    public interface PayloadProcessor {
        String process(byte[] data);
        String getProcessorName();
    }

    // A concrete implementation (Registered in META-INF/services)
    public static class JsonProcessor implements PayloadProcessor {
        public JsonProcessor() {} // Required by ServiceLoader
        @Override public String process(byte[] data) { return "{ \"status\": \"processed\" }"; }
        @Override public String getProcessorName() { return "JSON_PLUGIN (True SPI)"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 10: Plugin Architecture (True SPI Discovery) ===");

        // In a real system, the ServiceLoader would look for JARs in the classpath
        ServiceLoader<PayloadProcessor> loader = ServiceLoader.load(PayloadProcessor.class);

        System.out.println("Scanning for registered Payload Processors...");
        
        boolean found = false;
        for (PayloadProcessor processor : loader) {
            System.out.println("Found via SPI: " + processor.getProcessorName());
            System.out.println("Process Output: " + processor.process(new byte[0]));
            found = true;
        }

        if (!found) {
            System.out.println("⚠️ No processors found. Ensure META-INF/services is configured.");
        }

        System.out.println("\nL7 Mastery: This pattern is used by JDBC, JNDI, and the Java Compiler itself.");
    }
}
