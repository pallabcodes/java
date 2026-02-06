package com.backend.designpatterns.structural.adapter;

// Response Mapper
public class PayPalResponseMapper {

    public static PaymentResponse map(PayPalResponse res) {

        return new PaymentResponse(
                res.getTransactionId(),
                res.getValue(),
                res.getCurrencyCode(),
                mapStatus(res.getState())
        );
    }

    private static PaymentStatus mapStatus(String state) {
        return switch (state.toLowerCase()) {

            case "approved" -> PaymentStatus.SUCCESS;
            case "created" -> PaymentStatus.PENDING;
            case "denied" -> PaymentStatus.FAILED;

            default -> PaymentStatus.UNKNOWN;
        };
    }
}
