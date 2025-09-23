package com.netflix.productivity.security;

import com.netflix.productivity.multitenancy.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(req) && execution(* *(..))")
    public Object checkPermission(ProceedingJoinPoint pjp, RequirePermission req) throws Throwable {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser au)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_401_000");
        }
        // Tenant must match resolved context
        if (au.getTenantId() == null || !au.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AUTH_403_TENANT_MISMATCH");
        }
        List<String> roles = au.getRoles();
        String needed = req.value();
        // Simple mapping: ADMIN allows everything; else require explicit role match like ISSUE_WRITE
        if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("TENANT_ADMIN") || r.equalsIgnoreCase(needed))) {
            return pjp.proceed();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AUTH_403_NO_PERMISSION:" + needed);
    }
}


