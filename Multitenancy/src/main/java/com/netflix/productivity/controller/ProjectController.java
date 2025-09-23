package com.netflix.productivity.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.dto.ProjectDto;
import com.netflix.productivity.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ResponseMapper responseMapper;

    public ProjectController(ProjectService projectService, ResponseMapper responseMapper) {
        this.projectService = projectService;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> list(@RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
        return responseMapper.ok(projectService.list(tenantId, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> create(@Validated @RequestBody ProjectDto dto) {
        return responseMapper.created(projectService.create(dto));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<ProjectDto>> getByKey(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String key) {
        return responseMapper.ok(projectService.getByKey(tenantId, key));
    }
}


