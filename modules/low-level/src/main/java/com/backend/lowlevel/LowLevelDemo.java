package com.backend.lowlevel;

/**
 * L7 Low-Level Java Mastery Demo (Google -> Netflix Prep)
 */
public class LowLevelDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("\n--- STARTING L7 LOW-LEVEL JAVA MASTERY DEMO (FULL MASTERY) ---\n");

        Step01_MemoryLayout.main(null);
        System.out.println();

        Step02_VirtualThreads.main(null);
        System.out.println();

        Step03_ForeignMemory.main(null);
        System.out.println();

        // Step 04 uses Incubating features
        try {
            Step04_VectorAPI.main(null);
        } catch (NoClassDefFoundError | Exception e) {
            System.out.println("Step 04 (Vector API) skipped: Requires --add-modules jdk.incubator.vector");
        }
        System.out.println();

        Step05_Observability.main(null);
        System.out.println();

        Step06_Microbenchmarking.main(null);
        System.out.println();

        Step07_BinarySerialization.main(null);
        System.out.println();

        Step08_ReferenceTypes.main(null);
        System.out.println();

        Step09_NioSelectorEngine.main(null);
        System.out.println();

        Step10_PluginSPI.main(null);
        System.out.println();

        Step11_JMM_DeepDive.main(null);

        System.out.println("\n--- L7 LOW-LEVEL JAVA MASTERY DEMO COMPLETE ---");
    }
}
