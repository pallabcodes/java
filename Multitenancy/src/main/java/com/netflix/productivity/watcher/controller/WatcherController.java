package com.netflix.productivity.watcher.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.watcher.entity.IssueWatcher;
import com.netflix.productivity.watcher.service.WatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues/{issueId}/watchers")
@Tag(name = "Watchers")
public class WatcherController {
    private final WatcherService watchers;
    private final ResponseMapper responses;

    @GetMapping
    @Operation(summary = "List watchers of an issue")
    public ResponseEntity<ApiResponse<Page<IssueWatcher>>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                                @PathVariable String issueId,
                                                                Pageable pageable) {
        return responses.ok(watchers.list(tenantId, issueId, pageable));
    }

    @PostMapping
    @Operation(summary = "Add a watcher to an issue")
    public ResponseEntity<ApiResponse<Void>> add(@RequestHeader("X-Tenant-ID") String tenantId,
                                                 @PathVariable String issueId,
                                                 @RequestParam String userId,
                                                 @RequestHeader(value = "X-User-ID", required = false) String actorUserId) {
        watchers.add(tenantId, issueId, userId, actorUserId == null ? "unknown" : actorUserId);
        return responses.noContent();
    }

    @DeleteMapping
    @Operation(summary = "Remove a watcher from an issue")
    public ResponseEntity<ApiResponse<Void>> remove(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @RequestParam String userId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String actorUserId) {
        watchers.remove(tenantId, issueId, userId, actorUserId == null ? "unknown" : actorUserId);
        return responses.noContent();
    }
}

