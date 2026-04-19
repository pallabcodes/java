package com.backend.designpatterns.behavioral.observer;

import java.time.Instant;

public class GoogleDocsDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Observer Demo (Google Docs Collaboration) ===\n");

        Step03_DocumentSubject documentSubject = new Step03_DocumentSubject();

        // Register core decoupled services using the Step02 interface
        Step02_DocumentSubscriber spannerDb = new Step04_RevisionHistorySpannerService();
        Step02_DocumentSubscriber syncLayer = new Step04_CollaboratorSyncService();
        Step02_DocumentSubscriber aiGrammar = new Step04_GrammarAssistService();

        documentSubject.subscribe(spannerDb);
        documentSubject.subscribe(syncLayer);
        documentSubject.subscribe(aiGrammar);

        System.out.println("--- User Types 'Hello' ---");
        // User types 'H'
        documentSubject.publishEdit(new Step01_DocumentEditEvent("DOC-123", "alice@gmail.com", "H", 0, Instant.now()));

        System.out.println("\n--- Performance Scenario: Turning off Grammar AI ---");
        // To save CPU on large docs, user turns off grammar check.
        // We dynamically unsubscribe it without shutting down the document thread!
        documentSubject.unsubscribe(aiGrammar);
        
        // User types 'e'
        documentSubject.publishEdit(new Step01_DocumentEditEvent("DOC-123", "alice@gmail.com", "e", 1, Instant.now()));

        System.out.println("\n[L5 ACHIEVEMENT]: The core text editor is totally agnostic to the backend. " +
                           "It broadcasts keystrokes blindly, allowing N disconnected microservices to react instantly.");
    }
}
