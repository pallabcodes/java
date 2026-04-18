package com.backend.designpatterns.creational.prototype;

/**
 * Step 2: HIERARCHICAL COMPONENT
 */
public class Step02_ReportSection implements Step01_Prototype<Step02_ReportSection> {
    private String title;
    private String content;

    public Step02_ReportSection(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public String getTitle() { return title; }

    @Override
    public Step02_ReportSection copy() {
        return new Step02_ReportSection(this.title, this.content);
    }

    @Override
    public String toString() {
        return "Section[title='" + title + "']";
    }
}
