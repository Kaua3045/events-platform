package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list;

import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;

import java.time.Instant;

public record ListEventsOutput(
        String eventId,
        String organizationId,
        String title,
        String imageUrl,
        String categoryId,
        String eventType,
        String eventStatus,
        String addressCity,
        Instant startAt,
        Instant finishAt
) {

    public static ListEventsOutput from(final Event aEvent) {
        return new ListEventsOutput(
                aEvent.getId().value().toString(),
                aEvent.getOrganizationId().value().toString(),
                aEvent.getTitle(),
                aEvent.getImageUrl().orElse(null),
                aEvent.getCategoryId(),
                aEvent.getType().name(),
                aEvent.getStatus().name(),
                aEvent.getAddress().map(Address::getCity).orElse(null),
                aEvent.getStartAt(),
                aEvent.getFinishAt()
        );
    }
}
