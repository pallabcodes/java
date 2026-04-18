package netflix.functional.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Netflix Production-Grade Functional Programming Configuration Properties
 * 
 * This class centralizes all functional programming demonstration configuration properties
 * following Netflix's microservices configuration management standards.
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "functional")
@Validated
public class FunctionalConfig {

    /**
     * Examples configuration
     */
    @NotNull
    private Examples examples = new Examples();

    /**
     * Performance configuration
     */
    @NotNull
    private Performance performance = new Performance();

    /**
     * Security configuration
     */
    @NotNull
    private Security security = new Security();

    @Data
    public static class Examples {
        /**
         * Enable functional interfaces examples
         */
        private Boolean enableFunctionalInterfaces = true;

        /**
         * Enable lambda expressions examples
         */
        private Boolean enableLambdaExpressions = true;

        /**
         * Enable method references examples
         */
        private Boolean enableMethodReferences = true;

        /**
         * Enable streams examples
         */
        private Boolean enableStreams = true;

        /**
         * Enable optional examples
         */
        private Boolean enableOptional = true;

        /**
         * Enable completable future examples
         */
        private Boolean enableCompletableFuture = true;

        /**
         * Enable reactive programming examples
         */
        private Boolean enableReactiveProgramming = true;

        /**
         * Enable custom functional examples
         */
        private Boolean enableCustomFunctional = true;

        /**
         * Enable higher-order functions examples
         */
        private Boolean enableHigherOrderFunctions = true;

        /**
         * Enable monadic operations examples
         */
        private Boolean enableMonadicOperations = true;

        /**
         * Enable currying examples
         */
        private Boolean enableCurrying = true;

        /**
         * Enable partial application examples
         */
        private Boolean enablePartialApplication = true;

        /**
         * Enable function composition examples
         */
        private Boolean enableFunctionComposition = true;

        /**
         * Enable immutable data examples
         */
        private Boolean enableImmutableData = true;

        /**
         * Enable pattern matching examples
         */
        private Boolean enablePatternMatching = true;
    }

    @Data
    public static class Performance {
        /**
         * Enable caching
         */
        private Boolean enableCaching = true;

        /**
         * Enable metrics
         */
        private Boolean enableMetrics = true;

        /**
         * Enable profiling
         */
        private Boolean enableProfiling = false;

        /**
         * Enable parallel streams
         */
        private Boolean enableParallelStreams = true;

        /**
         * Enable async processing
         */
        private Boolean enableAsyncProcessing = true;
    }

    @Data
    public static class Security {
        /**
         * Enable validation
         */
        private Boolean enableValidation = true;

        /**
         * Enable sanitization
         */
        private Boolean enableSanitization = true;
    }
}
