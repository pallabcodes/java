package com.backend.functional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * L5 Google-Grade Functional Programming Demo
 */
@SpringBootApplication
public class FunctionalDemo implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FunctionalDemo.class, args);
    }

    @Override
    public void run(String... args) {
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
