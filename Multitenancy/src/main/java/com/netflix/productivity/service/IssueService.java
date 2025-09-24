package com.netflix.productivity.service;

import com.netflix.productivity.dto.IssueDto;
import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.mapper.IssueMapper;
import com.netflix.productivity.repository.IssueRepository;
import com.netflix.productivity.security.RequirePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IssueService {

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;

    public IssueService(IssueRepository issueRepository, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_LIST, key = "#tenantId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    public Page<IssueDto> list(String tenantId, Pageable pageable) {
        return issueRepository.findAllActiveByTenant(tenantId, pageable)
                .map(issueMapper::toDto);
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_LIST_BY_PROJECT, key = "#tenantId + ':' + #projectId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
    public Page<IssueDto> listByProject(String tenantId, String projectId, Pageable pageable) {
        String normalizedProjectId = normalize(projectId);
        return issueRepository.findByTenantAndProject(tenantId, normalizedProjectId, pageable)
                .map(issueMapper::toDto);
    }

    @RequirePermission("ISSUE_WRITE")
    public IssueDto create(IssueDto dto) {
        Issue entity = issueMapper.toEntity(dto);
        return issueMapper.toDto(issueRepository.save(entity));
    }

    @Cacheable(cacheNames = CacheNames.ISSUE_BY_KEY, key = "#tenantId + ':' + #key")
    @RequirePermission("ISSUE_READ")
    @Transactional(readOnly = true)
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


