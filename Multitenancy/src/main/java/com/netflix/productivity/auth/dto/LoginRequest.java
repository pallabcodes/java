package com.netflix.productivity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String tenantId;
    private String username;
    private String email;
    @NotBlank
    private String password;
}


