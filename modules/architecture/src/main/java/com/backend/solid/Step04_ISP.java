package com.backend.solid;

/**
 * Step 04: Interface Segregation Principle (ISP)
 * 
 * L5 Principles:
 * 1. Interface Lean-ness: No client should be forced to depend on methods it does not use.
 * 2. Granularity: Small, atomic interfaces are better than large, monolithic ones.
 * 3. Composition: Complex behavior should be composed of multiple simple interfaces.
 */
public class Step04_ISP {

    // Monolithic (Anti-pattern)
    // interface CloudResourceHandler { delete(); write(); read(); reboot(); }

    // 🏆 L5 Way: Segregated Interfaces
    
    public interface ResourceReader {
        void read();
    }

    public interface ResourceWriter {
        void write();
    }

    public interface ResourceAdmin {
        void reboot();
        void delete();
    }

    // A low-permission logging service only needs Read access
    public static class LogViewer implements ResourceReader {
        public void read() { System.out.println("LogViewer: Reading GCloud Logs..."); }
    }

    // A high-privileged service can implement multiple interfaces
    public static class AdminTool implements ResourceReader, ResourceAdmin {
        public void read() { System.out.println("AdminTool: Checking status..."); }
        public void reboot() { System.out.println("AdminTool: Rebooting instance..."); }
        public void delete() { System.out.println("AdminTool: Terminating instance..."); }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Interface Segregation Principle (Cloud Ops) ===");
        
        LogViewer viewer = new LogViewer();
        viewer.read();
        // viewer.delete(); // <-- COMPILER ERROR! (This is exactly what we want)

        System.out.println("---");
        
        AdminTool tool = new AdminTool();
        tool.read();
        tool.reboot();
    }
}
