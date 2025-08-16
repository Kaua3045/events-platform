package com.kaua.events.platform.domain.payments;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentMethod {

    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    BOLETO;

    public static Optional<PaymentMethod> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
