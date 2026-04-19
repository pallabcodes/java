package com.backend.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Step 03: Metaprogramming & Reflection
 * 
 * L5/L7 Principles:
 * 1. Introspection: Analyzing code structure at runtime.
 * 2. Framework Building: Using annotations to drive behavior (like Spring/Dagger).
 * 3. Dynamic Invocation: Decoupling logic from specific classes.
 */
public class Step03_Metaprogramming {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AuditLog { String operation(); }

    public static class AdminService {
        @AuditLog(operation = "DELETE_USER")
        public void deleteUser(String id) {
            System.out.println("Deleting user: " + id);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 03: Metaprogramming (Reflection Context) ===");

        AdminService service = new AdminService();
        Method[] methods = service.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(AuditLog.class)) {
                AuditLog audit = method.getAnnotation(AuditLog.class);
                System.out.println("Found Auditable Method: " + method.getName());
                System.out.println("Operation Label: " + audit.operation());
                
                // Dynamic invocation
                method.invoke(service, "user-404");
            }
        }
    }
}
