package com.netflix.productivity.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Workflow transition negative E2E")
class WorkflowTransitionNegativeE2E {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("productivity_workflow_neg_e2e")
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

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void transition_withWrongFromState_shouldFail() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-ID", "acme");

        // create issue
        IssueDto payload = IssueDto.builder()
                .tenantId("acme")
                .key("PROJ-102")
                .title("Wrong From State")
                .status(com.netflix.productivity.entity.Issue.IssueStatus.OPEN)
                .priority(com.netflix.productivity.entity.Issue.IssuePriority.MEDIUM)
                .type(com.netflix.productivity.entity.Issue.IssueType.BUG)
                .projectId("project-1")
                .reporterId("user-1")
                .build();
        ResponseEntity<String> createRes = rest.postForEntity("http://localhost:" + port + "/api/issues", new HttpEntity<>(payload, headers), String.class);
        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get workflow id and transitions list
        ResponseEntity<String> wfGetRes = rest.exchange("http://localhost:" + port + "/api/workflows/default?tenantId=acme&projectId=project-1",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        JsonNode wfJson = mapper.readTree(wfGetRes.getBody());
        String workflowId = wfJson.path("data").path("id").asText();

        ResponseEntity<String> trListRes = rest.exchange("http://localhost:" + port + "/api/workflows/" + workflowId + "/transitions?tenantId=acme&projectId=project-1",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        JsonNode trJson = mapper.readTree(trListRes.getBody());
        JsonNode first = trJson.path("data").get(0);
        String validFromStateId = first.path("fromStateId").asText();
        String toStateId = first.path("toStateId").asText();

        // fabricate wrong from state by swapping ids
        String wrongFromState = toStateId;

        String body = mapper.createObjectNode()
                .put("tenantId", "acme")
                .put("projectId", "project-1")
                .put("workflowId", workflowId)
                .put("fromStateId", wrongFromState)
                .put("toStateId", toStateId)
                .toString();

        ResponseEntity<String> trRes = rest.exchange("http://localhost:" + port + "/api/issues/PROJ-102/transition",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertThat(trRes.getStatusCode().is4xxClientError()).isTrue();
    }
}
