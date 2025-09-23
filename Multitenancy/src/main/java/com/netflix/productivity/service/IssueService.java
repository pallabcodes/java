package com.netflix.productivity.service;

import com.netflix.productivity.dto.IssueDto;
import com.netflix.productivity.entity.Issue;
import com.netflix.productivity.mapper.IssueMapper;
import com.netflix.productivity.repository.IssueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;

    public IssueService(IssueRepository issueRepository, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "issueList", key = "#tenantId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<IssueDto> list(String tenantId, Pageable pageable) {
        return issueRepository.findAllActiveByTenant(tenantId, pageable)
                .map(issueMapper::toDto);
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "issueListByProject", key = "#tenantId + ':' + #projectId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<IssueDto> listByProject(String tenantId, String projectId, Pageable pageable) {
        return issueRepository.findByTenantAndProject(tenantId, projectId, pageable)
                .map(issueMapper::toDto);
    }

    @com.netflix.productivity.security.RequirePermission("ISSUE_WRITE")
    public IssueDto create(IssueDto dto) {
        Issue entity = issueMapper.toEntity(dto);
        return issueMapper.toDto(issueRepository.save(entity));
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "issueByKey", key = "#tenantId + ':' + #key")
    @com.netflix.productivity.security.RequirePermission("ISSUE_READ")
    public IssueDto getByKey(String tenantId, String key) {
        Issue entity = issueRepository.findByTenantAndKey(tenantId, key)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
        return issueMapper.toDto(entity);
    }
}


