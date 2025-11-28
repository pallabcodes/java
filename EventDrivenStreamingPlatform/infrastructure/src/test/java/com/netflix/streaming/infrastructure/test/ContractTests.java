package com.netflix.streaming.infrastructure.test;

import com.netflix.streaming.events.BaseEvent;
import com.netflix.streaming.events.PlaybackStartedEvent;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract Tests for Event-Driven Architecture.
 *
 * Validates API contracts and event schemas between services:
 * - REST API contracts
 * - Event schema compatibility
 * - Consumer-driven contract testing
 * - Backward compatibility verification
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ContractTests extends EventDrivenTestBase {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testPlaybackServiceApiContract() {
        // Test REST API contract for playback service
        var request = new PlaybackCommandHandler.StartPlaybackRequest();
        request.setCorrelationId("contract-test-1");
        request.setUserId("contract-user-1");
        request.setContentId("contract-content-1");
        request.setContentType("MOVIE");
        request.setDeviceType("DESKTOP");
        request.setQuality("1080p");

        given()
            .contentType(ContentType.JSON)
            .header("X-Correlation-ID", "contract-test-1")
            .body(request)
        .when()
            .post("/api/v1/playback/sessions")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("sessionId", org.hamcrest.Matchers.notNullValue())
            .body("status", org.hamcrest.Matchers.equalTo("PLAYBACK_STARTED"));
    }

    @Test
    void testEventSchemaCompatibility() {
        // Test that events conform to expected schema
        PlaybackStartedEvent event = new PlaybackStartedEvent(
            "schema-test-1", null, "default",
            "session-schema-1", "user-schema-1", "content-schema-1",
            "MOVIE", "DESKTOP", "1080p", "1.0.0",
            "us-east-1", "WIFI", "cdn-schema", 0
        );

        // Verify required fields are present
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("PLAYBACK_STARTED");
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getCorrelationId()).isEqualTo("schema-test-1");
        assertThat(event.getAggregateId()).isEqualTo("session-schema-1");
        assertThat(event.getAggregateType()).isEqualTo("PlaybackSession");

        // Verify business fields
        assertThat(event.getUserId()).isEqualTo("user-schema-1");
        assertThat(event.getContentId()).isEqualTo("content-schema-1");
        assertThat(event.getContentType()).isEqualTo("MOVIE");
        assertThat(event.getDeviceType()).isEqualTo("DESKTOP");
        assertThat(event.getQuality()).isEqualTo("1080p");
    }

    @Test
    void testEventSerializationCompatibility() throws Exception {
        // Test that events can be serialized/deserialized correctly
        PlaybackStartedEvent originalEvent = new PlaybackStartedEvent(
            "serialization-test-1", null, "default",
            "session-serial-1", "user-serial-1", "content-serial-1",
            "MOVIE", "DESKTOP", "1080p", "1.0.0",
            "us-east-1", "WIFI", "cdn-serial", 0
        );

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(originalEvent);
        assertThat(json).isNotNull();
        assertThat(json).contains("PLAYBACK_STARTED");
        assertThat(json).contains("session-serial-1");

        // Deserialize back
        BaseEvent deserializedEvent = objectMapper.readValue(json, BaseEvent.class);
        assertThat(deserializedEvent).isInstanceOf(PlaybackStartedEvent.class);

        PlaybackStartedEvent typedEvent = (PlaybackStartedEvent) deserializedEvent;
        assertThat(typedEvent.getSessionId()).isEqualTo(originalEvent.getSessionId());
        assertThat(typedEvent.getUserId()).isEqualTo(originalEvent.getUserId());
        assertThat(typedEvent.getContentId()).isEqualTo(originalEvent.getContentId());
    }

    @Test
    void testEventVersionCompatibility() {
        // Test that events maintain backward compatibility
        PlaybackStartedEvent event = new PlaybackStartedEvent(
            "version-test-1", null, "default",
            "session-version-1", "user-version-1", "content-version-1",
            "MOVIE", "DESKTOP", "1080p", "1.0.0",
            "us-east-1", "WIFI", "cdn-version", 0
        );

        // Verify schema version is set
        assertThat(event.getSchemaVersion()).isEqualTo("1.0");

        // Test that new fields can be added without breaking existing consumers
        // (In real scenarios, this would test schema evolution)
        assertThat(event.getCausationId()).isNotNull();
        assertThat(event.getTenantId()).isEqualTo("default");
    }

    @Test
    void testApiResponseContracts() {
        // Test that API responses conform to expected contracts
        var request = new PlaybackCommandHandler.StartPlaybackRequest();
        request.setCorrelationId("response-contract-test-1");
        request.setUserId("response-user-1");
        request.setContentId("response-content-1");

        String response = given()
            .contentType(ContentType.JSON)
            .header("X-Correlation-ID", "response-contract-test-1")
            .body(request)
        .when()
            .post("/api/v1/playback/sessions")
        .then()
            .statusCode(200)
            .extract().asString();

        // Parse response and verify contract
        var responseMap = parseJsonResponse(response);
        assertThat(responseMap).containsKey("sessionId");
        assertThat(responseMap).containsKey("status");
        assertThat(responseMap.get("status")).isEqualTo("PLAYBACK_STARTED");

        // Verify session ID format (should contain user and content IDs)
        String sessionId = (String) responseMap.get("sessionId");
        assertThat(sessionId).contains("response-user-1");
        assertThat(sessionId).contains("response-content-1");
    }

    @Test
    void testErrorResponseContracts() {
        // Test error response contracts
        given()
            .contentType(ContentType.JSON)
            .header("X-Correlation-ID", "error-contract-test-1")
            .body("{}") // Invalid request body
        .when()
            .post("/api/v1/playback/sessions")
        .then()
            .statusCode(400) // Should be bad request for invalid data
            .contentType(ContentType.JSON);
    }

    @Test
    void testEventConsumerContract() {
        // Test that event consumers adhere to their contracts
        PlaybackStartedEvent event = new PlaybackStartedEvent(
            "consumer-contract-test-1", null, "default",
            "session-consumer-1", "user-consumer-1", "content-consumer-1",
            "MOVIE", "DESKTOP", "1080p", "1.0.0",
            "us-east-1", "WIFI", "cdn-consumer", 0
        );

        // Test event consumer interface
        TestEventConsumer consumer = new TestEventConsumer(eventCapture);
        consumer.consume(event);

        // Verify event was captured
        assertThat(eventCapture.getCapturedEvents()).hasSize(1);
        BaseEvent capturedEvent = eventCapture.getCapturedEvents().get(0);
        assertThat(capturedEvent.getEventId()).isEqualTo(event.getEventId());
        assertThat(capturedEvent.getCorrelationId()).isEqualTo(event.getCorrelationId());
    }

    @Test
    void testCorrelationIdPropagation() {
        // Test that correlation IDs are properly propagated through the system
        String correlationId = "correlation-propagation-test-1";

        var request = new PlaybackCommandHandler.StartPlaybackRequest();
        request.setCorrelationId(correlationId);
        request.setUserId("correlation-user-1");
        request.setContentId("correlation-content-1");

        given()
            .contentType(ContentType.JSON)
            .header("X-Correlation-ID", correlationId)
            .body(request)
        .when()
            .post("/api/v1/playback/sessions")
        .then()
            .statusCode(200);

        // Verify event was published with correct correlation ID
        PlaybackStartedEvent event = (PlaybackStartedEvent)
            eventCapture.getCapturedEvents().get(0);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
    }

    // Helper methods

    private java.util.Map<String, Object> parseJsonResponse(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(
                    java.util.HashMap.class, String.class, Object.class
                ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
}