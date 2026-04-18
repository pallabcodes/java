package com.backend.designpatterns.structural.facade;

/**
 * Step 3: THE FACADE (Orchestrator)
 */
public class Step03_OrderFulfillmentFacade {

    private final Step02_InventoryService inventory;
    private final Step02_PaymentService payment;
    private final Step02_ShippingService shipping;
    private final Step02_NotificationService notification;

    public Step03_OrderFulfillmentFacade() {
        this.inventory = new Step02_InventoryService();
        this.payment = new Step02_PaymentService();
        this.shipping = new Step02_ShippingService();
        this.notification = new Step02_NotificationService();
    }

    public Step01_CheckoutResult checkout(Step01_CheckoutRequest request) {
        System.out.println("========== [FACADE: ORCHESTRATION START] ==========");

        if (!inventory.checkStock(request.productId(), request.quantity())) {
            return new Step01_CheckoutResult(false, "Item is out of stock.", null);
        }

        inventory.reserveStock(request.productId(), request.quantity());

        boolean paymentSuccess = payment.chargeCreditCard(request.creditCardNumber(), request.totalAmount());
        
        if (!paymentSuccess) {
            inventory.releaseStock(request.productId(), request.quantity());
            notification.sendPaymentFailedAlert(request.email());
            return new Step01_CheckoutResult(false, "Payment declined. Reservation released.", null);
        }

        String trackingCode = shipping.dispatchOrder(request.shippingAddress());

        String orderSummary = "Your order for " + request.quantity() + "x " + request.productId() + 
                              " has been shipped! Tracking: " + trackingCode;
        notification.sendOrderConfirmation(request.email(), orderSummary);

        System.out.println("========== [FACADE: ORCHESTRATION COMPLETE] ==========\n");
        return new Step01_CheckoutResult(true, "Checkout successful", trackingCode);
    }
}
