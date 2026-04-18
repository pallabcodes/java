package com.backend.designpatterns.structural.decorator;

import java.util.function.UnaryOperator;

/**
 * Step 6: HANDLER REGISTRY
 */
public final class Step06_NotificationHandlers {

    private Step06_NotificationHandlers() {}

    public static UnaryOperator<String> upperCase() {
        return String::toUpperCase;
    }

    public static UnaryOperator<String> masked() {
        return msg -> msg.replaceAll("(?i)password|secret", "****");
    }

    public static Step05_NotificationAction toEmail() {
        return msg -> System.out.println("[EMAIL] Sent: " + msg);
    }

    public static Step05_NotificationAction toSlack() {
        return msg -> System.out.println("[SLACK] #alerts: " + msg);
    }

    public static Step05_NotificationAction toSms() {
        return msg -> System.out.println("[SMS] +1-555: " + msg);
    }

    public static Step05_NotificationAction toLogger() {
        return msg -> System.out.println("[LOG] Notification Audit: " + msg);
    }
}
