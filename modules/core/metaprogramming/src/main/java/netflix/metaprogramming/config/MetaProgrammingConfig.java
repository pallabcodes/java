package netflix.metaprogramming.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Arrays;
import java.util.List;

/**
 * Netflix Production-Grade Meta Programming Configuration
 * 
 * This configuration class provides type-safe access to meta programming
 * configuration properties and sets up necessary beans for meta programming
 * operations including reflection, annotations, proxies, and bytecode manipulation.
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "meta-programming")
public class MetaProgrammingConfig {

    /**
     * Reflection configuration
     */
    private ReflectionConfig reflection = new ReflectionConfig();
    
    /**
     * Annotations configuration
     */
    private AnnotationsConfig annotations = new AnnotationsConfig();
    
    /**
     * Proxies configuration
     */
    private ProxiesConfig proxies = new ProxiesConfig();
    
    /**
     * Bytecode configuration
     */
    private BytecodeConfig bytecode = new BytecodeConfig();
    
    /**
     * Serialization configuration
     */
    private SerializationConfig serialization = new SerializationConfig();
    
    /**
     * Monitoring configuration
     */
    private MonitoringConfig monitoring = new MonitoringConfig();

    @Data
    public static class ReflectionConfig {
        private boolean enabled = true;
        private boolean cacheEnabled = true;
        private int cacheSize = 1000;
        private boolean securityManagerEnabled = false;
    }

    @Data
    public static class AnnotationsConfig {
        private boolean processingEnabled = true;
        private boolean runtimeRetention = true;
        private boolean compileTimeRetention = true;
    }

    @Data
    public static class ProxiesConfig {
        private boolean dynamicProxiesEnabled = true;
        private boolean cglibProxiesEnabled = true;
        private boolean jdkProxiesEnabled = true;
    }

    @Data
    public static class BytecodeConfig {
        private boolean manipulationEnabled = true;
        private boolean asmEnabled = true;
        private boolean javassistEnabled = true;
        private boolean cglibEnabled = true;
    }

    @Data
    public static class SerializationConfig {
        private boolean jacksonEnabled = true;
        private boolean customSerializersEnabled = true;
        private boolean polymorphicSerializationEnabled = true;
    }

    @Data
    public static class MonitoringConfig {
        private boolean metricsEnabled = true;
        private boolean tracingEnabled = true;
        private boolean performanceMonitoringEnabled = true;
    }

    /**
     * Cache manager for meta programming operations
     */
    @Bean
    public CacheManager metaProgrammingCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = Arrays.asList(
            new ConcurrentMapCache("reflection-cache"),
            new ConcurrentMapCache("annotation-cache"),
            new ConcurrentMapCache("proxy-cache"),
            new ConcurrentMapCache("bytecode-cache"),
            new ConcurrentMapCache("serialization-cache")
        );
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
