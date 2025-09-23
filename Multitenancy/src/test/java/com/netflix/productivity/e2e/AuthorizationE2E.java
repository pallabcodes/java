package com.netflix.productivity.e2e;

import com.netflix.productivity.ProductivityPlatformApplication;
import com.netflix.productivity.auth.entity.Role;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.entity.UserRole;
import com.netflix.productivity.auth.repository.RoleRepository;
import com.netflix.productivity.auth.repository.UserRepository;
import com.netflix.productivity.auth.repository.UserRoleRepository;
import com.netflix.productivity.auth.service.PasswordService;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.repository.ProjectRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductivityPlatformApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthorizationE2E {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_authz_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordService passwords;
    @Autowired ProjectRepository projects;

    String tenant = "tenantA";

    @BeforeEach
    void seed() {
        userRoles.deleteAll();
        roles.deleteAll();
        users.deleteAll();
        projects.deleteAll();

        // roles
        Role admin = roles.save(Role.builder().tenantId(tenant).name("TENANT_ADMIN").build());
        Role viewer = roles.save(Role.builder().tenantId(tenant).name("VIEWER").build());

        // users
        User adminUser = users.save(User.builder()
                .id(UUID.randomUUID().toString()).tenantId(tenant)
                .email("admin@a.com").username("admin")
                .passwordHash(passwords.hash("secret"))
                .enabled(true).locked(false).tokenVersion(0).build());

        User viewerUser = users.save(User.builder()
                .id(UUID.randomUUID().toString()).tenantId(tenant)
                .email("viewer@a.com").username("viewer")
                .passwordHash(passwords.hash("secret"))
                .enabled(true).locked(false).tokenVersion(0).build());

        userRoles.save(UserRole.builder().tenantId(tenant).userId(adminUser.getId()).roleId(admin.getId()).build());
        userRoles.save(UserRole.builder().tenantId(tenant).userId(viewerUser.getId()).roleId(viewer.getId()).build());

        // project to read
        projects.save(Project.builder()
                .tenantId(tenant).key("PROJ").name("Project")
                .status(Project.ProjectStatus.PLANNED)
                .type(Project.ProjectType.SOFTWARE)
                .ownerId(adminUser.getId()).build());
    }

    @Test
    void viewerShouldBeForbiddenForProjectRead() {
        String token = login("viewer");
        HttpHeaders h = bearer(token);
        h.add("X-Tenant-ID", tenant);
        ResponseEntity<String> resp = rest.exchange(url("/api/projects/PROJ"), HttpMethod.GET, new HttpEntity<>(h), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminShouldAccessProjectRead() {
        String token = login("admin");
        HttpHeaders h = bearer(token);
        h.add("X-Tenant-ID", tenant);
        ResponseEntity<String> resp = rest.exchange(url("/api/projects/PROJ"), HttpMethod.GET, new HttpEntity<>(h), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String login(String username) {
        String body = "{\"tenantId\":\"" + tenant + "\",\"username\":\"" + username + "\",\"password\":\"secret\"}";
        ResponseEntity<String> login = rest.postForEntity(url("/api/auth/login"), new HttpEntity<>(body, json()), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractToken(login.getBody());
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private static HttpHeaders json() { HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); return h; }
    private static HttpHeaders bearer(String token) { HttpHeaders h = new HttpHeaders(); h.add("Authorization","Bearer "+token); return h; }
    private static String extractToken(String responseBody) {
        int pos = responseBody.indexOf("accessToken");
        int start = responseBody.indexOf('"', pos + 12) + 1;
        int end = responseBody.indexOf('"', start);
        return responseBody.substring(start, end);
    }
}


