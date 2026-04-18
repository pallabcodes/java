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
        /**
         * [L5 DEEP COPY LOGIC - Step-by-Step]
         * 
         * 1. THE STREAM: We convert the internal list to a stream of ReportSection instances.
         * 
         * 2. THE MAP (Method Reference): 'ReportSection::copy'
         *    - This is an INSTANCE method reference.
         *    - It is shorthand for: 'section -> section.copy()'
         *    - Java applies this to every section in the stream, triggering its own copy logic.
         * 
         * 3. INDEPENDENCE: Because each .copy() returns a BRAND NEW ReportSection object,
         *    the resulting list (clonedSections) has zero memory-links to the original list.
         * 
         * 4. THE COLLECTION: We collect these brand new objects into a new List.
         */
        List<ReportSection> clonedSections = this.sections.stream()
                .map(ReportSection::copy) 
                .collect(Collectors.toList());

        // 5. THE RESULT: Return a new Template instance with the deep-cloned data.
        return new ReportTemplate(this.templateName, clonedSections);
    }

    @Override
    public String toString() {
        return "ReportTemplate[name='" + templateName + "', sections=" + sections.size() + "]";
    }
}
