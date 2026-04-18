package com.backend.designpatterns.structural.decorator;

/**
 * Step 3: BASE DECORATOR
 */
public abstract class Step03_NotifierDecorator implements Step01_Notifier {
    
    protected Step01_Notifier wrappee;

    public Step03_NotifierDecorator(Step01_Notifier notifier) {
        this.wrappee = notifier;
    }

    @Override
    public void send(String message) {
        wrappee.send(message);
    }
}
