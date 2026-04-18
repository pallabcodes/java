package com.backend.designpatterns.structural.facade;

/**
 * Step 2: SUBSYSTEM (Notifications)
 */
public class Step02_NotificationService {
    
    public void sendOrderConfirmation(String email, String orderDetails) {
        System.out.println("[NotificationService] 📧 Sending confirmation email to " + email);
        System.out.println("  -> Content: " + orderDetails);
    }

    public void sendPaymentFailedAlert(String email) {
        System.out.println("[NotificationService] ⚠️ Sending payment failure alert to " + email);
    }
}
