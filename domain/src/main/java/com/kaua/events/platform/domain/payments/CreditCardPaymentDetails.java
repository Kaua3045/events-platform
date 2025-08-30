package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;

public record CreditCardPaymentDetails(BigDecimal amount, String name, String cpf, String email,
                                       String paymentToken, int installments) implements PaymentDetails {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CREDIT_CARD;
    }
}
