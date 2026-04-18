package com.backend.designpatterns.structural.decorator;

/**
 * PRODUCTION-GRADE HANDLERS
 * 
 * Instead of classes, we provide reusable functional pieces.
 */
public final class NotificationHandlers {

    private NotificationHandlers() {}

    // --- TRANSFORMERS ---

    public static java.util.function.UnaryOperator<String> upperCase() {
        return String::toUpperCase;
    }

    public static java.util.function.UnaryOperator<String> masked() {
        return msg -> msg.replaceAll("(?i)password|secret", "****");
    }

    // --- ACTIONS ---

    public static NotificationAction toEmail() {
        return msg -> System.out.println("[EMAIL] Sent: " + msg);
    }

    public static NotificationAction toSlack() {
        return msg -> System.out.println("[SLACK] #alerts: " + msg);
    }

    public static NotificationAction toSms() {
        return msg -> System.out.println("[SMS] +1-555: " + msg);
    }

    public static NotificationAction toLogger() {
        return msg -> System.out.println("[LOG] Notification Audit: " + msg);
    }
}
