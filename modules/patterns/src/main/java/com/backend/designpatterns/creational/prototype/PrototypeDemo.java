package com.backend.designpatterns.creational.prototype;

/**
 * THE PROTOTYPE DEMO
 * 
 * Demonstrates how to create new objects by copying existing ones.
 * 
 * Key takeaways:
 * 1. Deep Copying: We prove that modifying a clone does NOT affect the original "Master" template.
 * 2. Registry Usage: We fetch a pre-configured report template by name and clone it.
 * 3. Efficiency: We avoid complex initialization logic by simply copying a finished object.
 */

public class PrototypeDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Prototype Pattern Demo (Deep Copy Registry) ===");

        // Fetch from Registry
        Step03_ReportTemplate annualReport = Step04_TemplateRegistry.getTemplate("ANNUAL_REPORT");
        
        // Clone
        Step03_ReportTemplate instance1 = annualReport.copy();
        
        // Deep Copy Test
        System.out.println("\n[Testing Deep Copy Integrity...]");
        
        Step02_ReportSection firstSectionOfInstance1 = instance1.getSections().get(0);
        firstSectionOfInstance1.setTitle("MODIFIED TITLE");

        Step03_ReportTemplate instance2 = Step04_TemplateRegistry.getTemplate("ANNUAL_REPORT");
        String titleOfInstance2 = instance2.getSections().get(0).getTitle();

        if (!firstSectionOfInstance1.getTitle().equals(titleOfInstance2)) {
            System.out.println("✅ SUCCESS: Deep copy confirmed.");
        }

        // Identity Check
        System.out.println("Same Object? " + (annualReport == instance1));
    }
}
