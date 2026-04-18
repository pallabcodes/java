package netflix.metaprogramming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Netflix Production-Grade Java Meta Programming Application
 * 
 * This application demonstrates comprehensive meta programming concepts including:
 * - Reflection and dynamic class loading
 * - Annotations and annotation processing
 * - Dynamic proxies and AOP
 * - Bytecode manipulation and code generation
 * - Serialization and deserialization
 * - Custom class loaders and module systems
 * - Runtime code generation and modification
 * - Performance monitoring and optimization
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@EnableScheduling
public class JavaMetaProgrammingApplication {

    public static void main(String[] args) {
        log.info("Starting Netflix Java Meta Programming Application...");
        
        try {
            // Set system properties for meta programming
            System.setProperty("java.security.manager", "false");
            System.setProperty("sun.reflect.inflationThreshold", "15");
            System.setProperty("jdk.reflect.useDirectMethodHandle", "true");
            
            // Enable preview features for advanced meta programming
            System.setProperty("jdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK", "0");
            
            SpringApplication.run(JavaMetaProgrammingApplication.class, args);
            
            log.info("Netflix Java Meta Programming Application started successfully!");
            log.info("Application is running on: http://localhost:8080/meta-programming");
            log.info("Swagger UI available at: http://localhost:8080/meta-programming/swagger-ui.html");
            log.info("API Documentation available at: http://localhost:8080/meta-programming/api-docs");
            
        } catch (Exception e) {
            log.error("Failed to start Netflix Java Meta Programming Application", e);
            System.exit(1);
        }
    }
}
