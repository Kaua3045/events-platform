package com.kaua.events.platform.application.gateways.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

import java.math.BigDecimal;

public record PaymentCreditCardPaymentDetailsRequest(
        BigDecimal amount,
        String name,
        String documentNumber,
        String documentType,
        String email,
        String phoneNumber,
        String paymentToken,
        int installments
) implements PaymentDetailsRequest {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }
}
