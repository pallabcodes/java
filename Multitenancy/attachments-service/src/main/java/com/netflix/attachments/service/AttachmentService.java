package com.netflix.attachments.service;

import com.netflix.attachments.entity.Attachment;
import com.netflix.attachments.entity.UploadToken;
import com.netflix.attachments.repository.AttachmentRepository;
import com.netflix.attachments.repository.UploadTokenRepository;
import com.netflix.attachments.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UploadTokenRepository uploadTokenRepository;
    private final StorageService storageService;

    @Transactional
    public UploadToken mintUploadToken(String tenantId, String issueId, String filename, String contentType, long sizeBytes, String createdBy) {
        String storageKey = tenantId + "/" + issueId + "/" + UUID.randomUUID() + "/" + filename;
        UploadToken token = UploadToken.builder()
            .id(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .issueId(issueId)
            .filename(filename)
            .contentType(contentType)
            .sizeBytes(sizeBytes)
            .storageKey(storageKey)
            .expiresAt(OffsetDateTime.now().plusMinutes(15))
            .createdBy(createdBy)
            .build();
        return uploadTokenRepository.save(token);
    }

    @Transactional
    public Attachment uploadWithToken(String tenantId, String tokenId, InputStream data) {
        UploadToken token = uploadTokenRepository.findValid(tokenId, tenantId, OffsetDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        storageService.putObject(token.getStorageKey(), data, token.getSizeBytes(), token.getContentType());

        Attachment attachment = Attachment.builder()
            .id(UUID.randomUUID().toString())
            .tenantId(token.getTenantId())
            .issueId(token.getIssueId())
            .filename(token.getFilename())
            .contentType(token.getContentType())
            .sizeBytes(token.getSizeBytes())
            .storageKey(token.getStorageKey())
            .createdBy(token.getCreatedBy())
            .build();

        uploadTokenRepository.deleteById(token.getId());
        return attachmentRepository.save(attachment);
    }

    @Transactional(readOnly = true)
    public List<Attachment> list(String tenantId, String issueId) {
        return attachmentRepository.findByTenantIdAndIssueId(tenantId, issueId);
    }

    @Transactional
    public void delete(String tenantId, String id) {
        Optional<Attachment> found = attachmentRepository.findById(id);
        if (found.isPresent() && found.get().getTenantId().equals(tenantId)) {
            storageService.deleteObject(found.get().getStorageKey());
            attachmentRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Attachment not found");
        }
    }
}
