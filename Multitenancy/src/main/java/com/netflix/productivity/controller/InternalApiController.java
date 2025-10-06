package com.netflix.productivity.controller;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.repository.ProjectRepository;
import com.netflix.productivity.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalApiController {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping("/issues")
    public ResponseEntity<List<IssueLite>> getIssues(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String projectId,
            @RequestParam OffsetDateTime fromDate,
            @RequestParam OffsetDateTime toDate
    ) {
        List<Issue> issues = issueRepository.findByTenantAndProject(
                tenantId, projectId, PageRequest.of(0, 10000)).getContent();
        List<IssueLite> result = issues.stream().map(IssueLite::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectLite>> getProjects(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String projectId
    ) {
        List<Project> projects = projectRepository.findAllActiveByTenant(tenantId, PageRequest.of(0, 10000)).getContent();
        List<ProjectLite> result = projects.stream()
                .filter(p -> p.getId().equals(projectId))
                .map(ProjectLite::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserLite>> getUsers(
            @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        List<User> users = userRepository.findByTenantId(tenantId);
        List<UserLite> result = users.stream().map(UserLite::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public record IssueLite(String id, String tenantId, String projectId, String title, String description,
                            String status, String priority, String type, String assigneeId, String reporterId,
                            Integer storyPoints, Long timeEstimate, Long timeSpent,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime completedAt,
                            OffsetDateTime slaBreachedAt, String workflowState,
                            Long commentsCount, Long attachmentsCount, Long watchersCount) {
        public static IssueLite from(Issue i) {
            return new IssueLite(
                    i.getId(), i.getTenantId(), i.getProjectId(), i.getTitle(), i.getDescription(),
                    i.getStatus() != null ? i.getStatus().name() : null,
                    i.getPriority() != null ? i.getPriority().name() : null,
                    i.getType() != null ? i.getType().name() : null,
                    i.getAssigneeId(), i.getReporterId(),
                    i.getStoryPoints(), i.getTimeEstimate(), i.getTimeSpent(),
                    toOffset(i.getCreatedAt()), toOffset(i.getUpdatedAt()), toOffset(i.getCompletedAt()),
                    i.getSlaBreachedAt(), i.getWorkflowStateId(),
                    0L, 0L, 0L
            );
        }
    }

    public record ProjectLite(String id, String tenantId, String name, String description, String status,
                              String ownerId, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                              Long totalIssues, Long completedIssues) {
        public static ProjectLite from(Project p) {
            return new ProjectLite(
                    p.getId(), p.getTenantId(), p.getName(), p.getDescription(),
                    p.getStatus() != null ? p.getStatus().name() : null,
                    p.getOwnerId(), toOffset(p.getCreatedAt()), toOffset(p.getUpdatedAt()),
                    0L, 0L
            );
        }
    }

    public record UserLite(String id, String tenantId, String username, String email, String firstName,
                           String lastName, String role, OffsetDateTime createdAt, OffsetDateTime lastLoginAt,
                           boolean isActive) {
        public static UserLite from(User u) {
            return new UserLite(
                    u.getId(), u.getTenantId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(),
                    u.getRole(), toOffset(u.getCreatedAt()), null, u.isActive()
            );
        }
    }

    private static OffsetDateTime toOffset(java.time.LocalDateTime dt) {
        return dt == null ? null : dt.atOffset(ZoneOffset.UTC);
    }
}
