package com.kaua.events.platform.application.gateways.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

import java.math.BigDecimal;

public sealed interface PaymentDetailsRequest permits PaymentCreditCardPaymentDetailsRequest, PaymentPixPaymentDetailsRequest {

    BigDecimal amount();
    PaymentMethod method();
}
