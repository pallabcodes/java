package netflix.generics;

import netflix.generics.config.GenericsConfig;
import netflix.generics.service.GenericsDemoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Netflix Production-Grade Java Generics Application
 * 
 * This application demonstrates comprehensive Java Generics concepts following
 * Netflix's coding standards. It provides real-world examples of advanced
 * generics usage in production microservices.
 * 
 * Key Features Demonstrated:
 * - Basic Generics: Type parameters, generic classes, methods, and interfaces
 * - Advanced Generics: Wildcards, bounds, variance, and type erasure
 * - Generic Collections: Custom generic data structures and algorithms
 * - Generic APIs: Service layers with generic type safety
 * - Variance: Covariance, contravariance, and invariance
 * - Bounds: Upper bounds, lower bounds, and multiple bounds
 * - Wildcards: Unbounded, upper-bounded, and lower-bounded wildcards
 * - Type Erasure: Runtime type information and reflection
 * - Annotation Generics: Generic annotations and type parameters
 * - Performance: Generic performance optimizations and caching
 * 
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class JavaGenericsApplication implements CommandLineRunner {

    private final GenericsDemoService genericsDemoService;
    private final GenericsConfig genericsConfig;

    public JavaGenericsApplication(GenericsDemoService genericsDemoService, 
                                 GenericsConfig genericsConfig) {
        this.genericsDemoService = genericsDemoService;
        this.genericsConfig = genericsConfig;
    }

    /**
     * Main method to start the Java Generics demonstration application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaGenericsApplication.class, args);
    }

    /**
     * Runs the generics demonstration after Spring Boot initialization
     * 
     * @param args command line arguments
     * @throws Exception if demonstration fails
     */
    @Override
    public void run(String... args) throws Exception {
        genericsDemoService.runAllGenericsDemonstrations();
    }
}
