package com.backend.designpatterns.structural.adapter;

// Response Mapper
public class StripeResponseMapper {

    public static PaymentResponse map(StripeResponse res) {

        return new PaymentResponse(
                res.getId(),
                res.getAmount(),
                res.getCurrency(),
                mapStatus(res.getStatus())
        );
    }

    private static PaymentStatus mapStatus(String status) {

        return switch (status.toLowerCase()) {

            case "succeeded" -> PaymentStatus.SUCCESS;
            case "pending" -> PaymentStatus.PENDING;
            case "failed" -> PaymentStatus.FAILED;

            default -> PaymentStatus.UNKNOWN;
        };
    }
}
