package com.backend.designpatterns.behavioral.template;

/**
 * Step 1: THE TEMPLATE CLASS
 * 
 * Defines the strict, unchangeable algorithm skeleton for a Google Takeout export.
 */
public abstract class Step01_GoogleTakeoutProcessor {

    /**
     * THE TEMPLATE METHOD
     */
    public final void exportData(String userEmail) {
        System.out.println("\n[Takeout Engine] Initiating export for: " + userEmail);

        // 1. Mandatory Identity verification
        authenticateAccount(userEmail);

        // 2. Extensible Hook: Ask for user consent if needed
        if (requiresReauthorization()) {
            prompt2FA(userEmail);
        }

        // 3. PRODUCT-SPECIFIC LOGIC (Abstract)
        String rawData = gatherProductData(userEmail);

        // 4. Mandatory compression and encryption
        byte[] archive = compressAndEncrypt(rawData);

        // 5. Mandatory notification
        notifyUserReady(userEmail, archive);

        System.out.println("[Takeout Engine] Export lifecycle complete for " + userEmail);
    }

    // --- Extensible Abstract Methods ---

    protected abstract String gatherProductData(String userEmail);

    // --- Concrete Standardized Methods ---

    private void authenticateAccount(String userEmail) {
        System.out.println("  [GaiaAuth] Verifying OAuth token and active sessions for " + userEmail);
    }

    private byte[] compressAndEncrypt(String data) {
        System.out.println("  [ZopfliCompressor] Compressing " + data.length() + " bytes into a secure encrypted ZIP archive.");
        return new byte[1024]; // simulated binary output
    }

    private void notifyUserReady(String userEmail, byte[] archive) {
        System.out.println("  [Mailer] Sending secure download link to " + userEmail);
    }

    // --- Hook Methods ---

    protected boolean requiresReauthorization() {
        return false;
    }

    private void prompt2FA(String userEmail) {
        System.out.println("  [Security] Triggering Google Prompt 2FA on trusted device for " + userEmail);
    }
}
