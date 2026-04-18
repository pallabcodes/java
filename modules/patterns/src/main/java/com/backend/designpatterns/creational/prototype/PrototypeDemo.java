package com.backend.designpatterns.creational.prototype;

/**
 * PROTOTYPE PATTERN DEMO
 * 
 * Verifies Deep Copying and Registry-based object creation.
 */
public class PrototypeDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Prototype Pattern Demo (Deep Copy Registry) ===");

        // 1. Fetch a master template from the registry (Registry returns a CLONE)
        System.out.println("\n[1] Fetching master 'ANNUAL_REPORT' from Registry...");
        ReportTemplate annualReport = TemplateRegistry.getTemplate("ANNUAL_REPORT");
        System.out.println("Template fetched: " + annualReport);

        // 2. Clone the template (Double cloning for demonstration)
        System.out.println("\n[2] Cloning the template to create a specific instance...");
        ReportTemplate instance1 = annualReport.copy();
        
        // 3. DO A DEEP COPY TEST
        System.out.println("\n[3] Testing Deep Copy Integrity...");
        
        // Modify a section in the first copy
        ReportSection firstSectionOfInstance1 = instance1.getSections().get(0);
        String originalTitle = firstSectionOfInstance1.getTitle();
        firstSectionOfInstance1.setTitle("MODIFIED TITLE");

        // Fetch another clone from the registry
        ReportTemplate instance2 = TemplateRegistry.getTemplate("ANNUAL_REPORT");
        String titleOfInstance2 = instance2.getSections().get(0).getTitle();

        System.out.println("Instance 1 Section Title: " + firstSectionOfInstance1.getTitle());
        System.out.println("Instance 2 Section Title: " + titleOfInstance2);

        if (!firstSectionOfInstance1.getTitle().equals(titleOfInstance2)) {
            System.out.println("✅ SUCCESS: Deep copy confirmed. Modifying Clone 1 did NOT affect Clone 2.");
        } else {
            System.err.println("❌ FAILURE: Shallow copy detected! Clones are sharing internal section objects.");
        }

        // 4. Memory Identity Check
        System.out.println("\n[4] Identity Check:");
        System.out.println("Annual Report vs Instance 1: Same Object? " + (annualReport == instance1));
        System.out.println("Section 1 in Instance 1 vs Instance 2: Same Object? " + 
                           (instance1.getSections().get(0) == instance2.getSections().get(0)));

        System.out.println("\n[L5 ACHIEVEMENT]: Prototype pattern implemented with type-safe " +
                           "Deep Copying and a centralized Registry.");
    }
}
