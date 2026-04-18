package com.backend.designpatterns.behavioral.template;

/**
 * Step 2: CONCRETE IMPLEMENTATION
 */
public class Step02_GoogleDriveTakeoutProcessor extends Step01_GoogleTakeoutProcessor {

    @Override
    protected String gatherProductData(String userEmail) {
        System.out.println("  [Drive Backend] Traversing user's root folder hierarchy...");
        System.out.println("  [Drive Backend] Streaming binary blobs out of Colossus File System.");
        return "FILESYSTEM_BLOBS_DATA";
    }

    @Override
    protected boolean requiresReauthorization() {
        return true; 
    }
}
