package com.netflix.systemdesign.logging;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal HTTP exporter placeholder for shipping logs to a central endpoint.
 * Replace with agent side shipping in production (Fluent Bit, Vector, etc.).
 */
public class LogHttpExporter {
    private final String endpoint;

    public LogHttpExporter(String endpoint) {
        this.endpoint = endpoint;
    }

    public void export(String payload) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(bytes);
            conn.getOutputStream().flush();
            conn.getInputStream().close();
            conn.disconnect();
        } catch (Exception ignored) {
        }
    }
}


