package com.backend.designpatterns.creational.prototype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PROTOTYPE REGISTRY
 * 
 * Instead of creating templates from scratch every time (which is expensive),
 * the registry holds 'Master' instances. We just clone the master.
 */
public final class TemplateRegistry {
    
    private static final Map<String, ReportTemplate> REGISTRY = new ConcurrentHashMap<>();

    static {
        // Pre-populate with typical templates
        List<ReportSection> annualSections = new ArrayList<>();
        annualSections.add(new ReportSection("Financials", "Q1-Q4 summary..."));
        annualSections.add(new ReportSection("Strategy", "Next year vision..."));
        
        REGISTRY.put("ANNUAL_REPORT", new ReportTemplate("Annual Company Report", annualSections));

        List<ReportSection> monthlySections = new ArrayList<>();
        monthlySections.add(new ReportSection("KPIs", "Monthly key performance..."));
        
        REGISTRY.put("MONTHLY_SALES", new ReportTemplate("Monthly Sales Summary", monthlySections));
    }

    /**
     * Finds a master template and returns a CLONE of it.
     */
    public static ReportTemplate getTemplate(String key) {
        ReportTemplate master = REGISTRY.get(key);
        if (master == null) {
            throw new IllegalArgumentException("Unknown template key: " + key);
        }
        
        // Return a copy so the master instance in the registry remains pristine.
        return master.copy();
    }
}
