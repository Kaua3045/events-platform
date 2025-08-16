package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;

public final class PixPaymentDetails implements PaymentDetails {

    private final BigDecimal amount;

    public PixPaymentDetails(final BigDecimal aAmount) {
        this.amount = aAmount;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.PIX;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }
}
