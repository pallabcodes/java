package com.backend.designpatterns.behavioral.observer;

/**
 * Step 4: CONCRETE SUBSCRIBER
 * Captures edits and writes them to the database to power "Version History".
 */
public class Step04_RevisionHistorySpannerService implements Step02_DocumentSubscriber {

    @Override
    public void onDocumentEdit(Step01_DocumentEditEvent event) {
        System.out.println("  [SpannerDB] Saving diff delta from " + event.collaboratorEmail() + " to permanent storage.");
    }
}
