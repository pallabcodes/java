package com.backend.metaprogramming;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Step 02: Dynamic Proxies (Aspect-Oriented Programming)
 * 
 * L7 Principles:
 * 1. Decoupling: Wrapping cross-cutting concerns (metrics, security) separated from business logic.
 * 2. Runtime Behavior: Generating code (Proxies) at runtime to intercepts calls.
 * 3. Interface-based: JDK Dynamic Proxies requires interfaces; CGLIB doesn't (we focus on JDK).
 */
public class Step02_DynamicProxies {

    public interface OrderService {
        void processOrder(String id);
    }

    public static class OrderServiceImpl implements OrderService {
        @Override
        public void processOrder(String id) {
            System.out.println("Executing processOrder for: " + id);
        }
    }

    /**
     * L7 Mastery: A metrics handler that records execution time for ANY interface.
     */
    public static class MetricsHandler implements InvocationHandler {
        private final Object target;

        public MetricsHandler(Object target) { this.target = target; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long start = System.nanoTime();
            
            Object result = method.invoke(target, args);
            
            long duration = System.nanoTime() - start;
            System.out.println("[Metrics] Method " + method.getName() + " took " + duration + " ns");
            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 02: Dynamic Proxies (Pure Java AOP) ===");

        OrderService realService = new OrderServiceImpl();
        
        OrderService proxyService = (OrderService) Proxy.newProxyInstance(
                OrderService.class.getClassLoader(),
                new Class[]{OrderService.class},
                new MetricsHandler(realService)
        );

        proxyService.processOrder("ORDER-123");
        
        System.out.println("\nL5 Insight: This is how Spring @Transactional works under the hood.");
    }
}
