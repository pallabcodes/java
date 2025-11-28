package com.amigoscode.customer.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Custom permission evaluator for fine-grained access control.
 * Implements both Role-Based Access Control (RBAC) and Attribute-Based Access Control (ABAC).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();
        return checkPermission(authentication, permissionString, null, null);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();
        return checkPermission(authentication, permissionString, targetId, targetType);
    }

    /**
     * Check if the authentication has the required permission.
     */
    public boolean hasPermission(Authentication authentication, String[] requiredPermissions,
                                String resourceExpression, String action, Object[] methodArgs) {
        if (authentication == null || requiredPermissions == null || requiredPermissions.length == 0) {
            return false;
        }

        // Check if user has any of the required permissions
        boolean hasPermission = false;
        for (String permission : requiredPermissions) {
            if (checkPermission(authentication, permission, null, null)) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            log.warn("Access denied for user {}: missing required permissions {}",
                    authentication.getName(), requiredPermissions);
            return false;
        }

        // If resource expression is provided, evaluate ABAC
        if (resourceExpression != null && !resourceExpression.isEmpty()) {
            return evaluateAttributeBasedAccess(authentication, resourceExpression, action, methodArgs);
        }

        return true;
    }

    private boolean checkPermission(Authentication authentication, String permission, Serializable targetId, String targetType) {
        // Check direct role-based permissions
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authorityName = authority.getAuthority();

            // Check for ROLE_ prefixed authorities
            if (authorityName.startsWith("ROLE_")) {
                String role = authorityName.substring(5); // Remove ROLE_ prefix
                if (matchesPermission(role, permission)) {
                    return true;
                }
            }

            // Check for direct permission match
            if (matchesPermission(authorityName, permission)) {
                return true;
            }
        }

        // Check JWT claims for additional permissions
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return checkJwtClaimsForPermission(jwt, permission);
        }

        return false;
    }

    private boolean matchesPermission(String authority, String requiredPermission) {
        // Exact match
        if (authority.equalsIgnoreCase(requiredPermission)) {
            return true;
        }

        // Wildcard matching (e.g., "user.*" matches "user.read", "user.write")
        if (requiredPermission.contains("*")) {
            String regex = requiredPermission.replace("*", ".*");
            return authority.matches(regex);
        }

        // Hierarchical permission checking (e.g., "admin" has "user" permissions)
        if ("ADMIN".equalsIgnoreCase(authority)) {
            return true; // Admin has all permissions
        }

        return false;
    }

    private boolean checkJwtClaimsForPermission(Jwt jwt, String permission) {
        // Check realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object roles = realmAccess.get("roles");
            if (roles instanceof Collection) {
                for (Object role : (Collection<?>) roles) {
                    if (role instanceof String && matchesPermission((String) role, permission)) {
                        return true;
                    }
                }
            }
        }

        // Check resource_access claims
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object resource : resourceAccess.values()) {
                if (resource instanceof Map) {
                    Object roles = ((Map<?, ?>) resource).get("roles");
                    if (roles instanceof Collection) {
                        for (Object role : (Collection<?>) roles) {
                            if (role instanceof String && matchesPermission((String) role, permission)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean evaluateAttributeBasedAccess(Authentication authentication, String resourceExpression,
                                               String action, Object[] methodArgs) {
        try {
            // Create evaluation context
            StandardEvaluationContext context = new StandardEvaluationContext();

            // Add authentication object
            context.setVariable("authentication", authentication);

            // Add method arguments as variables (arg0, arg1, etc.)
            if (methodArgs != null) {
                for (int i = 0; i < methodArgs.length; i++) {
                    context.setVariable("arg" + i, methodArgs[i]);
                }
            }

            // Add principal (user) information
            context.setVariable("principal", authentication.getPrincipal());
            context.setVariable("username", authentication.getName());

            // Evaluate the resource expression
            Object result = expressionParser.parseExpression(resourceExpression).getValue(context);

            // For now, we assume the expression returns a boolean
            // In a real implementation, this might involve more complex logic
            if (result instanceof Boolean) {
                return (Boolean) result;
            }

            // If expression doesn't return boolean, assume access granted if result is not null
            return result != null;

        } catch (Exception e) {
            log.error("Error evaluating ABAC expression: {}", resourceExpression, e);
            return false; // Fail-safe: deny access on evaluation error
        }
    }
}
