package com.netflix.productivity.e2e;

import com.netflix.productivity.ProductivityPlatformApplication;
import com.netflix.productivity.dto.IssueDto;
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
@DisplayName("IssueController E2E - Multi-tenant")
class IssueControllerE2E {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_e2e_db")
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
    void shouldRespectTenantBoundaryOnCreateAndFetch() {
        String base = "http://localhost:" + port + "/api/issues";

        IssueDto dto = IssueDto.builder()
                .tenantId("tenantA")
                .key("PROJ-10")
                .title("A test issue")
                .status(com.netflix.productivity.entity.Issue.IssueStatus.OPEN)
                .priority(com.netflix.productivity.entity.Issue.IssuePriority.MEDIUM)
                .type(com.netflix.productivity.entity.Issue.IssueType.TASK)
                .projectId("project-1")
                .reporterId("user-1")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Tenant-ID", "tenantA");

        ResponseEntity<String> createResp = restTemplate.exchange(base, HttpMethod.POST, new HttpEntity<>(dto, headers), String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> okTenantFetch = restTemplate.exchange(base + "/PROJ-10", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(okTenantFetch.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders otherTenant = new HttpHeaders();
        otherTenant.add("X-Tenant-ID", "tenantB");
        ResponseEntity<String> crossTenantFetch = restTemplate.exchange(base + "/PROJ-10", HttpMethod.GET, new HttpEntity<>(otherTenant), String.class);
        assertThat(crossTenantFetch.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }
}


