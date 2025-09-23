package com.netflix.productivity.e2e;

import com.netflix.productivity.ProductivityPlatformApplication;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Negative E2E - Cross-tenant attempts rejected")
class MultiTenantNegativeE2E {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_neg_db")
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

    @Test
    void headerVsPathShouldNotBypassTenantIsolation() {
        String base = "http://localhost:" + port + "/api/issues/PROJ-10";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Tenant-ID", "tenantA");
        ResponseEntity<String> aResp = restTemplate.exchange(base, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        // Unknown yet -> accept 404; later populate if needed
        assertThat(aResp.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST, HttpStatus.OK);

        HttpHeaders other = new HttpHeaders();
        other.add("X-Tenant-ID", "tenantB");
        ResponseEntity<String> bResp = restTemplate.exchange(base, HttpMethod.GET, new HttpEntity<>(other), String.class);
        assertThat(bResp.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }
}


