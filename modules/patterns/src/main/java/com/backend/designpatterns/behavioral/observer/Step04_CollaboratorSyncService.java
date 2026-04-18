package com.backend.designpatterns.behavioral.observer;

/**
 * Step 4: CONCRETE SUBSCRIBER
 * Pushes the live edit over WebSockets so other users see the cursor move.
 */
public class Step04_CollaboratorSyncService implements Step02_DocumentSubscriber {

    @Override
    public void onDocumentEdit(Step01_DocumentEditEvent event) {
        System.out.println("  [WebSocketSync] Broadcasting keystroke '" + event.textChange() + 
                           "' to all 4 other active users viewing Doc " + event.documentId());
    }
}
