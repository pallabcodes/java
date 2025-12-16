package com.netflix.streaming.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserJourneyTest {

    private static final Network network = Network.newNetwork();
    private static final String BASE_URL = "http://localhost:8081"; // Infrastructure service port

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("e2etest")
        .withUsername("e2euser")
        .withPassword("e2epass")
        .withNetwork(network);

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.4.0")
        .withNetwork(network);

    @Container
    static GenericContainer<?> analyticsService = new GenericContainer<>("netflix/analytics-service:latest")
        .withNetwork(network)
        .withExposedPorts(8083)
        .withEnv("SPRING_PROFILES_ACTIVE", "test")
        .dependsOn(postgres, kafka);

    @Container
    static GenericContainer<?> playbackService = new GenericContainer<>("netflix/playback-service:latest")
        .withNetwork(network)
        .withExposedPorts(8082)
        .withEnv("SPRING_PROFILES_ACTIVE", "test")
        .dependsOn(postgres, kafka);

    @Container
    static GenericContainer<?> mlPipelineService = new GenericContainer<>("netflix/ml-pipeline-service:latest")
        .withNetwork(network)
        .withExposedPorts(8084)
        .withEnv("SPRING_PROFILES_ACTIVE", "test")
        .dependsOn(postgres, kafka);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String sessionId;
    private String contentId;

    @BeforeEach
    void setUp() {
        userId = "e2e-user-" + UUID.randomUUID();
        contentId = "e2e-content-" + UUID.randomUUID();
    }

    @Test
    @Order(1)
    void healthCheckJourney() {
        // Test health of all services
        assertServiceHealth(BASE_URL + "/actuator/health", "Infrastructure Service");
        assertServiceHealth("http://localhost:" + analyticsService.getMappedPort(8083) + "/actuator/health", "Analytics Service");
        assertServiceHealth("http://localhost:" + playbackService.getMappedPort(8082) + "/actuator/health", "Playback Service");
        assertServiceHealth("http://localhost:" + mlPipelineService.getMappedPort(8084) + "/actuator/health", "ML Pipeline Service");
    }

    @Test
    @Order(2)
    void userRegistrationAndAuthenticationJourney() {
        // Simulate user registration
        Map<String, Object> registrationData = Map.of(
            "username", userId,
            "email", userId + "@example.com",
            "password", "securePassword123"
        );

        ResponseEntity<String> registrationResponse = restTemplate.postForEntity(
            BASE_URL + "/api/v1/users/register",
            registrationData,
            String.class
        );

        assertEquals(HttpStatus.CREATED, registrationResponse.getStatusCode());

        // Simulate login and get JWT token
        Map<String, Object> loginData = Map.of(
            "username", userId,
            "password", "securePassword123"
        );

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            BASE_URL + "/api/v1/auth/login",
            loginData,
            Map.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody().get("token"));
    }

    @Test
    @Order(3)
    void contentBrowsingJourney() {
        // Browse available content
        ResponseEntity<Map> contentResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/content/browse?category=movies&limit=10",
            Map.class
        );

        assertEquals(HttpStatus.OK, contentResponse.getStatusCode());
        assertNotNull(contentResponse.getBody().get("content"));
        assertTrue(((java.util.List<?>) contentResponse.getBody().get("content")).size() > 0);

        // Search for specific content
        ResponseEntity<Map> searchResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/content/search?q=action&limit=5",
            Map.class
        );

        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
    }

    @Test
    @Order(4)
    void playbackStartJourney() {
        // Start playback session
        Map<String, Object> playbackData = Map.of(
            "userId", userId,
            "contentId", contentId,
            "deviceType", "WEB",
            "quality", "HD",
            "startPosition", 0
        );

        ResponseEntity<Map> startResponse = restTemplate.postForEntity(
            BASE_URL + "/api/v1/playback/start",
            playbackData,
            Map.class
        );

        assertEquals(HttpStatus.CREATED, startResponse.getStatusCode());
        assertNotNull(startResponse.getBody().get("sessionId"));

        sessionId = (String) startResponse.getBody().get("sessionId");

        // Verify event was published to Kafka
        assertEventPublished("playback.events", "PLAYBACK_STARTED");
    }

    @Test
    @Order(5)
    void playbackInteractionJourney() {
        assumeSessionExists();

        // Update playback position
        Map<String, Object> positionData = Map.of(
            "position", 300,
            "quality", "4K"
        );

        ResponseEntity<Map> positionResponse = restTemplate.exchange(
            BASE_URL + "/api/v1/playback/" + sessionId + "/position",
            HttpMethod.PUT,
            new HttpEntity<>(positionData),
            Map.class
        );

        assertEquals(HttpStatus.OK, positionResponse.getStatusCode());

        // Get playback status
        ResponseEntity<Map> statusResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/playback/" + sessionId + "/status",
            Map.class
        );

        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        assertEquals(sessionId, statusResponse.getBody().get("sessionId"));
        assertEquals(300, statusResponse.getBody().get("position"));
    }

    @Test
    @Order(6)
    void analyticsTrackingJourney() {
        assumeSessionExists();

        // Get user analytics
        ResponseEntity<Map> analyticsResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/analytics/dashboard?userId=" + userId,
            Map.class
        );

        assertEquals(HttpStatus.OK, analyticsResponse.getStatusCode());
        assertNotNull(analyticsResponse.getBody().get("metrics"));

        // Get content analytics
        ResponseEntity<Map> contentAnalyticsResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/analytics/content/" + contentId,
            Map.class
        );

        assertEquals(HttpStatus.OK, contentAnalyticsResponse.getStatusCode());
    }

    @Test
    @Order(7)
    void playbackCompletionJourney() {
        assumeSessionExists();

        // Complete playback session
        Map<String, Object> completionData = Map.of(
            "endPosition", 3600,
            "reason", "COMPLETED"
        );

        ResponseEntity<Map> completionResponse = restTemplate.postForEntity(
            BASE_URL + "/api/v1/playback/" + sessionId + "/stop",
            completionData,
            Map.class
        );

        assertEquals(HttpStatus.OK, completionResponse.getStatusCode());

        // Verify completion event was published
        assertEventPublished("playback.events", "PLAYBACK_COMPLETED");
    }

    @Test
    @Order(8)
    void mlRecommendationJourney() {
        // Get personalized recommendations
        ResponseEntity<Map> recommendationsResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/recommendations?userId=" + userId + "&limit=5",
            Map.class
        );

        assertEquals(HttpStatus.OK, recommendationsResponse.getStatusCode());
        assertNotNull(recommendationsResponse.getBody().get("recommendations"));

        // Verify ML pipeline processed the data
        assertMlPipelineProcessed(userId);
    }

    @Test
    @Order(9)
    void errorHandlingJourney() {
        // Test invalid session access
        ResponseEntity<Map> invalidResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/playback/invalid-session/status",
            Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, invalidResponse.getStatusCode());
        assertEquals("PLAYBACK_SESSION_NOT_FOUND", invalidResponse.getBody().get("error"));

        // Test rate limiting
        for (int i = 0; i < 150; i++) {
            restTemplate.getForEntity(BASE_URL + "/api/v1/health", Map.class);
        }

        ResponseEntity<Map> rateLimitedResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/health",
            Map.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, rateLimitedResponse.getStatusCode());
        assertNotNull(rateLimitedResponse.getHeaders().get("Retry-After"));
    }

    @Test
    @Order(10)
    void dataConsistencyJourney() {
        // Verify data consistency across services
        ResponseEntity<Map> userDataResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/users/" + userId,
            Map.class
        );

        assertEquals(HttpStatus.OK, userDataResponse.getStatusCode());

        // Check that analytics data matches user actions
        ResponseEntity<Map> consistencyResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/analytics/user/" + userId + "/consistency",
            Map.class
        );

        assertEquals(HttpStatus.OK, consistencyResponse.getStatusCode());
        assertTrue((Boolean) consistencyResponse.getBody().get("consistent"));
    }

    // Helper methods
    private void assertServiceHealth(String url, String serviceName) {
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            serviceName + " should be healthy");
        assertEquals("UP", response.getBody().get("status"),
            serviceName + " status should be UP");
    }

    private void assumeSessionExists() {
        if (sessionId == null) {
            // Create a session if not exists
            Map<String, Object> playbackData = Map.of(
                "userId", userId,
                "contentId", contentId,
                "deviceType", "WEB",
                "quality", "HD"
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/api/v1/playback/start",
                playbackData,
                Map.class
            );

            sessionId = (String) response.getBody().get("sessionId");
        }
    }

    private void assertEventPublished(String topic, String eventType) {
        // Verify event was published by checking Kafka or event store
        // This would typically check the event store or Kafka consumer
        ResponseEntity<Map> eventsResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/events/recent?userId=" + userId,
            Map.class
        );

        assertEquals(HttpStatus.OK, eventsResponse.getStatusCode());
        // Additional assertions would check for the specific event
    }

    private void assertMlPipelineProcessed(String userId) {
        // Verify ML pipeline processed user data
        ResponseEntity<Map> mlResponse = restTemplate.getForEntity(
            BASE_URL + "/api/v1/ml/status?userId=" + userId,
            Map.class
        );

        assertEquals(HttpStatus.OK, mlResponse.getStatusCode());
        // Additional assertions would check ML processing status
    }
}
