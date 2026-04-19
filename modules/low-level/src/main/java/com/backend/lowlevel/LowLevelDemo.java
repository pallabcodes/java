package com.backend.lowlevel;

/**
 * L7 Low-Level Java Mastery Demo (Google -> Netflix Prep)
 */
public class LowLevelDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("\n--- STARTING L7 LOW-LEVEL JAVA MASTERY DEMO (JAVA 21) ---\n");

        Step01_MemoryLayout.main(null);
        System.out.println();

        Step02_VirtualThreads.main(null);
        System.out.println();

        Step03_ForeignMemory.main(null);
        System.out.println();

        // Step 04 uses Incubating features and requires JVM flags
        try {
            Step04_VectorAPI.main(null);
        } catch (NoClassDefFoundError | Exception e) {
            System.out.println("Step 04 (Vector API) skipped: Requires --add-modules jdk.incubator.vector");
        }
        System.out.println();

        Step05_Observability.main(null);

        System.out.println("\n--- L7 LOW-LEVEL JAVA MASTERY DEMO COMPLETE ---");
    }
}
