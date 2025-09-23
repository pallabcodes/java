package com.netflix.productivity.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AuthUser implements UserDetails {
    private final String userId;
    private final String tenantId;
    private final List<String> roles;

    public AuthUser(String userId, String tenantId, List<String> roles) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.roles = roles;
    }

    public String getUserId() { return userId; }
    public String getTenantId() { return tenantId; }
    public List<String> getRoles() { return roles; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return userId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}


