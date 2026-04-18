package com.backend.designpatterns.structural.facade;

/**
 * Step 2: SUBSYSTEM (Inventory)
 */
public class Step02_InventoryService {
    
    public boolean checkStock(String productId, int quantity) {
        System.out.println("[InventoryService] Checking stock for product: " + productId);
        return !productId.equals("OUT_OF_STOCK_ITEM");
    }

    public void reserveStock(String productId, int quantity) {
        System.out.println("[InventoryService] Reserving " + quantity + " units of " + productId);
    }

    public void releaseStock(String productId, int quantity) {
        System.out.println("[InventoryService] ⚠️ Releasing reserved stock for " + productId);
    }
}
