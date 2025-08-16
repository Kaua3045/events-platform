package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutOutput;

public record CreateCheckoutResponse(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("qr_code_url") String qrCodeUrl
) {

    public static CreateCheckoutResponse from(final CreateCheckoutOutput aOutput) {
        return new CreateCheckoutResponse(
                aOutput.getOrderId(),
                aOutput.getPaymentMethod(),
                aOutput.getQrCodeUrl().orElse(null)
        );
    }
}
