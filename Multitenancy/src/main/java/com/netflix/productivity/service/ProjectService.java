package com.netflix.productivity.service;

import com.netflix.productivity.dto.ProjectDto;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.mapper.ProjectMapper;
import com.netflix.productivity.repository.ProjectRepository;
import com.netflix.productivity.security.RequirePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private static final String PROJECT_NOT_FOUND = "Project not found";

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Cacheable(cacheNames = CacheNames.PROJECT_LIST, key = "#tenantId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @RequirePermission("PROJECT_READ")
    @Transactional(readOnly = true)
    public Page<ProjectDto> list(String tenantId, Pageable pageable) {
        return projectRepository.findAllActiveByTenant(tenantId, pageable)
                .map(projectMapper::toDto);
    }

    @RequirePermission("PROJECT_WRITE")
    public ProjectDto create(ProjectDto dto) {
        Project entity = projectMapper.toEntity(dto);
        return projectMapper.toDto(projectRepository.save(entity));
    }

    @Cacheable(cacheNames = CacheNames.PROJECT_BY_KEY, key = "#tenantId + ':' + #key")
    @RequirePermission("PROJECT_READ")
    @Transactional(readOnly = true)
    public ProjectDto getByKey(String tenantId, String key) {
        String normalizedKey = normalize(key);
        Project entity = projectRepository.findByTenantAndKey(tenantId, normalizedKey)
                .orElseThrow(() -> new IllegalArgumentException(PROJECT_NOT_FOUND));
        return projectMapper.toDto(entity);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}


