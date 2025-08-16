package com.kaua.events.platform.domain.orders;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {

    CREATED,
    PAID,
    CANCELED,
    FAILED,
    EXPIRED,
    REFUNDED;

    public static Optional<OrderStatus> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
