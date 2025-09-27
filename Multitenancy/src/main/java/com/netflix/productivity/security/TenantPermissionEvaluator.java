package com.netflix.productivity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

@RequiredArgsConstructor
@Slf4j
public class TenantPermissionEvaluator implements PermissionEvaluator {
    
    private final SimplifiedSecurityConfig.TenantSecurityService tenantSecurityService;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject == null || permission == null) {
            return false;
        }
        
        String permissionStr = permission.toString();
        String targetId = targetDomainObject.toString();
        
        try {
            switch (permissionStr) {
                case "PROJECT_READ":
                case "PROJECT_WRITE":
                    return tenantSecurityService.hasAccessToProject(targetId);
                case "ISSUE_READ":
                case "ISSUE_WRITE":
                    return tenantSecurityService.hasAccessToIssue(targetId);
                case "TENANT_ADMIN":
                    return tenantSecurityService.hasRole("TENANT_ADMIN");
                case "TENANT_USER":
                    return tenantSecurityService.hasRole("TENANT_USER");
                default:
                    log.warn("Unknown permission: {}", permissionStr);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating permission {} for target {}", permissionStr, targetId, e);
            return false;
        }
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (targetId == null || permission == null) {
            return false;
        }
        
        String permissionStr = permission.toString();
        
        try {
            switch (targetType) {
                case "PROJECT":
                    return tenantSecurityService.hasAccessToProject(targetId.toString());
                case "ISSUE":
                    return tenantSecurityService.hasAccessToIssue(targetId.toString());
                case "TENANT":
                    return tenantSecurityService.belongsToTenant(targetId.toString(), authentication.getName());
                default:
                    log.warn("Unknown target type: {}", targetType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating permission {} for target type {} with id {}", 
                    permissionStr, targetType, targetId, e);
            return false;
        }
    }
}
