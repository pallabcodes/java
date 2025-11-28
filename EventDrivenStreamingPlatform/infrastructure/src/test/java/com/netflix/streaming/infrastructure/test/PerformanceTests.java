package com.netflix.streaming.infrastructure.test;

import com.netflix.streaming.events.PlaybackStartedEvent;
import com.netflix.streaming.playback.command.PlaybackCommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Tests for Event-Driven Architecture.
 *
 * Validates Netflix-scale performance requirements:
 * - High-throughput event processing
 * - Low latency event publishing
 * - Scalable concurrent operations
 * - Resource usage efficiency
 */
public class PerformanceTests extends EventDrivenTestBase {

    @Autowired
    private PlaybackCommandHandler commandHandler;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    @Test
    void testHighThroughputEventPublishing() throws Exception {
        // Netflix requirement: Handle millions of concurrent streams
        int eventCount = 10000; // Scale down for test environment
        int concurrency = 50;

        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failureCount = new AtomicLong(0);

        long startTime = System.nanoTime();

        // Create concurrent event publishers
        CompletableFuture<Void>[] futures = new CompletableFuture[concurrency];

        for (int i = 0; i < concurrency; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < eventCount / concurrency; j++) {
                    try {
                        PlaybackStartedEvent event = new PlaybackStartedEvent(
                            "perf-correlation-" + threadId + "-" + j, null, "default",
                            "perf-session-" + threadId + "-" + j,
                            "perf-user-" + threadId + "-" + j,
                            "perf-content-" + threadId + "-" + j,
                            "MOVIE", "DESKTOP", "1080p", "1.0.0",
                            "us-east-1", "WIFI", "cdn-perf", 0
                        );

                        kafkaTemplate.send("playback.events", event.getSessionId(), event).get(5, TimeUnit.SECONDS);
                        successCount.incrementAndGet();

                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }
            }, executor);
        }

        // Wait for all publishers to complete
        CompletableFuture.allOf(futures).get(60, TimeUnit.SECONDS);

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // Performance assertions
        assertThat(successCount.get()).isGreaterThanOrEqualTo(eventCount * 0.95); // 95% success rate
        assertThat(failureCount.get()).isLessThanOrEqualTo(eventCount * 0.05); // <5% failure rate

        double throughput = (double) successCount.get() / (durationMs / 1000.0); // events per second
        assertThat(throughput).isGreaterThan(100); // Minimum 100 events/second

        System.out.printf("Performance Test Results: %,d events in %,d ms (%,.0f events/sec)%n",
                         successCount.get(), durationMs, throughput);
    }

    @Test
    void testConcurrentCommandProcessing() throws Exception {
        // Test concurrent command processing (CQRS write side)
        int commandCount = 1000;
        int concurrency = 20;

        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failureCount = new AtomicLong(0);

        long startTime = System.nanoTime();

        CompletableFuture<Void>[] futures = new CompletableFuture[concurrency];

        for (int i = 0; i < concurrency; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < commandCount / concurrency; j++) {
                    try {
                        var request = new PlaybackCommandHandler.StartPlaybackRequest();
                        request.setCorrelationId("perf-cmd-" + threadId + "-" + j);
                        request.setUserId("perf-user-" + threadId + "-" + j);
                        request.setContentId("perf-content-" + threadId + "-" + j);

                        String sessionId = commandHandler.startPlayback(request);
                        assertThat(sessionId).isNotNull();
                        successCount.incrementAndGet();

                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // Performance assertions
        assertThat(successCount.get()).isEqualTo(commandCount);
        assertThat(failureCount.get()).isEqualTo(0);

        double throughput = (double) successCount.get() / (durationMs / 1000.0);
        assertThat(throughput).isGreaterThan(50); // Minimum 50 commands/second

        System.out.printf("Command Processing Results: %,d commands in %,d ms (%,.0f cmd/sec)%n",
                         successCount.get(), durationMs, throughput);
    }

    @Test
    void testEventProcessingLatency() throws Exception {
        // Test end-to-end event processing latency
        int sampleSize = 100;
        long[] latencies = new long[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            eventCapture.clear();

            long startTime = System.nanoTime();

            var request = new PlaybackCommandHandler.StartPlaybackRequest();
            request.setCorrelationId("latency-test-" + i);
            request.setUserId("latency-user-" + i);
            request.setContentId("latency-content-" + i);

            commandHandler.startPlayback(request);

            // Wait for event processing
            boolean eventReceived = eventCapture.waitForEvents(1, 5000);
            long endTime = System.nanoTime();

            assertThat(eventReceived).isTrue();

            latencies[i] = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        }

        // Calculate statistics
        double avgLatency = calculateAverage(latencies);
        double p95Latency = calculatePercentile(latencies, 95);
        double p99Latency = calculatePercentile(latencies, 99);

        // Netflix requirements: Sub-100ms for most operations
        assertThat(avgLatency).isLessThan(200); // Average < 200ms
        assertThat(p95Latency).isLessThan(500); // P95 < 500ms
        assertThat(p99Latency).isLessThan(1000); // P99 < 1s

        System.out.printf("Latency Test Results: Avg=%.1fms, P95=%.1fms, P99=%.1fms%n",
                         avgLatency, p95Latency, p99Latency);
    }

    @Test
    void testMemoryEfficiencyUnderLoad() throws Exception {
        // Test memory usage under sustained load
        Runtime runtime = Runtime.getRuntime();

        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Generate sustained load
        int loadDurationSeconds = 30;
        int eventsPerSecond = 50;

        long endTime = System.currentTimeMillis() + (loadDurationSeconds * 1000);

        while (System.currentTimeMillis() < endTime) {
            long batchStartTime = System.currentTimeMillis();

            // Send batch of events
            for (int i = 0; i < eventsPerSecond; i++) {
                var request = new PlaybackCommandHandler.StartPlaybackRequest();
                request.setCorrelationId("memory-test-" + System.currentTimeMillis() + "-" + i);
                request.setUserId("memory-user-" + i);
                request.setContentId("memory-content-" + i);

                try {
                    commandHandler.startPlayback(request);
                } catch (Exception e) {
                    // Ignore errors for memory test
                }
            }

            // Wait for next batch
            long batchDuration = System.currentTimeMillis() - batchStartTime;
            if (batchDuration < 1000) {
                Thread.sleep(1000 - batchDuration);
            }
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Memory leak check: Should not increase by more than 50MB under load
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024); // 50MB limit

        System.out.printf("Memory Test Results: Initial=%,d bytes, Final=%,d bytes, Increase=%,d bytes%n",
                         initialMemory, finalMemory, memoryIncrease);
    }

    @Test
    void testKafkaThroughputLimits() throws Exception {
        // Test Kafka throughput limits and backpressure handling
        int batchSize = 1000;
        int batches = 5;

        long totalStartTime = System.nanoTime();
        long totalEvents = 0;

        for (int batch = 0; batch < batches; batch++) {
            long batchStartTime = System.nanoTime();
            CompletableFuture<Void>[] futures = new CompletableFuture[batchSize];

            // Send batch of events concurrently
            for (int i = 0; i < batchSize; i++) {
                final int eventId = (batch * batchSize) + i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        PlaybackStartedEvent event = new PlaybackStartedEvent(
                            "throughput-test-" + eventId, null, "default",
                            "throughput-session-" + eventId,
                            "throughput-user-" + eventId,
                            "throughput-content-" + eventId,
                            "MOVIE", "DESKTOP", "1080p", "1.0.0",
                            "us-east-1", "WIFI", "cdn-throughput", 0
                        );

                        kafkaTemplate.send("playback.events", event.getSessionId(), event).get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
            }

            // Wait for batch completion
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
            long batchDurationMs = (System.nanoTime() - batchStartTime) / 1_000_000;

            totalEvents += batchSize;
            double batchThroughput = (double) batchSize / (batchDurationMs / 1000.0);

            System.out.printf("Batch %d: %,d events in %,d ms (%,.0f events/sec)%n",
                             batch, batchSize, batchDurationMs, batchThroughput);

            // Brief pause between batches
            Thread.sleep(1000);
        }

        long totalDurationMs = (System.nanoTime() - totalStartTime) / 1_000_000;
        double totalThroughput = (double) totalEvents / (totalDurationMs / 1000.0);

        // Kafka should handle reasonable throughput
        assertThat(totalThroughput).isGreaterThan(100); // Minimum 100 events/second

        System.out.printf("Total Throughput Test: %,d events in %,d ms (%,.0f events/sec)%n",
                         totalEvents, totalDurationMs, totalThroughput);
    }

    // Helper methods

    private double calculateAverage(long[] values) {
        return java.util.Arrays.stream(values).average().orElse(0.0);
    }

    private double calculatePercentile(long[] values, double percentile) {
        java.util.Arrays.sort(values);
        int index = (int) Math.ceil(percentile / 100.0 * values.length) - 1;
        return values[Math.max(0, Math.min(index, values.length - 1))];
    }
}