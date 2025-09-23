package com.netflix.productivity.integration;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.repository.IssueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("IssueRepository - Multi-tenancy Integration Test")
class IssueRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("productivity_test_db")
        .withUsername("test_user")
        .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private IssueRepository issueRepository;

    @Test
    void shouldPersistAndFindByTenantAndKey() {
        Issue issue = Issue.builder()
            .tenantId("tenantA")
            .key("PROJ-1")
            .title("Login bug")
            .status(Issue.IssueStatus.OPEN)
            .priority(Issue.IssuePriority.HIGH)
            .type(Issue.IssueType.BUG)
            .projectId("project-1")
            .reporterId("user-1")
            .build();

        Issue saved = issueRepository.save(issue);
        assertThat(saved.getId()).isNotNull();

        assertThat(issueRepository.findByTenantAndKey("tenantA", "PROJ-1")).isPresent();
        assertThat(issueRepository.findByTenantAndKey("tenantB", "PROJ-1")).isNotPresent();
    }
}


