package com.backend.designpatterns.structural.adapter;

// Error Mapper
public class StripeErrorMapper {

    static PaymentException map(
            StripeException ex
    ) {

        return switch (ex.code()) {

            case "card_declined" ->
                    new PaymentException(
                            "PAYMENT_DECLINED",
                            ex.getMessage()
                    );

            default ->
                    new PaymentException(
                            "STRIPE_UNKNOWN_ERROR",
                            ex.getMessage()
                    );
        };
    }
}
