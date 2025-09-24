package com.netflix.productivity.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.dto.IssueDto;
import com.netflix.productivity.service.IssueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final ResponseMapper responseMapper;

    public IssueController(IssueService issueService, ResponseMapper responseMapper) {
        this.issueService = issueService;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueDto>>> list(@RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
        return responseMapper.ok(issueService.list(tenantId, pageable));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<IssueDto>>> listByProject(@RequestHeader("X-Tenant-ID") String tenantId,
                                                                     @PathVariable String projectId,
                                                                     Pageable pageable) {
        final String normalizedProjectId = normalize(projectId);
        return responseMapper.ok(issueService.listByProject(tenantId, normalizedProjectId, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueDto>> create(@Validated @RequestBody IssueDto dto) {
        return responseMapper.created(issueService.create(dto));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<IssueDto>> getByKey(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String key) {
        final String normalizedKey = normalize(key);
        return responseMapper.ok(issueService.getByKey(tenantId, normalizedKey));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}


