package com.backend.designpatterns.structural.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Step 4: DYNAMIC INFRASTRUCTURE PROXY
 */
public class Step04_InfrastructureProxy implements InvocationHandler {

    private final Object target;

    private Step04_InfrastructureProxy(Object target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> interfaceType) {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{interfaceType},
            new Step04_InfrastructureProxy(target)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // [AOP ASPECT: LOGGING & METRICS]
        String methodName = method.getName();
        String params = (args == null) ? "none" : Arrays.toString(args);
        
        System.out.println("\n[PROXY] Entering: " + method.getDeclaringClass().getSimpleName() + "::" + methodName);
        System.out.println("[PROXY] Params: " + params);

        Instant start = Instant.now();
        Object result;
        
        try {
            // [DELEGATION]: Execute the actual logic
            result = method.invoke(target, args);
        } catch (Exception e) {
            // [AOP ASPECT: ERROR REPORTING]
            System.err.println("[PROXY] ERROR in " + methodName + ": " + e.getCause().getMessage());
            throw e.getCause(); // Re-throw the original exception
        } finally {
            Instant end = Instant.now();
            long latency = Duration.between(start, end).toMillis();
            System.out.println("[PROXY] Latency: " + latency + "ms");
            System.out.println("[PROXY] Exiting: " + methodName);
        }

        return result;
    }
}
