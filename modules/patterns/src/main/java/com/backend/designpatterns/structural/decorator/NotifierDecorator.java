package com.backend.designpatterns.structural.decorator;

// Role: Base Decorator
public abstract class NotifierDecorator implements Notifier {
    
    protected Notifier wrappee;

    public NotifierDecorator(Notifier notifier) {
        this.wrappee = notifier;
    }

    @Override
    public void send(String message) {
        wrappee.send(message);
    }
}
