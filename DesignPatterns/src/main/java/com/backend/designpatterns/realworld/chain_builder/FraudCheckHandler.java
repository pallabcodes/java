package com.backend.designpatterns.realworld.chain_builder;

public class FraudCheckHandler extends ValidationHandler {
    @Override
    public void validate(UserContext ctx) {
        if (ctx.isFlaggedForFraud()) {
            throw new RuntimeException("Validation Failed: User is flagged for fraud.");
        }
        System.out.println("Fraud Check: PASSED");
        if (next != null) next.validate(ctx);
    }
}
