package com.backend.designpatterns.realworld.template_strategy;

// Template Method Pattern
public abstract class ReportTemplate {

    // Template method defining the algorithm structure
    public final void generateReport(ExportStrategy exportStrategy) {
        System.out.println("--- Starting Report Generation ---");
        
        String data = collectData(); // Primitive operation (to be implemented by subclasses)
        String formattedData = processData(data); // Common operation
        
        // Delegating to Strategy
        exportStrategy.export(formattedData);
        
        System.out.println("--- Report Completed ---");
    }

    protected abstract String collectData();

    private String processData(String data) {
        return "Processed: [" + data + "]";
    }
}
