package com.kaua.events.platform.application.usecases.orders.create;

import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.payments.PaymentDetails;

import java.util.Optional;

public class CreateCheckoutOutput {

    private final String orderId;
    private final String paymentMethod;
    private final PaymentDetails paymentDetails;

    public CreateCheckoutOutput(
            final String orderId,
            final String paymentMethod,
            final PaymentDetails paymentDetails
    ) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails;
    }

    public static CreateCheckoutOutput from(
            final Order aOrder,
            final String paymentMethod,
            final PaymentDetails paymentDetails
    ) {
        return new CreateCheckoutOutput(aOrder.getId().value().toString(), paymentMethod, paymentDetails);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }
}
