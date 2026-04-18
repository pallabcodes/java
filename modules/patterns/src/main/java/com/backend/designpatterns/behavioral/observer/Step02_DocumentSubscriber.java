package com.backend.designpatterns.behavioral.observer;

/**
 * Step 2: THE SUBSCRIBER CONTRACT
 * 
 * Implement this interface to listen for typing events published by the Document Editor.
 */
public interface Step02_DocumentSubscriber {
    
    /**
     * Called by the Publisher when a new keystroke/edit occurs.
     */
    void onDocumentEdit(Step01_DocumentEditEvent event);
}
