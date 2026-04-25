package com.backend.core.concurrency;

public class ConcurrencyDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=========================================================");
        System.out.println("=== L7 CORE JAVA DEMO: MODERN CONCURRENCY & MEMORY   ===");
        System.out.println("=========================================================\n");

        System.out.println(">>> EXECUTING STEP 01: VIRTUAL THREADS & PINNING");
        Step01_VirtualThreadMechanics.main(args);
        
        System.out.println("\n---------------------------------------------------------\n");

        System.out.println(">>> EXECUTING STEP 02: HIGH-THROUGHPUT PRIMITIVES");
        Step02_ModernConcurrencyPrimitives.main(args);

        System.out.println("\n---------------------------------------------------------\n");

        System.out.println(">>> EXECUTING STEP 03: MEMORY PRESSURE & CONTENTION");
        Step03_MemoryPressureAndContention.main(args);

        System.out.println("\n---------------------------------------------------------\n");

        System.out.println(">>> EXECUTING STEP 04: STRUCTURED CONCURRENCY");
        Step04_StructuredConcurrency.main(args);

        System.out.println("\n---------------------------------------------------------\n");

        System.out.println(">>> EXECUTING STEP 05: SCOPED VALUES");
        Step05_ScopedValues.main(args);

        System.out.println("\n=========================================================");
        System.out.println("=== END OF L7 CONCURRENCY & MEMORY MASTERY            ===");
        System.out.println("=========================================================");
    }
}
