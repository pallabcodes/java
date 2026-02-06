package com.backend.designpatterns.realworld.legacy_wrapper;

// Adapter
public class LegacyAdapter implements ModernService {
    private final LegacySystem legacySystem;

    public LegacyAdapter(LegacySystem legacySystem) {
        this.legacySystem = legacySystem;
    }

    @Override
    public String getData(String id) {
        String xml = legacySystem.fetchXML(id);
        return convertToJson(xml);
    }

    private String convertToJson(String xml) {
        // Simple manual conversion simulation
        String id = xml.substring(xml.indexOf("<id>") + 4, xml.indexOf("</id>"));
        String value = xml.substring(xml.indexOf("<value>") + 7, xml.indexOf("</value>"));
        return "{ \"id\": \"" + id + "\", \"value\": \"" + value + "\" }";
    }
}
