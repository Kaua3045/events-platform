package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;

public sealed interface PaymentDetails permits CreditCardPaymentDetails, PixPaymentDetails {

    PaymentMethod method();
    BigDecimal amount();
}
