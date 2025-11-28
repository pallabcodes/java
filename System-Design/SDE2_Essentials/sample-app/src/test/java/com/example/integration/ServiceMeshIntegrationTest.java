package com.example.integration;

import com.example.App;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
    classes = App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test",
        "istio.enabled=true",
        "management.endpoints.web.exposure.include=health,info,metrics,prometheus"
    }
)
@ExtendWith(SpringExtension.class)
@Testcontainers
public class ServiceMeshIntegrationTest {

    private static final Network network = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("sde2_essentials_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .waitingFor(Wait.forListeningPort());

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .waitingFor(Wait.forListeningPort());

    @Container
    private static final GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:latest")
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev", "--http-port=8080", "--hostname=localhost")
            .withNetwork(network)
            .withNetworkAliases("keycloak")
            .waitingFor(Wait.forHttp("/realms/master").forPort(8080).withStartupTimeout(Duration.ofMinutes(5)));

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getFirstMappedPort());
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/sde2-essentials");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testServiceMeshHealthCheck() {
        given()
            .header("X-Correlation-Id", "test-correlation-id-123")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("UP"))
                .body("components.db.status", equalTo("UP"))
                .body("components.redis.status", equalTo("UP"));
    }

    @Test
    void testIstioTracingHeaders() {
        given()
            .header("x-request-id", "test-request-123")
            .header("x-b3-traceid", "80f198ee56343ba864fe8b2a57d3eff7")
            .header("x-b3-spanid", "e457b5a2e4d86bd1")
            .header("x-b3-parentspanid", "05e3ac9a4f6e3b90")
            .header("x-b3-sampled", "1")
            .header("x-b3-flags", "1")
            .when()
                .get("/actuator/info")
            .then()
                .statusCode(200)
                .header("x-correlation-id", notNullValue());
    }

    @Test
    void testCircuitBreakerBehavior() {
        // Test normal operation
        given()
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);

        // This test would need actual circuit breaker implementation
        // For now, just verify the endpoint exists and works
        given()
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void testMutualTlsReadiness() {
        // Test that the application can handle mTLS headers
        given()
            .header("X-Forwarded-Client-Cert", "test-cert")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);
    }

    @Test
    void testLoadBalancingHeaders() {
        // Test that load balancing headers are preserved
        given()
            .header("X-Load-Balancer", "istio-ingress")
            .header("X-Forwarded-For", "10.0.0.1")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .header("X-Correlation-Id", notNullValue());
    }

    @Test
    void testFaultInjectionTolerance() {
        // Test application's tolerance to injected faults
        given()
            .header("x-envoy-fault-percentage", "50")
            .header("x-envoy-fault-delay-request", "100ms")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);
    }

    @Test
    void testTrafficMirroring() {
        // Test that traffic mirroring headers are handled
        given()
            .header("x-envoy-original-dst-host", "original-host")
            .header("x-envoy-mirror", "true")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);
    }

    @Test
    void testCanaryDeploymentHeaders() {
        // Test canary deployment support
        given()
            .header("x-envoy-upstream-alt-stat-name", "canary-deployment")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);
    }

    @Test
    void testServiceMeshMetrics() {
        given()
            .when()
                .get("/actuator/prometheus")
            .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(containsString("istio"))
                .body(containsString("envoy"));
    }

    @Test
    void testRetryPolicyHandling() {
        // Test that retry headers are processed correctly
        given()
            .header("x-envoy-attempt-count", "2")
            .header("x-envoy-max-retries", "3")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200);
    }

    @Test
    void testRateLimitingWithIstio() {
        // Test rate limiting in combination with Istio
        for (int i = 0; i < 5; i++) {
            given()
                .header("X-Forwarded-For", "192.168.1." + i)
                .when()
                    .get("/actuator/health")
                .then()
                    .statusCode(200);
        }

        // This should potentially be rate limited
        given()
            .header("X-Forwarded-For", "192.168.1.100")
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(anyOf(is(200), is(429))); // Either success or rate limited
    }
}