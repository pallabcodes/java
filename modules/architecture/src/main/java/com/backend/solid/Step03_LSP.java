package com.backend.solid;

/**
 * Step 03: Liskov Substitution Principle (LSP)
 * 
 * L5 Principles:
 * 1. Substitutability: Objects of a superclass should be replaceable with objects of its subclasses 
 *    without affecting the correctness of the program.
 * 2. Contract Adherence: Subclasses must not violate the invariants of the base type.
 * 3. Behavior Consistency: Clients should be able to rely on the interface contracts.
 */
public class Step03_LSP {

    // Base type for Cloud Storage
    public interface CloudStorage {
        void upload(String key, byte[] data);
        byte[] download(String key);
    }

    // Google Cloud Storage Implementation (Properly adheres to contract)
    public static class GCSStorage implements CloudStorage {
        public void upload(String key, byte[] data) {
            System.out.println("Uploading " + key + " to GCS Bucket...");
        }
        public byte[] download(String key) {
            System.out.println("Downloading " + key + " from GCS...");
            return new byte[0];
        }
    }

    // Local Disk Implementation (Properly adheres to contract)
    public static class LocalStorage implements CloudStorage {
        public void upload(String key, byte[] data) {
            System.out.println("Writing " + key + " to local /tmp storage...");
        }
        public byte[] download(String key) {
            System.out.println("Reading " + key + " from local disk...");
            return new byte[0];
        }
    }

    // ⛔ WRONG: A ReadOnlyStorage that throws exceptions on upload() violates LSP 
    // because clients expecting 'CloudStorage' will break.
    
    // The Processor (Relies on CloudStorage contract)
    public static class StorageProcessor {
        public void process(CloudStorage storage, String filename) {
            storage.upload(filename, "dummy-data".getBytes());
            storage.download(filename);
            System.out.println("Storage workflow completed successfully.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 03: Liskov Substitution Principle (Cloud Storage) ===");
        
        StorageProcessor processor = new StorageProcessor();
        
        // We can swap GCS for Local without the processor ever knowing or breaking
        processor.process(new GCSStorage(), "report_q1.pdf");
        System.out.println("---");
        processor.process(new LocalStorage(), "backup.zip");
    }
}
