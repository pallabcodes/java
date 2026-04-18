package com.backend.designpatterns.creational.prototype;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * THE COMPLEX PROTOTYPE
 * 
 * Demonstrates 'Deep Copying'. If we just copied the List reference, 
 * modifying a section in the clone would modify the original.
 */
public class ReportTemplate implements Prototype<ReportTemplate> {
    private final String templateName;
    private final LocalDateTime lastUpdated;
    private List<ReportSection> sections;

    public ReportTemplate(String templateName, List<ReportSection> sections) {
        this.templateName = templateName;
        this.sections = sections;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addSection(ReportSection section) {
        this.sections.add(section);
    }

    public List<ReportSection> getSections() {
        return sections;
    }

    @Override
    public ReportTemplate copy() {
        // [L5 DEEP COPY LOGIC]
        // 1. Create a brand new list
        // 2. Iterate through original sections and call .copy() on each one
        List<ReportSection> clonedSections = this.sections.stream()
                .map(ReportSection::copy)
                .collect(Collectors.toList());

        // 3. Construct a new template with the cloned data
        return new ReportTemplate(this.templateName, clonedSections);
    }

    @Override
    public String toString() {
        return "ReportTemplate[name='" + templateName + "', sections=" + sections.size() + "]";
    }
}
