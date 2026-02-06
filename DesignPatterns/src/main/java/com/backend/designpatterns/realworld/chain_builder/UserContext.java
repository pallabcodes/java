package com.backend.designpatterns.realworld.chain_builder;

// Role: Request Object (built via Builder)
public class UserContext {
    private final String id;
    private final String region;
    private final boolean isMobile;
    private final boolean hasPromoCode;
    private final boolean isFlaggedForFraud;

    private UserContext(Builder builder) {
        this.id = builder.id;
        this.region = builder.region;
        this.isMobile = builder.isMobile;
        this.hasPromoCode = builder.hasPromoCode;
        this.isFlaggedForFraud = builder.isFlaggedForFraud;
    }

    public String getId() { return id; }
    public String getRegion() { return region; }
    public boolean isMobile() { return isMobile; }
    public boolean hasPromoCode() { return hasPromoCode; }
    public boolean isFlaggedForFraud() { return isFlaggedForFraud; }

    // Role: Builder
    public static class Builder {
        private final String id;
        private String region = "US";
        private boolean isMobile = false;
        private boolean hasPromoCode = false;
        private boolean isFlaggedForFraud = false;

        public Builder(String id) {
            this.id = id;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder mobile(boolean isMobile) {
            this.isMobile = isMobile;
            return this;
        }

        public Builder promoCode(boolean hasPromoCode) {
            this.hasPromoCode = hasPromoCode;
            return this;
        }

        public Builder flagged(boolean isFlagged) {
            this.isFlaggedForFraud = isFlagged;
            return this;
        }

        public UserContext build() {
            return new UserContext(this);
        }
    }
}
