package netflix.metaprogramming.reflection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Reflection Examples
 * 
 * This class demonstrates comprehensive reflection concepts including:
 * - Class introspection and metadata access
 * - Field access and modification
 * - Method invocation and parameter handling
 * - Constructor instantiation and object creation
 * - Generic type information and type erasure
 * - Annotation processing and metadata extraction
 * - Dynamic class loading and module access
 * - Performance optimization and caching
 * - Security considerations and best practices
 * - Error handling and exception management
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class ReflectionExamples {

    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    /**
     * Demonstrates basic class introspection
     * 
     * Shows how to get class information, superclasses, interfaces, and modifiers.
     */
    public void demonstrateClassIntrospection() {
        log.info("=== Demonstrating Class Introspection ===");
        
        Class<?> stringClass = String.class;
        
        // Basic class information
        log.debug("Class name: {}", stringClass.getName());
        log.debug("Simple name: {}", stringClass.getSimpleName());
        log.debug("Canonical name: {}", stringClass.getCanonicalName());
        log.debug("Package: {}", stringClass.getPackage());
        log.debug("Class loader: {}", stringClass.getClassLoader());
        
        // Modifiers
        int modifiers = stringClass.getModifiers();
        log.debug("Is public: {}", Modifier.isPublic(modifiers));
        log.debug("Is final: {}", Modifier.isFinal(modifiers));
        log.debug("Is abstract: {}", Modifier.isAbstract(modifiers));
        log.debug("Is interface: {}", Modifier.isInterface(modifiers));
        
        // Inheritance hierarchy
        Class<?> superclass = stringClass.getSuperclass();
        log.debug("Superclass: {}", superclass != null ? superclass.getName() : "None");
        
        Class<?>[] interfaces = stringClass.getInterfaces();
        log.debug("Interfaces: {}", Arrays.stream(interfaces)
                .map(Class::getName)
                .collect(Collectors.toList()));
        
        // Type parameters
        TypeVariable<?>[] typeParameters = stringClass.getTypeParameters();
        log.debug("Type parameters: {}", Arrays.stream(typeParameters)
                .map(TypeVariable::getName)
                .collect(Collectors.toList()));
    }

    /**
     * Demonstrates field access and modification
     * 
     * Shows how to access, read, and modify fields using reflection.
     */
    public void demonstrateFieldAccess() {
        log.info("=== Demonstrating Field Access ===");
        
        try {
            Class<?> personClass = Person.class;
            
            // Get all fields
            Field[] allFields = personClass.getDeclaredFields();
            log.debug("All fields: {}", Arrays.stream(allFields)
                    .map(Field::getName)
                    .collect(Collectors.toList()));
            
            // Get public fields
            Field[] publicFields = personClass.getFields();
            log.debug("Public fields: {}", Arrays.stream(publicFields)
                    .map(Field::getName)
                    .collect(Collectors.toList()));
            
            // Access specific field
            Field nameField = personClass.getDeclaredField("name");
            nameField.setAccessible(true);
            
            // Create instance and set field
            Person person = new Person();
            nameField.set(person, "John Doe");
            
            // Read field value
            String name = (String) nameField.get(person);
            log.debug("Name field value: {}", name);
            
            // Get field type and modifiers
            log.debug("Field type: {}", nameField.getType());
            log.debug("Field modifiers: {}", Modifier.toString(nameField.getModifiers()));
            
            // Access private field
            Field ageField = personClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.setInt(person, 30);
            
            int age = ageField.getInt(person);
            log.debug("Age field value: {}", age);
            
            // Get field annotations
            Annotation[] fieldAnnotations = nameField.getAnnotations();
            log.debug("Field annotations: {}", Arrays.stream(fieldAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
        } catch (Exception e) {
            log.error("Error in field access demonstration", e);
        }
    }

    /**
     * Demonstrates method invocation and parameter handling
     * 
     * Shows how to invoke methods with different parameter types and return values.
     */
    public void demonstrateMethodInvocation() {
        log.info("=== Demonstrating Method Invocation ===");
        
        try {
            Class<?> personClass = Person.class;
            Person person = new Person("Alice", 25);
            
            // Get all methods
            Method[] allMethods = personClass.getDeclaredMethods();
            log.debug("All methods: {}", Arrays.stream(allMethods)
                    .map(Method::getName)
                    .collect(Collectors.toList()));
            
            // Get public methods
            Method[] publicMethods = personClass.getMethods();
            log.debug("Public methods: {}", Arrays.stream(publicMethods)
                    .map(Method::getName)
                    .collect(Collectors.toList()));
            
            // Invoke no-argument method
            Method getNameMethod = personClass.getMethod("getName");
            String name = (String) getNameMethod.invoke(person);
            log.debug("Invoked getName(): {}", name);
            
            // Invoke method with parameters
            Method setNameMethod = personClass.getMethod("setName", String.class);
            setNameMethod.invoke(person, "Bob");
            log.debug("Invoked setName('Bob')");
            
            // Invoke static method
            Method staticMethod = personClass.getMethod("getDefaultPerson");
            Person defaultPerson = (Person) staticMethod.invoke(null);
            log.debug("Invoked static method: {}", defaultPerson);
            
            // Get method information
            log.debug("Method name: {}", getNameMethod.getName());
            log.debug("Method return type: {}", getNameMethod.getReturnType());
            log.debug("Method parameter types: {}", Arrays.toString(getNameMethod.getParameterTypes()));
            log.debug("Method modifiers: {}", Modifier.toString(getNameMethod.getModifiers()));
            
            // Get method annotations
            Annotation[] methodAnnotations = getNameMethod.getAnnotations();
            log.debug("Method annotations: {}", Arrays.stream(methodAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
            // Invoke method with primitive parameters
            Method setAgeMethod = personClass.getMethod("setAge", int.class);
            setAgeMethod.invoke(person, 30);
            log.debug("Invoked setAge(30)");
            
        } catch (Exception e) {
            log.error("Error in method invocation demonstration", e);
        }
    }

    /**
     * Demonstrates constructor instantiation and object creation
     * 
     * Shows how to create objects using different constructors.
     */
    public void demonstrateConstructorInstantiation() {
        log.info("=== Demonstrating Constructor Instantiation ===");
        
        try {
            Class<?> personClass = Person.class;
            
            // Get all constructors
            Constructor<?>[] constructors = personClass.getDeclaredConstructors();
            log.debug("All constructors: {}", Arrays.stream(constructors)
                    .map(Constructor::toString)
                    .collect(Collectors.toList()));
            
            // Get public constructors
            Constructor<?>[] publicConstructors = personClass.getConstructors();
            log.debug("Public constructors: {}", Arrays.stream(publicConstructors)
                    .map(Constructor::toString)
                    .collect(Collectors.toList()));
            
            // Create instance using no-argument constructor
            Constructor<?> noArgConstructor = personClass.getConstructor();
            Person person1 = (Person) noArgConstructor.newInstance();
            log.debug("Created person with no-arg constructor: {}", person1);
            
            // Create instance using parameterized constructor
            Constructor<?> paramConstructor = personClass.getConstructor(String.class, int.class);
            Person person2 = (Person) paramConstructor.newInstance("Charlie", 35);
            log.debug("Created person with param constructor: {}", person2);
            
            // Get constructor information
            log.debug("Constructor parameter types: {}", Arrays.toString(paramConstructor.getParameterTypes()));
            log.debug("Constructor modifiers: {}", Modifier.toString(paramConstructor.getModifiers()));
            
            // Get constructor annotations
            Annotation[] constructorAnnotations = paramConstructor.getAnnotations();
            log.debug("Constructor annotations: {}", Arrays.stream(constructorAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
        } catch (Exception e) {
            log.error("Error in constructor instantiation demonstration", e);
        }
    }

    /**
     * Demonstrates generic type information and type erasure
     * 
     * Shows how to work with generic types and handle type erasure.
     */
    public void demonstrateGenericTypeInformation() {
        log.info("=== Demonstrating Generic Type Information ===");
        
        try {
            // Get generic type information from field
            Field listField = GenericClass.class.getDeclaredField("genericList");
            Type genericType = listField.getGenericType();
            
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type rawType = parameterizedType.getRawType();
                
                log.debug("Raw type: {}", rawType);
                log.debug("Actual type arguments: {}", Arrays.toString(actualTypeArguments));
                
                for (Type typeArgument : actualTypeArguments) {
                    if (typeArgument instanceof Class) {
                        log.debug("Type argument class: {}", ((Class<?>) typeArgument).getName());
                    } else if (typeArgument instanceof TypeVariable) {
                        log.debug("Type argument variable: {}", ((TypeVariable<?>) typeArgument).getName());
                    }
                }
            }
            
            // Get generic type information from method
            Method genericMethod = GenericClass.class.getDeclaredMethod("genericMethod", Object.class);
            Type[] genericParameterTypes = genericMethod.getGenericParameterTypes();
            Type genericReturnType = genericMethod.getGenericReturnType();
            
            log.debug("Generic parameter types: {}", Arrays.toString(genericParameterTypes));
            log.debug("Generic return type: {}", genericReturnType);
            
            // Work with type variables
            TypeVariable<?>[] typeVariables = GenericClass.class.getTypeParameters();
            for (TypeVariable<?> typeVariable : typeVariables) {
                log.debug("Type variable: {}", typeVariable.getName());
                log.debug("Type variable bounds: {}", Arrays.toString(typeVariable.getBounds()));
            }
            
        } catch (Exception e) {
            log.error("Error in generic type information demonstration", e);
        }
    }

    /**
     * Demonstrates annotation processing and metadata extraction
     * 
     * Shows how to process annotations and extract metadata.
     */
    public void demonstrateAnnotationProcessing() {
        log.info("=== Demonstrating Annotation Processing ===");
        
        try {
            Class<?> annotatedClass = AnnotatedClass.class;
            
            // Get class-level annotations
            Annotation[] classAnnotations = annotatedClass.getAnnotations();
            log.debug("Class annotations: {}", Arrays.stream(classAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
            // Get specific annotation
            CustomAnnotation customAnnotation = annotatedClass.getAnnotation(CustomAnnotation.class);
            if (customAnnotation != null) {
                log.debug("Custom annotation value: {}", customAnnotation.value());
                log.debug("Custom annotation count: {}", customAnnotation.count());
            }
            
            // Get method annotations
            Method annotatedMethod = annotatedClass.getDeclaredMethod("annotatedMethod");
            Annotation[] methodAnnotations = annotatedMethod.getAnnotations();
            log.debug("Method annotations: {}", Arrays.stream(methodAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
            // Get field annotations
            Field annotatedField = annotatedClass.getDeclaredField("annotatedField");
            Annotation[] fieldAnnotations = annotatedField.getAnnotations();
            log.debug("Field annotations: {}", Arrays.stream(fieldAnnotations)
                    .map(Annotation::toString)
                    .collect(Collectors.toList()));
            
            // Check if annotation is present
            boolean hasCustomAnnotation = annotatedClass.isAnnotationPresent(CustomAnnotation.class);
            log.debug("Has custom annotation: {}", hasCustomAnnotation);
            
            // Get annotation with retention policy
            Retention retention = annotatedClass.getAnnotation(Retention.class);
            if (retention != null) {
                log.debug("Retention policy: {}", retention.value());
            }
            
        } catch (Exception e) {
            log.error("Error in annotation processing demonstration", e);
        }
    }

    /**
     * Demonstrates dynamic class loading and module access
     * 
     * Shows how to load classes dynamically and work with modules.
     */
    public void demonstrateDynamicClassLoading() {
        log.info("=== Demonstrating Dynamic Class Loading ===");
        
        try {
            // Load class by name
            String className = "java.lang.String";
            Class<?> loadedClass = Class.forName(className);
            log.debug("Loaded class: {}", loadedClass.getName());
            
            // Load class with class loader
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class<?> loadedClassWithLoader = classLoader.loadClass(className);
            log.debug("Loaded class with loader: {}", loadedClassWithLoader.getName());
            
            // Check if class is loaded
            boolean isLoaded = isClassLoaded(className);
            log.debug("Is class loaded: {}", isLoaded);
            
            // Get class loader hierarchy
            ClassLoader currentLoader = this.getClass().getClassLoader();
            while (currentLoader != null) {
                log.debug("Class loader: {}", currentLoader.getClass().getName());
                currentLoader = currentLoader.getParent();
            }
            
            // Load class from different module (if available)
            try {
                Class<?> moduleClass = Class.forName("java.base/java.lang.String");
                log.debug("Loaded class from module: {}", moduleClass.getName());
            } catch (ClassNotFoundException e) {
                log.debug("Module class loading not available or failed");
            }
            
        } catch (Exception e) {
            log.error("Error in dynamic class loading demonstration", e);
        }
    }

    /**
     * Demonstrates performance optimization and caching
     * 
     * Shows how to optimize reflection operations using caching.
     */
    @Cacheable(value = "reflection-cache", key = "#className")
    public Class<?> getCachedClass(String className) {
        log.debug("Loading class: {}", className);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("Class not found: {}", className, e);
            return null;
        }
    }

    @Cacheable(value = "reflection-cache", key = "#className + '.' + #methodName")
    public Method getCachedMethod(String className, String methodName) {
        log.debug("Loading method: {}.{}", className, methodName);
        try {
            Class<?> clazz = getCachedClass(className);
            if (clazz != null) {
                return clazz.getMethod(methodName);
            }
            return null;
        } catch (NoSuchMethodException e) {
            log.error("Method not found: {}.{}", className, methodName, e);
            return null;
        }
    }

    @Cacheable(value = "reflection-cache", key = "#className + '.' + #fieldName")
    public Field getCachedField(String className, String fieldName) {
        log.debug("Loading field: {}.{}", className, fieldName);
        try {
            Class<?> clazz = getCachedClass(className);
            if (clazz != null) {
                return clazz.getDeclaredField(fieldName);
            }
            return null;
        } catch (NoSuchFieldException e) {
            log.error("Field not found: {}.{}", className, fieldName, e);
            return null;
        }
    }

    /**
     * Demonstrates security considerations and best practices
     * 
     * Shows how to handle security in reflection operations.
     */
    public void demonstrateSecurityConsiderations() {
        log.info("=== Demonstrating Security Considerations ===");
        
        try {
            // Check if security manager is enabled
            SecurityManager securityManager = System.getSecurityManager();
            log.debug("Security manager enabled: {}", securityManager != null);
            
            // Check reflection permissions
            if (securityManager != null) {
                try {
                    securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
                    log.debug("Reflection access checks suppressed");
                } catch (SecurityException e) {
                    log.debug("Reflection access checks not suppressed: {}", e.getMessage());
                }
            }
            
            // Safe field access
            Field field = Person.class.getDeclaredField("name");
            if (field.canAccess(null)) {
                log.debug("Field is accessible");
            } else {
                log.debug("Field is not accessible, making accessible");
                field.setAccessible(true);
            }
            
            // Check field modifiers for security
            int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                log.debug("Field is private - access requires setAccessible(true)");
            }
            if (Modifier.isFinal(modifiers)) {
                log.debug("Field is final - modification may be restricted");
            }
            
        } catch (Exception e) {
            log.error("Error in security considerations demonstration", e);
        }
    }

    /**
     * Demonstrates error handling and exception management
     * 
     * Shows how to handle reflection-related exceptions.
     */
    public void demonstrateErrorHandling() {
        log.info("=== Demonstrating Error Handling ===");
        
        // Handle ClassNotFoundException
        try {
            Class.forName("NonExistentClass");
        } catch (ClassNotFoundException e) {
            log.debug("Class not found: {}", e.getMessage());
        }
        
        // Handle NoSuchMethodException
        try {
            Person.class.getMethod("nonExistentMethod");
        } catch (NoSuchMethodException e) {
            log.debug("Method not found: {}", e.getMessage());
        }
        
        // Handle NoSuchFieldException
        try {
            Person.class.getField("nonExistentField");
        } catch (NoSuchFieldException e) {
            log.debug("Field not found: {}", e.getMessage());
        }
        
        // Handle IllegalAccessException
        try {
            Field privateField = Person.class.getDeclaredField("name");
            Person person = new Person();
            privateField.get(person); // This will throw IllegalAccessException
        } catch (IllegalAccessException e) {
            log.debug("Illegal access: {}", e.getMessage());
        } catch (NoSuchFieldException e) {
            log.debug("Field not found: {}", e.getMessage());
        }
        
        // Handle InvocationTargetException
        try {
            Method method = Person.class.getMethod("methodThatThrowsException");
            method.invoke(new Person());
        } catch (InvocationTargetException e) {
            log.debug("Invocation target exception: {}", e.getCause().getMessage());
        } catch (Exception e) {
            log.debug("Other exception: {}", e.getMessage());
        }
    }

    // Helper methods

    private boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // Sample classes for demonstration

    public static class Person {
        private String name;
        private int age;

        public Person() {}

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public static Person getDefaultPerson() {
            return new Person("Default", 0);
        }

        public void methodThatThrowsException() throws Exception {
            throw new RuntimeException("Test exception");
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }

    public static class GenericClass<T> {
        private List<T> genericList;

        public <U> U genericMethod(U input) {
            return input;
        }
    }

    @CustomAnnotation(value = "Class Level", count = 1)
    public static class AnnotatedClass {
        @CustomAnnotation(value = "Field Level", count = 2)
        private String annotatedField;

        @CustomAnnotation(value = "Method Level", count = 3)
        public void annotatedMethod() {
            // Method implementation
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    public @interface CustomAnnotation {
        String value() default "";
        int count() default 0;
    }
}
