package com.netflix.productivity.service;

import com.netflix.productivity.dto.IssueDto;
import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.mapper.IssueMapper;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.security.RequirePermission;
import com.netflix.productivity.workflow.entity.WorkflowState;
import com.netflix.productivity.workflow.repository.WorkflowRepository;
import com.netflix.productivity.workflow.repository.WorkflowStateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.netflix.productivity.repository.projection.IssueListProjection;

@Service
@Transactional
public class IssueService {

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStateRepository stateRepository;

    public IssueService(IssueRepository issueRepository, IssueMapper issueMapper, WorkflowRepository workflowRepository, WorkflowStateRepository stateRepository) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
        this.workflowRepository = workflowRepository;
        this.stateRepository = stateRepository;
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_LIST, key = "#tenantId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    @Timed(value = "issues.list", description = "List issues", extraTags = {"op","list"})
    public Page<IssueDto> list(String tenantId, Pageable pageable) {
        return issueRepository.findAllActiveByTenant(tenantId, pageable)
                .map(issueMapper::toDto);
    }

    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    @Timed(value = "issues.listLite", description = "List issues lite", extraTags = {"op","listLite"})
    public Page<IssueListProjection> listLite(String tenantId, Pageable pageable) {
        return issueRepository.findListProjection(tenantId, pageable);
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_LIST_BY_PROJECT, key = "#tenantId + ':' + #projectId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    @Timed(value = "issues.list", description = "List issues by project", extraTags = {"op","listByProject"})
    public Page<IssueDto> listByProject(String tenantId, String projectId, Pageable pageable) {
        String normalizedProjectId = normalize(projectId);
        return issueRepository.findByTenantAndProject(tenantId, normalizedProjectId, pageable)
                .map(issueMapper::toDto);
    }

    @RequirePermission("ISSUE_WRITE")
    public IssueDto create(IssueDto dto) {
        Issue entity = issueMapper.toEntity(dto);
        // try attach default workflow and initial state if exists
        workflowRepository.findByTenantIdAndProjectIdAndKey(dto.getTenantId(), dto.getProjectId(), "default")
                .ifPresent(wf -> {
                    entity.setWorkflowId(wf.getId());
                    stateRepository.findByTenantIdAndProjectIdAndWorkflowIdAndInitialTrue(dto.getTenantId(), dto.getProjectId(), wf.getId())
                            .ifPresent(s -> entity.setWorkflowStateId(s.getId()));
                });
        return issueMapper.toDto(issueRepository.save(entity));
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_BY_KEY, key = "#tenantId + ':' + #key")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    @Timed(value = "issues.get", description = "Get issue by key")
    public IssueDto getByKey(String tenantId, String key) {
        String normalizedKey = normalize(key);
        Issue entity = issueRepository.findByTenantAndKey(tenantId, normalizedKey)
                .orElseThrow(() -> new IllegalArgumentException(ISSUE_NOT_FOUND));
        return issueMapper.toDto(entity);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}


