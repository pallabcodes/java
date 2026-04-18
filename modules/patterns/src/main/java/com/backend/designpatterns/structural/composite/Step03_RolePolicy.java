package com.backend.designpatterns.structural.composite;

/**
 * Step 3: LEAF NODE (Role)
 */
public class Step03_RolePolicy implements Step02_AccessPolicy {
    
    private final String requiredRole;

    public Step03_RolePolicy(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean isSatisfiedBy(Step01_UserContext context) {
        boolean match = requiredRole.equals(context.role());
        System.out.println("  [Leaf] RolePolicy (" + requiredRole + ") -> " + match);
        return match;
    }
}
