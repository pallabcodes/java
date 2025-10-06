package com.netflix.productivity.auth.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.api.ErrorCodes;
import com.netflix.productivity.auth.dto.LoginRequest;
import com.netflix.productivity.auth.dto.LoginResponse;
import com.netflix.productivity.auth.dto.ResetRequest;
import com.netflix.productivity.auth.dto.ResetConfirm;
import com.netflix.productivity.auth.dto.RefreshRequest;
import com.netflix.productivity.auth.dto.ChangePasswordRequest;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.repository.UserRepository;
import com.netflix.productivity.auth.repository.UserRoleRepository;
import com.netflix.productivity.auth.repository.RoleRepository;
import com.netflix.productivity.auth.service.JwtTokenService;
import com.netflix.productivity.auth.service.PasswordResetService;
import com.netflix.productivity.auth.service.RefreshTokenService;
import com.netflix.productivity.auth.service.PasswordService;
import jakarta.validation.Valid;
import com.netflix.productivity.audit.service.AuditService;
import com.netflix.productivity.audit.entity.AuditEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.netflix.productivity.auth.service.TotpService;
import com.netflix.productivity.auth.dto.TotpDtos;
import com.netflix.productivity.auth.service.WebAuthnService;
import com.netflix.productivity.auth.dto.WebAuthnDtos;
import com.netflix.productivity.auth.service.DeviceTrustService;
import com.netflix.productivity.auth.dto.DeviceTrustDtos;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final long ACCESS_TOKEN_TTL_MS = 3600000L;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final ResponseMapper responseMapper;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final TotpService totpService;
    private final WebAuthnService webAuthnService;
    private final DeviceTrustService deviceTrustService;

    public AuthenticationController(UserRepository userRepository, UserRoleRepository userRoleRepository,
                                    RoleRepository roleRepository,
                                    PasswordService passwordService, JwtTokenService jwtTokenService,
                                    ResponseMapper responseMapper,
                                    PasswordResetService passwordResetService,
                                    RefreshTokenService refreshTokenService,
                                    AuditService auditService,
                                    TotpService totpService,
                                    WebAuthnService webAuthnService,
                                    DeviceTrustService deviceTrustService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.responseMapper = responseMapper;
        this.passwordResetService = passwordResetService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.totpService = totpService;
        this.webAuthnService = webAuthnService;
        this.deviceTrustService = deviceTrustService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        final String tenant = req.getTenantId();
        final User user = resolveUser(tenant, req);
        if (user == null || !user.isEnabled() || user.isLocked() || !passwordService.matches(req.getPassword(), user.getPasswordHash())) {
            return responseMapper.unauthorized("Invalid credentials", ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
        final var roleNames = resolveRoleNames(tenant, user);
        final Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
        final String token = jwtTokenService.issue(user.getId(), tenant, user.getTokenVersion(), claims);
        // also issue refresh token and return as HttpOnly cookie and in body for non-browser clients
        String refresh = refreshTokenService.issue(tenant, String.valueOf(user.getId()), java.time.Duration.ofDays(30), req.getUserAgent(), req.getIp());
        var resp = responseMapper.ok(new LoginResponse(token, "Bearer", ACCESS_TOKEN_TTL_MS));
        var headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.SET_COOKIE,
                java.net.HttpCookie.parse("refreshToken=" + refresh + "; HttpOnly; Secure; Path=/; SameSite=Strict").get(0).toString());
        var response = new org.springframework.http.ResponseEntity<>(resp.getBody(), headers, resp.getStatusCode());
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(tenant)
                .entityType("USER")
                .entityId(user.getId().toString())
                .action("AUTH_LOGIN")
                .actorUserId(user.getId().toString())
                .message("User login")
                .build());
        return response;
    }

    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetRequest(@Valid @RequestBody ResetRequest req) {
        String token = passwordResetService.issueResetToken(req.getTenantId(), req.getEmail(), java.time.Duration.ofMinutes(15));
        Map<String, String> data = new HashMap<>();
        data.put("resetToken", token);
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(req.getTenantId())
                .entityType("USER")
                .entityId("unknown")
                .action("AUTH_RESET_REQUEST")
                .actorUserId("system")
                .message("Password reset requested for " + req.getEmail())
                .build());
        return responseMapper.ok(data);
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<ApiResponse<Void>> resetConfirm(@Valid @RequestBody ResetConfirm req) {
        passwordResetService.consumeResetToken(req.getTenantId(), req.getToken(), req.getNewPassword());
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(req.getTenantId())
                .entityType("USER")
                .entityId("unknown")
                .action("AUTH_RESET_CONFIRM")
                .actorUserId("system")
                .message("Password reset confirmed")
                .build());
        return responseMapper.noContent();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@Valid @RequestBody RefreshRequest req) {
        String newRefresh = refreshTokenService.rotate(req.getTenantId(), req.getRefreshToken(), java.time.Duration.ofDays(30), req.getUserAgent(), req.getIp());
        User user = refreshTokenService.getUserFromRefresh(req.getTenantId(), newRefresh);
        String access = refreshTokenService.mintAccessToken(user);
        Map<String, String> data = new HashMap<>();
        data.put("refreshToken", newRefresh);
        data.put("accessToken", access);
        data.put("tokenType", "Bearer");
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(req.getTenantId())
                .entityType("USER")
                .entityId(user.getId().toString())
                .action("AUTH_REFRESH")
                .actorUserId(user.getId().toString())
                .message("Token refreshed")
                .build());
        return responseMapper.ok(data);
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revoke(@Valid @RequestBody RefreshRequest req) {
        refreshTokenService.revoke(req.getTenantId(), req.getRefreshToken());
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(req.getTenantId())
                .entityType("USER")
                .entityId("unknown")
                .action("AUTH_REVOKE")
                .actorUserId("system")
                .message("Refresh token revoked")
                .build());
        return responseMapper.noContent();
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> sessions(@RequestParam("tenantId") String tenantId,
                                                                                     @RequestParam("userId") String userId) {
        var principal = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof com.netflix.productivity.security.AuthUser
                ? (com.netflix.productivity.security.AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        boolean isAdmin = principal != null && principal.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("TENANT_ADMIN"));
        if (!isAdmin) {
            if (principal == null || !userId.equals(principal.getUserId())) {
                return responseMapper.forbidden("Forbidden", ErrorCodes.AUTH_FORBIDDEN);
            }
        }
        var tokens = refreshTokenService.listActiveSessions(tenantId, userId);
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (var t : tokens) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("ip", t.getIpAddress());
            m.put("userAgent", t.getUserAgent());
            m.put("createdAt", t.getCreatedAt());
            m.put("expiresAt", t.getExpiresAt());
            data.add(m);
        }
        return responseMapper.ok(data);
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<ApiResponse<Void>> revokeAll(@RequestParam("tenantId") String tenantId,
                                                       @RequestParam("userId") String userId) {
        var principal = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof com.netflix.productivity.security.AuthUser
                ? (com.netflix.productivity.security.AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        boolean isAdmin = principal != null && principal.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("TENANT_ADMIN"));
        if (!isAdmin) {
            if (principal == null || !userId.equals(principal.getUserId())) {
                return responseMapper.forbidden("Forbidden", ErrorCodes.AUTH_FORBIDDEN);
            }
        }
        refreshTokenService.revokeAllForUser(tenantId, userId);
        return responseMapper.noContent();
    }

    @PostMapping("/sessions/revoke")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> revokeSession(@RequestParam("tenantId") String tenantId,
                                                                                           @RequestParam("userId") String userId,
                                                                                           @RequestParam("sessionId") Long sessionId) {
        var principal = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof com.netflix.productivity.security.AuthUser
                ? (com.netflix.productivity.security.AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        boolean isAdmin = principal != null && principal.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("TENANT_ADMIN"));
        if (!isAdmin) {
            if (principal == null || !userId.equals(principal.getUserId())) {
                return responseMapper.forbidden("Forbidden", ErrorCodes.AUTH_FORBIDDEN);
            }
        }
        refreshTokenService.revokeById(tenantId, sessionId);
        var tokens = refreshTokenService.listActiveSessions(tenantId, userId);
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (var t : tokens) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("ip", t.getIpAddress());
            m.put("userAgent", t.getUserAgent());
            m.put("createdAt", t.getCreatedAt());
            m.put("expiresAt", t.getExpiresAt());
            data.add(m);
        }
        return responseMapper.ok(data);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        var principal = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof com.netflix.productivity.security.AuthUser
                ? (com.netflix.productivity.security.AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        boolean isAdmin = principal != null && principal.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("TENANT_ADMIN"));
        User user = (req.getUsernameOrEmail().contains("@"))
                ? userRepository.findByTenantIdAndEmail(req.getTenantId(), req.getUsernameOrEmail()).orElse(null)
                : userRepository.findByTenantIdAndUsername(req.getTenantId(), req.getUsernameOrEmail()).orElse(null);
        if (!isAdmin) {
            if (principal == null || user == null || !user.getId().toString().equals(principal.getUserId())) {
                return responseMapper.forbidden("Forbidden", ErrorCodes.AUTH_FORBIDDEN);
            }
        }
        if (user == null || !passwordService.matches(req.getOldPassword(), user.getPasswordHash())) {
            return responseMapper.unauthorized("Invalid credentials", ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
        user.setPasswordHash(passwordService.hash(req.getNewPassword()));
        userRepository.save(user);
        auditService.record(AuditEvent.builder()
                .id(java.util.UUID.randomUUID().toString())
                .tenantId(req.getTenantId())
                .entityType("USER")
                .entityId(user.getId().toString())
                .action("AUTH_CHANGE_PASSWORD")
                .actorUserId(user.getId().toString())
                .message("Password changed")
                .build());
        return responseMapper.noContent();
    }

    @PostMapping("/mfa/totp/provision")
    public ResponseEntity<ApiResponse<TotpDtos.ProvisionResponse>> provisionTotp(@Valid @RequestBody TotpDtos.ProvisionRequest req) {
        var m = totpService.provision(req.getTenantId(), req.getUserId());
        String label = req.getTenantId() + ":" + req.getUserId();
        String otpauth = "otpauth://totp/" + java.net.URLEncoder.encode(label, java.nio.charset.StandardCharsets.UTF_8) + "?secret=" + m.getTotpSecret() + "&issuer=Productivity";
        return responseMapper.ok(new TotpDtos.ProvisionResponse(m.getTotpSecret(), otpauth));
    }

    @PostMapping("/mfa/totp/verify")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyTotp(@Valid @RequestBody TotpDtos.VerifyRequest req) {
        boolean ok = totpService.enable(req.getTenantId(), req.getUserId(), req.getCode());
        if (!ok) {
            return responseMapper.unauthorized("Invalid TOTP code", ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
        Map<String, String> data = new HashMap<>();
        data.put("status", "enabled");
        return responseMapper.ok(data);
    }

    @PostMapping("/webauthn/register/begin")
    public ResponseEntity<ApiResponse<WebAuthnDtos.BeginRegisterResponse>> webAuthnBeginRegister(@Valid @RequestBody WebAuthnDtos.BeginRegisterRequest req) {
        String challenge = webAuthnService.beginRegister(req.getTenantId(), req.getUserId());
        return responseMapper.ok(new WebAuthnDtos.BeginRegisterResponse(challenge));
    }

    @PostMapping("/webauthn/register/finish")
    public ResponseEntity<ApiResponse<Void>> webAuthnFinishRegister(@Valid @RequestBody WebAuthnDtos.FinishRegisterRequest req) {
        webAuthnService.finishRegister(req.getTenantId(), req.getUserId(), req.getCredentialId(), req.getPublicKey());
        return responseMapper.noContent();
    }

    @PostMapping("/webauthn/assert/begin")
    public ResponseEntity<ApiResponse<WebAuthnDtos.BeginAssertResponse>> webAuthnBeginAssert(@Valid @RequestBody WebAuthnDtos.BeginAssertRequest req) {
        String challenge = webAuthnService.beginAssert(req.getTenantId(), req.getUserId());
        return responseMapper.ok(new WebAuthnDtos.BeginAssertResponse(challenge));
    }

    @PostMapping("/webauthn/assert/finish")
    public ResponseEntity<ApiResponse<Map<String, String>>> webAuthnFinishAssert(@Valid @RequestBody WebAuthnDtos.FinishAssertRequest req) {
        boolean ok = webAuthnService.finishAssert(req.getTenantId(), req.getUserId(), req.getCredentialId());
        if (!ok) {
            return responseMapper.unauthorized("Invalid WebAuthn assertion", ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
        Map<String, String> data = new HashMap<>();
        data.put("status", "ok");
        return responseMapper.ok(data);
    }

    @PostMapping("/device/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerDevice(@Valid @RequestBody DeviceTrustDtos.RegisterRequest req) {
        String hash = deviceTrustService.fingerprintHash(req.getUserAgent(), req.getIp(), null);
        deviceTrustService.register(req.getTenantId(), req.getUserId(), hash, req.getLabel());
        Map<String, String> data = new HashMap<>();
        data.put("deviceHash", hash);
        return responseMapper.ok(data);
    }

    @PostMapping("/device/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDevice(@Valid @RequestBody DeviceTrustDtos.CheckRequest req) {
        String hash = deviceTrustService.fingerprintHash(req.getUserAgent(), req.getIp(), null);
        boolean trusted = deviceTrustService.isTrusted(req.getTenantId(), req.getUserId(), hash);
        Map<String, Object> data = new HashMap<>();
        data.put("trusted", trusted);
        if (!trusted) {
            data.put("challenge", "mfa_required");
        }
        return responseMapper.ok(data);
    }

    private User resolveUser(String tenant, LoginRequest req) {
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            return userRepository.findByTenantIdAndUsername(tenant, req.getUsername()).orElse(null);
        }
        return userRepository.findByTenantIdAndEmail(tenant, req.getEmail()).orElse(null);
    }

    private java.util.List<String> resolveRoleNames(String tenant, User user) {
        final var userRoles = userRoleRepository.findByTenantIdAndUserId(tenant, user.getId());
        final var roleIds = userRoles.stream().map(r -> r.getRoleId()).toList();
        return roleRepository.findAllById(roleIds).stream().map(r -> r.getName()).toList();
    }
}


