package com.backend.designpatterns.behavioral.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Step 3: THE PUBLISHER (Subject)
 * 
 * Manages the registry of Subscriptions dynamically.
 */
public class Step03_DocumentSubject {

    private final List<Step02_DocumentSubscriber> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(Step02_DocumentSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Step02_DocumentSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Broadcasts the keystroke/edit to all connected listeners.
     */
    public void publishEdit(Step01_DocumentEditEvent event) {
        System.out.println("\n[DocEngine] 📢 Keystroke Registered at index " + event.cursorPosition() + ": '" + event.textChange() + "'");
        
        for (Step02_DocumentSubscriber subscriber : subscribers) {
            subscriber.onDocumentEdit(event);
        }
    }
}
