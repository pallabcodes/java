package netflix.functional.optional;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Netflix Production-Grade Optional Examples
 * 
 * This class demonstrates comprehensive Optional concepts including:
 * - Optional creation and basic operations
 * - Monadic operations (map, flatMap, filter)
 * - Optional chaining and composition
 * - Optional with collections and streams
 * - Optional with exception handling
 * - Optional with custom operations
 * - Optional with performance considerations
 * - Optional with null safety
 * - Optional with default values
 * - Optional with conditional operations
 * - Optional with transformation
 * - Optional with validation
 * - Optional with error handling
 * - Optional with logging and monitoring
 * - Optional with testing
 * 
 * @author Netflix Java Functional Programming Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class OptionalExamples {

    /**
     * Demonstrates Optional creation and basic operations
     * 
     * Optional can be created in various ways and provides null safety.
     */
    public static void demonstrateOptionalCreation() {
        log.info("=== Demonstrating Optional Creation ===");
        
        // Empty Optional
        Optional<String> empty = Optional.empty();
        log.debug("Empty Optional: {}", empty);
        log.debug("Is empty: {}", empty.isEmpty());
        log.debug("Is present: {}", empty.isPresent());
        
        // Optional with value
        Optional<String> present = Optional.of("Hello World");
        log.debug("Present Optional: {}", present);
        log.debug("Is empty: {}", present.isEmpty());
        log.debug("Is present: {}", present.isPresent());
        log.debug("Value: {}", present.get());
        
        // Optional with nullable value
        String nullableValue = null;
        Optional<String> nullable = Optional.ofNullable(nullableValue);
        log.debug("Nullable Optional: {}", nullable);
        log.debug("Is empty: {}", nullable.isEmpty());
        
        // Optional with non-null value
        String nonNullValue = "Hello";
        Optional<String> nonNull = Optional.ofNullable(nonNullValue);
        log.debug("Non-null Optional: {}", nonNull);
        log.debug("Is present: {}", nonNull.isPresent());
        log.debug("Value: {}", nonNull.get());
        
        // Optional with supplier
        Optional<String> fromSupplier = Optional.ofNullable(getValue());
        log.debug("From supplier: {}", fromSupplier);
        
        // Optional with method reference
        Optional<String> fromMethodRef = Optional.ofNullable(getValue());
        log.debug("From method reference: {}", fromMethodRef);
    }

    /**
     * Demonstrates Optional monadic operations
     * 
     * Optional supports monadic operations like map, flatMap, and filter.
     */
    public static void demonstrateMonadicOperations() {
        log.info("=== Demonstrating Monadic Operations ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // Map operation
        Optional<String> mapped = optional.map(String::toUpperCase);
        log.debug("Mapped: {}", mapped.orElse("Empty"));
        
        // Map with custom function
        Optional<Integer> length = optional.map(String::length);
        log.debug("Length: {}", length.orElse(0));
        
        // FlatMap operation
        Optional<String> flatMapped = optional.flatMap(s -> Optional.of(s + "!"));
        log.debug("Flat mapped: {}", flatMapped.orElse("Empty"));
        
        // FlatMap with method reference
        Optional<String> flatMappedRef = optional.flatMap(OptionalExamples::processString);
        log.debug("Flat mapped with method reference: {}", flatMappedRef.orElse("Empty"));
        
        // Filter operation
        Optional<String> filtered = optional.filter(s -> s.length() > 5);
        log.debug("Filtered: {}", filtered.orElse("Empty"));
        
        // Filter with custom predicate
        Optional<String> customFiltered = optional.filter(s -> s.startsWith("Hello"));
        log.debug("Custom filtered: {}", customFiltered.orElse("Empty"));
        
        // Chaining operations
        Optional<String> chained = optional
                .map(String::toUpperCase)
                .filter(s -> s.length() > 5)
                .flatMap(s -> Optional.of(s + "!"))
                .map(s -> "Result: " + s);
        log.debug("Chained: {}", chained.orElse("Empty"));
    }

    /**
     * Demonstrates Optional chaining and composition
     * 
     * Optional can be chained and composed for complex operations.
     */
    public static void demonstrateOptionalChaining() {
        log.info("=== Demonstrating Optional Chaining ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // Basic chaining
        Optional<String> result1 = optional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s);
        log.debug("Basic chaining: {}", result1.orElse("Empty"));
        
        // Chaining with filter
        Optional<String> result2 = optional
                .filter(s -> s.length() > 5)
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s);
        log.debug("Chaining with filter: {}", result2.orElse("Empty"));
        
        // Chaining with flatMap
        Optional<String> result3 = optional
                .flatMap(s -> Optional.of(s.toUpperCase()))
                .flatMap(s -> Optional.of(s + "!"))
                .flatMap(s -> Optional.of("Result: " + s));
        log.debug("Chaining with flatMap: {}", result3.orElse("Empty"));
        
        // Chaining with orElse
        String result4 = optional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s)
                .orElse("Default value");
        log.debug("Chaining with orElse: {}", result4);
        
        // Chaining with orElseGet
        String result5 = optional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s)
                .orElseGet(() -> "Generated default");
        log.debug("Chaining with orElseGet: {}", result5);
        
        // Chaining with orElseThrow
        try {
            String result6 = optional
                    .map(String::toUpperCase)
                    .map(s -> s + "!")
                    .map(s -> "Result: " + s)
                    .orElseThrow(() -> new RuntimeException("No value present"));
            log.debug("Chaining with orElseThrow: {}", result6);
        } catch (RuntimeException e) {
            log.debug("Exception caught: {}", e.getMessage());
        }
    }

    /**
     * Demonstrates Optional with collections and streams
     * 
     * Optional can be used with collections and streams for functional programming.
     */
    public static void demonstrateOptionalWithCollections() {
        log.info("=== Demonstrating Optional with Collections ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        
        // Find first with Optional
        Optional<String> firstLongName = names.stream()
                .filter(name -> name.length() > 4)
                .findFirst();
        log.debug("First long name: {}", firstLongName.orElse("None"));
        
        // Find any with Optional
        Optional<String> anyLongName = names.stream()
                .filter(name -> name.length() > 4)
                .findAny();
        log.debug("Any long name: {}", anyLongName.orElse("None"));
        
        // Map with Optional
        List<Optional<String>> optionalNames = names.stream()
                .map(name -> name.length() > 4 ? Optional.of(name) : Optional.<String>empty())
                .collect(Collectors.toList());
        log.debug("Optional names: {}", optionalNames);
        
        // Filter present Optionals
        List<String> presentNames = names.stream()
                .map(name -> name.length() > 4 ? Optional.of(name) : Optional.<String>empty())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.debug("Present names: {}", presentNames);
        
        // FlatMap with Optional
        List<String> flatMappedNames = names.stream()
                .map(name -> name.length() > 4 ? Optional.of(name) : Optional.<String>empty())
                .flatMap(optional -> optional.map(Stream::of).orElse(Stream.<String>empty()))
                .collect(Collectors.toList());
        log.debug("Flat mapped names: {}", flatMappedNames);
        
        // Group by Optional presence
        Map<Boolean, List<String>> groupedByPresence = names.stream()
                .collect(Collectors.groupingBy(name -> name.length() > 4));
        log.debug("Grouped by presence: {}", groupedByPresence);
        
        // Partition by Optional presence
        Map<Boolean, List<String>> partitionedByPresence = names.stream()
                .collect(Collectors.partitioningBy(name -> name.length() > 4));
        log.debug("Partitioned by presence: {}", partitionedByPresence);
    }

    /**
     * Demonstrates Optional with exception handling
     * 
     * Optional can handle exceptions gracefully.
     */
    public static void demonstrateOptionalWithExceptionHandling() {
        log.info("=== Demonstrating Optional with Exception Handling ===");
        
        List<String> numbers = Arrays.asList("1", "2", "3", "4", "5", "invalid", "6");
        
        // Parse with Optional
        List<Optional<Integer>> parsedNumbers = numbers.stream()
                .map(OptionalExamples::parseInteger)
                .collect(Collectors.toList());
        log.debug("Parsed numbers: {}", parsedNumbers);
        
        // Filter valid numbers
        List<Integer> validNumbers = numbers.stream()
                .map(OptionalExamples::parseInteger)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.debug("Valid numbers: {}", validNumbers);
        
        // Handle exceptions with Optional
        List<String> processedNumbers = numbers.stream()
                .map(number -> {
                    try {
                        Integer.parseInt(number);
                        return Optional.of("Valid: " + number);
                    } catch (NumberFormatException e) {
                        return Optional.of("Invalid: " + number);
                    }
                })
                .map(Optional::get)
                .collect(Collectors.toList());
        log.debug("Processed numbers: {}", processedNumbers);
        
        // Safe division with Optional
        Optional<Double> result = safeDivide(10.0, 2.0);
        log.debug("Safe division result: {}", result.orElse(0.0));
        
        Optional<Double> errorResult = safeDivide(10.0, 0.0);
        log.debug("Safe division error result: {}", errorResult.orElse(0.0));
    }

    /**
     * Demonstrates Optional with custom operations
     * 
     * Custom operations can be created for specific Optional needs.
     */
    public static void demonstrateCustomOperations() {
        log.info("=== Demonstrating Custom Operations ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // Custom map operation
        Optional<String> customMapped = optional
                .map(s -> s.toUpperCase())
                .map(s -> s + "!")
                .map(s -> "Result: " + s);
        log.debug("Custom mapped: {}", customMapped.orElse("Empty"));
        
        // Custom filter operation
        Optional<String> customFiltered = optional
                .filter(s -> s.length() > 5)
                .filter(s -> s.startsWith("Hello"))
                .filter(s -> s.endsWith("World"));
        log.debug("Custom filtered: {}", customFiltered.orElse("Empty"));
        
        // Custom flatMap operation
        Optional<String> customFlatMapped = optional
                .flatMap(s -> Optional.of(s.toUpperCase()))
                .flatMap(s -> Optional.of(s + "!"))
                .flatMap(s -> Optional.of("Result: " + s));
        log.debug("Custom flat mapped: {}", customFlatMapped.orElse("Empty"));
        
        // Custom operation with orElse
        String customOrElse = optional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s)
                .orElse("Default value");
        log.debug("Custom orElse: {}", customOrElse);
        
        // Custom operation with orElseGet
        String customOrElseGet = optional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s)
                .orElseGet(() -> "Generated default");
        log.debug("Custom orElseGet: {}", customOrElseGet);
    }

    /**
     * Demonstrates Optional with performance considerations
     * 
     * Optional can be optimized for better performance.
     */
    public static void demonstratePerformanceConsiderations() {
        log.info("=== Demonstrating Performance Considerations ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        
        // Performance with map
        long startTime = System.currentTimeMillis();
        List<Optional<String>> mappedNames = names.stream()
                .map(name -> Optional.of(name.toUpperCase()))
                .collect(Collectors.toList());
        long mapTime = System.currentTimeMillis() - startTime;
        log.debug("Map time: {} ms", mapTime);
        
        // Performance with filter
        startTime = System.currentTimeMillis();
        List<String> filteredNames = names.stream()
                .filter(name -> name.length() > 4)
                .collect(Collectors.toList());
        long filterTime = System.currentTimeMillis() - startTime;
        log.debug("Filter time: {} ms", filterTime);
        
        // Performance with flatMap
        startTime = System.currentTimeMillis();
        List<String> flatMappedPerformanceNames = names.stream()
                .map(name -> Optional.of(name.toUpperCase()))
                .flatMap(optional -> optional.map(Stream::of).orElse(Stream.<String>empty()))
                .collect(Collectors.toList());
        long flatMapTime = System.currentTimeMillis() - startTime;
        log.debug("FlatMap time: {} ms", flatMapTime);
        
        // Performance with orElse
        startTime = System.currentTimeMillis();
        String orElseResult = names.stream()
                .filter(name -> name.length() > 10)
                .findFirst()
                .orElse("Default");
        long orElseTime = System.currentTimeMillis() - startTime;
        log.debug("OrElse time: {} ms", orElseTime);
        
        // Performance with orElseGet
        startTime = System.currentTimeMillis();
        String orElseGetResult = names.stream()
                .filter(name -> name.length() > 10)
                .findFirst()
                .orElseGet(() -> "Generated default");
        long orElseGetTime = System.currentTimeMillis() - startTime;
        log.debug("OrElseGet time: {} ms", orElseGetTime);
    }

    /**
     * Demonstrates Optional with null safety
     * 
     * Optional provides null safety for better code reliability.
     */
    public static void demonstrateNullSafety() {
        log.info("=== Demonstrating Null Safety ===");
        
        // Null safety with Optional.of
        try {
            Optional<String> nullOptional = Optional.of(null);
            log.debug("Null Optional: {}", nullOptional);
        } catch (NullPointerException e) {
            log.debug("NullPointerException caught: {}", e.getMessage());
        }
        
        // Null safety with Optional.ofNullable
        Optional<String> nullSafeOptional = Optional.ofNullable(null);
        log.debug("Null safe Optional: {}", nullSafeOptional);
        log.debug("Is empty: {}", nullSafeOptional.isEmpty());
        
        // Null safety with map
        Optional<String> nullSafeMapped = nullSafeOptional
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s);
        log.debug("Null safe mapped: {}", nullSafeMapped.orElse("Empty"));
        
        // Null safety with flatMap
        Optional<String> nullSafeFlatMapped = nullSafeOptional
                .flatMap(s -> Optional.of(s.toUpperCase()))
                .flatMap(s -> Optional.of(s + "!"))
                .flatMap(s -> Optional.of("Result: " + s));
        log.debug("Null safe flat mapped: {}", nullSafeFlatMapped.orElse("Empty"));
        
        // Null safety with filter
        Optional<String> nullSafeFiltered = nullSafeOptional
                .filter(s -> s.length() > 5)
                .filter(s -> s.startsWith("Hello"));
        log.debug("Null safe filtered: {}", nullSafeFiltered.orElse("Empty"));
    }

    /**
     * Demonstrates Optional with default values
     * 
     * Optional can provide default values when no value is present.
     */
    public static void demonstrateDefaultValues() {
        log.info("=== Demonstrating Default Values ===");
        
        Optional<String> empty = Optional.empty();
        Optional<String> present = Optional.of("Hello World");
        
        // orElse with constant
        String default1 = empty.orElse("Default value");
        log.debug("Default with orElse: {}", default1);
        
        String present1 = present.orElse("Default value");
        log.debug("Present with orElse: {}", present1);
        
        // orElseGet with supplier
        String default2 = empty.orElseGet(() -> "Generated default");
        log.debug("Default with orElseGet: {}", default2);
        
        String present2 = present.orElseGet(() -> "Generated default");
        log.debug("Present with orElseGet: {}", present2);
        
        // orElseThrow with exception
        try {
            String default3 = empty.orElseThrow(() -> new RuntimeException("No value present"));
            log.debug("Default with orElseThrow: {}", default3);
        } catch (RuntimeException e) {
            log.debug("Exception caught: {}", e.getMessage());
        }
        
        String present3 = present.orElseThrow(() -> new RuntimeException("No value present"));
        log.debug("Present with orElseThrow: {}", present3);
        
        // orElse with method reference
        String default4 = empty.orElse(getDefaultValue());
        log.debug("Default with method reference: {}", default4);
        
        String present4 = present.orElse(getDefaultValue());
        log.debug("Present with method reference: {}", present4);
    }

    /**
     * Demonstrates Optional with conditional operations
     * 
     * Optional can perform conditional operations based on presence.
     */
    public static void demonstrateConditionalOperations() {
        log.info("=== Demonstrating Conditional Operations ===");
        
        Optional<String> empty = Optional.empty();
        Optional<String> present = Optional.of("Hello World");
        
        // ifPresent
        empty.ifPresent(value -> log.debug("Empty ifPresent: {}", value));
        present.ifPresent(value -> log.debug("Present ifPresent: {}", value));
        
        // ifPresentOrElse
        empty.ifPresentOrElse(
            value -> log.debug("Empty ifPresentOrElse: {}", value),
            () -> log.debug("Empty ifPresentOrElse: No value")
        );
        present.ifPresentOrElse(
            value -> log.debug("Present ifPresentOrElse: {}", value),
            () -> log.debug("Present ifPresentOrElse: No value")
        );
        
        // Custom conditional operation
        String result1 = empty.map(String::toUpperCase).orElse("DEFAULT");
        log.debug("Custom conditional empty: {}", result1);
        
        String result2 = present.map(String::toUpperCase).orElse("DEFAULT");
        log.debug("Custom conditional present: {}", result2);
        
        // Conditional with filter
        Optional<String> filtered1 = empty.filter(s -> s.length() > 5);
        log.debug("Filtered empty: {}", filtered1.orElse("Empty"));
        
        Optional<String> filtered2 = present.filter(s -> s.length() > 5);
        log.debug("Filtered present: {}", filtered2.orElse("Empty"));
        
        // Conditional with flatMap
        Optional<String> flatMapped1 = empty.flatMap(s -> Optional.of(s.toUpperCase()));
        log.debug("Flat mapped empty: {}", flatMapped1.orElse("Empty"));
        
        Optional<String> flatMapped2 = present.flatMap(s -> Optional.of(s.toUpperCase()));
        log.debug("Flat mapped present: {}", flatMapped2.orElse("Empty"));
    }

    /**
     * Demonstrates Optional with transformation
     * 
     * Optional can transform values in various ways.
     */
    public static void demonstrateTransformation() {
        log.info("=== Demonstrating Transformation ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // String transformation
        Optional<String> upperCase = optional.map(String::toUpperCase);
        log.debug("Upper case: {}", upperCase.orElse("Empty"));
        
        Optional<String> lowerCase = optional.map(String::toLowerCase);
        log.debug("Lower case: {}", lowerCase.orElse("Empty"));
        
        Optional<String> trimmed = optional.map(String::trim);
        log.debug("Trimmed: {}", trimmed.orElse("Empty"));
        
        // Length transformation
        Optional<Integer> length = optional.map(String::length);
        log.debug("Length: {}", length.orElse(0));
        
        // Custom transformation
        Optional<String> custom = optional.map(s -> s + "!")
                .map(s -> "Result: " + s)
                .map(s -> s.toUpperCase());
        log.debug("Custom: {}", custom.orElse("Empty"));
        
        // Transformation with flatMap
        Optional<String> flatMapped = optional.flatMap(s -> Optional.of(s.toUpperCase()))
                .flatMap(s -> Optional.of(s + "!"))
                .flatMap(s -> Optional.of("Result: " + s));
        log.debug("Flat mapped: {}", flatMapped.orElse("Empty"));
        
        // Transformation with filter
        Optional<String> filtered = optional.filter(s -> s.length() > 5)
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .map(s -> "Result: " + s);
        log.debug("Filtered: {}", filtered.orElse("Empty"));
    }

    /**
     * Demonstrates Optional with validation
     * 
     * Optional can validate values and provide error handling.
     */
    public static void demonstrateValidation() {
        log.info("=== Demonstrating Validation ===");
        
        Optional<String> valid = Optional.of("Hello World");
        Optional<String> invalid = Optional.of("Hi");
        
        // Validation with filter
        Optional<String> validated1 = valid.filter(s -> s.length() > 5);
        log.debug("Validated valid: {}", validated1.orElse("Invalid"));
        
        Optional<String> validated2 = invalid.filter(s -> s.length() > 5);
        log.debug("Validated invalid: {}", validated2.orElse("Invalid"));
        
        // Validation with custom predicate
        Optional<String> customValidated1 = valid.filter(s -> s.startsWith("Hello"));
        log.debug("Custom validated valid: {}", customValidated1.orElse("Invalid"));
        
        Optional<String> customValidated2 = invalid.filter(s -> s.startsWith("Hello"));
        log.debug("Custom validated invalid: {}", customValidated2.orElse("Invalid"));
        
        // Validation with multiple conditions
        Optional<String> multiValidated1 = valid.filter(s -> s.length() > 5)
                .filter(s -> s.startsWith("Hello"))
                .filter(s -> s.endsWith("World"));
        log.debug("Multi validated valid: {}", multiValidated1.orElse("Invalid"));
        
        Optional<String> multiValidated2 = invalid.filter(s -> s.length() > 5)
                .filter(s -> s.startsWith("Hello"))
                .filter(s -> s.endsWith("World"));
        log.debug("Multi validated invalid: {}", multiValidated2.orElse("Invalid"));
        
        // Validation with error message
        String result1 = valid.filter(s -> s.length() > 5)
                .map(s -> "Valid: " + s)
                .orElse("Invalid: Too short");
        log.debug("Validation with error: {}", result1);
        
        String result2 = invalid.filter(s -> s.length() > 5)
                .map(s -> "Valid: " + s)
                .orElse("Invalid: Too short");
        log.debug("Validation with error: {}", result2);
    }

    /**
     * Demonstrates Optional with error handling
     * 
     * Optional can handle errors gracefully.
     */
    public static void demonstrateErrorHandling() {
        log.info("=== Demonstrating Error Handling ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // Error handling with try-catch
        Optional<String> errorHandled = optional.map(s -> {
            try {
                return s.toUpperCase();
            } catch (Exception e) {
                log.warn("Error occurred: {}", e.getMessage());
                return "Error";
            }
        });
        log.debug("Error handled: {}", errorHandled.orElse("Empty"));
        
        // Error handling with orElse
        String result1 = optional.map(s -> {
            try {
                return s.toUpperCase();
            } catch (Exception e) {
                return null;
            }
        }).orElse("Default");
        log.debug("Error handled with orElse: {}", result1);
        
        // Error handling with orElseGet
        String result2 = optional.map(s -> {
            try {
                return s.toUpperCase();
            } catch (Exception e) {
                return null;
            }
        }).orElseGet(() -> "Generated default");
        log.debug("Error handled with orElseGet: {}", result2);
        
        // Error handling with orElseThrow
        try {
            String result3 = optional.map(s -> {
                try {
                    return s.toUpperCase();
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred", e);
                }
            }).orElseThrow(() -> new RuntimeException("No value present"));
            log.debug("Error handled with orElseThrow: {}", result3);
        } catch (RuntimeException e) {
            log.debug("Exception caught: {}", e.getMessage());
        }
    }

    /**
     * Demonstrates Optional with logging and monitoring
     * 
     * Optional can be used with logging and monitoring for better observability.
     */
    public static void demonstrateLoggingAndMonitoring() {
        log.info("=== Demonstrating Logging and Monitoring ===");
        
        Optional<String> optional = Optional.of("Hello World");
        
        // Logging with ifPresent
        optional.ifPresent(value -> log.debug("Processing value: {}", value));
        
        // Logging with ifPresentOrElse
        optional.ifPresentOrElse(
            value -> log.debug("Value present: {}", value),
            () -> log.debug("No value present")
        );
        
        // Logging with map
        Optional<String> logged = optional.map(value -> {
            log.debug("Mapping value: {}", value);
            return value.toUpperCase();
        });
        log.debug("Mapped result: {}", logged.orElse("Empty"));
        
        // Logging with filter
        Optional<String> filtered = optional.filter(value -> {
            boolean matches = value.length() > 5;
            log.debug("Filtering value: {}, matches: {}", value, matches);
            return matches;
        });
        log.debug("Filtered result: {}", filtered.orElse("Empty"));
        
        // Logging with flatMap
        Optional<String> flatMapped = optional.flatMap(value -> {
            log.debug("Flat mapping value: {}", value);
            return Optional.of(value.toUpperCase());
        });
        log.debug("Flat mapped result: {}", flatMapped.orElse("Empty"));
        
        // Logging with orElse
        String result = optional.map(String::toUpperCase)
                .orElse("Default");
        log.debug("Final result: {}", result);
    }

    /**
     * Demonstrates Optional with testing
     * 
     * Optional can be used effectively in testing scenarios.
     */
    public static void demonstrateTesting() {
        log.info("=== Demonstrating Testing ===");
        
        // Test with empty Optional
        Optional<String> empty = Optional.empty();
        assert empty.isEmpty();
        assert !empty.isPresent();
        assert empty.orElse("Default").equals("Default");
        
        // Test with present Optional
        Optional<String> present = Optional.of("Hello");
        assert !present.isEmpty();
        assert present.isPresent();
        assert present.get().equals("Hello");
        assert present.orElse("Default").equals("Hello");
        
        // Test with map
        Optional<String> mapped = present.map(String::toUpperCase);
        assert mapped.isPresent();
        assert mapped.get().equals("HELLO");
        
        // Test with filter
        Optional<String> filtered = present.filter(s -> s.length() > 3);
        assert filtered.isPresent();
        assert filtered.get().equals("Hello");
        
        Optional<String> filteredEmpty = present.filter(s -> s.length() > 10);
        assert filteredEmpty.isEmpty();
        
        // Test with flatMap
        Optional<String> flatMapped = present.flatMap(s -> Optional.of(s + "!"));
        assert flatMapped.isPresent();
        assert flatMapped.get().equals("Hello!");
        
        // Test with orElseGet
        String result = empty.orElseGet(() -> "Generated");
        assert result.equals("Generated");
        
        // Test with orElseThrow
        try {
            empty.orElseThrow(() -> new RuntimeException("No value"));
            assert false; // Should not reach here
        } catch (RuntimeException e) {
            assert e.getMessage().equals("No value");
        }
        
        log.debug("All tests passed!");
    }

    // Helper methods

    private static String getValue() {
        return "Hello from supplier";
    }

    private static Optional<String> processString(String s) {
        return Optional.of(s.toUpperCase());
    }

    private static Optional<Integer> parseInteger(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Double> safeDivide(double a, double b) {
        if (b == 0) {
            return Optional.empty();
        }
        return Optional.of(a / b);
    }

    private static String getDefaultValue() {
        return "Default value from method";
    }
}
