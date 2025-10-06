package com.netflix.attachments.controller;

import com.netflix.attachments.entity.Attachment;
import com.netflix.attachments.entity.UploadToken;
import com.netflix.attachments.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "Attachment upload and management endpoints")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload/token")
    @Operation(summary = "Mint upload token")
    public ResponseEntity<UploadToken> mintToken(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam @NotBlank String issueId,
            @RequestParam @NotBlank String filename,
            @RequestParam @NotBlank String contentType,
            @RequestParam long sizeBytes,
            @RequestHeader(value = "X-User-ID", required = false) String userId
    ) {
        UploadToken token = attachmentService.mintUploadToken(tenantId, issueId, filename, contentType, sizeBytes, userId != null ? userId : "system");
        return ResponseEntity.ok(token);
    }

    @PostMapping(value = "/upload/{tokenId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload using token")
    public ResponseEntity<Attachment> upload(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String tokenId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        Attachment saved = attachmentService.uploadWithToken(tenantId, tokenId, file.getInputStream());
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "List attachments for an issue")
    public ResponseEntity<List<Attachment>> list(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String issueId
    ) {
        return ResponseEntity.ok(attachmentService.list(tenantId, issueId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attachment")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String id
    ) {
        attachmentService.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
