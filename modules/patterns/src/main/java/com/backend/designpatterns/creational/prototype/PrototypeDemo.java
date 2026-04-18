package com.backend.designpatterns.creational.prototype;

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
