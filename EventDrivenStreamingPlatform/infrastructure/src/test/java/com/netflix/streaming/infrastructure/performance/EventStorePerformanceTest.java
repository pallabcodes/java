package com.netflix.streaming.infrastructure.performance;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.infrastructure.store.PostgreSQLEventStore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
public class EventStorePerformanceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("perfdb")
        .withUsername("perfuser")
        .withPassword("perfpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgreSQLEventStore eventStore;

    private TestEvent testEvent;
    private String aggregateId;

    @Setup(Level.Trial)
    public void setup() {
        aggregateId = "perf-aggregate-" + UUID.randomUUID();

        // Pre-populate with some events for read tests
        for (int i = 0; i < 100; i++) {
            eventStore.save(new TestEvent(aggregateId, "correlation-" + i));
        }

        testEvent = new TestEvent(aggregateId, "perf-correlation");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void saveEvent(Blackhole blackhole) {
        TestEvent event = new TestEvent("save-aggregate-" + UUID.randomUUID(), "save-correlation");
        eventStore.save(event);
        blackhole.consume(event);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void findByAggregateId(Blackhole blackhole) {
        List<BaseEvent> events = eventStore.findByAggregateId(aggregateId);
        blackhole.consume(events);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void findByEventType(Blackhole blackhole) {
        List<BaseEvent> events = eventStore.findByEventType("TEST_EVENT");
        blackhole.consume(events);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void getAggregateVersion(Blackhole blackhole) {
        long version = eventStore.getAggregateVersion(aggregateId);
        blackhole.consume(version);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void replayEvents(Blackhole blackhole) {
        List<BaseEvent> events = eventStore.replayEvents(aggregateId);
        blackhole.consume(events);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void saveEventWithLargePayload(Blackhole blackhole) {
        String largePayload = "x".repeat(5000); // 5KB payload
        TestEvent event = new TestEvent("large-aggregate-" + UUID.randomUUID(), "large-correlation", largePayload);
        eventStore.save(event);
        blackhole.consume(event);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void concurrentSaveEvents() {
        // This would be tested with multiple threads
        TestEvent event = new TestEvent("concurrent-aggregate", "concurrent-correlation-" + Thread.currentThread().getId());
        eventStore.save(event);
    }

    // Test event implementation
    public static class TestEvent extends BaseEvent {
        private final String aggregateId;
        private final String largePayload;

        public TestEvent(String aggregateId, String correlationId) {
            this(aggregateId, correlationId, null);
        }

        public TestEvent(String aggregateId, String correlationId, String largePayload) {
            super(correlationId, null, "default");
            this.aggregateId = aggregateId;
            this.largePayload = largePayload;
        }

        @Override
        public String getAggregateId() {
            return aggregateId;
        }

        @Override
        public String getAggregateType() {
            return "TestAggregate";
        }

        public String getLargePayload() {
            return largePayload;
        }
    }
}
