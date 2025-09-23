package com.netflix.productivity.integration;

import com.netflix.productivity.ProductivityPlatformApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductivityPlatformApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Search Integration - Trigram + JSONB")
class SearchIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_search_db")
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
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seed() {
        // Ensure schema/migrations applied by Spring Boot + Flyway
        // Insert issues for tenantA and tenantB to verify isolation and search
        jdbcTemplate.update("insert into issues (id, tenant_id, key, title, description, status, priority, type, project_id, reporter_id, created_at, updated_at, version) values (uuid_generate_v4(), 'tenantA', 'T1-1', 'Login fails', 'Users cannot login with special characters', 'OPEN', 'HIGH', 'BUG', 'project-1', 'user-1', now(), now(), 0)");
        jdbcTemplate.update("insert into issues (id, tenant_id, key, title, description, status, priority, type, project_id, reporter_id, created_at, updated_at, version) values (uuid_generate_v4(), 'tenantA', 'T1-2', 'Search is slow', 'Trigram indexing should help queries', 'OPEN', 'MEDIUM', 'IMPROVEMENT', 'project-1', 'user-2', now(), now(), 0)");
        jdbcTemplate.update("insert into issues (id, tenant_id, key, title, description, status, priority, type, project_id, reporter_id, created_at, updated_at, version) values (uuid_generate_v4(), 'tenantB', 'T2-1', 'Login broken', 'Different tenant data', 'OPEN', 'HIGH', 'BUG', 'project-2', 'user-3', now(), now(), 0)");
    }

    @Test
    void trigramSearchShouldBeTenantScopedAndCaseInsensitive() {
        String url = "http://localhost:" + port + "/api/search/issues?q=login";

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Tenant-ID", "tenantA");
        ResponseEntity<String> respA = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(respA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respA.getBody()).contains("T1-1");
        assertThat(respA.getBody()).doesNotContain("T2-1");

        headers = new HttpHeaders();
        headers.add("X-Tenant-ID", "tenantB");
        ResponseEntity<String> respB = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(respB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respB.getBody()).contains("T2-1");
        assertThat(respB.getBody()).doesNotContain("T1-1");
    }
}


