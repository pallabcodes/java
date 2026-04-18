package com.backend.designpatterns.behavioral.observer;

/**
 * Step 4: CONCRETE SUBSCRIBER
 * Triggers background AI to check for typos.
 */
public class Step04_GrammarAssistService implements Step02_DocumentSubscriber {

    @Override
    public void onDocumentEdit(Step01_DocumentEditEvent event) {
        System.out.println("  [GrammarAI] Analyzing text change around cursor index " + event.cursorPosition() + " for spelling errors.");
    }
}
