package com.netflix.productivity.attachment.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.attachment.service.AttachmentService;
import com.netflix.productivity.attachment.service.SignedUrlService;
import com.netflix.productivity.attachment.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments/upload")
@Tag(name = "Attachments")
public class AttachmentUploadController {
    private final SignedUrlService signedUrls;
    private final AttachmentService attachments;
    private final ResponseMapper responses;
    private final StorageService storage;

    @PostMapping("/mint")
    @Operation(summary = "Mint signed upload URL")
    public ResponseEntity<ApiResponse<String>> mint(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @RequestParam String issueId,
                                                    @RequestParam String storageKey,
                                                    @RequestParam long sizeBytes) {
        String url = signedUrls.generateUpload("/api/attachments/upload/receive", tenantId, issueId, storageKey, sizeBytes);
        return responses.ok(url);
    }

    @PostMapping("/receive")
    @Operation(summary = "Receive file with signed token")
    public ResponseEntity<ApiResponse<Void>> receive(@RequestHeader("X-Tenant-ID") String tenantId,
                                                     @RequestParam String token,
                                                     @RequestParam MultipartFile file,
                                                     @RequestParam String filename,
                                                     @RequestParam(required = false) String contentType) throws Exception {
        var claims = signedUrls.validateUpload(token);
        if (!tenantId.equals(claims.tenantId())) {
            return responses.forbidden("Tenant mismatch");
        }
        if (file.getSize() != claims.sizeBytes()) {
            return responses.badRequest("Size mismatch");
        }
        try (var in = file.getInputStream()) {
            storage.save(claims.storageKey(), in);
        }
        attachments.create(tenantId, claims.issueId(), filename, contentType, file.getSize(), claims.storageKey(), "u");
        return responses.noContent();
    }
}

