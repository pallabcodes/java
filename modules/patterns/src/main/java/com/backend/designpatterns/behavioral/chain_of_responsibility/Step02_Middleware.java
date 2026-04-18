package com.backend.designpatterns.behavioral.chain_of_responsibility;

/**
 * Step 2: THE CHAIN CONTRACT
 * 
 * Defines how middlewares link together and guarantees the short-circuit logic.
 */
public abstract class Step02_Middleware {
    
    private Step02_Middleware next;

    /**
     * Fluent Builder Pattern for linking chains.
     */
    public Step02_Middleware linkWith(Step02_Middleware next) {
        this.next = next;
        return next;
    }

    /**
     * Subclasses implement their specific logic here.
     */
    public abstract boolean check(Step01_HttpRequest request);

    /**
     * Runs the next check in the chain. 
     */
    protected boolean checkNext(Step01_HttpRequest request) {
        if (next == null) {
            return true;
        }
        return next.check(request);
    }
}
