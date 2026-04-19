package com.backend.metaprogramming;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Step 05: Annotation Mastery (Framework Level)
 * 
 * L7 Principles:
 * 1. Declarative Programming: Moving logic from code to metadata.
 * 2. Scanners: Dynamically finding and processing annotated elements.
 * 3. Configuration Binding: Automating the mapping from properties to objects.
 */
public class Step05_AnnotationMastery {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigValue { String name(); }

    public static class AppConfig {
        @ConfigValue(name = "server.port")
        private int port;

        @ConfigValue(name = "server.host")
        private String host;

        @Override public String toString() { return "AppConfig{port=" + port + ", host='" + host + "'}"; }
    }

    /**
     * L7 Mastery: A mini configuration injector (Simulating Spring/Dagger logic).
     */
    public static void injectConfig(Object target, Map<String, String> properties) throws Exception {
        Class<?> clazz = target.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue config = field.getAnnotation(ConfigValue.class);
                String value = properties.get(config.name());
                
                if (value != null) {
                    field.setAccessible(true);
                    // L7 Note: Handling multiple types (int, String) introspectively
                    if (field.getType() == int.class) {
                        field.set(target, Integer.parseInt(value));
                    } else {
                        field.set(target, value);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 05: Annotation Processing (Configuration Demo) ===");

        Map<String, String> mockExternalProperties = new HashMap<>();
        mockExternalProperties.put("server.port", "8443");
        mockExternalProperties.put("server.host", "netflix-prod-api");

        AppConfig config = new AppConfig();
        injectConfig(config, mockExternalProperties);

        System.out.println("Injected Config: " + config);
        
        System.out.println("\nL5 Insight: This is how automated DI and Config management systems work.");
    }
}
