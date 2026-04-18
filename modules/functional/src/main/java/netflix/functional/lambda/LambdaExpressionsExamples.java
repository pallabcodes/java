package netflix.functional.lambda;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Netflix Production-Grade Lambda Expressions Examples
 * 
 * This class demonstrates comprehensive lambda expressions concepts including:
 * - Basic lambda syntax and structure
 * - Lambda expressions with different parameter counts
 * - Lambda expressions with different return types
 * - Lambda expressions with collections
 * - Lambda expressions with streams
 * - Lambda expressions with functional interfaces
 * - Lambda expressions with method references
 * - Lambda expressions with exception handling
 * - Lambda expressions with variable capture
 * - Lambda expressions with this and super references
 * - Lambda expressions with generic types
 * - Lambda expressions with varargs
 * - Lambda expressions with arrays
 * - Lambda expressions with nested classes
 * - Lambda expressions with anonymous classes
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class LambdaExpressionsExamples {

    /**
     * Demonstrates basic lambda expressions
     * 
     * Lambda expressions provide a concise way to represent anonymous functions.
     */
    public static void demonstrateBasicLambdas() {
        log.info("=== Demonstrating Basic Lambda Expressions ===");
        
        // No parameters
        Runnable noParams = () -> log.debug("Hello from lambda with no parameters!");
        noParams.run();
        
        // Single parameter
        Consumer<String> singleParam = name -> log.debug("Hello, " + name + "!");
        singleParam.accept("Alice");
        
        // Single parameter with type
        Consumer<String> singleParamTyped = (String name) -> log.debug("Hello, " + name + "!");
        singleParamTyped.accept("Bob");
        
        // Single parameter with braces
        Consumer<String> singleParamBraces = name -> {
            String greeting = "Hello, " + name + "!";
            log.debug(greeting);
        };
        singleParamBraces.accept("Charlie");
        
        // Two parameters
        BiConsumer<String, Integer> twoParams = (name, age) -> 
            log.debug(name + " is " + age + " years old");
        twoParams.accept("David", 30);
        
        // Two parameters with types
        BiConsumer<String, Integer> twoParamsTyped = (String name, Integer age) -> 
            log.debug(name + " is " + age + " years old");
        twoParamsTyped.accept("Eve", 25);
        
        // Two parameters with braces
        BiConsumer<String, Integer> twoParamsBraces = (name, age) -> {
            String message = name + " is " + age + " years old";
            log.debug(message);
        };
        twoParamsBraces.accept("Frank", 35);
    }

    /**
     * Demonstrates lambda expressions with return values
     * 
     * Lambda expressions can return values using the arrow syntax.
     */
    public static void demonstrateLambdaReturns() {
        log.info("=== Demonstrating Lambda Expressions with Return Values ===");
        
        // Single parameter with return
        Function<String, String> toUpperCase = name -> name.toUpperCase();
        log.debug("Uppercase: {}", toUpperCase.apply("hello"));
        
        // Single parameter with explicit return
        Function<String, String> toUpperCaseExplicit = name -> {
            return name.toUpperCase();
        };
        log.debug("Uppercase explicit: {}", toUpperCaseExplicit.apply("world"));
        
        // Two parameters with return
        BiFunction<String, String, String> concatenate = (s1, s2) -> s1 + " " + s2;
        log.debug("Concatenated: {}", concatenate.apply("Hello", "World"));
        
        // Two parameters with explicit return
        BiFunction<String, String, String> concatenateExplicit = (s1, s2) -> {
            return s1 + " " + s2;
        };
        log.debug("Concatenated explicit: {}", concatenateExplicit.apply("Hello", "World"));
        
        // No parameters with return
        Supplier<String> getGreeting = () -> "Hello from supplier!";
        log.debug("Greeting: {}", getGreeting.get());
        
        // No parameters with explicit return
        Supplier<String> getGreetingExplicit = () -> {
            return "Hello from supplier explicit!";
        };
        log.debug("Greeting explicit: {}", getGreetingExplicit.get());
        
        // Boolean return
        Predicate<String> isLong = name -> name.length() > 5;
        log.debug("Is 'hello' long: {}", isLong.test("hello"));
        log.debug("Is 'hi' long: {}", isLong.test("hi"));
        
        // Boolean return with explicit return
        Predicate<String> isLongExplicit = name -> {
            return name.length() > 5;
        };
        log.debug("Is 'hello' long explicit: {}", isLongExplicit.test("hello"));
    }

    /**
     * Demonstrates lambda expressions with collections
     * 
     * Lambda expressions are commonly used with collections for iteration and processing.
     */
    public static void demonstrateLambdaWithCollections() {
        log.info("=== Demonstrating Lambda Expressions with Collections ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        
        // forEach with lambda
        log.debug("Names using forEach:");
        names.forEach(name -> log.debug("  " + name));
        
        // forEach with method reference
        log.debug("Names using method reference:");
        names.forEach(System.out::println);
        
        // filter with lambda
        List<String> longNames = names.stream()
                .filter(name -> name.length() > 4)
                .collect(Collectors.toList());
        log.debug("Long names: {}", longNames);
        
        // map with lambda
        List<String> upperNames = names.stream()
                .map(name -> name.toUpperCase())
                .collect(Collectors.toList());
        log.debug("Upper names: {}", upperNames);
        
        // reduce with lambda
        Optional<String> longestName = names.stream()
                .reduce((name1, name2) -> name1.length() > name2.length() ? name1 : name2);
        log.debug("Longest name: {}", longestName.orElse("None"));
        
        // collect with lambda
        Map<String, Integer> nameLengths = names.stream()
                .collect(Collectors.toMap(
                    name -> name,
                    name -> name.length()
                ));
        log.debug("Name lengths: {}", nameLengths);
        
        // groupBy with lambda
        Map<Integer, List<String>> groupedByLength = names.stream()
                .collect(Collectors.groupingBy(String::length));
        log.debug("Grouped by length: {}", groupedByLength);
    }

    /**
     * Demonstrates lambda expressions with streams
     * 
     * Lambda expressions are essential for stream operations.
     */
    public static void demonstrateLambdaWithStreams() {
        log.info("=== Demonstrating Lambda Expressions with Streams ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // filter with lambda
        List<Integer> evenNumbers = numbers.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
        log.debug("Even numbers: {}", evenNumbers);
        
        // map with lambda
        List<Integer> squaredNumbers = numbers.stream()
                .map(n -> n * n)
                .collect(Collectors.toList());
        log.debug("Squared numbers: {}", squaredNumbers);
        
        // reduce with lambda
        int sum = numbers.stream()
                .reduce(0, (a, b) -> a + b);
        log.debug("Sum: {}", sum);
        
        // reduce with method reference
        int sumMethodRef = numbers.stream()
                .reduce(0, Integer::sum);
        log.debug("Sum with method reference: {}", sumMethodRef);
        
        // collect with lambda
        List<String> numberStrings = numbers.stream()
                .map(n -> "Number: " + n)
                .collect(Collectors.toList());
        log.debug("Number strings: {}", numberStrings);
        
        // anyMatch with lambda
        boolean hasEven = numbers.stream()
                .anyMatch(n -> n % 2 == 0);
        log.debug("Has even numbers: {}", hasEven);
        
        // allMatch with lambda
        boolean allPositive = numbers.stream()
                .allMatch(n -> n > 0);
        log.debug("All positive: {}", allPositive);
        
        // noneMatch with lambda
        boolean noneNegative = numbers.stream()
                .noneMatch(n -> n < 0);
        log.debug("None negative: {}", noneNegative);
    }

    /**
     * Demonstrates lambda expressions with functional interfaces
     * 
     * Lambda expressions can be assigned to functional interface variables.
     */
    public static void demonstrateLambdaWithFunctionalInterfaces() {
        log.info("=== Demonstrating Lambda Expressions with Functional Interfaces ===");
        
        // Function interface
        Function<String, Integer> stringLength = s -> s.length();
        log.debug("Length of 'hello': {}", stringLength.apply("hello"));
        
        // BiFunction interface
        BiFunction<String, String, String> concatenate = (s1, s2) -> s1 + " " + s2;
        log.debug("Concatenated: {}", concatenate.apply("Hello", "World"));
        
        // Predicate interface
        Predicate<String> isLong = s -> s.length() > 5;
        log.debug("Is 'hello' long: {}", isLong.test("hello"));
        
        // Consumer interface
        Consumer<String> print = s -> log.debug("Value: {}", s);
        print.accept("Hello World");
        
        // Supplier interface
        Supplier<String> getValue = () -> "Hello from supplier";
        log.debug("Value: {}", getValue.get());
        
        // UnaryOperator interface
        UnaryOperator<String> toUpperCase = s -> s.toUpperCase();
        log.debug("Uppercase: {}", toUpperCase.apply("hello"));
        
        // BinaryOperator interface
        BinaryOperator<Integer> add = (a, b) -> a + b;
        log.debug("Sum: {}", add.apply(5, 3));
    }

    /**
     * Demonstrates lambda expressions with method references
     * 
     * Method references provide a shorthand for lambda expressions that call existing methods.
     */
    public static void demonstrateLambdaWithMethodReferences() {
        log.info("=== Demonstrating Lambda Expressions with Method References ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        
        // Static method reference
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        log.debug("Upper names: {}", upperNames);
        
        // Instance method reference
        List<Integer> lengths = names.stream()
                .map(String::length)
                .collect(Collectors.toList());
        log.debug("Lengths: {}", lengths);
        
        // Constructor reference
        List<String> greetings = names.stream()
                .map(name -> "Hello, " + name)
                .collect(Collectors.toList());
        log.debug("Greetings: {}", greetings);
        
        // Static method reference with parameters
        List<String> formatted = names.stream()
                .map(name -> String.format("Name: %s", name))
                .collect(Collectors.toList());
        log.debug("Formatted: {}", formatted);
        
        // Method reference with forEach
        names.forEach(System.out::println);
        
        // Method reference with filter
        List<String> longNames = names.stream()
                .filter(name -> name.length() > 4)
                .collect(Collectors.toList());
        log.debug("Long names: {}", longNames);
    }

    /**
     * Demonstrates lambda expressions with exception handling
     * 
     * Lambda expressions can handle exceptions using try-catch blocks.
     */
    public static void demonstrateLambdaWithExceptionHandling() {
        log.info("=== Demonstrating Lambda Expressions with Exception Handling ===");
        
        List<String> numbers = Arrays.asList("1", "2", "3", "4", "5", "invalid", "6");
        
        // Lambda with exception handling
        List<Integer> parsedNumbers = numbers.stream()
                .map(number -> {
                    try {
                        return Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid number: {}", number);
                        return 0;
                    }
                })
                .collect(Collectors.toList());
        log.debug("Parsed numbers: {}", parsedNumbers);
        
        // Lambda with exception handling and filtering
        List<Integer> validNumbers = numbers.stream()
                .map(number -> {
                    try {
                        return Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.debug("Valid numbers: {}", validNumbers);
        
        // Lambda with exception handling and default value
        List<Integer> numbersWithDefault = numbers.stream()
                .map(number -> {
                    try {
                        return Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                })
                .collect(Collectors.toList());
        log.debug("Numbers with default: {}", numbersWithDefault);
    }

    /**
     * Demonstrates lambda expressions with variable capture
     * 
     * Lambda expressions can capture variables from their enclosing scope.
     */
    public static void demonstrateLambdaWithVariableCapture() {
        log.info("=== Demonstrating Lambda Expressions with Variable Capture ===");
        
        // Capturing local variables
        String prefix = "Hello, ";
        String suffix = "!";
        
        Consumer<String> greet = name -> {
            String message = prefix + name + suffix;
            log.debug(message);
        };
        
        greet.accept("Alice");
        greet.accept("Bob");
        
        // Capturing effectively final variables
        final String finalPrefix = "Greetings, ";
        Consumer<String> greetFinal = name -> {
            String message = finalPrefix + name + "!";
            log.debug(message);
        };
        
        greetFinal.accept("Charlie");
        
        // Capturing instance variables
        LambdaExpressionsExamples example = new LambdaExpressionsExamples();
        example.captureInstanceVariable();
        
        // Capturing static variables
        captureStaticVariable();
    }

    /**
     * Demonstrates capturing instance variables
     */
    private void captureInstanceVariable() {
        String instancePrefix = "Instance: ";
        
        Consumer<String> greet = name -> {
            String message = instancePrefix + name;
            log.debug(message);
        };
        
        greet.accept("David");
    }

    /**
     * Demonstrates capturing static variables
     */
    private static void captureStaticVariable() {
        String staticPrefix = "Static: ";
        
        Consumer<String> greet = name -> {
            String message = staticPrefix + name;
            log.debug(message);
        };
        
        greet.accept("Eve");
    }

    /**
     * Demonstrates lambda expressions with this and super references
     * 
     * Lambda expressions can reference this and super in certain contexts.
     */
    public static void demonstrateLambdaWithThisAndSuper() {
        log.info("=== Demonstrating Lambda Expressions with This and Super ===");
        
        // Lambda with this reference (in instance context)
        LambdaExpressionsExamples example = new LambdaExpressionsExamples();
        example.lambdaWithThis();
        
        // Lambda with super reference (in instance context)
        example.lambdaWithSuper();
    }

    /**
     * Demonstrates lambda with this reference
     */
    private void lambdaWithThis() {
        Consumer<String> greet = name -> {
            String message = this.getClass().getSimpleName() + ": " + name;
            log.debug(message);
        };
        
        greet.accept("Frank");
    }

    /**
     * Demonstrates lambda with super reference
     */
    private void lambdaWithSuper() {
        Consumer<String> greet = name -> {
            String message = super.getClass().getSimpleName() + ": " + name;
            log.debug(message);
        };
        
        greet.accept("Grace");
    }

    /**
     * Demonstrates lambda expressions with generic types
     * 
     * Lambda expressions can work with generic types.
     */
    public static void demonstrateLambdaWithGenerics() {
        log.info("=== Demonstrating Lambda Expressions with Generics ===");
        
        // Generic function
        Function<String, Integer> stringLength = s -> s.length();
        log.debug("Length: {}", stringLength.apply("hello"));
        
        // Generic predicate
        Predicate<String> isLong = s -> s.length() > 5;
        log.debug("Is long: {}", isLong.test("hello"));
        
        // Generic consumer
        Consumer<String> print = s -> log.debug("Value: {}", s);
        print.accept("world");
        
        // Generic supplier
        Supplier<String> getValue = () -> "Hello from generic supplier";
        log.debug("Value: {}", getValue.get());
        
        // Generic bifunction
        BiFunction<String, String, String> concatenate = (s1, s2) -> s1 + " " + s2;
        log.debug("Concatenated: {}", concatenate.apply("Hello", "World"));
        
        // Generic with collections
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        List<Integer> lengths = names.stream()
                .map(stringLength)
                .collect(Collectors.toList());
        log.debug("Lengths: {}", lengths);
    }

    /**
     * Demonstrates lambda expressions with varargs
     * 
     * Lambda expressions can work with varargs parameters.
     */
    public static void demonstrateLambdaWithVarargs() {
        log.info("=== Demonstrating Lambda Expressions with Varargs ===");
        
        // Lambda with varargs
        Function<String[], String> joinStrings = strings -> String.join(", ", strings);
        String result = joinStrings.apply(new String[]{"Alice", "Bob", "Charlie"});
        log.debug("Joined: {}", result);
        
        // Lambda with varargs and stream
        Function<String[], List<String>> processStrings = strings -> 
            Arrays.stream(strings)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        
        List<String> processed = processStrings.apply(new String[]{"alice", "bob", "charlie"});
        log.debug("Processed: {}", processed);
        
        // Lambda with varargs and filtering
        Function<String[], List<String>> filterLongStrings = strings ->
            Arrays.stream(strings)
                .filter(s -> s.length() > 4)
                .collect(Collectors.toList());
        
        List<String> filtered = filterLongStrings.apply(new String[]{"Alice", "Bob", "Charlie", "David"});
        log.debug("Filtered: {}", filtered);
    }

    /**
     * Demonstrates lambda expressions with arrays
     * 
     * Lambda expressions can work with arrays.
     */
    public static void demonstrateLambdaWithArrays() {
        log.info("=== Demonstrating Lambda Expressions with Arrays ===");
        
        String[] names = {"Alice", "Bob", "Charlie", "David", "Eve"};
        
        // Lambda with array iteration
        Arrays.stream(names).forEach(name -> log.debug("Name: {}", name));
        
        // Lambda with array filtering
        String[] longNames = Arrays.stream(names)
                .filter(name -> name.length() > 4)
                .toArray(String[]::new);
        log.debug("Long names: {}", Arrays.toString(longNames));
        
        // Lambda with array mapping
        String[] upperNames = Arrays.stream(names)
                .map(String::toUpperCase)
                .toArray(String[]::new);
        log.debug("Upper names: {}", Arrays.toString(upperNames));
        
        // Lambda with array reduction
        Optional<String> longestName = Arrays.stream(names)
                .reduce((name1, name2) -> name1.length() > name2.length() ? name1 : name2);
        log.debug("Longest name: {}", longestName.orElse("None"));
    }

    /**
     * Demonstrates lambda expressions with nested classes
     * 
     * Lambda expressions can be used within nested classes.
     */
    public static void demonstrateLambdaWithNestedClasses() {
        log.info("=== Demonstrating Lambda Expressions with Nested Classes ===");
        
        // Static nested class
        class StaticNestedClass {
            public void processNames(List<String> names) {
                names.forEach(name -> log.debug("Static nested: {}", name));
            }
        }
        
        StaticNestedClass staticNested = new StaticNestedClass();
        staticNested.processNames(Arrays.asList("Alice", "Bob"));
        
        // Inner class
        LambdaExpressionsExamples example = new LambdaExpressionsExamples();
        example.processWithInnerClass();
    }

    /**
     * Demonstrates lambda with inner class
     */
    private void processWithInnerClass() {
        class InnerClass {
            public void processNames(List<String> names) {
                names.forEach(name -> log.debug("Inner class: {}", name));
            }
        }
        
        InnerClass inner = new InnerClass();
        inner.processNames(Arrays.asList("Charlie", "David"));
    }

    /**
     * Demonstrates lambda expressions with anonymous classes
     * 
     * Lambda expressions can be used within anonymous classes.
     */
    public static void demonstrateLambdaWithAnonymousClasses() {
        log.info("=== Demonstrating Lambda Expressions with Anonymous Classes ===");
        
        // Anonymous class with lambda
        Runnable anonymousRunnable = new Runnable() {
            @Override
            public void run() {
                List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
                names.forEach(name -> log.debug("Anonymous class: {}", name));
            }
        };
        
        anonymousRunnable.run();
        
        // Anonymous class with lambda and local variables
        String prefix = "Hello, ";
        Runnable anonymousWithLocal = new Runnable() {
            @Override
            public void run() {
                List<String> names = Arrays.asList("David", "Eve");
                names.forEach(name -> log.debug(prefix + name));
            }
        };
        
        anonymousWithLocal.run();
    }
}
