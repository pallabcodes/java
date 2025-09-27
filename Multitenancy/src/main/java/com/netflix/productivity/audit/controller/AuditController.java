package com.netflix.productivity.audit.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.audit.entity.AuditEvent;
import com.netflix.productivity.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues/{issueId}/audit")
@Tag(name = "Audit Logs")
public class AuditController {

    private final AuditService audits;
    private final ResponseMapper responses;

    public AuditController(AuditService audits, ResponseMapper responses) {
        this.audits = audits;
        this.responses = responses;
    }

    @GetMapping
    @Operation(summary = "List audit events for issue")
    public ResponseEntity<ApiResponse<Page<AuditEvent>>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                              @PathVariable String issueId,
                                                              Pageable pageable) {
        return responses.ok(audits.listByIssue(tenantId, issueId, pageable));
    }
}


