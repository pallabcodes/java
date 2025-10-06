package com.netflix.productivity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpaMethodInterceptor implements MethodInterceptor {

    private final OpaPolicyClient opaPolicyClient;

    @Value("${app.opa.service-enabled:false}")
    private boolean serviceEnabled;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!serviceEnabled) return invocation.proceed();
        try {
            String methodName = invocation.getMethod().getName();
            String className = invocation.getThis() != null ? invocation.getThis().getClass().getName() : "unknown";

            Map<String, Object> input = new HashMap<>();
            input.put("target", className);
            input.put("method", methodName);
            input.put("args", invocation.getArguments());

            boolean allow = opaPolicyClient.allow(input);
            if (!allow) {
                throw new SecurityException("OPA denied method execution");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("OPA method enforcement error", e);
            throw new SecurityException("OPA error");
        }
        return invocation.proceed();
    }
}
