package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.domain.orders.Order;

import java.util.Optional;

public class CreateCheckoutOutput {

    private final String orderId;
    private final String paymentMethod;
    private final String qrCodeUrl;

    public CreateCheckoutOutput(
            final String orderId,
            final String paymentMethod,
            final String qrCodeUrl
    ) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.qrCodeUrl = qrCodeUrl;
    }

    public static CreateCheckoutOutput from(
            final Order aOrder,
            final String paymentMethod,
            final String qrCodeUrl
    ) {
        return new CreateCheckoutOutput(aOrder.getId().value().toString(), paymentMethod, qrCodeUrl);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Optional<String> getQrCodeUrl() {
        return Optional.ofNullable(qrCodeUrl);
    }
}
