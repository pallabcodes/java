package com.backend.designpatterns.structural.facade;

import java.util.UUID;

/**
 * Step 2: SUBSYSTEM (Shipping)
 */
public class Step02_ShippingService {

    public String dispatchOrder(String address) {
        System.out.println("[ShippingService] Requesting courier dispatch to: " + address);
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[ShippingService] 🚚 Order Dispatched! Tracking: " + trackingNumber);
        return trackingNumber;
    }
}
