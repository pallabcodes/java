package com.backend.designpatterns.realworld.template_strategy;

public class ReportDemo {

    public static void main(String[] args) {
        // The Report Structure is fixed (Template)
        // Data collection is specific to FinancialReport (Inheritance)
        ReportTemplate report = new FinancialReport();

        // The Export format is pluggable (Strategy)
        System.out.println("Client wants PDF:");
        report.generateReport(new PdfExportStrategy());

        System.out.println("\nClient wants CSV:");
        report.generateReport(new CsvExportStrategy());
    }
}
