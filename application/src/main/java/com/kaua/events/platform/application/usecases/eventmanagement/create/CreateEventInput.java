package com.kaua.events.platform.application.usecases.eventmanagement.create;

import java.time.Instant;

public record CreateEventInput(
        String organizationId,
        String title,
        String description,
        String eventType,
        CreateEventAddressInput address,
        String categoryId,
        Instant startAt,
        Instant finishAt
) {

    public static CreateEventInput with(
            final String organizationId,
            final String title,
            final String description,
            final String eventType,
            final CreateEventAddressInput address,
            final String categoryId,
            final Instant startAt,
            final Instant finishAt
    ) {
        return new CreateEventInput(
                organizationId,
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
