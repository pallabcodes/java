package com.netflix.productivity.workflow.service;

import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.service.AuditService;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.workflow.entity.WorkflowState;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import com.netflix.productivity.workflow.repository.WorkflowStateRepository;
import com.netflix.productivity.workflow.repository.WorkflowTransitionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowEngineService {

    private final IssueRepository issueRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowTransitionRepository transitionRepository;

    private final AuditService auditService;

    public WorkflowEngineService(IssueRepository issueRepository,
                                 WorkflowStateRepository stateRepository,
                                 WorkflowTransitionRepository transitionRepository,
                                 AuditService auditService) {
        this.issueRepository = issueRepository;
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.auditService = auditService;
    }

    @RequirePermission("ISSUE_TRANSITION")
    public Issue transition(String tenantId, String projectId, String workflowId, String issueKey, String fromStateId, String toStateId) {
        Issue issue = issueRepository.findByTenantAndKey(tenantId, issueKey)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        // Validate fromState matches issue current state if set
        if (issue.getWorkflowStateId() != null && fromStateId != null && !issue.getWorkflowStateId().equals(fromStateId)) {
            throw new IllegalArgumentException("From state mismatch with issue current state");
        }

        List<WorkflowTransition> allowed = transitionRepository
                .findByTenantIdAndProjectIdAndWorkflowIdAndFromStateIdOrderByOrdinalAsc(tenantId, projectId, workflowId, fromStateId);

        WorkflowTransition chosen = allowed.stream().filter(t -> t.getToStateId().equals(toStateId)).findFirst()
                .orElse(null);
        boolean ok = chosen != null;
        if (!ok) {
            throw new IllegalArgumentException("Transition not allowed");
        }

        // Optional per-transition permission string check hooks via annotation already guard method.
        // If transition defines a stricter permission, enforce it imperatively as defense in depth.
        if (chosen.getRequiredPermission() != null && !chosen.getRequiredPermission().isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities() == null) {
                throw new AccessDeniedException("Missing permission: " + chosen.getRequiredPermission());
            }
            boolean has = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(chosen.getRequiredPermission()) || a.getAuthority().equals("ROLE_" + chosen.getRequiredPermission()));
            if (!has) {
                throw new AccessDeniedException("Missing permission: " + chosen.getRequiredPermission());
            }
        }

        WorkflowState to = stateRepository.findById(toStateId)
                .orElseThrow(() -> new IllegalArgumentException("Target state not found"));

        if (to.getStatus() != null) {
            try {
                Issue.IssueStatus mapped = Issue.IssueStatus.valueOf(to.getStatus());
                issue.setStatus(mapped);
            } catch (IllegalArgumentException ignored) {
                // if mapping invalid, do not change status
            }
        }

        // set current workflow linkage
        issue.setWorkflowId(workflowId);
        issue.setWorkflowStateId(toStateId);

        Issue saved = issueRepository.save(issue);
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(tenantId)
                .projectId(projectId)
                .issueId(saved.getId())
                .entityType("ISSUE")
                .entityId(saved.getId())
                .action("TRANSITION")
                .actorUserId("unknown")
                .message("Transition applied")
                .build());
        return saved;
    }

    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    public List<WorkflowTransition> discoverTransitions(String tenantId, String projectId, String workflowId, String issueKey) {
        Issue issue = issueRepository.findByTenantAndKey(tenantId, issueKey)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
        String from = issue.getWorkflowStateId();
        return transitionRepository.findByTenantIdAndProjectIdAndWorkflowIdAndFromStateIdOrderByOrdinalAsc(tenantId, projectId, workflowId, from);
    }

    @RequirePermission("ISSUE_TRANSITION")
    public Map<String, String> bulkTransition(String tenantId, String projectId, String workflowId, List<String> issueKeys, String toStateId) {
        WorkflowState to = stateRepository.findById(toStateId)
                .orElseThrow(() -> new IllegalArgumentException("Target state not found"));
        return issueKeys.stream().collect(Collectors.toMap(k -> k, k -> {
            try {
                Issue issue = issueRepository.findByTenantAndKey(tenantId, k)
                        .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
                List<WorkflowTransition> allowed = transitionRepository
                        .findByTenantIdAndProjectIdAndWorkflowIdAndFromStateIdOrderByOrdinalAsc(tenantId, projectId, workflowId, issue.getWorkflowStateId());
                boolean ok = allowed.stream().anyMatch(t -> t.getToStateId().equals(toStateId));
                if (!ok) return "not_allowed";
                if (to.getStatus() != null) {
                    try { issue.setStatus(Issue.IssueStatus.valueOf(to.getStatus())); } catch (IllegalArgumentException ignored) {}
                }
                issue.setWorkflowId(workflowId);
                issue.setWorkflowStateId(toStateId);
                issueRepository.save(issue);
                return "ok";
            } catch (Exception e) {
                return "error";
            }
        }));
    }
}


