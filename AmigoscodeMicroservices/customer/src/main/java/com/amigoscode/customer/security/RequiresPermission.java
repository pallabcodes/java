package com.amigoscode.customer.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for fine-grained permission-based access control.
 * This implements Attribute-Based Access Control (ABAC) by checking specific permissions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * The required permission(s). User must have at least one of these permissions.
     */
    String[] value();

    /**
     * Optional resource identifier for ABAC evaluation.
     * Can be a SpEL expression that will be evaluated against method parameters.
     */
    String resource() default "";

    /**
     * Optional action being performed on the resource.
     */
    String action() default "";

    /**
     * Whether to check tenant ownership for multi-tenant scenarios.
     */
    boolean checkTenantOwnership() default false;
}
