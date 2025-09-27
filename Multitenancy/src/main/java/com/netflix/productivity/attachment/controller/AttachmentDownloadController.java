package com.netflix.productivity.attachment.controller;

import com.netflix.productivity.attachment.service.AttachmentService;
import com.netflix.productivity.attachment.service.SignedUrlService;
import com.netflix.productivity.attachment.entity.Attachment;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments/download")
@Tag(name = "Attachments")
public class AttachmentDownloadController {
    private final SignedUrlService signedUrls;
    private final AttachmentService attachments;

    @GetMapping
    public void download(@RequestHeader("X-Tenant-ID") String tenantId,
                         @RequestParam String token,
                         HttpServletResponse response) throws Exception {
        String raw = new String(java.util.Base64.getUrlDecoder().decode(token), java.nio.charset.StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        if (parts.length != 4) {
            response.setStatus(400);
            return;
        }
        String tokTenant = parts[0];
        String attachmentId = parts[1];
        if (!tenantId.equals(tokTenant) || !signedUrls.validate(token, tokTenant, attachmentId)) {
            response.setStatus(403);
            return;
        }

        Attachment meta = attachments.getMetadata(tenantId, attachmentId);
        String filename = meta.getFilename() == null ? "attachment" : meta.getFilename();
        String contentType = meta.getContentType() == null ? "application/octet-stream" : meta.getContentType();
        long size = meta.getSizeBytes();

        try (InputStream in = attachments.openForDownload(tenantId, attachmentId); OutputStream out = response.getOutputStream()) {
            response.setStatus(200);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Length", Long.toString(size));
            in.transferTo(out);
            out.flush();
        }
    }

    @RequestMapping(method = org.springframework.web.bind.annotation.RequestMethod.HEAD)
    public void validateHead(@RequestHeader("X-Tenant-ID") String tenantId,
                             @RequestParam String token,
                             HttpServletResponse response) throws Exception {
        String raw = new String(java.util.Base64.getUrlDecoder().decode(token), java.nio.charset.StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        if (parts.length != 4) {
            response.setStatus(400);
            return;
        }
        String tokTenant = parts[0];
        String attachmentId = parts[1];
        if (!tenantId.equals(tokTenant) || !signedUrls.validate(token, tokTenant, attachmentId)) {
            response.setStatus(403);
            return;
        }
        Attachment meta = attachments.getMetadata(tenantId, attachmentId);
        response.setStatus(200);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + (meta.getFilename() == null ? "attachment" : meta.getFilename()) + "\"");
        response.setHeader("Content-Type", meta.getContentType() == null ? "application/octet-stream" : meta.getContentType());
        response.setHeader("Content-Length", Long.toString(meta.getSizeBytes()));
    }
}

