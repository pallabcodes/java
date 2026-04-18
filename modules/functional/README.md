# Java Functional Programming - Netflix Production Grade

## Overview

This project provides comprehensive coverage of Java functional programming concepts, designed specifically for Netflix production environments. It demonstrates advanced functional programming patterns, built-in functional interfaces, custom functional interfaces, and production-ready implementations.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Built-in Functional Interfaces](#built-in-functional-interfaces)
3. [Advanced Functional Programming](#advanced-functional-programming)
4. [Custom Functional Interfaces](#custom-functional-interfaces)
5. [Stream API and Collections](#stream-api-and-collections)
6. [Optional and Monadic Operations](#optional-and-monadic-operations)
7. [CompletableFuture and Async Programming](#completablefuture-and-async-programming)
8. [Production Best Practices](#production-best-practices)
9. [Testing and Debugging](#testing-and-debugging)
10. [Performance Considerations](#performance-considerations)

## Project Structure

```
JavaFunctionalProgramming/
├── src/main/java/netflix/functional/
│   ├── interfaces/
│   │   └── FunctionalInterfacesExamples.java
│   ├── advanced/
│   │   └── AdvancedFunctionalInterfaces.java
│   ├── custom/
│   │   └── CustomFunctionalInterfaces.java
│   ├── lambda/
│   │   └── LambdaExpressionsExamples.java
│   ├── streams/
│   │   └── StreamApiExamples.java
│   ├── optional/
│   │   └── OptionalExamples.java
│   └── async/
│       └── CompletableFutureExamples.java
├── src/test/java/
├── build.gradle
├── settings.gradle
└── README.md
```

## Built-in Functional Interfaces

### Core Functional Interfaces

#### 1. Function<T, R>
- **Purpose**: Takes one argument, returns a result
- **Key Methods**: `apply()`, `andThen()`, `compose()`
- **Use Cases**: Data transformation, mapping operations

```java
Function<String, Integer> stringLength = String::length;
Function<String, String> toUpperCase = String::toUpperCase;
Function<String, String> composed = toUpperCase.andThen(s -> "Hello, " + s);
```

#### 2. BiFunction<T, U, R>
- **Purpose**: Takes two arguments, returns a result
- **Key Methods**: `apply()`, `andThen()`
- **Use Cases**: Combining two values, complex transformations

```java
BiFunction<String, String, String> concatenate = (s1, s2) -> s1 + " " + s2;
BiFunction<Integer, Integer, Integer> add = Integer::sum;
```

#### 3. Predicate<T>
- **Purpose**: Takes one argument, returns boolean
- **Key Methods**: `test()`, `and()`, `or()`, `negate()`
- **Use Cases**: Filtering, validation, conditional logic

```java
Predicate<String> isLong = s -> s.length() > 5;
Predicate<String> startsWithA = s -> s.startsWith("A");
Predicate<String> complex = isLong.and(startsWithA);
```

#### 4. BiPredicate<T, U>
- **Purpose**: Takes two arguments, returns boolean
- **Key Methods**: `test()`, `and()`, `or()`, `negate()`
- **Use Cases**: Comparing two values, complex conditions

```java
BiPredicate<String, String> isEqual = String::equals;
BiPredicate<Integer, Integer> isGreater = (a, b) -> a > b;
```

#### 5. Consumer<T>
- **Purpose**: Takes one argument, returns void
- **Key Methods**: `accept()`, `andThen()`
- **Use Cases**: Side effects, logging, printing

```java
Consumer<String> print = System.out::println;
Consumer<String> log = s -> log.debug("Processing: {}", s);
Consumer<String> composed = print.andThen(log);
```

#### 6. BiConsumer<T, U>
- **Purpose**: Takes two arguments, returns void
- **Key Methods**: `accept()`, `andThen()`
- **Use Cases**: Processing pairs of values, logging combinations

```java
BiConsumer<String, Integer> printNameAndAge = (name, age) -> 
    System.out.println(name + " is " + age + " years old");
```

#### 7. Supplier<T>
- **Purpose**: Takes no arguments, returns a result
- **Key Methods**: `get()`
- **Use Cases**: Lazy evaluation, factory methods, generators

```java
Supplier<String> getCurrentTime = () -> new Date().toString();
Supplier<Double> getRandomNumber = Math::random;
```

#### 8. UnaryOperator<T>
- **Purpose**: Takes one argument, returns same type
- **Key Methods**: `apply()`, `andThen()`, `compose()`
- **Use Cases**: Single-type transformations

```java
UnaryOperator<String> toUpperCase = String::toUpperCase;
UnaryOperator<Integer> square = n -> n * n;
```

#### 9. BinaryOperator<T>
- **Purpose**: Takes two arguments, returns same type
- **Key Methods**: `apply()`, `andThen()`
- **Use Cases**: Combining two values of same type

```java
BinaryOperator<Integer> add = Integer::sum;
BinaryOperator<String> concatenate = (s1, s2) -> s1 + " " + s2;
```

### Specialized Functional Interfaces

#### Primitive Type Interfaces
- **IntFunction<R>**: `int` → `R`
- **LongFunction<R>**: `long` → `R`
- **DoubleFunction<R>**: `double` → `R`
- **ToIntFunction<T>**: `T` → `int`
- **ToLongFunction<T>**: `T` → `long`
- **ToDoubleFunction<T>**: `T` → `double`

#### Primitive Unary Operators
- **IntUnaryOperator**: `int` → `int`
- **LongUnaryOperator**: `long` → `long`
- **DoubleUnaryOperator**: `double` → `double`

#### Primitive Binary Operators
- **IntBinaryOperator**: `(int, int)` → `int`
- **LongBinaryOperator**: `(long, long)` → `long`
- **DoubleBinaryOperator**: `(double, double)` → `double`

#### Primitive Predicates
- **IntPredicate**: `int` → `boolean`
- **LongPredicate**: `long` → `boolean`
- **DoublePredicate**: `double` → `boolean`

#### Primitive Consumers
- **IntConsumer**: `int` → `void`
- **LongConsumer**: `long` → `void`
- **DoubleConsumer**: `double` → `void`

#### Primitive Suppliers
- **IntSupplier**: `()` → `int`
- **LongSupplier**: `()` → `long`
- **DoubleSupplier**: `()` → `double`

## Advanced Functional Programming

### Extending Functional Interfaces

#### Extended Function with Additional Methods
```java
@FunctionalInterface
interface ExtendedFunction<T, R> extends Function<T, R> {
    default ExtendedFunction<T, R> withLogging(String operation) {
        return t -> {
            log.debug("Starting operation: {}", operation);
            R result = this.apply(t);
            log.debug("Completed operation: {} with result: {}", operation, result);
            return result;
        };
    }
    
    default ExtendedFunction<T, R> withRetry(int maxRetries) {
        return t -> {
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    return this.apply(t);
                } catch (Exception e) {
                    retries++;
                    if (retries >= maxRetries) {
                        throw new RuntimeException("Max retries reached", e);
                    }
                }
            }
            throw new RuntimeException("Unknown error");
        };
    }
}
```

### Method References

#### Static Method References
```java
Function<String, Integer> parseInt = Integer::parseInt;
Function<String, String> toUpperCase = String::toUpperCase;
```

#### Instance Method References
```java
Function<String, String> toUpperCase = String::toUpperCase;
BiFunction<String, String, Boolean> equals = String::equals;
```

#### Constructor References
```java
Function<String, StringBuilder> stringBuilderConstructor = StringBuilder::new;
BiFunction<String, String, String> stringConstructor = String::new;
```

### Higher-Order Functions

#### Functions that Return Functions (Currying)
```java
Function<Integer, Function<Integer, Integer>> add = x -> y -> x + y;
Function<Integer, Integer> add5 = add.apply(5);
```

#### Functions that Take Functions as Parameters
```java
Function<Function<String, String>, String> applyToString = f -> f.apply("hello");
String result = applyToString.apply(String::toUpperCase);
```

### Monadic Operations

#### Maybe Monad Implementation
```java
interface Maybe<T> {
    <R> Maybe<R> map(Function<T, R> mapper);
    <R> Maybe<R> flatMap(Function<T, Maybe<R>> mapper);
    T getOrElse(T defaultValue);
    boolean isPresent();
}
```

## Custom Functional Interfaces

### Netflix-Specific Functional Interfaces

#### 1. Validator<T>
```java
@FunctionalInterface
interface Validator<T> {
    boolean validate(T value);
    
    default Validator<T> and(Validator<T> other) {
        return value -> this.validate(value) && other.validate(value);
    }
    
    default Validator<T> or(Validator<T> other) {
        return value -> this.validate(value) || other.validate(value);
    }
}
```

#### 2. Transformer<T, R>
```java
@FunctionalInterface
interface Transformer<T, R> {
    R transform(T input);
    
    default <V> Transformer<T, V> andThen(Transformer<R, V> other) {
        return input -> other.transform(this.transform(input));
    }
}
```

#### 3. ContextualAction<T, C>
```java
@FunctionalInterface
interface ContextualAction<T, C> {
    void execute(T input, C context);
}
```

#### 4. Error Handling Functions
```java
@FunctionalInterface
interface ErrorHandlingFunction<T, R, E extends Exception> {
    R apply(T input) throws E;
    
    default ErrorHandlingFunction<T, R, E> andThen(ErrorHandlingFunction<R, R, E> other) {
        return input -> {
            R result = this.apply(input);
            return other.apply(result);
        };
    }
}
```

#### 5. Caching Functions
```java
@FunctionalInterface
interface CachingFunction<T, R> {
    R apply(T input);
    
    default CachingFunction<T, R> withCache(Map<T, R> cache) {
        return input -> cache.computeIfAbsent(input, this::apply);
    }
}
```

#### 6. Retry Functions
```java
@FunctionalInterface
interface RetryFunction<T, R> {
    R apply(T input);
    
    default RetryFunction<T, R> withRetry(int maxRetries) {
        return input -> {
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    return this.apply(input);
                } catch (Exception e) {
                    retries++;
                    if (retries >= maxRetries) {
                        throw new RuntimeException("Max retries reached", e);
                    }
                }
            }
            throw new RuntimeException("Unknown error");
        };
    }
}
```

#### 7. Circuit Breaker Functions
```java
@FunctionalInterface
interface CircuitBreakerFunction<T, R> {
    R apply(T input);
    
    default CircuitBreakerFunction<T, R> withCircuitBreaker(boolean isOpen, R fallback) {
        return input -> {
            if (isOpen) {
                return fallback;
            }
            try {
                return this.apply(input);
            } catch (Exception e) {
                return fallback;
            }
        };
    }
}
```

#### 8. Rate Limited Functions
```java
@FunctionalInterface
interface RateLimitedFunction<T, R> {
    R apply(T input);
    
    default RateLimitedFunction<T, R> withRateLimit(int maxRequests, long timeWindow) {
        return input -> {
            // Rate limiting implementation
            return this.apply(input);
        };
    }
}
```

#### 9. Monitored Functions
```java
@FunctionalInterface
interface MonitoredFunction<T, R> {
    R apply(T input);
    
    default MonitoredFunction<T, R> withMonitoring(String operationName) {
        return input -> {
            long startTime = System.currentTimeMillis();
            try {
                R result = this.apply(input);
                long duration = System.currentTimeMillis() - startTime;
                log.debug("Operation {} completed in {} ms", operationName, duration);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Operation {} failed after {} ms: {}", operationName, duration, e.getMessage());
                throw e;
            }
        };
    }
}
```

## Stream API and Collections

### Basic Stream Operations

#### Filtering
```java
List<String> longNames = names.stream()
    .filter(s -> s.length() > 5)
    .collect(Collectors.toList());
```

#### Mapping
```java
List<String> upperNames = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

#### Reduction
```java
Optional<String> longest = names.stream()
    .reduce((s1, s2) -> s1.length() > s2.length() ? s1 : s2);
```

#### Collecting
```java
Map<String, Integer> nameLengths = names.stream()
    .collect(Collectors.toMap(Function.identity(), String::length));
```

### Advanced Stream Operations

#### Grouping
```java
Map<Integer, List<String>> groupedByLength = names.stream()
    .collect(Collectors.groupingBy(String::length));
```

#### Partitioning
```java
Map<Boolean, List<String>> partitioned = names.stream()
    .collect(Collectors.partitioningBy(s -> s.length() > 5));
```

#### Custom Collectors
```java
Collector<String, ?, List<String>> customCollector = Collector.of(
    ArrayList::new,
    List::add,
    (list1, list2) -> { list1.addAll(list2); return list1; }
);
```

## Optional and Monadic Operations

### Basic Optional Operations

#### Creation
```java
Optional<String> empty = Optional.empty();
Optional<String> present = Optional.of("Hello");
Optional<String> nullable = Optional.ofNullable(getValue());
```

#### Transformation
```java
Optional<String> mapped = optional.map(String::toUpperCase);
Optional<String> flatMapped = optional.flatMap(s -> Optional.of(s + "!"));
```

#### Filtering
```java
Optional<String> filtered = optional.filter(s -> s.length() > 5);
```

#### Default Values
```java
String result = optional.orElse("Default");
String result2 = optional.orElseGet(() -> "Generated");
String result3 = optional.orElseThrow(() -> new RuntimeException("No value"));
```

### Advanced Optional Operations

#### Chaining
```java
Optional<String> chained = optional
    .map(String::toUpperCase)
    .filter(s -> s.length() > 5)
    .flatMap(s -> Optional.of(s + "!"))
    .map(s -> "Result: " + s);
```

#### Conditional Operations
```java
optional.ifPresent(value -> log.debug("Value: {}", value));
optional.ifPresentOrElse(
    value -> log.debug("Value: {}", value),
    () -> log.debug("No value")
);
```

## CompletableFuture and Async Programming

### Basic CompletableFuture Operations

#### Creation
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<Void> runnable = CompletableFuture.runAsync(() -> log.debug("Running"));
```

#### Composition
```java
CompletableFuture<String> composed = future
    .thenApply(String::toUpperCase)
    .thenApply(s -> s + "!")
    .thenApply(s -> "Result: " + s);
```

#### Exception Handling
```java
CompletableFuture<String> errorHandled = future
    .handle((result, throwable) -> {
        if (throwable != null) {
            return "Error occurred";
        }
        return result;
    });
```

### Advanced CompletableFuture Operations

#### Parallel Execution
```java
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<String> combined = future1
    .thenCombine(future2, (s1, s2) -> s1 + " " + s2);
```

#### Timeout and Cancellation
```java
CompletableFuture<String> withTimeout = future
    .orTimeout(1, TimeUnit.SECONDS)
    .handle((result, throwable) -> {
        if (throwable instanceof TimeoutException) {
            return "Timeout occurred";
        }
        return result;
    });
```

## Production Best Practices

### 1. Error Handling
- Always handle exceptions in functional operations
- Use `handle()` and `exceptionally()` for CompletableFuture
- Implement circuit breaker patterns for resilience

### 2. Performance Optimization
- Use primitive functional interfaces when possible
- Implement caching for expensive operations
- Use appropriate thread pools for async operations

### 3. Monitoring and Logging
- Add logging to functional operations
- Monitor performance metrics
- Implement health checks

### 4. Testing
- Write comprehensive unit tests
- Test error scenarios
- Use mocking for external dependencies

### 5. Documentation
- Document all custom functional interfaces
- Provide usage examples
- Include performance considerations

## Testing and Debugging

### Unit Testing
```java
@Test
void testFunctionComposition() {
    Function<String, String> upperCase = String::toUpperCase;
    Function<String, String> addPrefix = s -> "Hello, " + s;
    Function<String, String> composed = upperCase.andThen(addPrefix);
    
    String result = composed.apply("world");
    assertEquals("Hello, WORLD", result);
}
```

### Integration Testing
```java
@Test
void testStreamProcessing() {
    List<String> names = Arrays.asList("alice", "bob", "charlie");
    List<String> result = names.stream()
        .map(String::toUpperCase)
        .filter(s -> s.length() > 3)
        .collect(Collectors.toList());
    
    assertEquals(Arrays.asList("ALICE", "CHARLIE"), result);
}
```

## Performance Considerations

### 1. Primitive Types
- Use primitive functional interfaces when possible
- Avoid boxing/unboxing overhead
- Consider performance implications of lambda expressions

### 2. Caching
- Implement caching for expensive operations
- Use appropriate cache eviction strategies
- Monitor cache hit rates

### 3. Parallel Processing
- Use parallel streams judiciously
- Consider thread pool sizing
- Monitor CPU utilization

### 4. Memory Management
- Be aware of lambda capture semantics
- Avoid memory leaks in long-running applications
- Monitor memory usage

## Conclusion

This project provides comprehensive coverage of Java functional programming concepts, specifically designed for Netflix production environments. It demonstrates advanced patterns, best practices, and production-ready implementations that can be used as a reference for building robust, scalable applications.

The examples cover all major aspects of functional programming in Java, from basic functional interfaces to advanced monadic operations, providing a solid foundation for developers working in functional programming paradigms.
