package com.kaua.events.platform.application.usecases.orders.create.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

public record CreateCheckoutCreditCardPaymentDetails(
        String userId,
        String paymentToken,
        int installments
) implements CreateCheckoutPaymentDetailsInput {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }
}
