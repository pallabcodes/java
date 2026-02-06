package com.backend.designpatterns.structural.adapter;

// Error Mapper
public class PayPalErrorMapper {

    static PaymentException map(
            PayPalException ex
    ) {

        return switch (ex.code()) {

            case "payment_denied" ->
                    new PaymentException(
                            "PAYMENT_DECLINED",
                            ex.getMessage()
                    );

            default ->
                    new PaymentException(
                            "PAYPAL_UNKNOWN_ERROR",
                            ex.getMessage()
                    );
        };
    }
}