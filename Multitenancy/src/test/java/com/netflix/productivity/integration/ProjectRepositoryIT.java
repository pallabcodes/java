package com.netflix.productivity.integration;

import com.netflix.productivity.entity.Project;
import com.netflix.productivity.repository.ProjectRepository;
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
@DisplayName("ProjectRepository - Multi-tenancy Integration Test")
class ProjectRepositoryIT {

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
    private ProjectRepository projectRepository;

    @Test
    void shouldPersistAndFindByTenantAndKey() {
        Project project = Project.builder()
            .tenantId("tenantA")
            .key("PROJ")
            .name("Project A")
            .status(Project.ProjectStatus.PLANNED)
            .type(Project.ProjectType.SOFTWARE)
            .ownerId("user-1")
            .build();

        Project saved = projectRepository.save(project);
        assertThat(saved.getId()).isNotNull();

        assertThat(projectRepository.findByTenantAndKey("tenantA", "PROJ")).isPresent();
        assertThat(projectRepository.findByTenantAndKey("tenantB", "PROJ")).isNotPresent();
    }
}


