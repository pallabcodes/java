package com.backend.designpatterns.behavioral.chain_of_responsibility;

/**
 * Step 5: CONCRETE LINK
 * 
 * Simply logs the request *after* it has successfully passed all security checks.
 */
public class Step05_LoggingMiddleware extends Step02_Middleware {

    @Override
    public boolean check(Step01_HttpRequest request) {
        System.out.println("[Middleware: Logger] Request to '" + request.endpoint() + 
                           "' authenticated globally. Proceeding to Controller.\n");
        return checkNext(request);
    }
}
