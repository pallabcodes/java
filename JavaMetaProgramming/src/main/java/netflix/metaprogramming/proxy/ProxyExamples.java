package netflix.metaprogramming.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netflix Production-Grade Dynamic Proxy Examples
 * 
 * This class demonstrates comprehensive dynamic proxy concepts including:
 * - JDK Dynamic Proxies
 * - CGLIB Proxies
 * - Aspect-Oriented Programming (AOP)
 * - Method Interception and Advice
 * - Proxy Caching and Performance Optimization
 * - Security and Access Control
 * - Error Handling and Exception Management
 * - Custom Proxy Implementations
 * - Netflix-specific Proxy Patterns
 * - Integration with Spring Framework
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class ProxyExamples {

    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    /**
     * Demonstrates JDK Dynamic Proxies
     * 
     * Shows how to create and use JDK dynamic proxies for interface-based proxying.
     */
    public void demonstrateJdkDynamicProxies() {
        log.info("=== Demonstrating JDK Dynamic Proxies ===");
        
        try {
            // Create a simple service interface
            ServiceInterface service = new ServiceImpl();
            
            // Create JDK dynamic proxy
            ServiceInterface proxy = (ServiceInterface) Proxy.newProxyInstance(
                ServiceInterface.class.getClassLoader(),
                new Class[]{ServiceInterface.class},
                new ServiceInvocationHandler(service)
            );
            
            // Use the proxy
            String result = proxy.processData("Hello World");
            log.debug("Proxy result: {}", result);
            
            // Test method with parameters
            int sum = proxy.add(5, 3);
            log.debug("Proxy add result: {}", sum);
            
            // Test method with exception
            try {
                proxy.throwException();
            } catch (RuntimeException e) {
                log.debug("Proxy exception caught: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in JDK dynamic proxy demonstration", e);
        }
    }

    /**
     * Demonstrates CGLIB Proxies
     * 
     * Shows how to create and use CGLIB proxies for class-based proxying.
     */
    public void demonstrateCglibProxies() {
        log.info("=== Demonstrating CGLIB Proxies ===");
        
        try {
            // Create CGLIB proxy
            ServiceClass serviceClass = createCglibProxy(ServiceClass.class);
            
            // Use the proxy
            String result = serviceClass.processData("Hello CGLIB");
            log.debug("CGLIB proxy result: {}", result);
            
            // Test method with parameters
            int sum = serviceClass.add(10, 20);
            log.debug("CGLIB proxy add result: {}", sum);
            
        } catch (Exception e) {
            log.error("Error in CGLIB proxy demonstration", e);
        }
    }

    /**
     * Demonstrates Aspect-Oriented Programming (AOP)
     * 
     * Shows how to implement AOP patterns using dynamic proxies.
     */
    public void demonstrateAspectOrientedProgramming() {
        log.info("=== Demonstrating Aspect-Oriented Programming ===");
        
        try {
            // Create service with logging aspect
            ServiceInterface loggingService = createLoggingProxy(new ServiceImpl());
            
            // Create service with caching aspect
            ServiceInterface cachingService = createCachingProxy(new ServiceImpl());
            
            // Create service with security aspect
            ServiceInterface securityService = createSecurityProxy(new ServiceImpl());
            
            // Test logging aspect
            loggingService.processData("Test logging");
            
            // Test caching aspect
            cachingService.processData("Test caching");
            cachingService.processData("Test caching"); // Should use cache
            
            // Test security aspect
            securityService.processData("Test security");
            
        } catch (Exception e) {
            log.error("Error in AOP demonstration", e);
        }
    }

    /**
     * Demonstrates method interception and advice
     * 
     * Shows how to intercept method calls and apply advice.
     */
    public void demonstrateMethodInterception() {
        log.info("=== Demonstrating Method Interception ===");
        
        try {
            // Create service with method interception
            ServiceInterface interceptedService = createInterceptedProxy(new ServiceImpl());
            
            // Test method interception
            interceptedService.processData("Test interception");
            
            // Test method with parameters
            int result = interceptedService.add(15, 25);
            log.debug("Intercepted add result: {}", result);
            
        } catch (Exception e) {
            log.error("Error in method interception demonstration", e);
        }
    }

    /**
     * Demonstrates proxy caching and performance optimization
     * 
     * Shows how to cache proxies for better performance.
     */
    @Cacheable(value = "proxy-cache", key = "#interfaceClass.name")
    public <T> T getCachedProxy(Class<T> interfaceClass, T target) {
        log.debug("Creating cached proxy for: {}", interfaceClass.getName());
        
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class[]{interfaceClass},
            new ServiceInvocationHandler(target)
        );
    }

    /**
     * Demonstrates security and access control
     * 
     * Shows how to implement security in proxy patterns.
     */
    public void demonstrateSecurityAndAccessControl() {
        log.info("=== Demonstrating Security and Access Control ===");
        
        try {
            // Create service with security proxy
            ServiceInterface secureService = createSecurityProxy(new ServiceImpl());
            
            // Test with valid access
            secureService.processData("Valid access");
            
            // Test with invalid access (would be blocked by security proxy)
            // This is a simplified example - real security would check permissions
            
        } catch (Exception e) {
            log.error("Error in security demonstration", e);
        }
    }

    /**
     * Demonstrates error handling and exception management
     * 
     * Shows how to handle exceptions in proxy patterns.
     */
    public void demonstrateErrorHandling() {
        log.info("=== Demonstrating Error Handling ===");
        
        try {
            // Create service with error handling proxy
            ServiceInterface errorHandlingService = createErrorHandlingProxy(new ServiceImpl());
            
            // Test normal operation
            errorHandlingService.processData("Normal operation");
            
            // Test operation that throws exception
            try {
                errorHandlingService.throwException();
            } catch (RuntimeException e) {
                log.debug("Exception handled by proxy: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in error handling demonstration", e);
        }
    }

    /**
     * Demonstrates custom proxy implementations
     * 
     * Shows how to create custom proxy implementations.
     */
    public void demonstrateCustomProxyImplementations() {
        log.info("=== Demonstrating Custom Proxy Implementations ===");
        
        try {
            // Create custom proxy
            ServiceInterface customProxy = createCustomProxy(new ServiceImpl());
            
            // Test custom proxy
            customProxy.processData("Custom proxy test");
            
        } catch (Exception e) {
            log.error("Error in custom proxy demonstration", e);
        }
    }

    /**
     * Demonstrates Netflix-specific proxy patterns
     * 
     * Shows how to implement Netflix-specific proxy patterns.
     */
    public void demonstrateNetflixProxyPatterns() {
        log.info("=== Demonstrating Netflix Proxy Patterns ===");
        
        try {
            // Create Netflix service proxy
            ServiceInterface netflixProxy = createNetflixProxy(new ServiceImpl());
            
            // Test Netflix proxy
            netflixProxy.processData("Netflix proxy test");
            
        } catch (Exception e) {
            log.error("Error in Netflix proxy demonstration", e);
        }
    }

    // Helper methods

    private ServiceClass createCglibProxy(Class<ServiceClass> clazz) {
        // This is a simplified example - real CGLIB would use Enhancer
        return new ServiceClass() {
            @Override
            public String processData(String data) {
                log.debug("CGLIB proxy before method call");
                String result = super.processData(data);
                log.debug("CGLIB proxy after method call");
                return result;
            }
        };
    }

    private ServiceInterface createLoggingProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new LoggingInvocationHandler(target)
        );
    }

    private ServiceInterface createCachingProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new CachingInvocationHandler(target)
        );
    }

    private ServiceInterface createSecurityProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new SecurityInvocationHandler(target)
        );
    }

    private ServiceInterface createInterceptedProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new InterceptedInvocationHandler(target)
        );
    }

    private ServiceInterface createErrorHandlingProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new ErrorHandlingInvocationHandler(target)
        );
    }

    private ServiceInterface createCustomProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new CustomInvocationHandler(target)
        );
    }

    private ServiceInterface createNetflixProxy(ServiceInterface target) {
        return (ServiceInterface) Proxy.newProxyInstance(
            ServiceInterface.class.getClassLoader(),
            new Class[]{ServiceInterface.class},
            new NetflixInvocationHandler(target)
        );
    }

    // Invocation handlers

    public static class ServiceInvocationHandler implements InvocationHandler {
        private final Object target;

        public ServiceInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.debug("Service proxy before method call: {}", method.getName());
            
            try {
                Object result = method.invoke(target, args);
                log.debug("Service proxy after method call: {}", method.getName());
                return result;
            } catch (Exception e) {
                log.error("Service proxy error in method call: {}", method.getName(), e);
                throw e;
            }
        }
    }

    public static class LoggingInvocationHandler implements InvocationHandler {
        private final Object target;

        public LoggingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTime = System.currentTimeMillis();
            log.debug("Logging proxy - Method: {}, Args: {}", method.getName(), Arrays.toString(args));
            
            try {
                Object result = method.invoke(target, args);
                long duration = System.currentTimeMillis() - startTime;
                log.debug("Logging proxy - Method: {} completed in {}ms, Result: {}", 
                         method.getName(), duration, result);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Logging proxy - Method: {} failed after {}ms", method.getName(), duration, e);
                throw e;
            }
        }
    }

    public static class CachingInvocationHandler implements InvocationHandler {
        private final Object target;
        private final Map<String, Object> cache = new ConcurrentHashMap<>();

        public CachingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String cacheKey = method.getName() + Arrays.toString(args);
            
            if (cache.containsKey(cacheKey)) {
                log.debug("Caching proxy - Cache hit for method: {}", method.getName());
                return cache.get(cacheKey);
            }
            
            log.debug("Caching proxy - Cache miss for method: {}", method.getName());
            Object result = method.invoke(target, args);
            cache.put(cacheKey, result);
            
            return result;
        }
    }

    public static class SecurityInvocationHandler implements InvocationHandler {
        private final Object target;

        public SecurityInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.debug("Security proxy - Checking access for method: {}", method.getName());
            
            // Simulate security check
            if (method.getName().equals("throwException")) {
                log.debug("Security proxy - Access denied for method: {}", method.getName());
                throw new SecurityException("Access denied");
            }
            
            log.debug("Security proxy - Access granted for method: {}", method.getName());
            return method.invoke(target, args);
        }
    }

    public static class InterceptedInvocationHandler implements InvocationHandler {
        private final Object target;

        public InterceptedInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.debug("Intercepted proxy - Before method: {}", method.getName());
            
            // Pre-processing
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof String) {
                        args[i] = ((String) args[i]).toUpperCase();
                    }
                }
            }
            
            Object result = method.invoke(target, args);
            
            // Post-processing
            if (result instanceof String) {
                result = "Processed: " + result;
            }
            
            log.debug("Intercepted proxy - After method: {}", method.getName());
            return result;
        }
    }

    public static class ErrorHandlingInvocationHandler implements InvocationHandler {
        private final Object target;

        public ErrorHandlingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                log.error("Error handling proxy - Method: {} failed with: {}", 
                         method.getName(), cause.getMessage());
                
                // Handle specific exceptions
                if (cause instanceof RuntimeException) {
                    return "Error handled: " + cause.getMessage();
                }
                
                throw cause;
            }
        }
    }

    public static class CustomInvocationHandler implements InvocationHandler {
        private final Object target;

        public CustomInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.debug("Custom proxy - Custom logic for method: {}", method.getName());
            
            // Custom logic here
            if (method.getName().equals("processData")) {
                args[0] = "Custom processed: " + args[0];
            }
            
            return method.invoke(target, args);
        }
    }

    public static class NetflixInvocationHandler implements InvocationHandler {
        private final Object target;

        public NetflixInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            log.debug("Netflix proxy - Netflix-specific logic for method: {}", method.getName());
            
            // Netflix-specific logic
            long startTime = System.currentTimeMillis();
            
            try {
                Object result = method.invoke(target, args);
                long duration = System.currentTimeMillis() - startTime;
                
                // Netflix monitoring
                log.debug("Netflix proxy - Method: {} completed in {}ms", method.getName(), duration);
                
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Netflix proxy - Method: {} failed after {}ms", method.getName(), duration, e);
                throw e;
            }
        }
    }

    // Sample interfaces and classes

    public interface ServiceInterface {
        String processData(String data);
        int add(int a, int b);
        void throwException();
    }

    public static class ServiceImpl implements ServiceInterface {
        @Override
        public String processData(String data) {
            log.debug("Processing data: {}", data);
            return "Processed: " + data;
        }

        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public void throwException() {
            throw new RuntimeException("Test exception");
        }
    }

    public static class ServiceClass {
        public String processData(String data) {
            log.debug("ServiceClass processing data: {}", data);
            return "ServiceClass processed: " + data;
        }

        public int add(int a, int b) {
            return a + b;
        }
    }
}
