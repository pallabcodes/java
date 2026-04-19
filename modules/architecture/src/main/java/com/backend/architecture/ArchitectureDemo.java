package com.backend.architecture;

import com.backend.solid.SolidDemo;

/**
 * L7 Architecture Mastery Demo
 */
public class ArchitectureDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("\n--- STARTING L7 ARCHITECTURE MASTERY DEMO ---\n");

        System.out.println(">>> 1. SOLID Principles (Google Standard)");
        SolidDemo.main(null);
        System.out.println();

        System.out.println(">>> 2. Design Primitives (Systems Thinking)");
        DesignPrimitives.main(null);

        System.out.println("\n--- L7 ARCHITECTURE MASTERY DEMO COMPLETE ---");
    }
}
