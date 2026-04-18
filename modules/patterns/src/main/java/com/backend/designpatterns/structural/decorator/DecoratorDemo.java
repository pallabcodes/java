package com.backend.designpatterns.structural.decorator;

import static com.backend.designpatterns.structural.decorator.NotificationHandlers.*;

public class DecoratorDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Decorator Pattern Demo (Functional Pipelines) ===\n");

        // 1. [SCENARIO: FULL AUDIT PIPELINE]
        // We want to Mask secrets, UpperCase it, then send to Email and Slack.
        NotificationPipeline auditPipeline = NotificationPipeline.builder()
                .addTransformer(masked())
                .addTransformer(upperCase())
                .addAction(toLogger())
                .addAction(toEmail())
                .addAction(toSlack())
                .build();

        System.out.println("--- Executing Audit Pipeline ---");
        auditPipeline.send("The secret password is: MySecret123");


        // 2. [SCENARIO: LIGHTWEIGHT SMS]
        // Just send as is to SMS.
        NotificationPipeline smsOnly = NotificationPipeline.builder()
                .addAction(toSms())
                .build();

        System.out.println("\n--- Executing SMS Only Pipeline ---");
        smsOnly.send("Low balance alert!");


        // 3. [L5 RATIONALE: DYNAMIC COMPOSITION]
        // We can create a pipeline conditionally at runtime without new classes.
        boolean isCritical = true;
        var dynamicBuilder = NotificationPipeline.builder().addAction(toLogger());
        
        if (isCritical) {
            dynamicBuilder.addAction(toSlack()).addTransformer(msg -> "!!! CRITICAL: " + msg);
        }

        System.out.println("\n--- Executing Dynamic Pipeline ---");
        dynamicBuilder.send("Database connection lost.");

        System.out.println("\n[L5 ACHIEVEMENT]: Refactored nested decorator hell into a " +
                           "flat, readable functional pipeline.");
    }
}
