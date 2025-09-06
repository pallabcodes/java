package netflix.functional;

import netflix.functional.config.FunctionalConfig;
import netflix.functional.service.FunctionalDemoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Netflix Production-Grade Java Functional Programming Application
 * 
 * This application demonstrates comprehensive Java Functional Programming concepts following
 * Netflix's coding standards. It provides real-world examples of advanced
 * functional programming usage in production microservices.
 * 
 * Key Features Demonstrated:
 * - Functional Interfaces: All built-in functional interfaces with examples
 * - Lambda Expressions: Anonymous functions and closures
 * - Method References: Static, instance, and constructor references
 * - Stream API: Intermediate and terminal operations
 * - Optional: Monadic operations and null safety
 * - CompletableFuture: Asynchronous programming and composition
 * - Reactive Programming: Project Reactor and reactive streams
 * - Custom Functional Interfaces: Higher-order functions and composition
 * - Monadic Operations: Map, flatMap, filter, reduce
 * - Currying and Partial Application: Function decomposition
 * - Function Composition: Combining functions
 * - Immutable Data: Functional data structures
 * - Pattern Matching: Switch expressions and sealed classes
 * - Performance: Parallel streams and async processing
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class JavaFunctionalProgrammingApplication implements CommandLineRunner {

    private final FunctionalDemoService functionalDemoService;
    private final FunctionalConfig functionalConfig;

    public JavaFunctionalProgrammingApplication(FunctionalDemoService functionalDemoService, 
                                              FunctionalConfig functionalConfig) {
        this.functionalDemoService = functionalDemoService;
        this.functionalConfig = functionalConfig;
    }

    /**
     * Main method to start the Java Functional Programming demonstration application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaFunctionalProgrammingApplication.class, args);
    }

    /**
     * Runs the functional programming demonstration after Spring Boot initialization
     * 
     * @param args command line arguments
     * @throws Exception if demonstration fails
     */
    @Override
    public void run(String... args) throws Exception {
        functionalDemoService.runAllFunctionalDemonstrations();
    }
}
