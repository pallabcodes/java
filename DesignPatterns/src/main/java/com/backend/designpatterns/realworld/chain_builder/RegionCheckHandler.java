package com.backend.designpatterns.realworld.chain_builder;

public class RegionCheckHandler extends ValidationHandler {
    @Override
    public void validate(UserContext ctx) {
        if ("RESTRICTED".equalsIgnoreCase(ctx.getRegion())) {
            throw new RuntimeException("Validation Failed: Service unavailable in region " + ctx.getRegion());
        }
        System.out.println("Region Check: PASSED (" + ctx.getRegion() + ")");
        if (next != null) next.validate(ctx);
    }
}
