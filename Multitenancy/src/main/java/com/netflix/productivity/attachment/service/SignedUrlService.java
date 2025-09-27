package com.netflix.productivity.attachment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SignedUrlService {

    @Value("${attachments.signing.secret:change-me}")
    private String secret;

    @Value("${attachments.signing.ttlSeconds:300}")
    private long ttlSeconds;

    public String generate(String baseDownloadUrl, String tenantId, String attachmentId) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = tenantId + ":" + attachmentId + ":" + exp;
        String sig = hmacSha256(payload, secret);
        String token = base64Url(payload + ":" + sig);
        return baseDownloadUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    public boolean validate(String token, String expectedTenantId, String expectedAttachmentId) {
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        if (parts.length != 4) return false;
        String tenantId = parts[0];
        String attachmentId = parts[1];
        long exp = Long.parseLong(parts[2]);
        String sig = parts[3];
        if (!expectedTenantId.equals(tenantId) || !expectedAttachmentId.equals(attachmentId)) return false;
        if (Instant.now().getEpochSecond() > exp) return false;
        String payload = tenantId + ":" + attachmentId + ":" + exp;
        String calc = hmacSha256(payload, secret);
        return constantTimeEquals(sig, calc);
    }

    public String generateUpload(String baseUploadUrl, String tenantId, String issueId, String storageKey, long sizeBytes) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = String.join(":", tenantId, issueId, storageKey, Long.toString(sizeBytes), Long.toString(exp));
        String sig = hmacSha256(payload, secret);
        String token = base64Url(payload + ":" + sig);
        return baseUploadUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    public record UploadClaims(String tenantId, String issueId, String storageKey, long sizeBytes) {}

    public UploadClaims validateUpload(String token) {
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        if (parts.length != 6) throw new IllegalArgumentException("Invalid token");
        String tenantId = parts[0];
        String issueId = parts[1];
        String storageKey = parts[2];
        long sizeBytes = Long.parseLong(parts[3]);
        long exp = Long.parseLong(parts[4]);
        String sig = parts[5];
        if (Instant.now().getEpochSecond() > exp) throw new IllegalArgumentException("Expired token");
        String payload = String.join(":", tenantId, issueId, storageKey, Long.toString(sizeBytes), Long.toString(exp));
        String calc = hmacSha256(payload, secret);
        if (!constantTimeEquals(sig, calc)) throw new IllegalArgumentException("Invalid signature");
        return new UploadClaims(tenantId, issueId, storageKey, sizeBytes);
    }

    private static String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Signing failure", e);
        }
    }

    private static String base64Url(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}

