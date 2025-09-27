package com.netflix.productivity.label.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.label.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues/{issueId}/labels")
@Tag(name = "Labels")
public class IssueLabelController {
    private final LabelService labels;
    private final ResponseMapper responses;

    @PostMapping("/{labelId}")
    @Operation(summary = "Assign label to issue")
    public ResponseEntity<ApiResponse<Void>> assign(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @PathVariable String labelId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        labels.assignToIssue(tenantId, issueId, labelId, userId == null ? "unknown" : userId);
        return responses.noContent();
    }

    @DeleteMapping("/{labelId}")
    @Operation(summary = "Remove label from issue")
    public ResponseEntity<ApiResponse<Void>> remove(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @PathVariable String labelId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        labels.removeFromIssue(tenantId, issueId, labelId, userId == null ? "unknown" : userId);
        return responses.noContent();
    }
}

