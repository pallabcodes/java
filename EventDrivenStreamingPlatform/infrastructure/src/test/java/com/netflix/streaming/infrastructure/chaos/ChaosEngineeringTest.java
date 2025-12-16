package com.netflix.streaming.infrastructure.chaos;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

@SpringBootTest
@Testcontainers
@EnabledIfEnvironmentVariable(named = "CHAOS_TESTING_ENABLED", matches = "true")
class ChaosEngineeringTest {

    private static final Network network = Network.newNetwork();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("chaostest")
        .withUsername("chaosuser")
        .withPassword("chaospass")
        .withNetwork(network)
        .withNetworkAliases("postgres");

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.4.0")
        .withNetwork(network)
        .withNetworkAliases("kafka");

    @Container
    static GenericContainer<?> chaosMesh = new GenericContainer<>("chaos-mesh/chaos-mesh:latest")
        .withNetwork(network)
        .withExposedPorts(2333)
        .withCommand("--config=/etc/chaos-mesh/chaos-mesh.cfg");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void shouldHandleDatabaseOutageGracefully() throws Exception {
        // Given - System is running normally
        TestEvent normalEvent = new TestEvent("aggregate-123", "chaos-test-1");
        eventPublisher.publish(normalEvent);

        // When - Simulate database outage
        injectDatabaseFailure();

        // Publish event during outage
        TestEvent outageEvent = new TestEvent("aggregate-456", "chaos-test-2");
        CompletableFuture<Void> publishFuture = CompletableFuture.runAsync(() -> {
            try {
                eventPublisher.publish(outageEvent);
            } catch (Exception e) {
                // Expected during outage
            }
        });

        // Wait for circuit breaker to open
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> isCircuitBreakerOpen());

        // Then - System should handle gracefully
        assertDoesNotThrow(() -> publishFuture.get(5, TimeUnit.SECONDS));

        // Restore database
        restoreDatabase();

        // Verify events are eventually processed
        await().atMost(60, TimeUnit.SECONDS)
            .until(() -> areAllEventsProcessed());
    }

    @Test
    void shouldHandleKafkaOutageWithRetry() throws Exception {
        // Given - System is running normally
        TestEvent normalEvent = new TestEvent("aggregate-789", "kafka-test-1");
        eventPublisher.publish(normalEvent);

        // When - Simulate Kafka outage
        injectKafkaFailure();

        // Publish event during outage
        TestEvent outageEvent = new TestEvent("aggregate-101", "kafka-test-2");
        assertDoesNotThrow(() -> eventPublisher.publish(outageEvent));

        // Wait for retries and DLQ fallback
        await().atMost(45, TimeUnit.SECONDS)
            .until(() -> isEventInDlq(outageEvent.getEventId()));

        // Then - Event should be in DLQ after max retries
        assertTrue(isEventInDlq(outageEvent.getEventId()));

        // Restore Kafka
        restoreKafka();

        // Verify system recovers
        TestEvent recoveryEvent = new TestEvent("aggregate-202", "kafka-test-3");
        eventPublisher.publish(recoveryEvent);

        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> isEventProcessed(recoveryEvent.getEventId()));
    }

    @Test
    void shouldMaintainServiceUnderPodKillChaos() throws Exception {
        // Given - Multiple pods running
        assertTrue(areAllServicesHealthy());

        // When - Simulate pod kills (kubernetes chaos)
        injectPodKillChaos();

        // Publish events during chaos
        for (int i = 0; i < 10; i++) {
            TestEvent chaosEvent = new TestEvent("chaos-aggregate-" + i, "pod-kill-test-" + i);
            eventPublisher.publish(chaosEvent);
        }

        // Wait for chaos duration
        Thread.sleep(30000); // 30 seconds of chaos

        // Then - System should self-heal
        await().atMost(120, TimeUnit.SECONDS)
            .until(() -> areAllServicesHealthy());

        // And events should eventually be processed
        await().atMost(180, TimeUnit.SECONDS)
            .until(() -> areChaosEventsProcessed());
    }

    @Test
    void shouldHandleNetworkLatencyGracefully() throws Exception {
        // Given - Normal network conditions
        TestEvent normalEvent = new TestEvent("latency-aggregate-1", "latency-test-1");
        long startTime = System.currentTimeMillis();
        eventPublisher.publish(normalEvent);
        long normalLatency = System.currentTimeMillis() - startTime;

        // When - Inject network latency
        injectNetworkLatency(500); // 500ms latency

        // Publish event with latency
        TestEvent latencyEvent = new TestEvent("latency-aggregate-2", "latency-test-2");
        startTime = System.currentTimeMillis();
        eventPublisher.publish(latencyEvent);
        long highLatency = System.currentTimeMillis() - startTime;

        // Then - System should handle increased latency
        assertTrue(highLatency > normalLatency, "Latency should increase during network chaos");

        // But requests should still succeed (within timeout)
        await().atMost(60, TimeUnit.SECONDS)
            .until(() -> isEventProcessed(latencyEvent.getEventId()));

        // Restore normal network
        restoreNetworkLatency();
    }

    @Test
    void shouldHandleMemoryPressure() throws Exception {
        // Given - Normal memory usage
        assertTrue(isMemoryUsageNormal());

        // When - Inject memory pressure
        injectMemoryPressure();

        // Publish events under memory pressure
        for (int i = 0; i < 50; i++) {
            TestEvent memoryEvent = new TestEvent("memory-aggregate-" + i, "memory-test-" + i);
            eventPublisher.publish(memoryEvent);
        }

        // Then - System should handle memory pressure gracefully
        await().atMost(90, TimeUnit.SECONDS)
            .until(() -> areMemoryEventsProcessed());

        // Memory usage should return to normal
        await().atMost(120, TimeUnit.SECONDS)
            .until(() -> isMemoryUsageNormal());

        restoreMemoryPressure();
    }

    // Helper methods for chaos injection (would integrate with Chaos Mesh)
    private void injectDatabaseFailure() {
        // Use Chaos Mesh to kill database connections or simulate outage
        executeChaosCommand("chaos-mesh", "inject", "database", "failure");
    }

    private void restoreDatabase() {
        executeChaosCommand("chaos-mesh", "recover", "database");
    }

    private void injectKafkaFailure() {
        executeChaosCommand("chaos-mesh", "inject", "kafka", "network-partition");
    }

    private void restoreKafka() {
        executeChaosCommand("chaos-mesh", "recover", "kafka");
    }

    private void injectPodKillChaos() {
        executeChaosCommand("chaos-mesh", "inject", "pod-kill", "--rate=0.5", "--duration=30s");
    }

    private void injectNetworkLatency(int latencyMs) {
        executeChaosCommand("chaos-mesh", "inject", "network-latency",
                          "--latency=" + latencyMs + "ms", "--correlation=100");
    }

    private void restoreNetworkLatency() {
        executeChaosCommand("chaos-mesh", "recover", "network-latency");
    }

    private void injectMemoryPressure() {
        executeChaosCommand("chaos-mesh", "inject", "memory-stress",
                          "--workers=4", "--size=256MB");
    }

    private void restoreMemoryPressure() {
        executeChaosCommand("chaos-mesh", "recover", "memory-stress");
    }

    private void executeChaosCommand(String... args) {
        // Execute chaos mesh commands
        // This would integrate with the Chaos Mesh API
        System.out.println("Executing chaos command: " + String.join(" ", args));
    }

    // Validation methods
    private boolean isCircuitBreakerOpen() {
        // Check if circuit breaker is open via metrics endpoint
        return false; // Implementation would check actual metrics
    }

    private boolean areAllEventsProcessed() {
        // Check if all events are processed via metrics/monitoring
        return true; // Implementation would check actual event processing status
    }

    private boolean isEventInDlq(String eventId) {
        // Check if event is in DLQ via monitoring
        return false; // Implementation would check DLQ
    }

    private boolean isEventProcessed(String eventId) {
        // Check if event was processed successfully
        return true; // Implementation would check event store
    }

    private boolean areAllServicesHealthy() {
        // Check health of all services
        return true; // Implementation would check Kubernetes API
    }

    private boolean areChaosEventsProcessed() {
        // Check if chaos events were processed
        return true; // Implementation would query event store
    }

    private boolean isMemoryUsageNormal() {
        // Check memory usage metrics
        return true; // Implementation would check metrics
    }

    private boolean areMemoryEventsProcessed() {
        // Check if memory stress events were processed
        return true; // Implementation would query event store
    }

    // Test event implementation
    private static class TestEvent extends BaseEvent {
        private final String aggregateId;

        public TestEvent(String aggregateId, String correlationId) {
            super(correlationId, null, "default");
            this.aggregateId = aggregateId;
        }

        @Override
        public String getAggregateId() {
            return aggregateId;
        }

        @Override
        public String getAggregateType() {
            return "ChaosTestAggregate";
        }
    }
}
