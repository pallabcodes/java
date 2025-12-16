package com.netflix.streaming.analytics;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "playback-service", port = "8082")
class PlaybackServiceContractTest {

    @Autowired
    private RestTemplate restTemplate;

    @Pact(consumer = "analytics-service")
    public RequestResponsePact playbackStartedEventContract(PactDslWithProvider builder) {
        return builder
            .given("Playback session exists")
            .uponReceiving("A request to get playback status")
                .path("/v1/playback/session-123/status")
                .method("GET")
                .headers(Map.of("Authorization", "Bearer token"))
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody((body) -> {
                    body.stringType("sessionId", "session-123");
                    body.stringType("userId", "user-456");
                    body.stringType("contentId", "movie-789");
                    body.stringType("status", "PLAYING");
                    body.numberType("position", 120);
                    body.stringType("quality", "HD");
                }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "playbackStartedEventContract")
    void shouldGetPlaybackStatusSuccessfully(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/v1/playback/session-123/status";

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("session-123", response.getBody().get("sessionId"));
        assertEquals("user-456", response.getBody().get("userId"));
        assertEquals("movie-789", response.getBody().get("contentId"));
        assertEquals("PLAYING", response.getBody().get("status"));
        assertEquals(120, response.getBody().get("position"));
        assertEquals("HD", response.getBody().get("quality"));
    }

    @Pact(consumer = "analytics-service")
    public RequestResponsePact playbackNotFoundContract(PactDslWithProvider builder) {
        return builder
            .given("Playback session does not exist")
            .uponReceiving("A request to get non-existent playback status")
                .path("/v1/playback/non-existent-session/status")
                .method("GET")
                .headers(Map.of("Authorization", "Bearer token"))
            .willRespondWith()
                .status(404)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody((body) -> {
                    body.stringType("error", "PLAYBACK_SESSION_NOT_FOUND");
                    body.stringType("message", "Playback session not found");
                    body.numberType("timestamp", System.currentTimeMillis());
                }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "playbackNotFoundContract")
    void shouldHandlePlaybackNotFound(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/v1/playback/non-existent-session/status";

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("PLAYBACK_SESSION_NOT_FOUND", response.getBody().get("error"));
        assertEquals("Playback session not found", response.getBody().get("message"));
    }

    @Pact(consumer = "analytics-service")
    public RequestResponsePact playbackServiceUnavailableContract(PactDslWithProvider builder) {
        return builder
            .given("Playback service is experiencing high load")
            .uponReceiving("A request during high load")
                .path("/v1/playback/session-123/status")
                .method("GET")
                .headers(Map.of("Authorization", "Bearer token"))
            .willRespondWith()
                .status(503)
                .headers(Map.of(
                    "Content-Type", "application/json",
                    "Retry-After", "30"
                ))
                .body(newJsonBody((body) -> {
                    body.stringType("error", "SERVICE_UNAVAILABLE");
                    body.stringType("message", "Service temporarily unavailable due to high load");
                    body.numberType("retryAfter", 30);
                }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "playbackServiceUnavailableContract")
    void shouldHandleServiceUnavailable(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/v1/playback/session-123/status";

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().get("error"));
        assertEquals("Service temporarily unavailable due to high load", response.getBody().get("message"));
        assertEquals(30, response.getBody().get("retryAfter"));
    }

    @Pact(consumer = "analytics-service")
    public RequestResponsePact playbackUpdatePositionContract(PactDslWithProvider builder) {
        return builder
            .given("Playback session is active")
            .uponReceiving("A request to update playback position")
                .path("/v1/playback/session-123/position")
                .method("PUT")
                .headers(Map.of(
                    "Authorization", "Bearer token",
                    "Content-Type", "application/json"
                ))
                .body(newJsonBody((body) -> {
                    body.numberType("position", 300);
                    body.stringType("quality", "4K");
                }).build())
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody((body) -> {
                    body.stringType("sessionId", "session-123");
                    body.numberType("position", 300);
                    body.stringType("quality", "4K");
                    body.stringType("status", "UPDATED");
                }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "playbackUpdatePositionContract")
    void shouldUpdatePlaybackPositionSuccessfully(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/v1/playback/session-123/position";
        Map<String, Object> requestBody = Map.of(
            "position", 300,
            "quality", "4K"
        );

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("session-123", response.getBody().get("sessionId"));
        assertEquals(300, response.getBody().get("position"));
        assertEquals("4K", response.getBody().get("quality"));
        assertEquals("UPDATED", response.getBody().get("status"));
    }
}
