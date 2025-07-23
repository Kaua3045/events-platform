package com.kaua.events.platform.application.usecases.eventmanagement.update;

import java.time.Instant;

public record UpdateEventInput(
        String userId,
        String eventId,
        String title,
        String description,
        String eventType,
        UpdateEventAddressInput address,
        String categoryId,
        Instant startAt,
        Instant finishAt
) {

    public static UpdateEventInput with(
            final String userId,
            final String eventId,
            final String title,
            final String description,
            final String eventType,
            final UpdateEventAddressInput address,
            final String categoryId,
            final Instant startAt,
            final Instant finishAt
    ) {
        return new UpdateEventInput(
                userId,
                eventId,
                title,
                description,
                eventType,
                address,
                categoryId,
                startAt,
                finishAt
        );
    }
}
