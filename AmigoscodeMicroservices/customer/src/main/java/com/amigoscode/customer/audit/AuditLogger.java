package com.amigoscode.customer.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogger {

    @Value("${app.security.audit.enabled:true}")
    private boolean auditEnabled;

    @Value("${app.security.audit.include-sensitive-data:false}")
    private boolean includeSensitiveData;

    @Before("execution(* com.amigoscode.customer.CustomerController.*(..))")
    public void auditBeforeControllerMethod(JoinPoint joinPoint) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("CONTROLLER_ACCESS", joinPoint);
        event.setAction("ACCESS_ATTEMPT");
        logAuditEvent(event);
    }

    @AfterReturning("execution(* com.amigoscode.customer.CustomerController.*(..))")
    public void auditAfterControllerMethod(JoinPoint joinPoint, Object result) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("CONTROLLER_ACCESS", joinPoint);
        event.setAction("ACCESS_SUCCESS");
        event.setResult("SUCCESS");
        logAuditEvent(event);
    }

    @AfterThrowing(pointcut = "execution(* com.amigoscode.customer.CustomerController.*(..))", throwing = "ex")
    public void auditAfterControllerMethodThrowing(JoinPoint joinPoint, Exception ex) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("CONTROLLER_ACCESS", joinPoint);
        event.setAction("ACCESS_FAILURE");
        event.setResult("FAILURE");
        event.setErrorMessage(ex.getMessage());
        logAuditEvent(event);
    }

    @Before("execution(* com.amigoscode.customer.CustomerService.*(..))")
    public void auditBeforeServiceMethod(JoinPoint joinPoint) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("SERVICE_OPERATION", joinPoint);
        event.setAction("SERVICE_CALL");
        logAuditEvent(event);
    }

    @AfterReturning("execution(* com.amigoscode.customer.CustomerService.*(..))")
    public void auditAfterServiceMethod(JoinPoint joinPoint, Object result) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("SERVICE_OPERATION", joinPoint);
        event.setAction("SERVICE_SUCCESS");
        event.setResult("SUCCESS");
        logAuditEvent(event);
    }

    @AfterThrowing(pointcut = "execution(* com.amigoscode.customer.CustomerService.*(..))", throwing = "ex")
    public void auditAfterServiceMethodThrowing(JoinPoint joinPoint, Exception ex) {
        if (!auditEnabled) return;

        AuditEvent event = createAuditEvent("SERVICE_OPERATION", joinPoint);
        event.setAction("SERVICE_FAILURE");
        event.setResult("FAILURE");
        event.setErrorMessage(ex.getMessage());
        logAuditEvent(event);
    }

    private AuditEvent createAuditEvent(String eventType, JoinPoint joinPoint) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(Instant.now());
        event.setEventType(eventType);
        event.setClassName(joinPoint.getTarget().getClass().getSimpleName());
        event.setMethodName(joinPoint.getSignature().getName());

        // Get current user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            event.setUserId(getUserIdFromAuthentication(authentication));
            event.setUsername(authentication.getName());
            event.setRoles(authentication.getAuthorities().toString());
        }

        // Get request information
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            event.setIpAddress(getClientIpAddress(request));
            event.setUserAgent(request.getHeader("User-Agent"));
            event.setRequestUri(request.getRequestURI());
            event.setHttpMethod(request.getMethod());
        }

        // Add method parameters (excluding sensitive data)
        if (includeSensitiveData) {
            event.setParameters(Arrays.toString(joinPoint.getArgs()));
        } else {
            event.setParameters("[REDACTED]");
        }

        return event;
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return authentication.getName();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private void logAuditEvent(AuditEvent event) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("auditId", event.getId());
        logData.put("timestamp", event.getTimestamp());
        logData.put("eventType", event.getEventType());
        logData.put("action", event.getAction());
        logData.put("result", event.getResult());
        logData.put("userId", event.getUserId());
        logData.put("username", event.getUsername());
        logData.put("roles", event.getRoles());
        logData.put("ipAddress", event.getIpAddress());
        logData.put("userAgent", event.getUserAgent());
        logData.put("requestUri", event.getRequestUri());
        logData.put("httpMethod", event.getHttpMethod());
        logData.put("className", event.getClassName());
        logData.put("methodName", event.getMethodName());
        logData.put("parameters", event.getParameters());

        if (event.getErrorMessage() != null) {
            logData.put("errorMessage", event.getErrorMessage());
        }

        log.info("AUDIT_EVENT: {}", logData);
    }

    public void logSecurityEvent(String eventType, String action, String details) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(Instant.now());
        event.setEventType(eventType);
        event.setAction(action);
        event.setDetails(details);

        // Get current user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            event.setUserId(getUserIdFromAuthentication(authentication));
            event.setUsername(authentication.getName());
        }

        logAuditEvent(event);
    }

    public static class AuditEvent {
        private String id;
        private Instant timestamp;
        private String eventType;
        private String action;
        private String result;
        private String userId;
        private String username;
        private String roles;
        private String ipAddress;
        private String userAgent;
        private String requestUri;
        private String httpMethod;
        private String className;
        private String methodName;
        private String parameters;
        private String errorMessage;
        private String details;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRoles() { return roles; }
        public void setRoles(String roles) { this.roles = roles; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getRequestUri() { return requestUri; }
        public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = this.httpMethod; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }

        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
