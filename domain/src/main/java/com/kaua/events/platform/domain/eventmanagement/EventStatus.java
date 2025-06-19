package com.kaua.events.platform.domain.eventmanagement;

import java.util.Arrays;
import java.util.Optional;

public enum EventStatus {

//    DRAFT,
    SCHEDULED,
    STARTED,
    FINISHED,
    DELETED;

    public static Optional<EventStatus> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
