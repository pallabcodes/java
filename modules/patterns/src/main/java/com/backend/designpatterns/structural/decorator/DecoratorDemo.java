package com.backend.designpatterns.structural.decorator;

import static com.backend.designpatterns.structural.decorator.Step06_NotificationHandlers.*;

public class DecoratorDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Decorator Pattern Demo (Functional Pipelines) ===\n");

        Step07_NotificationPipeline auditPipeline = Step07_NotificationPipeline.builder()
                .addTransformer(masked())
                .addTransformer(upperCase())
                .addAction(toLogger())
                .addAction(toEmail())
                .addAction(toSlack())
                .build();

        System.out.println("--- Executing Audit Pipeline ---");
        auditPipeline.send("The secret password is: MySecret123");


        Step07_NotificationPipeline smsOnly = Step07_NotificationPipeline.builder()
                .addAction(toSms())
                .build();

        System.out.println("\n--- Executing SMS Only Pipeline ---");
        smsOnly.send("Low balance alert!");


        boolean isCritical = true;
        var dynamicBuilder = Step07_NotificationPipeline.builder().addAction(toLogger());
        
        if (isCritical) {
            dynamicBuilder.addAction(toSlack()).addTransformer(msg -> "!!! CRITICAL: " + msg);
        }

        System.out.println("\n--- Executing Dynamic Pipeline ---");
        dynamicBuilder.send("Database connection lost.");
    }
}
