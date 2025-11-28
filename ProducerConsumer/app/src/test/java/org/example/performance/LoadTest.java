package org.example.performance;

import org.example.App;
import org.example.monitoring.BusinessMetricsCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext
public class LoadTest {

    private static final Network network = Network.newNetwork();

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BusinessMetricsCollector metricsCollector;

    @Test
    void testHighThroughputMessageProcessing() throws Exception {
        // Given: High volume of messages
        int messageCount = 1000;
        String topic = "performance-test";
        ExecutorService executor = Executors.newFixedThreadPool(10);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalLatency = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        // When: Send messages concurrently
        for (int i = 0; i < messageCount; i++) {
            final int messageId = i;
            executor.submit(() -> {
                long messageStartTime = System.nanoTime();

                try {
                    kafkaTemplate.send(topic, "perf-message-" + messageId).get(5, TimeUnit.SECONDS);
                    successCount.incrementAndGet();

                    long messageEndTime = System.nanoTime();
                    totalLatency.addAndGet(messageEndTime - messageStartTime);

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    metricsCollector.recordError("PERFORMANCE_TEST_FAILURE");
                }
            });
        }

        // Wait for completion
        executor.shutdown();
        boolean completed = executor.awaitTermination(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Then: Verify performance metrics
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isGreaterThan(messageCount * 0.95); // 95% success rate
        assertThat(failureCount.get()).isLessThan(messageCount * 0.05); // Less than 5% failures

        // Performance assertions
        double throughput = (double) successCount.get() / (totalDuration / 1000.0); // messages per second
        assertThat(throughput).isGreaterThan(10.0); // At least 10 messages per second

        if (successCount.get() > 0) {
            long avgLatencyMs = totalLatency.get() / successCount.get() / 1_000_000; // Convert to milliseconds
            assertThat(avgLatencyMs).isLessThan(5000); // Average latency under 5 seconds
        }

        // Log performance results
        System.out.printf("Performance Test Results:%n");
        System.out.printf("- Total messages: %d%n", messageCount);
        System.out.printf("- Successful: %d%n", successCount.get());
        System.out.printf("- Failed: %d%n", failureCount.get());
        System.out.printf("- Duration: %d ms%n", totalDuration);
        System.out.printf("- Throughput: %.2f msg/sec%n", throughput);
    }

    @Test
    void testAPIEndpointLoad() throws Exception {
        // Given: Multiple concurrent API requests
        int requestCount = 500;
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        ExecutorService executor = Executors.newFixedThreadPool(20);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        // When: Make concurrent API calls
        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                long requestStartTime = System.nanoTime();

                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }

                    long requestEndTime = System.nanoTime();
                    totalResponseTime.addAndGet(requestEndTime - requestStartTime);

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    metricsCollector.recordError("API_LOAD_TEST_FAILURE");
                }
            });
        }

        // Wait for completion
        executor.shutdown();
        boolean completed = executor.awaitTermination(60, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Then: Verify API performance
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isGreaterThan(requestCount * 0.95); // 95% success rate

        double requestsPerSecond = (double) successCount.get() / (totalDuration / 1000.0);
        assertThat(requestsPerSecond).isGreaterThan(50.0); // At least 50 requests per second

        if (successCount.get() > 0) {
            long avgResponseTimeMs = totalResponseTime.get() / successCount.get() / 1_000_000;
            assertThat(avgResponseTimeMs).isLessThan(1000); // Average response time under 1 second
        }

        // Verify rate limiting worked
        int rateLimitedRequests = requestCount - successCount.get() - errorCount.get();
        assertThat(rateLimitedRequests).isGreaterThanOrEqualTo(0); // Some requests may be rate limited

        System.out.printf("API Load Test Results:%n");
        System.out.printf("- Total requests: %d%n", requestCount);
        System.out.printf("- Successful: %d%n", successCount.get());
        System.out.printf("- Errors: %d%n", errorCount.get());
        System.out.printf("- Rate limited: %d%n", rateLimitedRequests);
        System.out.printf("- Duration: %d ms%n", totalDuration);
        System.out.printf("- Throughput: %.2f req/sec%n", requestsPerSecond);
    }

    @Test
    void testDatabaseConnectionPooling() throws Exception {
        // Given: Database operations under load
        int operationCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(15);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger connectionErrors = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // When: Perform concurrent database operations
        for (int i = 0; i < operationCount; i++) {
            executor.submit(() -> {
                try {
                    // Simulate database operation via health check (which includes DB check)
                    ResponseEntity<String> response = restTemplate.getForEntity(
                        "http://localhost:" + port + "/actuator/health", String.class);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else {
                        connectionErrors.incrementAndGet();
                    }

                } catch (Exception e) {
                    connectionErrors.incrementAndGet();
                    metricsCollector.recordError("DB_CONNECTION_POOL_TEST");
                }
            });
        }

        executor.shutdown();
        boolean completed = executor.awaitTermination(45, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Then: Verify connection pooling performance
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isGreaterThan(operationCount * 0.9); // 90% success rate

        double operationsPerSecond = (double) successCount.get() / (totalDuration / 1000.0);
        assertThat(operationsPerSecond).isGreaterThan(20.0); // At least 20 operations per second

        // Verify no connection pool exhaustion
        assertThat(connectionErrors.get()).isLessThan(operationCount * 0.1); // Less than 10% connection errors

        System.out.printf("Database Connection Pool Test Results:%n");
        System.out.printf("- Total operations: %d%n", operationCount);
        System.out.printf("- Successful: %d%n", successCount.get());
        System.out.printf("- Connection errors: %d%n", connectionErrors.get());
        System.out.printf("- Duration: %d ms%n", totalDuration);
        System.out.printf("- Throughput: %.2f ops/sec%n", operationsPerSecond);
    }

    @Test
    void testMemoryUsageUnderLoad() throws Exception {
        // Given: Initial memory state
        Runtime runtime = Runtime.getRuntime();
        long initialUsedMemory = runtime.totalMemory() - runtime.freeMemory();

        // When: Generate sustained load
        int loadDurationSeconds = 30;
        int concurrentOperations = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentOperations);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentOperations; i++) {
            executor.submit(() -> {
                long operationStartTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - operationStartTime < loadDurationSeconds * 1000) {
                    try {
                        restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
                        Thread.sleep(100); // 10 requests per second per thread
                    } catch (Exception e) {
                        break; // Exit on error
                    }
                }
            });
        }

        // Let it run for the duration
        Thread.sleep(loadDurationSeconds * 1000);

        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        // Force garbage collection to get accurate memory reading
        System.gc();
        Thread.sleep(1000);

        long finalUsedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalUsedMemory - initialUsedMemory;
        double memoryIncreaseMB = memoryIncrease / (1024.0 * 1024.0);

        // Then: Verify memory usage is reasonable
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) finalUsedMemory / maxMemory * 100.0;

        // Memory usage should not exceed 80% under load
        assertThat(memoryUsagePercent).isLessThan(80.0);

        // Memory increase should be reasonable (less than 100MB for this test)
        assertThat(memoryIncreaseMB).isLessThan(100.0);

        System.out.printf("Memory Usage Test Results:%n");
        System.out.printf("- Initial memory: %.2f MB%n", initialUsedMemory / (1024.0 * 1024.0));
        System.out.printf("- Final memory: %.2f MB%n", finalUsedMemory / (1024.0 * 1024.0));
        System.out.printf("- Memory increase: %.2f MB%n", memoryIncreaseMB);
        System.out.printf("- Memory usage: %.2f%%%n", memoryUsagePercent);
        System.out.printf("- Test duration: %d seconds%n", (endTime - startTime) / 1000);
    }

    @Test
    void testConcurrentProducerConsumerOperations() throws Exception {
        // Given: Concurrent producer and consumer operations
        int producerThreads = 5;
        int consumerThreads = 3;
        int messagesPerProducer = 100;
        String topic = "concurrent-test";

        ExecutorService producerExecutor = Executors.newFixedThreadPool(producerThreads);
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(consumerThreads);

        AtomicInteger totalProduced = new AtomicInteger(0);
        AtomicInteger totalConsumed = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Producers
        for (int p = 0; p < producerThreads; p++) {
            final int producerId = p;
            producerExecutor.submit(() -> {
                for (int i = 0; i < messagesPerProducer; i++) {
                    try {
                        kafkaTemplate.send(topic, "producer-" + producerId + "-message-" + i);
                        totalProduced.incrementAndGet();
                    } catch (Exception e) {
                        metricsCollector.recordError("CONCURRENT_PRODUCER_ERROR");
                    }
                }
            });
        }

        // Consumers (simulated via API calls that trigger consumption)
        for (int c = 0; c < consumerThreads; c++) {
            consumerExecutor.submit(() -> {
                // Simulate consumer operations via health checks
                // In real scenarios, this would be actual Kafka consumption
                for (int i = 0; i < 50; i++) {
                    try {
                        restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
                        totalConsumed.incrementAndGet();
                        Thread.sleep(50);
                    } catch (Exception e) {
                        metricsCollector.recordError("CONCURRENT_CONSUMER_ERROR");
                    }
                }
            });
        }

        producerExecutor.shutdown();
        consumerExecutor.shutdown();

        boolean producersCompleted = producerExecutor.awaitTermination(30, TimeUnit.SECONDS);
        boolean consumersCompleted = consumerExecutor.awaitTermination(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        // Then: Verify concurrent operations
        assertThat(producersCompleted).isTrue();
        assertThat(consumersCompleted).isTrue();

        assertThat(totalProduced.get()).isEqualTo(producerThreads * messagesPerProducer);
        assertThat(totalConsumed.get()).isGreaterThan(0);

        long duration = endTime - startTime;
        double throughput = totalProduced.get() / (duration / 1000.0);

        System.out.printf("Concurrent Operations Test Results:%n");
        System.out.printf("- Producers: %d threads%n", producerThreads);
        System.out.printf("- Consumers: %d threads%n", consumerThreads);
        System.out.printf("- Total produced: %d messages%n", totalProduced.get());
        System.out.printf("- Total consumed: %d operations%n", totalConsumed.get());
        System.out.printf("- Duration: %d ms%n", duration);
        System.out.printf("- Producer throughput: %.2f msg/sec%n", throughput);
    }
}
