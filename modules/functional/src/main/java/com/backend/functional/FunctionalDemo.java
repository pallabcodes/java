package com.backend.functional;

/**
 * L5 Google-Grade Functional Programming Demo
 */
public class FunctionalDemo {

    public static void main(String[] args) {
        FunctionalDemo demo = new FunctionalDemo();
        demo.run();
    }

    public void run() {
        System.out.println("\n--- STARTING L5 FUNCTIONAL PROGRAMMING DEMO ---\n");

        Step01_PureFunctions.main(null);
        System.out.println();
        
        Step02_EmailFilterPipeline.main(null);
        System.out.println();
        
        Step03_ResultMonad.main(null);
        System.out.println();
        
        Step04_ThreadSafeMemoizer.main(null);
        System.out.println();
        
        Step05_LazyResource.main(null);
        System.out.println();
        
        Step06_ConfigInjection.main(null);

        System.out.println("\n--- L5 FUNCTIONAL PROGRAMMING DEMO COMPLETE ---");
    }
}
