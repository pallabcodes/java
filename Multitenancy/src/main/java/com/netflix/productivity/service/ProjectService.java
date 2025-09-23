package com.netflix.productivity.service;

import com.netflix.productivity.dto.ProjectDto;
import com.netflix.productivity.entity.Project;
import com.netflix.productivity.mapper.ProjectMapper;
import com.netflix.productivity.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "projectList", key = "#tenantId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProjectDto> list(String tenantId, Pageable pageable) {
        return projectRepository.findAllActiveByTenant(tenantId, pageable)
                .map(projectMapper::toDto);
    }

    @com.netflix.productivity.security.RequirePermission("PROJECT_WRITE")
    public ProjectDto create(ProjectDto dto) {
        Project entity = projectMapper.toEntity(dto);
        return projectMapper.toDto(projectRepository.save(entity));
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = "projectByKey", key = "#tenantId + ':' + #key")
    @com.netflix.productivity.security.RequirePermission("PROJECT_READ")
    public ProjectDto getByKey(String tenantId, String key) {
        Project entity = projectRepository.findByTenantAndKey(tenantId, key)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return projectMapper.toDto(entity);
    }
}


