package com.netflix.productivity.e2e;

import com.netflix.productivity.ProductivityPlatformApplication;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.repository.UserRepository;
import com.netflix.productivity.auth.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductivityPlatformApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthFlowE2E {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_auth_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;
    @Autowired
    UserRepository users;
    @Autowired
    PasswordService passwords;

    @BeforeEach
    void seedUser() {
        users.deleteAll();
        users.save(User.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId("tenantA")
                .email("dev@tenantA.com")
                .username("dev")
                .passwordHash(passwords.hash("secret"))
                .enabled(true)
                .locked(false)
                .tokenVersion(0)
                .build());
    }

    @Test
    void loginShouldReturnJwtAndAuthenticatedAccess() {
        String url = "http://localhost:" + port + "/api/auth/login";
        String body = "{\"tenantId\":\"tenantA\",\"username\":\"dev\",\"password\":\"secret\"}";
        ResponseEntity<String> login = rest.postForEntity(url, new HttpEntity<>(body, headersJson()), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).contains("accessToken");

        String token = extractToken(login.getBody());
        HttpHeaders h = new HttpHeaders();
        h.add("Authorization", "Bearer " + token);
        h.add("X-Tenant-ID", "tenantA");
        ResponseEntity<String> health = rest.exchange("http://localhost:" + port + "/api/projects", HttpMethod.GET, new HttpEntity<>(h), String.class);
        // projects may require extra setup; asserting not UNAUTHORIZED is enough here
        assertThat(health.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static HttpHeaders headersJson() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private static String extractToken(String responseBody) {
        int pos = responseBody.indexOf("accessToken");
        int start = responseBody.indexOf('"', pos + 12) + 1;
        int end = responseBody.indexOf('"', start);
        return responseBody.substring(start, end);
    }
}


