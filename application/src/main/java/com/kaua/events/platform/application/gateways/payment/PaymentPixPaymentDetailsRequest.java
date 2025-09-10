package com.kaua.events.platform.application.gateways.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

import java.math.BigDecimal;

public record PaymentPixPaymentDetailsRequest(
        BigDecimal amount
) implements PaymentDetailsRequest {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.PIX;
    }
}
