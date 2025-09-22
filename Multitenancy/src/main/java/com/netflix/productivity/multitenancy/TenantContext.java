package com.netflix.productivity.multitenancy;

import lombok.extern.slf4j.Slf4j;

/**
 * Netflix Production-Grade Tenant Context
 * 
 * This class demonstrates Netflix production standards for tenant context management including:
 * 1. Thread-local tenant storage for request isolation
 * 2. Tenant identification and validation
 * 3. Security and access control per tenant
 * 4. Performance optimization for tenant-specific operations
 * 5. Monitoring and observability per tenant
 * 6. Error handling and fallback mechanisms
 * 7. Caching strategies for tenant-specific data
 * 8. Scalability and resource management
 * 
 * For C/C++ engineers:
 * - Thread-local storage is like thread-specific variables in C++
 * - Tenant context is like a global state manager per request
 * - Context switching is like switching between different workspaces
 * - Thread safety is like mutex protection in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_REQUEST_ID = new ThreadLocal<>();

    /**
     * Set the current tenant for the current thread
     * 
     * @param tenantId Tenant identifier
     */
    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Attempted to set null or empty tenant ID");
            return;
        }
        
        CURRENT_TENANT.set(tenantId);
        log.debug("Set current tenant to: {}", tenantId);
    }

    /**
     * Get the current tenant for the current thread
     * 
     * @return Current tenant identifier
     */
    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        if (tenant == null) {
            log.warn("No tenant context found for current thread");
            return "default";
        }
        return tenant;
    }

    /**
     * Set the current user for the current thread
     * 
     * @param userId User identifier
     */
    public static void setCurrentUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to set null or empty user ID");
            return;
        }
        
        CURRENT_USER.set(userId);
        log.debug("Set current user to: {}", userId);
    }

    /**
     * Get the current user for the current thread
     * 
     * @return Current user identifier
     */
    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * Set the current role for the current thread
     * 
     * @param role Role identifier
     */
    public static void setCurrentRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            log.warn("Attempted to set null or empty role");
            return;
        }
        
        CURRENT_ROLE.set(role);
        log.debug("Set current role to: {}", role);
    }

    /**
     * Get the current role for the current thread
     * 
     * @return Current role identifier
     */
    public static String getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    /**
     * Set the current request ID for the current thread
     * 
     * @param requestId Request identifier
     */
    public static void setCurrentRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            log.warn("Attempted to set null or empty request ID");
            return;
        }
        
        CURRENT_REQUEST_ID.set(requestId);
        log.debug("Set current request ID to: {}", requestId);
    }

    /**
     * Get the current request ID for the current thread
     * 
     * @return Current request identifier
     */
    public static String getCurrentRequestId() {
        return CURRENT_REQUEST_ID.get();
    }

    /**
     * Check if tenant context is set
     * 
     * @return true if tenant context is set
     */
    public static boolean hasTenantContext() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Check if user context is set
     * 
     * @return true if user context is set
     */
    public static boolean hasUserContext() {
        return CURRENT_USER.get() != null;
    }

    /**
     * Check if role context is set
     * 
     * @return true if role context is set
     */
    public static boolean hasRoleContext() {
        return CURRENT_ROLE.get() != null;
    }

    /**
     * Check if request context is set
     * 
     * @return true if request context is set
     */
    public static boolean hasRequestContext() {
        return CURRENT_REQUEST_ID.get() != null;
    }

    /**
     * Clear all context for the current thread
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
        CURRENT_ROLE.remove();
        CURRENT_REQUEST_ID.remove();
        log.debug("Cleared all context for current thread");
    }

    /**
     * Clear tenant context for the current thread
     */
    public static void clearTenant() {
        CURRENT_TENANT.remove();
        log.debug("Cleared tenant context for current thread");
    }

    /**
     * Clear user context for the current thread
     */
    public static void clearUser() {
        CURRENT_USER.remove();
        log.debug("Cleared user context for current thread");
    }

    /**
     * Clear role context for the current thread
     */
    public static void clearRole() {
        CURRENT_ROLE.remove();
        log.debug("Cleared role context for current thread");
    }

    /**
     * Clear request context for the current thread
     */
    public static void clearRequest() {
        CURRENT_REQUEST_ID.remove();
        log.debug("Cleared request context for current thread");
    }

    /**
     * Get a summary of the current context
     * 
     * @return Context summary string
     */
    public static String getContextSummary() {
        return String.format("TenantContext{tenant='%s', user='%s', role='%s', request='%s'}", 
                getCurrentTenant(), 
                getCurrentUser(), 
                getCurrentRole(), 
                getCurrentRequestId());
    }

    /**
     * Validate tenant context
     * 
     * @return true if tenant context is valid
     */
    public static boolean validateContext() {
        if (!hasTenantContext()) {
            log.error("Tenant context is required but not set");
            return false;
        }
        
        String tenant = getCurrentTenant();
        if (tenant.equals("default")) {
            log.warn("Using default tenant context");
        }
        
        return true;
    }

    /**
     * Set context from tenant information
     * 
     * @param tenantId Tenant identifier
     * @param userId User identifier
     * @param role Role identifier
     * @param requestId Request identifier
     */
    public static void setContext(String tenantId, String userId, String role, String requestId) {
        setCurrentTenant(tenantId);
        setCurrentUser(userId);
        setCurrentRole(role);
        setCurrentRequestId(requestId);
        log.debug("Set complete context: {}", getContextSummary());
    }

    /**
     * Get tenant-specific cache key
     * 
     * @param baseKey Base cache key
     * @return Tenant-specific cache key
     */
    public static String getTenantCacheKey(String baseKey) {
        String tenant = getCurrentTenant();
        return String.format("%s:tenant:%s", baseKey, tenant);
    }

    /**
     * Get user-specific cache key
     * 
     * @param baseKey Base cache key
     * @return User-specific cache key
     */
    public static String getUserCacheKey(String baseKey) {
        String user = getCurrentUser();
        String tenant = getCurrentTenant();
        return String.format("%s:tenant:%s:user:%s", baseKey, tenant, user);
    }

    /**
     * Check if current user has admin role
     * 
     * @return true if user has admin role
     */
    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Check if current user has manager role
     * 
     * @return true if user has manager role
     */
    public static boolean isManager() {
        String role = getCurrentRole();
        return "MANAGER".equalsIgnoreCase(role) || isAdmin();
    }

    /**
     * Check if current user has user role
     * 
     * @return true if user has user role
     */
    public static boolean isUser() {
        String role = getCurrentRole();
        return "USER".equalsIgnoreCase(role) || isManager();
    }
}
