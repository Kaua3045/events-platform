package com.kaua.events.platform.domain.ticket;

import java.util.Arrays;
import java.util.Optional;

public enum TicketStatus {

    AVAILABLE,
    INACTIVE,
    SOLD_OUT;

    public static Optional<TicketStatus> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
