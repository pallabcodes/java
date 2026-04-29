package com.backend.designpatterns.creational.prototype;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 3: THE COMPLEX PROTOTYPE (Deep Copying)
 * 
 * A full report template containing multiple sections.
 * 
 * Why Deep Copying?
 * If we just copied the list reference, changing a section in the clone 
 * would also change it in the original.
 * 
 * Solution: When we clone the template, we also call .copy() on every 
 * individual section inside it.
 */
public class Step03_ReportTemplate implements Step01_Prototype<Step03_ReportTemplate> {
    private final String templateName;
    @SuppressWarnings("unused")
    private final LocalDateTime lastUpdated;
    private List<Step02_ReportSection> sections;

    public Step03_ReportTemplate(String templateName, List<Step02_ReportSection> sections) {
        this.templateName = templateName;
        this.sections = sections;
        this.lastUpdated = LocalDateTime.now();
    }

    public List<Step02_ReportSection> getSections() {
        return sections;
    }

    @Override
    public Step03_ReportTemplate copy() {
        List<Step02_ReportSection> clonedSections = this.sections.stream()
                .map(Step02_ReportSection::copy) 
                .collect(Collectors.toList());

        return new Step03_ReportTemplate(this.templateName, clonedSections);
    }

    @Override
    public String toString() {
        return "ReportTemplate[name='" + templateName + "', sections=" + sections.size() + "]";
    }
}
