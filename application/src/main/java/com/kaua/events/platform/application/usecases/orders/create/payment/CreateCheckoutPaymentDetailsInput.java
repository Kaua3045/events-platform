package com.kaua.events.platform.application.usecases.orders.create.payment;

import com.kaua.events.platform.domain.payments.PaymentMethod;

public sealed interface CreateCheckoutPaymentDetailsInput permits CreateCheckoutCreditCardPaymentDetails, CreateCheckoutPixPaymentDetails {

    PaymentMethod method();
}
