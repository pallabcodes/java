package com.backend.designpatterns.structural.composite;

public class CompositeDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Composite Pattern Demo (Dynamic Policy Engine) ===\n");

        // 1. Constructing Tree
        Step02_AccessPolicy adminPolicy = new Step03_RolePolicy("ADMIN");
        
        Step02_AccessPolicy standardItPolicy = new Step04_AllOfPolicy()
            .add(new Step03_DepartmentPolicy("IT"))
            .add(new Step03_TimeOfDayPolicy(9, 17));

        Step02_AccessPolicy globalSecurityPolicy = new Step04_AnyOfPolicy()
            .add(adminPolicy)
            .add(standardItPolicy);

        // 2. Simulating Users
        Step01_UserContext standardDev = new Step01_UserContext("jdoe", "USER", "IT", 14);
        Step01_UserContext lateNightDev = new Step01_UserContext("jsmith", "USER", "IT", 23);
        Step01_UserContext adminUser = new Step01_UserContext("superuser", "ADMIN", "HR", 2);
        
        // 3. Evaluation
        System.out.println("--- Testing Standard Dev (2 PM) ---");
        boolean result1 = globalSecurityPolicy.isSatisfiedBy(standardDev);
        System.out.println("FINAL RESULT: " + result1 + "\n");

        System.out.println("--- Testing Late Night Dev (11 PM) ---");
        boolean result2 = globalSecurityPolicy.isSatisfiedBy(lateNightDev);
        System.out.println("FINAL RESULT: " + result2 + "\n");

        System.out.println("--- Testing Super Admin (2 AM) ---");
        boolean result3 = globalSecurityPolicy.isSatisfiedBy(adminUser);
        System.out.println("FINAL RESULT: " + result3 + "\n");
    }
}
