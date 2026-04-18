package com.backend.designpatterns.realworld.template_strategy;

public class PdfExportStrategy implements ExportStrategy {
    @Override
    public void export(String data) {
        System.out.println("Step 3: Exporting data to PDF -> " + data);
    }
}
