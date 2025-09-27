package com.netflix.productivity.attachment.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.attachment.entity.Attachment;
import com.netflix.productivity.attachment.service.AttachmentService;
import com.netflix.productivity.attachment.service.SignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues/{issueId}/attachments")
@Tag(name = "Attachments")
public class AttachmentController {
    private final AttachmentService attachments;
    private final ResponseMapper responses;
    private final SignedUrlService signedUrls;

    @GetMapping
    @Operation(summary = "List attachments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponse<Page<Attachment>>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                              @PathVariable String issueId,
                                                              Pageable pageable) {
        return responses.ok(attachments.list(tenantId, issueId, pageable));
    }

    @PostMapping
    @Operation(summary = "Register an attachment metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponse<Attachment>> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                                          @PathVariable String issueId,
                                                          @RequestParam String filename,
                                                          @RequestParam(required = false) String contentType,
                                                          @RequestParam long sizeBytes,
                                                          @RequestParam String storageKey,
                                                          @RequestHeader(value = "X-User-ID", required = false) String userId) {
        Attachment saved = attachments.create(tenantId, issueId, filename, contentType, sizeBytes, storageKey, userId == null ? "unknown" : userId);
        return responses.created(saved);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Soft delete attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String issueId,
                                                    @PathVariable String attachmentId,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        attachments.softDelete(tenantId, attachmentId, userId == null ? "unknown" : userId);
        return responses.noContent();
    }

    @GetMapping("/{attachmentId}/signed-download")
    @Operation(summary = "Generate signed download URL for attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ApiResponse<String>> signedDownload(@RequestHeader("X-Tenant-ID") String tenantId,
                                                             @PathVariable String issueId,
                                                             @PathVariable String attachmentId,
                                                             @RequestParam(defaultValue = "/api/attachments/download") String baseUrl) {
        String url = signedUrls.generate(baseUrl, tenantId, attachmentId);
        return responses.ok(url);
    }
}

