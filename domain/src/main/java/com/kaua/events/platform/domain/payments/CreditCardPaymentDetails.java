package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;

public final class CreditCardPaymentDetails implements PaymentDetails {

    private final BigDecimal amount;

    public CreditCardPaymentDetails(final BigDecimal aAmount) {
        this.amount = aAmount;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }
}
