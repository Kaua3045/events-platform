package com.kaua.events.platform.domain.eventmanagement;

import java.util.Arrays;
import java.util.Optional;

public enum EventType {

    REMOTE,
    IN_PERSON;

    public static Optional<EventType> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
