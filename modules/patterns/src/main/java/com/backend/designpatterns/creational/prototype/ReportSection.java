package com.backend.designpatterns.creational.prototype;

/**
 * A sub-component of our complex template.
 * Must also implement Prototype to support deep copying.
 */
public class ReportSection implements Prototype<ReportSection> {
    private String title;
    private String content;

    public ReportSection(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Setters for demonstrating that modifications to clones don't affect originals
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    
    public String getTitle() { return title; }

    @Override
    public ReportSection copy() {
        // Deep copy of a simple object is just a new instantiation with same values.
        return new ReportSection(this.title, this.content);
    }

    @Override
    public String toString() {
        return "Section[title='" + title + "']";
    }
}
