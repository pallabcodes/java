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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public AuthenticationController(UserRepository userRepository, UserRoleRepository userRoleRepository,
                                    RoleRepository roleRepository,
                                    PasswordService passwordService, JwtTokenService jwtTokenService,
                                    ResponseMapper responseMapper,
                                    PasswordResetService passwordResetService,
                                    RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.responseMapper = responseMapper;
        this.passwordResetService = passwordResetService;
        this.refreshTokenService = refreshTokenService;
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
        return responseMapper.ok(new LoginResponse(token, "Bearer", ACCESS_TOKEN_TTL_MS));
    }

    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetRequest(@Valid @RequestBody ResetRequest req) {
        String token = passwordResetService.issueResetToken(req.getTenantId(), req.getEmail(), java.time.Duration.ofMinutes(15));
        Map<String, String> data = new HashMap<>();
        data.put("resetToken", token);
        return responseMapper.ok(data);
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<ApiResponse<Void>> resetConfirm(@Valid @RequestBody ResetConfirm req) {
        passwordResetService.consumeResetToken(req.getTenantId(), req.getToken(), req.getNewPassword());
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
        return responseMapper.ok(data);
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revoke(@Valid @RequestBody RefreshRequest req) {
        refreshTokenService.revoke(req.getTenantId(), req.getRefreshToken());
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
        return responseMapper.noContent();
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


