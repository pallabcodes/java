package com.backend.designpatterns.behavioral.chain_of_responsibility;

/**
 * Step 4: CONCRETE LINK
 * 
 * Verifies that the request contains a valid JWT/Bearer token.
 */
public class Step04_AuthMiddleware extends Step02_Middleware {

    @Override
    public boolean check(Step01_HttpRequest request) {
        System.out.println("[Middleware: Auth] Validating Bearer Token...");

        if (request.bearerToken() == null || !request.bearerToken().startsWith("Bearer ")) {
            System.err.println("  ❌ 401 UNAUTHORIZED: Missing or invalid token format.");
            return false;
        }

        if (request.bearerToken().equals("Bearer EXPIRED_TOKEN")) {
            System.err.println("  ❌ 403 FORBIDDEN: Token is expired.");
            return false;
        }

        System.out.println("  ✅ Token is valid.");
        return checkNext(request);
    }
}
