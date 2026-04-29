package com.backend.designpatterns.creational.prototype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Step 4: PROTOTYPE REGISTRY (The Cache)
 * 
 * Stores pre-configured prototypes in a "Master List".
 * 
 * Benefits:
 * 1. Performance: Instead of building complex objects over and over, we build them once at startup.
 * 2. Convenience: Clients can ask for a "Standard" template by name.
 * 3. Isolation: The registry returns a CLONE, so the client can modify their copy 
 *    without ruining the original "Master" version.
 */
public final class Step04_TemplateRegistry {
    
    private static final Map<String, Step03_ReportTemplate> REGISTRY = new ConcurrentHashMap<>();

    static {
        List<Step02_ReportSection> annualSections = new ArrayList<>();
        annualSections.add(new Step02_ReportSection("Financials", "Q1-Q4 summary..."));
        annualSections.add(new Step02_ReportSection("Strategy", "Next year vision..."));
        
        REGISTRY.put("ANNUAL_REPORT", new Step03_ReportTemplate("Annual Company Report", annualSections));

        List<Step02_ReportSection> monthlySections = new ArrayList<>();
        monthlySections.add(new Step02_ReportSection("KPIs", "Monthly key performance..."));
        
        REGISTRY.put("MONTHLY_SALES", new Step03_ReportTemplate("Monthly Sales Summary", monthlySections));
    }

    public static Step03_ReportTemplate getTemplate(String key) {
        Step03_ReportTemplate master = REGISTRY.get(key);
        if (master == null) {
            throw new IllegalArgumentException("Unknown template key: " + key);
        }
        return master.copy();
    }
}
