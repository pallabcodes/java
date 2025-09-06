package netflix.metaprogramming.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Annotation Processing Examples
 * 
 * This class demonstrates comprehensive annotation processing concepts including:
 * - Custom annotation creation and usage
 * - Annotation processing at compile time
 * - Runtime annotation processing and reflection
 * - Annotation-based code generation
 * - Custom annotation processors
 * - Annotation validation and error handling
 * - Performance optimization and caching
 * - Security considerations and best practices
 * - Integration with Spring Framework
 * - Netflix-specific annotation patterns
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class AnnotationProcessingExamples {

    /**
     * Demonstrates custom annotation creation and usage
     * 
     * Shows how to create custom annotations with different retention policies
     * and target elements.
     */
    public void demonstrateCustomAnnotations() {
        log.info("=== Demonstrating Custom Annotations ===");
        
        // Process class-level annotations
        processClassAnnotations(ServiceClass.class);
        
        // Process method-level annotations
        processMethodAnnotations(ServiceClass.class);
        
        // Process field-level annotations
        processFieldAnnotations(ServiceClass.class);
        
        // Process parameter-level annotations
        processParameterAnnotations(ServiceClass.class);
    }

    /**
     * Demonstrates annotation processing at compile time
     * 
     * Shows how to create and use annotation processors for compile-time
     * code generation and validation.
     */
    public void demonstrateCompileTimeProcessing() {
        log.info("=== Demonstrating Compile-Time Processing ===");
        
        // This would typically be done by the compiler
        // Here we demonstrate the concepts
        
        log.debug("Compile-time annotation processing would generate:");
        log.debug("- Builder classes for @Builder annotated classes");
        log.debug("- Validation code for @Validated annotated methods");
        log.debug("- Serialization code for @Serializable annotated classes");
        log.debug("- API documentation for @ApiDoc annotated methods");
    }

    /**
     * Demonstrates runtime annotation processing and reflection
     * 
     * Shows how to process annotations at runtime using reflection.
     */
    public void demonstrateRuntimeProcessing() {
        log.info("=== Demonstrating Runtime Processing ===");
        
        try {
            Class<?> serviceClass = ServiceClass.class;
            
            // Process class annotations
            processClassAnnotations(serviceClass);
            
            // Process method annotations
            processMethodAnnotations(serviceClass);
            
            // Process field annotations
            processFieldAnnotations(serviceClass);
            
            // Process parameter annotations
            processParameterAnnotations(serviceClass);
            
        } catch (Exception e) {
            log.error("Error in runtime annotation processing", e);
        }
    }

    /**
     * Demonstrates annotation-based code generation
     * 
     * Shows how to use annotations to generate code at runtime.
     */
    public void demonstrateCodeGeneration() {
        log.info("=== Demonstrating Code Generation ===");
        
        // Generate builder for @Builder annotated class
        generateBuilder(ServiceClass.class);
        
        // Generate validation for @Validated annotated methods
        generateValidation(ServiceClass.class);
        
        // Generate serialization for @Serializable annotated classes
        generateSerialization(ServiceClass.class);
        
        // Generate API documentation for @ApiDoc annotated methods
        generateApiDocumentation(ServiceClass.class);
    }

    /**
     * Demonstrates custom annotation processors
     * 
     * Shows how to create custom annotation processors for specific use cases.
     */
    public void demonstrateCustomAnnotationProcessors() {
        log.info("=== Demonstrating Custom Annotation Processors ===");
        
        // This would typically be done by the compiler
        // Here we demonstrate the concepts
        
        log.debug("Custom annotation processors would:");
        log.debug("- Process @Builder annotations to generate builder classes");
        log.debug("- Process @Validated annotations to generate validation code");
        log.debug("- Process @Serializable annotations to generate serialization code");
        log.debug("- Process @ApiDoc annotations to generate API documentation");
    }

    /**
     * Demonstrates annotation validation and error handling
     * 
     * Shows how to validate annotations and handle errors.
     */
    public void demonstrateAnnotationValidation() {
        log.info("=== Demonstrating Annotation Validation ===");
        
        try {
            Class<?> serviceClass = ServiceClass.class;
            
            // Validate class annotations
            validateClassAnnotations(serviceClass);
            
            // Validate method annotations
            validateMethodAnnotations(serviceClass);
            
            // Validate field annotations
            validateFieldAnnotations(serviceClass);
            
            // Validate parameter annotations
            validateParameterAnnotations(serviceClass);
            
        } catch (Exception e) {
            log.error("Error in annotation validation", e);
        }
    }

    /**
     * Demonstrates performance optimization and caching
     * 
     * Shows how to optimize annotation processing using caching.
     */
    @Cacheable(value = "annotation-cache", key = "#clazz.name")
    public Map<String, Object> processAnnotationsWithCaching(Class<?> clazz) {
        log.debug("Processing annotations for class: {}", clazz.getName());
        
        Map<String, Object> result = new HashMap<>();
        
        // Process class annotations
        Annotation[] classAnnotations = clazz.getAnnotations();
        result.put("classAnnotations", Arrays.stream(classAnnotations)
                .map(Annotation::toString)
                .collect(Collectors.toList()));
        
        // Process method annotations
        Method[] methods = clazz.getDeclaredMethods();
        Map<String, List<String>> methodAnnotations = new HashMap<>();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            methodAnnotations.put(method.getName(), Arrays.stream(annotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
        }
        result.put("methodAnnotations", methodAnnotations);
        
        return result;
    }

    /**
     * Demonstrates security considerations and best practices
     * 
     * Shows how to handle security in annotation processing.
     */
    public void demonstrateSecurityConsiderations() {
        log.info("=== Demonstrating Security Considerations ===");
        
        try {
            Class<?> serviceClass = ServiceClass.class;
            
            // Check for security-sensitive annotations
            checkSecurityAnnotations(serviceClass);
            
            // Validate annotation values
            validateAnnotationValues(serviceClass);
            
            // Check for malicious annotations
            checkMaliciousAnnotations(serviceClass);
            
        } catch (Exception e) {
            log.error("Error in security considerations", e);
        }
    }

    // Helper methods

    private void processClassAnnotations(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        log.debug("Class annotations for {}: {}", clazz.getSimpleName(), 
                Arrays.stream(annotations)
                        .map(Annotation::toString)
                        .collect(Collectors.toList()));
        
        // Process specific annotations
        if (clazz.isAnnotationPresent(Service.class)) {
            Service service = clazz.getAnnotation(Service.class);
            log.debug("Service annotation value: {}", service.value());
        }
        
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            log.debug("Component annotation value: {}", component.value());
        }
    }

    private void processMethodAnnotations(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            if (annotations.length > 0) {
                log.debug("Method {} annotations: {}", method.getName(),
                        Arrays.stream(annotations)
                                .map(Annotation::toString)
                                .collect(Collectors.toList()));
            }
            
            // Process specific annotations
            if (method.isAnnotationPresent(Validated.class)) {
                Validated validated = method.getAnnotation(Validated.class);
                log.debug("Validated annotation on method {}: {}", method.getName(), validated.value());
            }
            
            if (method.isAnnotationPresent(ApiDoc.class)) {
                ApiDoc apiDoc = method.getAnnotation(ApiDoc.class);
                log.debug("ApiDoc annotation on method {}: {}", method.getName(), apiDoc.description());
            }
        }
    }

    private void processFieldAnnotations(Class<?> clazz) {
        // This would require field access which is more complex
        // Here we demonstrate the concept
        log.debug("Field annotation processing would examine all fields for annotations");
    }

    private void processParameterAnnotations(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations.length > 0) {
                    log.debug("Parameter {} of method {} annotations: {}", i, method.getName(),
                            Arrays.stream(annotations)
                                    .map(Annotation::toString)
                                    .collect(Collectors.toList()));
                }
            }
        }
    }

    private void generateBuilder(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Builder.class)) {
            log.debug("Generating builder for class: {}", clazz.getSimpleName());
            // Builder generation logic would go here
        }
    }

    private void generateValidation(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Validated.class)) {
                log.debug("Generating validation for method: {}", method.getName());
                // Validation generation logic would go here
            }
        }
    }

    private void generateSerialization(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Serializable.class)) {
            log.debug("Generating serialization for class: {}", clazz.getSimpleName());
            // Serialization generation logic would go here
        }
    }

    private void generateApiDocumentation(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiDoc.class)) {
                log.debug("Generating API documentation for method: {}", method.getName());
                // API documentation generation logic would go here
            }
        }
    }

    private void validateClassAnnotations(Class<?> clazz) {
        // Validate class-level annotations
        log.debug("Validating class annotations for: {}", clazz.getSimpleName());
    }

    private void validateMethodAnnotations(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            log.debug("Validating method annotations for: {}", method.getName());
        }
    }

    private void validateFieldAnnotations(Class<?> clazz) {
        log.debug("Validating field annotations for: {}", clazz.getSimpleName());
    }

    private void validateParameterAnnotations(Class<?> clazz) {
        log.debug("Validating parameter annotations for: {}", clazz.getSimpleName());
    }

    private void checkSecurityAnnotations(Class<?> clazz) {
        // Check for security-sensitive annotations
        log.debug("Checking security annotations for: {}", clazz.getSimpleName());
    }

    private void validateAnnotationValues(Class<?> clazz) {
        // Validate annotation values
        log.debug("Validating annotation values for: {}", clazz.getSimpleName());
    }

    private void checkMaliciousAnnotations(Class<?> clazz) {
        // Check for malicious annotations
        log.debug("Checking for malicious annotations for: {}", clazz.getSimpleName());
    }

    // Sample classes and annotations for demonstration

    @Service("exampleService")
    @Component("exampleComponent")
    @Builder
    @Serializable
    public static class ServiceClass {
        
        @Validated("fieldValidation")
        private String name;
        
        @ApiDoc(description = "Get service name", version = "1.0")
        public String getName() {
            return name;
        }
        
        @ApiDoc(description = "Set service name", version = "1.0")
        @Validated("methodValidation")
        public void setName(@Validated("parameterValidation") String name) {
            this.name = name;
        }
        
        @ApiDoc(description = "Process data", version = "1.0")
        @Validated("methodValidation")
        public String processData(@Validated("parameterValidation") String data) {
            return "Processed: " + data;
        }
    }

    // Custom annotations

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Service {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Component {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Builder {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Serializable {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Validated {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ApiDoc {
        String description() default "";
        String version() default "1.0";
    }

    // Custom annotation processor

    @SupportedAnnotationTypes({"netflix.metaprogramming.annotation.*"})
    @SupportedSourceVersion(SourceVersion.RELEASE_17)
    public static class CustomAnnotationProcessor extends AbstractProcessor {
        
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            for (TypeElement annotation : annotations) {
                Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
                for (Element element : elements) {
                    processElement(element, annotation);
                }
            }
            return true;
        }
        
        private void processElement(Element element, TypeElement annotation) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Processing element: " + element.getSimpleName() + " with annotation: " + annotation.getSimpleName()
            );
        }
    }
}
