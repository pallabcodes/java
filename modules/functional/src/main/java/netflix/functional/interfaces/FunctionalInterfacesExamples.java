package netflix.functional.interfaces;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Functional Interfaces Examples
 * 
 * This class demonstrates all built-in functional interfaces in Java 8+ including:
 * - Function<T, R> - Single input, single output
 * - BiFunction<T, U, R> - Two inputs, single output
 * - Predicate<T> - Single input, boolean output
 * - BiPredicate<T, U> - Two inputs, boolean output
 * - Consumer<T> - Single input, no output
 * - BiConsumer<T, U> - Two inputs, no output
 * - Supplier<T> - No input, single output
 * - UnaryOperator<T> - Single input, same type output
 * - BinaryOperator<T> - Two inputs, same type output
 * - Custom functional interfaces
 * - Higher-order functions
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class FunctionalInterfacesExamples {

    /**
     * Demonstrates Function<T, R> interface
     * 
     * Function represents a function that takes one argument and produces a result.
     * It's the most commonly used functional interface.
     */
    public static void demonstrateFunction() {
        log.info("=== Demonstrating Function<T, R> ===");
        
        // Basic function usage
        Function<String, Integer> stringLength = String::length;
        Function<String, String> toUpperCase = String::toUpperCase;
        Function<String, String> addPrefix = s -> "Hello, " + s;
        
        // Function composition
        Function<String, String> composed = addPrefix.andThen(toUpperCase);
        
        // Apply functions
        String input = "world";
        log.debug("Original: {}", input);
        log.debug("Length: {}", stringLength.apply(input));
        log.debug("Uppercase: {}", toUpperCase.apply(input));
        log.debug("With prefix: {}", addPrefix.apply(input));
        log.debug("Composed: {}", composed.apply(input));
        
        // Function chaining
        Function<String, String> pipeline = stringLength
                .andThen(i -> "Length: " + i)
                .andThen(String::toUpperCase);
        
        log.debug("Pipeline result: {}", pipeline.apply(input));
        
        // Function with collections
        List<String> names = Arrays.asList("alice", "bob", "charlie");
        List<Integer> lengths = names.stream()
                .map(stringLength)
                .collect(Collectors.toList());
        
        log.debug("Name lengths: {}", lengths);
    }

    /**
     * Demonstrates BiFunction<T, U, R> interface
     * 
     * BiFunction represents a function that takes two arguments and produces a result.
     */
    public static void demonstrateBiFunction() {
        log.info("=== Demonstrating BiFunction<T, U, R> ===");
        
        // Basic bifunction usage
        BiFunction<String, String, String> concatenate = (s1, s2) -> s1 + " " + s2;
        BiFunction<Integer, Integer, Integer> add = Integer::sum;
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        
        // Apply bifunctions
        log.debug("Concatenate: {}", concatenate.apply("Hello", "World"));
        log.debug("Add: {}", add.apply(5, 3));
        log.debug("Repeat: {}", repeat.apply("Java", 3));
        
        // Bifunction with collections
        List<String> firstNames = Arrays.asList("John", "Jane", "Bob");
        List<String> lastNames = Arrays.asList("Doe", "Smith", "Johnson");
        
        List<String> fullNames = new ArrayList<>();
        for (int i = 0; i < Math.min(firstNames.size(), lastNames.size()); i++) {
            fullNames.add(concatenate.apply(firstNames.get(i), lastNames.get(i)));
        }
        
        log.debug("Full names: {}", fullNames);
        
        // Bifunction composition
        BiFunction<String, String, String> upperConcat = concatenate.andThen(String::toUpperCase);
        log.debug("Upper concatenate: {}", upperConcat.apply("hello", "world"));
    }

    /**
     * Demonstrates Predicate<T> interface
     * 
     * Predicate represents a function that takes one argument and returns a boolean.
     */
    public static void demonstratePredicate() {
        log.info("=== Demonstrating Predicate<T> ===");
        
        // Basic predicate usage
        Predicate<String> isLong = s -> s.length() > 5;
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> isNotEmpty = s -> !s.isEmpty();
        
        // Predicate composition
        Predicate<String> isLongAndStartsWithA = isLong.and(startsWithA);
        Predicate<String> isLongOrStartsWithA = isLong.or(startsWithA);
        Predicate<String> isNotLong = isLong.negate();
        
        // Test predicates
        String testString = "Alice";
        log.debug("String: {}", testString);
        log.debug("Is long: {}", isLong.test(testString));
        log.debug("Starts with A: {}", startsWithA.test(testString));
        log.debug("Is not empty: {}", isNotEmpty.test(testString));
        log.debug("Is long and starts with A: {}", isLongAndStartsWithA.test(testString));
        log.debug("Is long or starts with A: {}", isLongOrStartsWithA.test(testString));
        log.debug("Is not long: {}", isNotLong.test(testString));
        
        // Predicate with collections
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        List<String> longNames = names.stream()
                .filter(isLong)
                .collect(Collectors.toList());
        
        log.debug("Long names: {}", longNames);
        
        // Complex predicate
        Predicate<String> complexPredicate = s -> s.length() > 3 && s.length() < 10 && s.contains("a");
        List<String> complexFiltered = names.stream()
                .filter(complexPredicate)
                .collect(Collectors.toList());
        
        log.debug("Complex filtered: {}", complexFiltered);
    }

    /**
     * Demonstrates BiPredicate<T, U> interface
     * 
     * BiPredicate represents a function that takes two arguments and returns a boolean.
     */
    public static void demonstrateBiPredicate() {
        log.info("=== Demonstrating BiPredicate<T, U> ===");
        
        // Basic bipredicate usage
        BiPredicate<String, String> isEqual = String::equals;
        BiPredicate<Integer, Integer> isGreater = (a, b) -> a > b;
        BiPredicate<String, Integer> isLengthGreater = (s, n) -> s.length() > n;
        
        // Test bipredicates
        log.debug("Is equal: {}", isEqual.test("hello", "hello"));
        log.debug("Is greater: {}", isGreater.test(10, 5));
        log.debug("Is length greater: {}", isLengthGreater.test("hello", 3));
        
        // Bipredicate composition
        BiPredicate<String, String> isEqualAndLong = isEqual.and((s1, s2) -> s1.length() > 5);
        BiPredicate<String, String> isEqualOrLong = isEqual.or((s1, s2) -> s1.length() > 5);
        
        log.debug("Is equal and long: {}", isEqualAndLong.test("hello", "hello"));
        log.debug("Is equal or long: {}", isEqualOrLong.test("hi", "hi"));
        
        // Bipredicate with collections
        List<String> names1 = Arrays.asList("Alice", "Bob", "Charlie");
        List<String> names2 = Arrays.asList("Alice", "Bob", "David");
        
        for (int i = 0; i < Math.min(names1.size(), names2.size()); i++) {
            log.debug("Names {} and {} are equal: {}", 
                     names1.get(i), names2.get(i), 
                     isEqual.test(names1.get(i), names2.get(i)));
        }
    }

    /**
     * Demonstrates Consumer<T> interface
     * 
     * Consumer represents a function that takes one argument and returns no result.
     */
    public static void demonstrateConsumer() {
        log.info("=== Demonstrating Consumer<T> ===");
        
        // Basic consumer usage
        Consumer<String> print = System.out::println;
        Consumer<String> printWithPrefix = s -> System.out.println("Message: " + s);
        Consumer<String> printLength = s -> System.out.println("Length: " + s.length());
        
        // Consumer composition
        Consumer<String> printAndLength = printWithPrefix.andThen(printLength);
        
        // Apply consumers
        String message = "Hello World";
        print.accept(message);
        printWithPrefix.accept(message);
        printLength.accept(message);
        printAndLength.accept(message);
        
        // Consumer with collections
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        names.forEach(printWithPrefix);
        
        // Consumer chaining
        Consumer<String> pipeline = printWithPrefix
                .andThen(s -> System.out.println("Uppercase: " + s.toUpperCase()))
                .andThen(s -> System.out.println("Length: " + s.length()));
        
        pipeline.accept("Functional Programming");
    }

    /**
     * Demonstrates BiConsumer<T, U> interface
     * 
     * BiConsumer represents a function that takes two arguments and returns no result.
     */
    public static void demonstrateBiConsumer() {
        log.info("=== Demonstrating BiConsumer<T, U> ===");
        
        // Basic biconsumer usage
        BiConsumer<String, Integer> printNameAndAge = (name, age) -> 
            System.out.println(name + " is " + age + " years old");
        BiConsumer<String, String> printFullName = (first, last) -> 
            System.out.println("Full name: " + first + " " + last);
        
        // Apply biconsumers
        printNameAndAge.accept("Alice", 25);
        printFullName.accept("John", "Doe");
        
        // Biconsumer composition
        BiConsumer<String, Integer> printAndLog = printNameAndAge.andThen((name, age) -> 
            System.out.println("Logged: " + name + " - " + age));
        
        printAndLog.accept("Bob", 30);
        
        // Biconsumer with collections
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        List<Integer> ages = Arrays.asList(25, 30, 35);
        
        for (int i = 0; i < Math.min(names.size(), ages.size()); i++) {
            printNameAndAge.accept(names.get(i), ages.get(i));
        }
    }

    /**
     * Demonstrates Supplier<T> interface
     * 
     * Supplier represents a function that takes no arguments and returns a result.
     */
    public static void demonstrateSupplier() {
        log.info("=== Demonstrating Supplier<T> ===");
        
        // Basic supplier usage
        Supplier<String> getCurrentTime = () -> new Date().toString();
        Supplier<Double> getRandomNumber = Math::random;
        Supplier<String> getGreeting = () -> "Hello from Supplier!";
        
        // Apply suppliers
        log.debug("Current time: {}", getCurrentTime.get());
        log.debug("Random number: {}", getRandomNumber.get());
        log.debug("Greeting: {}", getGreeting.get());
        
        // Supplier with collections
        List<String> greetings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            greetings.add(getGreeting.get());
        }
        
        log.debug("Greetings: {}", greetings);
        
        // Supplier with Optional
        Optional<String> optionalValue = Optional.ofNullable(getGreeting.get());
        optionalValue.ifPresent(System.out::println);
        
        // Supplier with lazy evaluation
        Supplier<String> expensiveOperation = () -> {
            log.debug("Performing expensive operation...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Expensive result";
        };
        
        // Only execute when needed
        log.debug("About to call expensive operation...");
        String result = expensiveOperation.get();
        log.debug("Result: {}", result);
    }

    /**
     * Demonstrates UnaryOperator<T> interface
     * 
     * UnaryOperator represents a function that takes one argument and returns a result of the same type.
     */
    public static void demonstrateUnaryOperator() {
        log.info("=== Demonstrating UnaryOperator<T> ===");
        
        // Basic unary operator usage
        UnaryOperator<String> toUpperCase = String::toUpperCase;
        UnaryOperator<String> addPrefix = s -> "Mr. " + s;
        UnaryOperator<Integer> square = n -> n * n;
        UnaryOperator<Integer> increment = n -> n + 1;
        
        // Apply unary operators
        log.debug("Uppercase: {}", toUpperCase.apply("hello"));
        log.debug("With prefix: {}", addPrefix.apply("Smith"));
        log.debug("Square: {}", square.apply(5));
        log.debug("Increment: {}", increment.apply(10));
        
        // Unary operator composition
        Function<String, String> composed = addPrefix.andThen(toUpperCase);
        log.debug("Composed: {}", composed.apply("smith"));
        
        // Unary operator with collections
        List<String> names = Arrays.asList("alice", "bob", "charlie");
        List<String> processed = names.stream()
                .map(toUpperCase)
                .collect(Collectors.toList());
        
        log.debug("Processed names: {}", processed);
        
        // Unary operator chaining
        Function<String, String> pipeline = toUpperCase
                .andThen(s -> s + "!")
                .andThen(s -> "Hello, " + s);
        
        log.debug("Pipeline result: {}", pipeline.apply("world"));
    }

    /**
     * Demonstrates BinaryOperator<T> interface
     * 
     * BinaryOperator represents a function that takes two arguments and returns a result of the same type.
     */
    public static void demonstrateBinaryOperator() {
        log.info("=== Demonstrating BinaryOperator<T> ===");
        
        // Basic binary operator usage
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<Integer> multiply = (a, b) -> a * b;
        BinaryOperator<String> concatenate = (s1, s2) -> s1 + " " + s2;
        BinaryOperator<Integer> max = Integer::max;
        BinaryOperator<Integer> min = Integer::min;
        
        // Apply binary operators
        log.debug("Add: {}", add.apply(5, 3));
        log.debug("Multiply: {}", multiply.apply(4, 6));
        log.debug("Concatenate: {}", concatenate.apply("Hello", "World"));
        log.debug("Max: {}", max.apply(10, 7));
        log.debug("Min: {}", min.apply(10, 7));
        
        // Binary operator with collections
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> sum = numbers.stream().reduce(add);
        Optional<Integer> product = numbers.stream().reduce(multiply);
        Optional<Integer> maximum = numbers.stream().reduce(max);
        Optional<Integer> minimum = numbers.stream().reduce(min);
        
        log.debug("Sum: {}", sum.orElse(0));
        log.debug("Product: {}", product.orElse(1));
        log.debug("Maximum: {}", maximum.orElse(0));
        log.debug("Minimum: {}", minimum.orElse(0));
        
        // Binary operator with custom logic
        BinaryOperator<String> longest = (s1, s2) -> s1.length() > s2.length() ? s1 : s2;
        Optional<String> longestString = Arrays.asList("short", "medium", "very long").stream()
                .reduce(longest);
        
        log.debug("Longest string: {}", longestString.orElse(""));
    }

    /**
     * Demonstrates custom functional interfaces
     * 
     * Custom functional interfaces can be created for specific use cases.
     */
    public static void demonstrateCustomFunctionalInterfaces() {
        log.info("=== Demonstrating Custom Functional Interfaces ===");
        
        // Custom functional interface for validation
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
        
        // Custom functional interface for transformation
        @FunctionalInterface
        interface Transformer<T, R> {
            R transform(T input);
            
            default <V> Transformer<T, V> andThen(Transformer<R, V> other) {
                return input -> other.transform(this.transform(input));
            }
        }
        
        // Custom functional interface for action with context
        @FunctionalInterface
        interface ContextualAction<T, C> {
            void execute(T input, C context);
        }
        
        // Usage examples
        Validator<String> isNotEmpty = s -> s != null && !s.isEmpty();
        Validator<String> isLongEnough = s -> s.length() >= 3;
        Validator<String> complexValidator = isNotEmpty.and(isLongEnough);
        
        log.debug("Is 'hello' valid: {}", complexValidator.validate("hello"));
        log.debug("Is 'hi' valid: {}", complexValidator.validate("hi"));
        log.debug("Is empty valid: {}", complexValidator.validate(""));
        
        Transformer<String, String> toUpperCase = String::toUpperCase;
        Transformer<String, String> addPrefix = s -> "Hello, " + s;
        Transformer<String, String> composed = toUpperCase.andThen(addPrefix);
        
        log.debug("Transformed: {}", composed.transform("world"));
        
        ContextualAction<String, Integer> printWithContext = (s, count) -> 
            System.out.println("Processing " + s + " (count: " + count + ")");
        
        printWithContext.execute("test", 42);
    }

    /**
     * Demonstrates higher-order functions
     * 
     * Higher-order functions are functions that take other functions as parameters
     * or return functions as results.
     */
    public static void demonstrateHigherOrderFunctions() {
        log.info("=== Demonstrating Higher-Order Functions ===");
        
        // Function that returns a function
        Function<Integer, Function<Integer, Integer>> createAdder = x -> y -> x + y;
        Function<Integer, Integer> add5 = createAdder.apply(5);
        Function<Integer, Integer> add10 = createAdder.apply(10);
        
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
    }
}
