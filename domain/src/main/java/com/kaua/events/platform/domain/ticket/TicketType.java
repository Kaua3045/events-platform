package com.kaua.events.platform.domain.ticket;

import java.util.Arrays;
import java.util.Optional;

public enum TicketType {

    STANDARD,
    VIP,
    STUDENT,
    PROMOTIONAL;

    public static Optional<TicketType> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
