package com.backend.designpatterns.behavioral.template;

/**
 * Step 2: CONCRETE IMPLEMENTATION
 */
public class Step02_GmailTakeoutProcessor extends Step01_GoogleTakeoutProcessor {

    @Override
    protected String gatherProductData(String userEmail) {
        System.out.println("  [Gmail Backend] Querying Spanner for all email threads...");
        System.out.println("  [Gmail Backend] Formatting 15,000 emails into industry-standard MBOX format.");
        return "MBOX_DATA_DUMP_STRING";
    }
}
