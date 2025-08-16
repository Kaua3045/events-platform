package com.kaua.events.platform.application.usecases.orders.create.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

public final class CreateCheckoutCreditCardPaymentDetails implements CreateCheckoutPaymentDetailsInput {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }
}
