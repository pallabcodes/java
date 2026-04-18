package netflix.functional.advanced;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Advanced Functional Interfaces
 * 
 * This class demonstrates advanced functional programming concepts including:
 * - Extending functional interfaces with combinator patterns
 * - All specialized functional interfaces (IntFunction, LongFunction, etc.)
 * - Advanced composition and chaining patterns
 * - Method references and constructor references
 * - Higher-order functions and currying
 * - Monadic operations and functors
 * - Custom functional interface hierarchies
 * - Netflix-specific functional patterns
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class AdvancedFunctionalInterfaces {

    /**
     * Demonstrates extending functional interfaces with combinator patterns
     * 
     * This shows how to extend built-in functional interfaces with additional functionality.
     */
    public static void demonstrateExtendingFunctionalInterfaces() {
        log.info("=== Demonstrating Extending Functional Interfaces ===");
        
        // Extended Function with additional methods
        @FunctionalInterface
        interface ExtendedFunction<T, R> extends Function<T, R> {
            default ExtendedFunction<T, R> andThen(ExtendedFunction<R, R> after) {
                return t -> after.apply(this.apply(t));
            }
            
            default ExtendedFunction<T, R> compose(ExtendedFunction<T, T> before) {
                return t -> this.apply(before.apply(t));
            }
            
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
                            log.debug("Retry {} of {}", retries, maxRetries);
                        }
                    }
                    throw new RuntimeException("Unknown error");
                };
            }
            
            default ExtendedFunction<T, R> withCaching(Map<T, R> cache) {
                return t -> cache.computeIfAbsent(t, this::apply);
            }
        }
        
        // Extended Predicate with additional methods
        @FunctionalInterface
        interface ExtendedPredicate<T> extends Predicate<T> {
            default ExtendedPredicate<T> and(ExtendedPredicate<T> other) {
                return t -> this.test(t) && other.test(t);
            }
            
            default ExtendedPredicate<T> or(ExtendedPredicate<T> other) {
                return t -> this.test(t) || other.test(t);
            }
            
            default ExtendedPredicate<T> negate() {
                return t -> !this.test(t);
            }
            
            default ExtendedPredicate<T> withLogging(String description) {
                return t -> {
                    boolean result = this.test(t);
                    log.debug("Predicate {} on {}: {}", description, t, result);
                    return result;
                };
            }
            
            default ExtendedPredicate<T> withCaching(Map<T, Boolean> cache) {
                return t -> cache.computeIfAbsent(t, this::test);
            }
        }
        
        // Extended Consumer with additional methods
        @FunctionalInterface
        interface ExtendedConsumer<T> extends Consumer<T> {
            default ExtendedConsumer<T> andThen(ExtendedConsumer<T> after) {
                return t -> {
                    this.accept(t);
                    after.accept(t);
                };
            }
            
            default ExtendedConsumer<T> withLogging(String operation) {
                return t -> {
                    log.debug("Starting consumer operation: {}", operation);
                    this.accept(t);
                    log.debug("Completed consumer operation: {}", operation);
                };
            }
            
            default ExtendedConsumer<T> withErrorHandling(Consumer<Exception> errorHandler) {
                return t -> {
                    try {
                        this.accept(t);
                    } catch (Exception e) {
                        errorHandler.accept(e);
                    }
                };
            }
        }
        
        // Extended Supplier with additional methods
        @FunctionalInterface
        interface ExtendedSupplier<T> extends Supplier<T> {
            default ExtendedSupplier<T> withLogging(String operation) {
                return () -> {
                    log.debug("Starting supplier operation: {}", operation);
                    T result = this.get();
                    log.debug("Completed supplier operation: {} with result: {}", operation, result);
                    return result;
                };
            }
            
            default ExtendedSupplier<T> withCaching(Supplier<T> cache) {
                return () -> {
                    T cached = cache.get();
                    if (cached != null) {
                        return cached;
                    }
                    T result = this.get();
                    // In real implementation, you'd store the result in cache
                    return result;
                };
            }
            
            default ExtendedSupplier<T> withRetry(int maxRetries) {
                return () -> {
                    int retries = 0;
                    while (retries < maxRetries) {
                        try {
                            return this.get();
                        } catch (Exception e) {
                            retries++;
                            if (retries >= maxRetries) {
                                throw new RuntimeException("Max retries reached", e);
                            }
                            log.debug("Retry {} of {}", retries, maxRetries);
                        }
                    }
                    throw new RuntimeException("Unknown error");
                };
            }
        }
        
        // Usage examples
        ExtendedFunction<String, String> upperCase = String::toUpperCase;
        ExtendedFunction<String, String> withLogging = upperCase.withLogging("uppercase");
        ExtendedFunction<String, String> withRetry = upperCase.withRetry(3);
        
        String result = withLogging.apply("hello");
        log.debug("Result: {}", result);
        
        ExtendedPredicate<String> isLong = s -> s.length() > 5;
        ExtendedPredicate<String> withLoggingPredicate = isLong.withLogging("length check");
        
        boolean testResult = withLoggingPredicate.test("hello world");
        log.debug("Test result: {}", testResult);
        
        ExtendedConsumer<String> printer = System.out::println;
        ExtendedConsumer<String> withLoggingConsumer = printer.withLogging("print");
        
        withLoggingConsumer.accept("Hello from extended consumer");
        
        ExtendedSupplier<String> greeting = () -> "Hello from extended supplier";
        ExtendedSupplier<String> withLoggingSupplier = greeting.withLogging("greeting");
        
        String greetingResult = withLoggingSupplier.get();
        log.debug("Greeting: {}", greetingResult);
    }

    /**
     * Demonstrates all specialized functional interfaces
     * 
     * Java provides specialized functional interfaces for primitive types.
     */
    public static void demonstrateSpecializedFunctionalInterfaces() {
        log.info("=== Demonstrating Specialized Functional Interfaces ===");
        
        // IntFunction - takes int, returns generic type
        IntFunction<String> intToString = i -> "Number: " + i;
        log.debug("IntFunction: {}", intToString.apply(42));
        
        // LongFunction - takes long, returns generic type
        LongFunction<String> longToString = l -> "Long: " + l;
        log.debug("LongFunction: {}", longToString.apply(123456789L));
        
        // DoubleFunction - takes double, returns generic type
        DoubleFunction<String> doubleToString = d -> "Double: " + d;
        log.debug("DoubleFunction: {}", doubleToString.apply(3.14159));
        
        // ToIntFunction - takes generic type, returns int
        ToIntFunction<String> stringToInt = String::length;
        log.debug("ToIntFunction: {}", stringToInt.applyAsInt("hello"));
        
        // ToLongFunction - takes generic type, returns long
        ToLongFunction<String> stringToLong = s -> s.length() * 1000L;
        log.debug("ToLongFunction: {}", stringToLong.applyAsLong("hello"));
        
        // ToDoubleFunction - takes generic type, returns double
        ToDoubleFunction<String> stringToDouble = s -> s.length() * 1.5;
        log.debug("ToDoubleFunction: {}", stringToDouble.applyAsDouble("hello"));
        
        // IntToLongFunction - takes int, returns long
        IntToLongFunction intToLong = i -> i * 1000L;
        log.debug("IntToLongFunction: {}", intToLong.applyAsLong(42));
        
        // IntToDoubleFunction - takes int, returns double
        IntToDoubleFunction intToDouble = i -> i * 1.5;
        log.debug("IntToDoubleFunction: {}", intToDouble.applyAsDouble(42));
        
        // LongToIntFunction - takes long, returns int
        LongToIntFunction longToInt = l -> (int) (l / 1000);
        log.debug("LongToIntFunction: {}", longToInt.applyAsInt(42000L));
        
        // LongToDoubleFunction - takes long, returns double
        LongToDoubleFunction longToDouble = l -> l / 1000.0;
        log.debug("LongToDoubleFunction: {}", longToDouble.applyAsDouble(42000L));
        
        // DoubleToIntFunction - takes double, returns int
        DoubleToIntFunction doubleToInt = d -> (int) d;
        log.debug("DoubleToIntFunction: {}", doubleToInt.applyAsInt(3.14159));
        
        // DoubleToLongFunction - takes double, returns long
        DoubleToLongFunction doubleToLong = d -> (long) d;
        log.debug("DoubleToLongFunction: {}", doubleToLong.applyAsLong(3.14159));
        
        // IntUnaryOperator - takes int, returns int
        IntUnaryOperator square = x -> x * x;
        log.debug("IntUnaryOperator: {}", square.applyAsInt(5));
        
        // LongUnaryOperator - takes long, returns long
        LongUnaryOperator cube = x -> x * x * x;
        log.debug("LongUnaryOperator: {}", cube.applyAsLong(5L));
        
        // DoubleUnaryOperator - takes double, returns double
        DoubleUnaryOperator sqrt = Math::sqrt;
        log.debug("DoubleUnaryOperator: {}", sqrt.applyAsDouble(16.0));
        
        // IntBinaryOperator - takes two ints, returns int
        IntBinaryOperator add = Integer::sum;
        log.debug("IntBinaryOperator: {}", add.applyAsInt(5, 3));
        
        // LongBinaryOperator - takes two longs, returns long
        LongBinaryOperator multiply = (a, b) -> a * b;
        log.debug("LongBinaryOperator: {}", multiply.applyAsLong(5L, 3L));
        
        // DoubleBinaryOperator - takes two doubles, returns double
        DoubleBinaryOperator divide = (a, b) -> a / b;
        log.debug("DoubleBinaryOperator: {}", divide.applyAsDouble(10.0, 2.0));
        
        // IntPredicate - takes int, returns boolean
        IntPredicate isEven = i -> i % 2 == 0;
        log.debug("IntPredicate: {}", isEven.test(42));
        
        // LongPredicate - takes long, returns boolean
        LongPredicate isPositive = l -> l > 0;
        log.debug("LongPredicate: {}", isPositive.test(123L));
        
        // DoublePredicate - takes double, returns boolean
        DoublePredicate isFinite = Double::isFinite;
        log.debug("DoublePredicate: {}", isFinite.test(3.14159));
        
        // IntConsumer - takes int, returns void
        IntConsumer printInt = i -> System.out.println("Int: " + i);
        printInt.accept(42);
        
        // LongConsumer - takes long, returns void
        LongConsumer printLong = l -> System.out.println("Long: " + l);
        printLong.accept(123456789L);
        
        // DoubleConsumer - takes double, returns void
        DoubleConsumer printDouble = d -> System.out.println("Double: " + d);
        printDouble.accept(3.14159);
        
        // IntSupplier - takes nothing, returns int
        IntSupplier randomInt = () -> (int) (Math.random() * 100);
        log.debug("IntSupplier: {}", randomInt.getAsInt());
        
        // LongSupplier - takes nothing, returns long
        LongSupplier currentTime = System::currentTimeMillis;
        log.debug("LongSupplier: {}", currentTime.getAsLong());
        
        // DoubleSupplier - takes nothing, returns double
        DoubleSupplier randomDouble = Math::random;
        log.debug("DoubleSupplier: {}", randomDouble.getAsDouble());
    }

    /**
     * Demonstrates advanced composition and chaining patterns
     * 
     * This shows how to create complex functional compositions.
     */
    public static void demonstrateAdvancedComposition() {
        log.info("=== Demonstrating Advanced Composition ===");
        
        // Function composition with error handling
        Function<String, String> safeUpperCase = s -> {
            try {
                return s.toUpperCase();
            } catch (Exception e) {
                log.warn("Error in uppercase: {}", e.getMessage());
                return s;
            }
        };
        
        Function<String, String> safeTrim = s -> {
            try {
                return s.trim();
            } catch (Exception e) {
                log.warn("Error in trim: {}", e.getMessage());
                return s;
            }
        };
        
        Function<String, String> safeAddPrefix = s -> {
            try {
                return "Processed: " + s;
            } catch (Exception e) {
                log.warn("Error in add prefix: {}", e.getMessage());
                return s;
            }
        };
        
        // Compose functions with error handling
        Function<String, String> safePipeline = safeTrim
                .andThen(safeUpperCase)
                .andThen(safeAddPrefix);
        
        log.debug("Safe pipeline result: {}", safePipeline.apply("  hello world  "));
        
        // Predicate composition with logging
        Predicate<String> isNotEmpty = s -> s != null && !s.isEmpty();
        Predicate<String> isLongEnough = s -> s.length() >= 3;
        Predicate<String> containsLetter = s -> s.matches(".*[a-zA-Z].*");
        
        Predicate<String> complexPredicate = isNotEmpty
                .and(isLongEnough)
                .and(containsLetter);
        
        log.debug("Complex predicate test: {}", complexPredicate.test("hello"));
        
        // Consumer composition with error handling
        Consumer<String> logConsumer = s -> log.debug("Logging: {}", s);
        Consumer<String> printConsumer = s -> System.out.println("Printing: " + s);
        Consumer<String> errorConsumer = s -> {
            if (s.contains("error")) {
                log.error("Error detected: {}", s);
            }
        };
        
        Consumer<String> composedConsumer = logConsumer
                .andThen(printConsumer)
                .andThen(errorConsumer);
        
        composedConsumer.accept("test message");
        composedConsumer.accept("error message");
        
        // Supplier composition with caching
        Supplier<String> expensiveSupplier = () -> {
            log.debug("Expensive operation...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Expensive result";
        };
        
        Map<String, String> cache = new HashMap<>();
        Supplier<String> cachedSupplier = () -> cache.computeIfAbsent("key", k -> expensiveSupplier.get());
        
        log.debug("First call: {}", cachedSupplier.get());
        log.debug("Second call: {}", cachedSupplier.get());
        
        // Function composition with Optional
        Function<String, Optional<String>> safeParse = s -> {
            try {
                return Optional.of(s.toUpperCase());
            } catch (Exception e) {
                return Optional.empty();
            }
        };
        
        Function<Optional<String>, String> safeGet = opt -> opt.orElse("Default");
        
        Function<String, String> safeComposition = safeParse.andThen(safeGet);
        
        log.debug("Safe composition: {}", safeComposition.apply("hello"));
        log.debug("Safe composition with null: {}", safeComposition.apply(null));
    }

    /**
     * Demonstrates method references and constructor references
     * 
     * Method references provide a way to refer to methods without invoking them.
     */
    public static void demonstrateMethodReferences() {
        log.info("=== Demonstrating Method References ===");
        
        // Static method references
        Function<String, Integer> parseInt = Integer::parseInt;
        log.debug("Parse int: {}", parseInt.apply("42"));
        
        // Instance method references
        Function<String, String> toUpperCase = String::toUpperCase;
        log.debug("To uppercase: {}", toUpperCase.apply("hello"));
        
        // Instance method references with different types
        BiFunction<String, String, Boolean> equals = String::equals;
        log.debug("Equals: {}", equals.apply("hello", "hello"));
        
        // Constructor references
        Function<String, StringBuilder> stringBuilderConstructor = StringBuilder::new;
        StringBuilder sb = stringBuilderConstructor.apply("Hello");
        log.debug("StringBuilder: {}", sb);
        
        // Constructor references with two parameters (using lambda to handle checked exceptions)
        BiFunction<byte[], String, String> stringConstructor = (bytes, charset) -> {
            try {
                return new String(bytes, charset);
            } catch (java.io.UnsupportedEncodingException e) {
                return "Error: " + e.getMessage();
            }
        };
        String result = stringConstructor.apply("Hello".getBytes(), "UTF-8");
        log.debug("String constructor: {}", result);
        
        // Method references with collections
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        
        // Static method reference
        names.forEach(System.out::println);
        
        // Instance method reference
        names.forEach(String::toUpperCase);
        
        // Method reference with streams
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        log.debug("Upper names: {}", upperNames);
        
        // Method reference with filtering
        List<String> longNames = names.stream()
                .filter(s -> s.length() > 4)
                .collect(Collectors.toList());
        log.debug("Long names: {}", longNames);
        
        // Method reference with sorting
        List<String> sortedNames = names.stream()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
        log.debug("Sorted names: {}", sortedNames);
        
        // Method reference with reduction
        Optional<String> longest = names.stream()
                .reduce((s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        log.debug("Longest name: {}", longest.orElse("None"));
        
        // Method reference with mapping
        List<Integer> lengths = names.stream()
                .map(String::length)
                .collect(Collectors.toList());
        log.debug("Name lengths: {}", lengths);
        
        // Method reference with custom objects
        List<Person> people = Arrays.asList(
                new Person("Alice", 25),
                new Person("Bob", 30),
                new Person("Charlie", 35)
        );
        
        // Method reference to getter
        List<String> personNames = people.stream()
                .map(Person::getName)
                .collect(Collectors.toList());
        log.debug("Person names: {}", personNames);
        
        // Method reference to setter
        people.forEach(person -> person.setAge(person.getAge() + 1));
        log.debug("Aged people: {}", people);
        
        // Method reference with filtering
        List<Person> youngPeople = people.stream()
                .filter(person -> person.getAge() < 30)
                .collect(Collectors.toList());
        log.debug("Young people: {}", youngPeople);
    }

    /**
     * Demonstrates higher-order functions and currying
     * 
     * Higher-order functions take functions as parameters or return functions.
     */
    public static void demonstrateHigherOrderFunctions() {
        log.info("=== Demonstrating Higher-Order Functions ===");
        
        // Function that returns a function (currying)
        Function<Integer, Function<Integer, Integer>> add = x -> y -> x + y;
        Function<Integer, Integer> add5 = add.apply(5);
        Function<Integer, Integer> add10 = add.apply(10);
        
        log.debug("Add 5 to 3: {}", add5.apply(3));
        log.debug("Add 10 to 3: {}", add10.apply(3));
        
        // Function that takes a function as parameter
        Function<Function<String, String>, String> applyToString = f -> f.apply("hello");
        String result = applyToString.apply(String::toUpperCase);
        log.debug("Applied function: {}", result);
        
        // Higher-order function for filtering
        Function<Predicate<String>, List<String>> createFilter = predicate -> 
            Arrays.asList("apple", "banana", "cherry", "date").stream()
                .filter(predicate)
                .collect(Collectors.toList());
        
        List<String> longFruits = createFilter.apply(s -> s.length() > 5);
        List<String> fruitsStartingWithA = createFilter.apply(s -> s.startsWith("a"));
        
        log.debug("Long fruits: {}", longFruits);
        log.debug("Fruits starting with 'a': {}", fruitsStartingWithA);
        
        // Higher-order function for mapping
        Function<Function<String, String>, List<String>> createMapper = mapper -> 
            Arrays.asList("hello", "world", "java").stream()
                .map(mapper)
                .collect(Collectors.toList());
        
        List<String> uppercased = createMapper.apply(String::toUpperCase);
        List<String> withPrefix = createMapper.apply(s -> "Prefix: " + s);
        
        log.debug("Uppercased: {}", uppercased);
        log.debug("With prefix: {}", withPrefix);
        
        // Higher-order function for reduction
        Function<BinaryOperator<String>, Optional<String>> createReducer = reducer -> 
            Arrays.asList("hello", "world", "java").stream()
                .reduce(reducer);
        
        Optional<String> concatenated = createReducer.apply((s1, s2) -> s1 + " " + s2);
        Optional<String> longest = createReducer.apply((s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        
        log.debug("Concatenated: {}", concatenated.orElse("None"));
        log.debug("Longest: {}", longest.orElse("None"));
        
        // Higher-order function for validation
        Function<Predicate<String>, Function<String, Boolean>> createValidator = predicate -> 
            input -> predicate.test(input);
        
        Function<String, Boolean> isLongValidator = createValidator.apply(s -> s.length() > 5);
        Function<String, Boolean> isNotEmptyValidator = createValidator.apply(s -> !s.isEmpty());
        
        log.debug("Is 'hello' long: {}", isLongValidator.apply("hello"));
        log.debug("Is 'hello world' long: {}", isLongValidator.apply("hello world"));
        log.debug("Is empty not empty: {}", isNotEmptyValidator.apply(""));
        log.debug("Is 'hello' not empty: {}", isNotEmptyValidator.apply("hello"));
        
        // Higher-order function for transformation
        Function<Function<String, String>, Function<String, String>> createTransformer = transformer -> 
            input -> transformer.apply(input);
        
        Function<String, String> upperTransformer = createTransformer.apply(String::toUpperCase);
    }

    // Advanced Monadic Structures
    interface Maybe<T> {
        <R> Maybe<R> map(Function<T, R> mapper);
        <R> Maybe<R> flatMap(Function<T, Maybe<R>> mapper);
        T getOrElse(T defaultValue);
        boolean isPresent();
        
        static <T> Maybe<T> just(T value) {
            return new Just<>(value);
        }
        
        static <T> Maybe<T> nothing() {
            return new Nothing<>();
        }
    }

    // Static nested classes for the Maybe monad
    static class Just<T> implements Maybe<T> {
        private final T value;
        
        public Just(T value) {
            this.value = value;
        }
        
        @Override
        public <R> Maybe<R> map(Function<T, R> mapper) {
            return new Just<>(mapper.apply(value));
        }
        
        @Override
        public <R> Maybe<R> flatMap(Function<T, Maybe<R>> mapper) {
            return mapper.apply(value);
        }
        
        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }
        
        @Override
        public boolean isPresent() {
            return true;
        }
        
        @Override
        public String toString() {
            return "Just(" + value + ")";
        }
    }
    
    static class Nothing<T> implements Maybe<T> {
        @Override
        public <R> Maybe<R> map(Function<T, R> mapper) {
            return new Nothing<>();
        }
        
        @Override
        public <R> Maybe<R> flatMap(Function<T, Maybe<R>> mapper) {
            return new Nothing<>();
        }
        
        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }
        
        @Override
        public boolean isPresent() {
            return false;
        }
        
        @Override
        public String toString() {
            return "Nothing";
        }
    }

    /**
     * Demonstrates monadic operations and functors
     * 
     * Monads provide a way to chain operations in a functional style.
     */
    public static void demonstrateMonadicOperations() {
        log.info("=== Demonstrating Monadic Operations ===");
        
        
        
        // Usage examples
        Maybe<String> maybeString = Maybe.just("hello");
        Maybe<String> maybeNothing = Maybe.nothing();
        
        // Map operation
        Maybe<String> mapped = maybeString.map(String::toUpperCase);
        log.debug("Mapped: {}", mapped);
        
        // FlatMap operation
        Maybe<String> flatMapped = maybeString.flatMap(s -> Maybe.just(s + " world"));
        log.debug("Flat mapped: {}", flatMapped);
        
        // Get or else
        String result = maybeString.getOrElse("default");
        log.debug("Get or else: {}", result);
        
        String nothingResult = maybeNothing.getOrElse("default");
        log.debug("Nothing get or else: {}", nothingResult);
        
        // Chaining operations
        Maybe<String> chained = maybeString
                .map(String::toUpperCase)
                .flatMap(s -> Maybe.just(s + "!"))
                .map(s -> "Result: " + s);
        log.debug("Chained: {}", chained);
        
        // Functor operations
        Maybe<Integer> length = maybeString.map(String::length);
        log.debug("Length: {}", length);
        
        Maybe<String> prefixed = maybeString.map(s -> "Hello, " + s);
        log.debug("Prefixed: {}", prefixed);
    }

    // Helper class for method references
    static class Person {
        private String name;
        private int age;
        
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
        
        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
}
