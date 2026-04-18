package com.backend.designpatterns.realworld.template_strategy;

// Concrete Class implementing primitive operations
public class FinancialReport extends ReportTemplate {

    @Override
    protected String collectData() {
        System.out.println("Step 1: Collecting Financial Data from DB...");
        return "Financial Figures: Q1 Revenue $1M";
    }
}
