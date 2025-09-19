package com.algorithmpractice.solid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Netflix Production-Grade Comprehensive Test Suite
 *
 * <p>This test suite validates all Netflix production-grade utilities implemented
 * for SDE-2 Senior Backend Engineers. It covers type inference, exception handling,
 * BigDecimal operations, user input validation, file operations, and null safety.</p>
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>✅ Date/Time handling with type inference</li>
 *   <li>✅ Exception handling with recovery patterns</li>
 *   <li>✅ BigDecimal financial calculations</li>
 *   <li>✅ User input validation and sanitization</li>
 *   <li>✅ File operations with resource management</li>
 *   <li>✅ Null safety with Optional patterns</li>
 *   <li>✅ Cross-language developer compatibility</li>
 *   <li>✅ Performance and memory efficiency</li>
 * </ul>
 *
 * @author Netflix Backend Engineering Team
 * @version 2.0.0
 * @since 2024
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.algorithmpractice.solid=DEBUG",
    "netflix.production.mode=true",
    "netflix.test.environment=true"
})
class NetflixProductionGradeTestSuite {

    // ========== DATE/TIME HANDLING TESTS ==========

    @Nested
    @DisplayName("Netflix Date/Time Handling Tests")
    class DateTimeHandlingTests {

        @Test
        @DisplayName("Test type inference with date operations")
        void testTypeInferenceWithDates() {
            var dateUtils = new NetflixDateTimeUtils();
            var result = dateUtils.demonstrateTypeInferenceWithDates();

            assertNotNull(result, "Date inference result should not be null");
            assertTrue(result.containsKey("currentDateTime"), "Should contain current date time");
            assertTrue(result.containsKey("currentDate"), "Should contain current date");
            assertTrue(result.containsKey("currentTime"), "Should contain current time");
            assertTrue(result.containsKey("dateList"), "Should contain date list");
            assertTrue(result.containsKey("dateMap"), "Should contain date map");

            // Validate wrapper classes
            var activeCount = result.get("isTotalPositive");
            assertTrue(activeCount instanceof Boolean, "Should be Boolean wrapper");
        }

        @Test
        @DisplayName("Test date parsing and formatting")
        void testDateParsingAndFormatting() {
            var dateUtils = new NetflixDateTimeUtils();
            var dateStrings = Arrays.asList(
                "2024-01-15 10:30:00",
                "01/15/2024 10:30",
                "2024-01-15T10:30:00"
            );

            var result = dateUtils.demonstrateDateParsingAndFormatting(dateStrings);

            assertNotNull(result, "Parsing result should not be null");
            assertTrue(result.containsKey("parsedDates"), "Should contain parsed dates");
            assertTrue(result.containsKey("formattedResults"), "Should contain formatted results");
            assertTrue(result.containsKey("successCount"), "Should contain success count");

            // Validate type inference with wrapper classes
            var successCount = result.get("successCount");
            assertTrue(successCount instanceof Integer, "Success count should be Integer wrapper");
        }

        @Test
        @DisplayName("Test timezone conversions")
        void testTimezoneConversions() {
            var dateUtils = new NetflixDateTimeUtils();
            var baseDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            var targetTimezones = Arrays.asList("UTC", "America/New_York", "Europe/London", "Asia/Tokyo");

            var result = dateUtils.demonstrateTimezoneConversions(baseDateTime, targetTimezones);

            assertNotNull(result, "Timezone result should not be null");
            assertTrue(result.containsKey("timezoneConversions"), "Should contain timezone conversions");
            assertTrue(result.containsKey("successfulConversions"), "Should contain successful conversions");

            // Validate wrapper classes
            var successfulConversions = result.get("successfulConversions");
            assertTrue(successfulConversions instanceof Integer, "Should be Integer wrapper");
        }
    }

    // ========== EXCEPTION HANDLING TESTS ==========

    @Nested
    @DisplayName("Netflix Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Test type inference with exception patterns")
        void testTypeInferenceWithExceptions() {
            var exceptionHandler = new NetflixExceptionHandler();
            var result = exceptionHandler.demonstrateTypeInferenceWithExceptions();

            assertNotNull(result, "Exception result should not be null");
            assertTrue(result.containsKey("exceptionResults"), "Should contain exception results");
            assertTrue(result.containsKey("successCount"), "Should contain success count");
            assertTrue(result.containsKey("failureCount"), "Should contain failure count");

            // Validate wrapper classes
            var successCount = result.get("successCount");
            assertTrue(successCount instanceof Integer, "Should be Integer wrapper");
        }

        @Test
        @DisplayName("Test retry and recovery patterns")
        void testRetryAndRecoveryPatterns() {
            var exceptionHandler = new NetflixExceptionHandler();
            var operations = Arrays.asList("stable_operation", "unstable_operation", "failing_operation");

            var result = exceptionHandler.demonstrateRetryAndRecovery(operations);

            assertNotNull(result, "Retry result should not be null");
            assertTrue(result.containsKey("retryResults"), "Should contain retry results");
            assertTrue(result.containsKey("totalAttempts"), "Should contain total attempts");
            assertTrue(result.containsKey("successRate"), "Should contain success rate");

            // Validate wrapper classes
            var totalAttempts = result.get("totalAttempts");
            assertTrue(totalAttempts instanceof Integer, "Should be Integer wrapper");
        }

        @Test
        @DisplayName("Test circuit breaker pattern")
        void testCircuitBreakerPattern() {
            var exceptionHandler = new NetflixExceptionHandler();
            var serviceName = "test_service";

            var result = exceptionHandler.demonstrateCircuitBreaker(serviceName);

            assertNotNull(result, "Circuit breaker result should not be null");
            assertTrue(result.containsKey("circuitBreakerState"), "Should contain circuit breaker state");
            assertTrue(result.containsKey("canExecuteOperation"), "Should contain execution flag");

            // Validate wrapper classes
            var canExecute = result.get("canExecuteOperation");
            assertTrue(canExecute instanceof Boolean, "Should be Boolean wrapper");
        }
    }

    // ========== BIGDECIMAL FINANCIAL CALCULATIONS TESTS ==========

    @Nested
    @DisplayName("Netflix BigDecimal Financial Tests")
    class BigDecimalFinancialTests {

        @Test
        @DisplayName("Test type inference with BigDecimal operations")
        void testTypeInferenceWithBigDecimal() {
            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var result = bigDecimalUtils.demonstrateTypeInferenceWithBigDecimal();

            assertNotNull(result, "BigDecimal result should not be null");
            assertTrue(result.containsKey("subtotal"), "Should contain subtotal");
            assertTrue(result.containsKey("taxAmount"), "Should contain tax amount");
            assertTrue(result.containsKey("total"), "Should contain total");

            // Validate BigDecimal precision
            var subtotal = result.get("subtotal");
            assertTrue(subtotal instanceof BigDecimal, "Should be BigDecimal");
            assertEquals(2, ((BigDecimal) subtotal).scale(), "Should have correct scale");
        }

        @Test
        @DisplayName("Test financial calculations")
        void testFinancialCalculations() {
            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var financialData = Map.<String, Object>of(
                "principal", BigDecimal.valueOf(100000),
                "interestRate", BigDecimal.valueOf(5.5),
                "loanTermYears", BigDecimal.valueOf(30),
                "paymentFrequency", BigDecimal.valueOf(12)
            );

            var result = bigDecimalUtils.demonstrateFinancialCalculations(financialData);

            assertNotNull(result, "Financial result should not be null");
            assertTrue(result.containsKey("monthlyPayment"), "Should contain monthly payment");
            assertTrue(result.containsKey("totalPayments"), "Should contain total payments");
            assertTrue(result.containsKey("totalInterest"), "Should contain total interest");

            // Validate wrapper classes
            var isValid = result.get("isValidCalculation");
            assertTrue(isValid instanceof Boolean, "Should be Boolean wrapper");
        }

        @Test
        @DisplayName("Test precision and rounding")
        void testPrecisionAndRounding() {
            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var decimalValues = Arrays.asList(
                BigDecimal.valueOf(10.123456789),
                BigDecimal.valueOf(20.987654321),
                BigDecimal.valueOf(30.555555555)
            );

            var result = bigDecimalUtils.demonstratePrecisionAndRounding(decimalValues);

            assertNotNull(result, "Precision result should not be null");
            assertTrue(result.containsKey("precisionComparison"), "Should contain precision comparison");
            assertTrue(result.containsKey("totalValues"), "Should contain total values");

            // Validate wrapper classes
            var totalValues = result.get("totalValues");
            assertTrue(totalValues instanceof Integer, "Should be Integer wrapper");
        }
    }

    // ========== USER INPUT VALIDATION TESTS ==========

    @Nested
    @DisplayName("Netflix User Input Validation Tests")
    class UserInputValidationTests {

        @Test
        @DisplayName("Test input validation and sanitization")
        void testInputValidationAndSanitization() {
            var inputHandler = new NetflixUserInputHandler();
            var rawInputs = Map.of(
                "name", "John Doe<script>alert('xss')</script>",
                "email", "john.doe@example.com",
                "password", "ValidPass123!",
                "description", "Normal description"
            );

            var result = inputHandler.demonstrateInputSanitization(rawInputs);

            assertNotNull(result, "Sanitization result should not be null");
            assertTrue(result.containsKey("sanitizationResults"), "Should contain sanitization results");
            assertTrue(result.containsKey("securityMetrics"), "Should contain security metrics");

            // Validate security metrics
            var securityMetrics = (Map<String, Integer>) result.get("securityMetrics");
            assertTrue(securityMetrics.containsKey("safeInputs"), "Should contain safe inputs count");
        }

        @Test
        @DisplayName("Test type conversion and validation")
        void testTypeConversionAndValidation() {
            var inputHandler = new NetflixUserInputHandler();
            var inputValues = Map.<String, Object>of(
                "userId", "123",
                "age", 25,
                "balance", BigDecimal.valueOf(1000.50),
                "isActive", true,
                "invalidField", null
            );

            var result = inputHandler.demonstrateTypeConversion(inputValues);

            assertNotNull(result, "Conversion result should not be null");
            assertTrue(result.containsKey("conversionResults"), "Should contain conversion results");
            assertTrue(result.containsKey("successfulConversions"), "Should contain successful conversions");

            // Validate wrapper classes
            var successfulConversions = result.get("successfulConversions");
            assertTrue(successfulConversions instanceof Integer, "Should be Integer wrapper");
        }

        @Test
        @DisplayName("Test variable scoping with input processing")
        void testVariableScopingWithInput() {
            var inputHandler = new NetflixUserInputHandler();
            var result = inputHandler.demonstrateVariableScopingWithInput("test_processing");

            assertNotNull(result, "Scoping result should not be null");
            assertTrue(result.containsKey("processingResults"), "Should contain processing results");
            assertTrue(result.containsKey("inputCount"), "Should contain input count");

            // Validate wrapper classes
            var inputCount = result.get("inputCount");
            assertTrue(inputCount instanceof Integer, "Should be Integer wrapper");
        }
    }

    // ========== FILE OPERATIONS TESTS ==========

    @Nested
    @DisplayName("Netflix File Operations Tests")
    class FileOperationsTests {

        @Test
        @DisplayName("Test type inference with file operations")
        void testTypeInferenceWithFileOperations() {
            var fileOps = new NetflixFileOperations();
            var testPath = Paths.get(System.getProperty("java.io.tmpdir"), "netflix_test");

            try {
                // Create test directory
                java.nio.file.Files.createDirectories(testPath);

                var result = fileOps.demonstrateTypeInferenceWithFileOperations(testPath);

                assertNotNull(result, "File operations result should not be null");
                assertTrue(result.containsKey("fileSystemInfo"), "Should contain file system info");
                assertTrue(result.containsKey("operationResults"), "Should contain operation results");

                // Validate wrapper classes
                var totalOperations = result.get("totalOperations");
                assertTrue(totalOperations instanceof Integer, "Should be Integer wrapper");

            } finally {
                // Cleanup
                try {
                    java.nio.file.Files.deleteIfExists(testPath);
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }

        @Test
        @DisplayName("Test streaming file operations")
        void testStreamingFileOperations() {
            var fileOps = new NetflixFileOperations();
            var testPath = Paths.get(System.getProperty("java.io.tmpdir"), "netflix_test");

            try {
                // Create test directory
                java.nio.file.Files.createDirectories(testPath);
                var sourceFile = testPath.resolve("source.txt");
                var destFile = testPath.resolve("destination.txt");

                // Create test file
                java.nio.file.Files.writeString(sourceFile, "Test content for streaming operations");

                var result = fileOps.demonstrateStreamingFileOperations(sourceFile, destFile);

                assertNotNull(result, "Streaming result should not be null");
                assertTrue(result.containsKey("streamingMetrics"), "Should contain streaming metrics");
                assertTrue(result.containsKey("performanceAnalysis"), "Should contain performance analysis");

                // Validate wrapper classes
                var performanceAnalysis = (Map<String, Object>) result.get("performanceAnalysis");
                var success = performanceAnalysis.get("overallSuccess");
                assertTrue(success instanceof Boolean, "Should be Boolean wrapper");

            } finally {
                // Cleanup
                try {
                    java.nio.file.Files.deleteIfExists(testPath.resolve("source.txt"));
                    java.nio.file.Files.deleteIfExists(testPath.resolve("destination.txt"));
                    java.nio.file.Files.deleteIfExists(testPath);
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }

        @Test
        @DisplayName("Test batch file operations")
        void testBatchFileOperations() {
            var fileOps = new NetflixFileOperations();
            var testPath = Paths.get(System.getProperty("java.io.tmpdir"), "netflix_batch_test");

            try {
                // Create test directory
                java.nio.file.Files.createDirectories(testPath);

                var result = fileOps.demonstrateBatchFileOperations(testPath, "validate");

                assertNotNull(result, "Batch result should not be null");
                assertTrue(result.containsKey("batchResults"), "Should contain batch results");
                assertTrue(result.containsKey("batchMetrics"), "Should contain batch metrics");

                // Validate wrapper classes
                var totalFiles = result.get("totalFiles");
                assertTrue(totalFiles instanceof Integer, "Should be Integer wrapper");

            } finally {
                // Cleanup
                try {
                    java.nio.file.Files.deleteIfExists(testPath);
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    // ========== NULL SAFETY TESTS ==========

    @Nested
    @DisplayName("Netflix Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("Test type inference with Optional patterns")
        void testTypeInferenceWithOptional() {
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var result = nullSafetyUtils.demonstrateTypeInferenceWithOptional();

            assertNotNull(result, "Optional result should not be null");
            assertTrue(result.containsKey("optionalString"), "Should contain optional string");
            assertTrue(result.containsKey("optionalInteger"), "Should contain optional integer");
            assertTrue(result.containsKey("optionalList"), "Should contain optional list");
            assertTrue(result.containsKey("optionalMap"), "Should contain optional map");

            // Validate wrapper classes
            var operationCount = result.get("operationCount");
            assertTrue(operationCount instanceof Integer, "Should be Integer wrapper");
        }

        @Test
        @DisplayName("Test null-safe method chaining")
        void testNullSafeMethodChaining() {
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var userData = Map.<String, Object>of(
                "user", Map.of(
                    "name", "John Doe",
                    "account", Map.of(
                        "balance", BigDecimal.valueOf(1000.00)
                    )
                )
            );

            var result = nullSafetyUtils.demonstrateNullSafeMethodChaining(userData);

            assertNotNull(result, "Chaining result should not be null");
            assertTrue(result.containsKey("userName"), "Should contain user name");
            assertTrue(result.containsKey("userBalance"), "Should contain user balance");

            // Validate wrapper classes
            var chainingDepth = result.get("chainingDepth");
            assertTrue(chainingDepth instanceof Integer, "Should be Integer wrapper");
        }

        @Test
        @DisplayName("Test Optional with collections")
        void testOptionalWithCollections() {
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var dataCollections = Map.<String, Object>of(
                "users", Arrays.asList(
                    Map.of("name", "Alice", "active", true),
                    Map.of("name", "Bob", "active", false)
                ),
                "userMap", Map.of(
                    "user1", Map.of("name", "Charlie", "email", "charlie@test.com")
                )
            );

            var result = nullSafetyUtils.demonstrateOptionalWithCollections(dataCollections);

            assertNotNull(result, "Collections result should not be null");
            assertTrue(result.containsKey("userList"), "Should contain user list");
            assertTrue(result.containsKey("userMap"), "Should contain user map");
            assertTrue(result.containsKey("userCount"), "Should contain user count");

            // Validate wrapper classes
            var userCount = result.get("userCount");
            assertTrue(userCount instanceof Integer, "Should be Integer wrapper");
        }
    }

    // ========== CROSS-LANGUAGE COMPATIBILITY TESTS ==========

    @Nested
    @DisplayName("Cross-Language Compatibility Tests")
    class CrossLanguageCompatibilityTests {

        @Test
        @DisplayName("Test TypeScript/Node.js developer patterns")
        void testTypeScriptNodeJsPatterns() {
            // Test date handling similar to JavaScript Date
            var dateUtils = new NetflixDateTimeUtils();
            var dateResult = dateUtils.demonstrateTypeInferenceWithDates();

            assertNotNull(dateResult, "Date result should not be null");
            assertTrue(dateResult.containsKey("currentDateTime"), "Should have current date time like JS Date.now()");

            // Test BigDecimal handling similar to JavaScript Number
            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var bigDecimalResult = bigDecimalUtils.demonstrateTypeInferenceWithBigDecimal();

            assertNotNull(bigDecimalResult, "BigDecimal result should not be null");
            assertTrue(bigDecimalResult.containsKey("total"), "Should have total like JS Number operations");

            // Test null safety similar to TypeScript strict null checks
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var nullResult = nullSafetyUtils.demonstrateTypeInferenceWithOptional();

            assertNotNull(nullResult, "Null safety result should not be null");
            assertTrue(nullResult.containsKey("hasStringValue"), "Should have null checks like TypeScript");
        }

        @Test
        @DisplayName("Test functional programming patterns")
        void testFunctionalProgrammingPatterns() {
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var inputData = Map.<String, Object>of(
                "name", "Functional Test",
                "value", 42,
                "items", Arrays.asList("item1", "item2", "item3")
            );

            var result = nullSafetyUtils.demonstrateOptionalWithFunctionalProgramming(inputData);

            assertNotNull(result, "Functional result should not be null");
            assertTrue(result.containsKey("processedData"), "Should contain processed data");
            assertTrue(result.containsKey("processedList"), "Should contain processed list");

            // Validate functional composition
            var hasProcessedData = result.get("hasProcessedData");
            assertTrue(hasProcessedData instanceof Boolean, "Should be Boolean wrapper");
        }
    }

    // ========== PERFORMANCE AND MEMORY TESTS ==========

    @Nested
    @DisplayName("Performance and Memory Tests")
    class PerformanceAndMemoryTests {

        @Test
        @DisplayName("Test performance characteristics of all utilities")
        void testPerformanceCharacteristics() {
            var startTime = System.currentTimeMillis();

            // Test all utilities performance
            var dateUtils = new NetflixDateTimeUtils();
            var dateResult = dateUtils.demonstrateTypeInferenceWithDates();

            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var bigDecimalResult = bigDecimalUtils.demonstrateTypeInferenceWithBigDecimal();

            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var nullResult = nullSafetyUtils.demonstrateTypeInferenceWithOptional();

            var endTime = System.currentTimeMillis();
            var totalTime = endTime - startTime;

            // Performance should be reasonable (less than 5 seconds for all operations)
            assertTrue(totalTime < 5000, "Performance should be reasonable: " + totalTime + "ms");

            // All results should be non-null
            assertNotNull(dateResult, "Date result should not be null");
            assertNotNull(bigDecimalResult, "BigDecimal result should not be null");
            assertNotNull(nullResult, "Null safety result should not be null");
        }

        @Test
        @DisplayName("Test memory efficiency with wrapper classes")
        void testMemoryEfficiency() {
            var memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Create many wrapper objects
            var wrapperList = new ArrayList<Integer>();
            for (var i = 0; i < 10000; i++) {
                wrapperList.add(Integer.valueOf(i));
            }

            // Run various operations that create wrapper objects
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var result = nullSafetyUtils.demonstrateTypeInferenceWithOptional();

            var memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            var memoryUsed = memoryAfter - memoryBefore;

            // Memory usage should be reasonable (less than 50MB for operations)
            assertTrue(memoryUsed < 50 * 1024 * 1024, "Memory usage should be reasonable: " + memoryUsed + " bytes");

            // Result should still be valid
            assertNotNull(result, "Result should not be null after memory operations");
        }

        @Test
        @DisplayName("Test thread safety of utilities")
        void testThreadSafety() {
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var results = new ArrayList<Map<String, Object>>();

            // Run operations in parallel
            var threads = new ArrayList<Thread>();
            for (var i = 0; i < 10; i++) {
                var thread = new Thread(() -> {
                    var result = nullSafetyUtils.demonstrateTypeInferenceWithOptional();
                    synchronized (results) {
                        results.add(result);
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to complete
            for (var thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // All threads should have completed successfully
            assertEquals(10, results.size(), "All threads should complete");
            for (var result : results) {
                assertNotNull(result, "Each thread result should not be null");
                assertTrue(result.containsKey("optionalString"), "Each result should contain optional string");
            }
        }
    }

    // ========== INTEGRATION TESTS ==========

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Test complete workflow integration")
        void testCompleteWorkflowIntegration() {
            // Test a complete workflow that uses multiple utilities

            // 1. Create user input data
            var inputHandler = new NetflixUserInputHandler();
            var rawInputs = Map.of(
                "name", "Integration Test User",
                "email", "integration@test.com",
                "balance", "1000.50"
            );

            // 2. Validate and sanitize input
            var validationResult = inputHandler.demonstrateInputSanitization(rawInputs);
            assertNotNull(validationResult, "Validation should succeed");

            // 3. Convert types safely
            var inputValues = Map.<String, Object>of(
                "name", "Integration Test User",
                "email", "integration@test.com",
                "balance", "1000.50",
                "createdDate", LocalDateTime.now().toString()
            );

            var conversionResult = inputHandler.demonstrateTypeConversion(inputValues);
            assertNotNull(conversionResult, "Conversion should succeed");

            // 4. Perform financial calculations
            var bigDecimalUtils = new NetflixBigDecimalUtils();
            var financialData = Map.<String, Object>of(
                "principal", BigDecimal.valueOf(1000.50),
                "interestRate", BigDecimal.valueOf(5.0),
                "loanTermYears", BigDecimal.valueOf(1),
                "paymentFrequency", BigDecimal.valueOf(12)
            );

            var financialResult = bigDecimalUtils.demonstrateFinancialCalculations(financialData);
            assertNotNull(financialResult, "Financial calculation should succeed");

            // 5. Test null safety throughout
            var nullSafetyUtils = new NetflixNullSafetyUtils();
            var nullSafetyResult = nullSafetyUtils.demonstrateTypeInferenceWithOptional();
            assertNotNull(nullSafetyResult, "Null safety should work");

            // 6. Test date/time operations
            var dateUtils = new NetflixDateTimeUtils();
            var dateResult = dateUtils.demonstrateTypeInferenceWithDates();
            assertNotNull(dateResult, "Date operations should succeed");

            // All operations should complete successfully
            assertTrue(validationResult.containsKey("sanitizationResults"), "Should have validation results");
            assertTrue(conversionResult.containsKey("conversionResults"), "Should have conversion results");
            assertTrue(financialResult.containsKey("monthlyPayment"), "Should have financial results");
            assertTrue(nullSafetyResult.containsKey("optionalString"), "Should have null safety results");
            assertTrue(dateResult.containsKey("currentDateTime"), "Should have date results");
        }
    }
}
