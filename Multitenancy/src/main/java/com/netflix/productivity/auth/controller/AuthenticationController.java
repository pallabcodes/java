package com.netflix.productivity.auth.controller;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import com.netflix.productivity.auth.dto.LoginRequest;
import com.netflix.productivity.auth.dto.LoginResponse;
import com.netflix.productivity.auth.entity.User;
import com.netflix.productivity.auth.repository.UserRepository;
import com.netflix.productivity.auth.repository.UserRoleRepository;
import com.netflix.productivity.auth.repository.RoleRepository;
import com.netflix.productivity.auth.service.JwtTokenService;
import com.netflix.productivity.auth.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final ResponseMapper responseMapper;

    public AuthenticationController(UserRepository userRepository, UserRoleRepository userRoleRepository,
                                    RoleRepository roleRepository,
                                    PasswordService passwordService, JwtTokenService jwtTokenService,
                                    ResponseMapper responseMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        String tenant = req.getTenantId();
        User user = (req.getUsername() != null && !req.getUsername().isBlank())
                ? userRepository.findByTenantIdAndUsername(tenant, req.getUsername()).orElse(null)
                : userRepository.findByTenantIdAndEmail(tenant, req.getEmail()).orElse(null);
        if (user == null || !user.isEnabled() || user.isLocked() || !passwordService.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid credentials", null, "AUTH_401_001"));
        }
        var userRoles = userRoleRepository.findByTenantIdAndUserId(tenant, user.getId());
        var roleIds = userRoles.stream().map(r -> r.getRoleId()).toList();
        var names = roleRepository.findAllById(roleIds).stream().map(r -> r.getName()).toList();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", names);
        String token = jwtTokenService.issue(user.getId(), tenant, user.getTokenVersion(), claims);
        return responseMapper.ok(new LoginResponse(token, "Bearer", 3600000));
    }
}


