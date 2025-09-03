package com.kaua.events.platform.domain.payments;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentStatus {

    NEW,
    PENDING,
    APPROVED,
    IDENTIFIED,
    PAID,
    FAILED,
    EXPIRED,
    CANCELLED;

    public static Optional<PaymentStatus> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
