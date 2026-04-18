package netflix.generics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Netflix Production-Grade Generics Configuration Properties
 * 
 * This class centralizes all generics demonstration configuration properties
 * following Netflix's microservices configuration management standards.
 * 
 * @author Netflix Java Generics Team
 * @version 1.0
 * @since 2024
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "generics")
@Validated
public class GenericsConfig {

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
         * Enable basic generics examples
         */
        private Boolean enableBasicGenerics = true;

        /**
         * Enable advanced generics examples
         */
        private Boolean enableAdvancedGenerics = true;

        /**
         * Enable generic collections examples
         */
        private Boolean enableGenericCollections = true;

        /**
         * Enable generic APIs examples
         */
        private Boolean enableGenericApis = true;

        /**
         * Enable variance examples
         */
        private Boolean enableVarianceExamples = true;

        /**
         * Enable bounds examples
         */
        private Boolean enableBoundsExamples = true;

        /**
         * Enable wildcard examples
         */
        private Boolean enableWildcardExamples = true;

        /**
         * Enable type erasure examples
         */
        private Boolean enableTypeErasureExamples = true;

        /**
         * Enable reflection generics examples
         */
        private Boolean enableReflectionGenerics = true;

        /**
         * Enable annotation generics examples
         */
        private Boolean enableAnnotationGenerics = true;
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
