package com.backend.designpatterns.structural.composite;

/**
 * Step 3: LEAF NODE (Department)
 */
public class Step03_DepartmentPolicy implements Step02_AccessPolicy {

    private final String requiredDepartment;

    public Step03_DepartmentPolicy(String requiredDepartment) {
        this.requiredDepartment = requiredDepartment;
    }

    @Override
    public boolean isSatisfiedBy(Step01_UserContext context) {
        boolean match = requiredDepartment.equals(context.department());
        System.out.println("  [Leaf] DepartmentPolicy (" + requiredDepartment + ") -> " + match);
        return match;
    }
}
