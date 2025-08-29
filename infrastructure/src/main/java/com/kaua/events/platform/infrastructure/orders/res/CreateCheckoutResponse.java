package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutOutput;

public record CreateCheckoutResponse(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("pix") String qrCodeUrl,
        @JsonProperty("qr_code_image_url") String qrCodeImageUrl
) {

    public static CreateCheckoutResponse from(final CreateCheckoutOutput aOutput) {
        return new CreateCheckoutResponse(
                aOutput.getOrderId(),
                aOutput.getPaymentMethod(),
                aOutput.getQrCodeUrl().orElse(null),
                aOutput.getQrCodeImageUrl().orElse(null)
        );
    }
}
