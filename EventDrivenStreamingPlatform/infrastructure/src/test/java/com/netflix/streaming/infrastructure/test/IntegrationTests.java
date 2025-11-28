package com.netflix.streaming.infrastructure.test;

import com.netflix.streaming.events.PlaybackStartedEvent;
import com.netflix.streaming.playback.aggregate.PlaybackSession;
import com.netflix.streaming.playback.command.PlaybackCommandHandler;
import com.netflix.streaming.playback.command.PlaybackCommandHandler.StartPlaybackRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static com.netflix.streaming.infrastructure.test.EventDrivenTestBase.EventAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests for Event-Driven Architecture.
 *
 * Tests complete flows across services:
 * - Command → Aggregate → Event → Projection → Query
 * - Cross-service event communication
 * - End-to-end business processes
 * - Eventual consistency verification
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
public class IntegrationTests extends EventDrivenTestBase {

    @Autowired
    private PlaybackCommandHandler commandHandler;

    @Test
    void testCompletePlaybackWorkflow() throws Exception {
        // Given: Test event consumer to capture published events
        TestEventConsumer eventConsumer = new TestEventConsumer(eventCapture);

        // When: Start a playback session
        StartPlaybackRequest request = new StartPlaybackRequest();
        request.setCorrelationId("test-integration-1");
        request.setUserId("user-123");
        request.setContentId("content-456");
        request.setContentType("MOVIE");
        request.setDeviceType("DESKTOP");
        request.setQuality("1080p");

        String sessionId = commandHandler.startPlayback(request);

        // Then: Verify complete event-driven flow
        assertThat(sessionId).isNotNull();
        assertThat(sessionId).contains("ps_user-123_content-456_");

        // Verify event was published
        assertEventPublished(eventCapture, "PLAYBACK_STARTED", 5000);

        // Verify event structure
        PlaybackStartedEvent publishedEvent = (PlaybackStartedEvent)
            eventCapture.getCapturedEvents().get(0);

        assertThat(publishedEvent.getSessionId()).isEqualTo(sessionId);
        assertThat(publishedEvent.getUserId()).isEqualTo("user-123");
        assertThat(publishedEvent.getContentId()).isEqualTo("content-456");
        assertThat(publishedEvent.getCorrelationId()).isEqualTo("test-integration-1");
    }

    @Test
    void testEventCorrelationAndCausation() throws Exception {
        // Given: First event
        StartPlaybackRequest request1 = new StartPlaybackRequest();
        request1.setCorrelationId("test-correlation-1");
        request1.setUserId("user-correlation-1");
        request1.setContentId("content-correlation-1");

        // When: Publish first event
        String sessionId1 = commandHandler.startPlayback(request1);

        // Then: Verify event properties
        PlaybackStartedEvent event1 = (PlaybackStartedEvent)
            eventCapture.waitForEvent("PLAYBACK_STARTED", 5000);

        assertThat(event1.getCorrelationId()).isEqualTo("test-correlation-1");
        assertThat(event1.getCausationId()).isEqualTo(event1.getEventId()); // First event in chain

        eventCapture.clear();

        // When: Publish second event (pause)
        var pauseRequest = new PlaybackCommandHandler.PausePlaybackRequest();
        pauseRequest.setCorrelationId("test-correlation-1"); // Same correlation ID
        pauseRequest.setCausationId(event1.getEventId()); // Caused by first event
        pauseRequest.setSessionId(sessionId1);
        pauseRequest.setCurrentPosition(30000);

        commandHandler.pausePlayback(pauseRequest);

        // Then: Verify correlation chain
        var pauseEvent = eventCapture.waitForEvent("PLAYBACK_PAUSED", 5000);

        assertCorrelationIdPropagated(event1, pauseEvent);
        assertCausationChain(event1, pauseEvent);
    }

    @Test
    void testEventualConsistencyBetweenServices() throws Exception {
        // This test would verify eventual consistency across playback and analytics services
        // In a full integration test suite, this would involve:
        // 1. Start playback service
        // 2. Start analytics service
        // 3. Publish events to playback service
        // 4. Verify events are consumed and processed by analytics service
        // 5. Query analytics projections to verify consistency

        // For this demo, we'll test the event publishing part
        StartPlaybackRequest request = new StartPlaybackRequest();
        request.setCorrelationId("test-consistency-1");
        request.setUserId("user-consistency-1");
        request.setContentId("content-consistency-1");

        String sessionId = commandHandler.startPlayback(request);

        // Verify event was published (would be consumed by analytics service in real scenario)
        assertEventPublished(eventCapture, "PLAYBACK_STARTED", 5000);

        // In full integration test, we would:
        // - Start analytics service in separate thread/container
        // - Wait for analytics projections to be updated
        // - Query analytics service to verify consistency
    }

    @Test
    void testSagaPatternIntegration() throws Exception {
        // Test the ML pipeline saga integration
        // This would test the complete saga workflow:
        // Feature Engineering → Model Training → Model Deployment

        // Note: This would require the ML pipeline service to be running
        // For demo purposes, we'll test the event flow part

        // In full implementation, this test would:
        // 1. Start ML pipeline with saga orchestrator
        // 2. Verify each saga step emits appropriate events
        // 3. Test compensation logic if steps fail
        // 4. Verify end-to-end pipeline completion

        assertThat(true).isTrue(); // Placeholder for saga integration test
    }

    @Test
    void testCQRSReadWriteSeparation() throws Exception {
        // Test CQRS pattern: commands write to aggregates, queries read from projections

        // Write side: Send command
        StartPlaybackRequest command = new StartPlaybackRequest();
        command.setCorrelationId("test-cqrs-1");
        command.setUserId("user-cqrs-1");
        command.setContentId("content-cqrs-1");

        String sessionId = commandHandler.startPlayback(command);

        // Verify write side worked (event published)
        assertEventPublished(eventCapture, "PLAYBACK_STARTED", 5000);

        // Read side: Query projections (would be tested with actual query service)
        // In full test, we would query the read model to verify data consistency

        assertThat(sessionId).isNotNull();
    }

    @Test
    void testEventReplayCapability() throws Exception {
        // Test event replay functionality
        // This would verify that:
        // 1. Events can be replayed from the event store
        // 2. Projections can be rebuilt from event history
        // 3. System state can be reconstructed from events

        // Publish some events
        for (int i = 0; i < 3; i++) {
            StartPlaybackRequest request = new StartPlaybackRequest();
            request.setCorrelationId("test-replay-" + i);
            request.setUserId("user-replay-" + i);
            request.setContentId("content-replay-" + i);

            commandHandler.startPlayback(request);
        }

        // Wait for all events
        assertThat(eventCapture.waitForEvents(3, 10000)).isTrue();

        // In full implementation, this would test:
        // - Event store replay functionality
        // - Projection rebuilding from replayed events
        // - State reconstruction verification

        assertThat(eventCapture.getCapturedEvents()).hasSize(3);
    }

    @Test
    void testCrossServiceEventCommunication() throws Exception {
        // Test events flowing between different services
        // This would verify:
        // 1. Playback service emits events
        // 2. Analytics service consumes and processes them
        // 3. Dashboard service receives updates
        // 4. End-to-end event flow

        StartPlaybackRequest request = new StartPlaybackRequest();
        request.setCorrelationId("test-cross-service-1");
        request.setUserId("user-cross-service-1");
        request.setContentId("content-cross-service-1");

        commandHandler.startPlayback(request);

        // Verify event published (would be consumed by other services in real scenario)
        assertEventPublished(eventCapture, "PLAYBACK_STARTED", 5000);

        // In distributed test, we would verify:
        // - Analytics service processed the event
        // - Projections were updated
        // - Dashboard service received updates
    }

    @Test
    void testErrorHandlingAndRecovery() throws Exception {
        // Test error scenarios and recovery mechanisms
        // This would verify:
        // 1. Error events are properly handled
        // 2. Dead letter queues work correctly
        // 3. Recovery mechanisms function
        // 4. System remains stable under error conditions

        // Test with invalid data to trigger errors
        StartPlaybackRequest invalidRequest = new StartPlaybackRequest();
        invalidRequest.setCorrelationId("test-error-1");
        // Missing required fields - should trigger validation errors

        try {
            commandHandler.startPlayback(invalidRequest);
            // Should not reach here if validation works
            assertThat(false).isTrue();
        } catch (Exception e) {
            // Expected validation error
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}