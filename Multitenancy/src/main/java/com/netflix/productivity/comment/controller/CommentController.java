package com.netflix.productivity.comment.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.comment.entity.Comment;
import com.netflix.productivity.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@Tag(name = "Comments")
public class CommentController {

    private final CommentService comments;
    private final ResponseMapper responses;

    public CommentController(CommentService comments, ResponseMapper responses) {
        this.comments = comments;
        this.responses = responses;
    }

    @GetMapping
    @Operation(summary = "List comments")
    public ResponseEntity<ApiResponse<Page<Comment>>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                           @PathVariable String issueId,
                                                           Pageable pageable) {
        return responses.ok(comments.list(tenantId, issueId, pageable));
    }

    @PostMapping
    @Operation(summary = "Add comment")
    public ResponseEntity<ApiResponse<Comment>> add(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @RequestBody Comment body) {
        body.setTenantId(tenantId);
        body.setIssueId(issueId);
        return responses.created(comments.add(body));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Soft delete comment")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @PathVariable String commentId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        comments.softDelete(tenantId, commentId, userId == null ? "unknown" : userId);
        return responses.noContent();
    }
}


