package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutOutput;
import com.kaua.events.platform.domain.payments.PaymentDetails;

public record CreateCheckoutResponse(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("payment_details") PaymentDetails paymentDetails
) {

    public static CreateCheckoutResponse from(final CreateCheckoutOutput aOutput) {
        return new CreateCheckoutResponse(
                aOutput.getOrderId(),
                aOutput.getPaymentMethod(),
                aOutput.getPaymentDetails()
        );
    }
}
