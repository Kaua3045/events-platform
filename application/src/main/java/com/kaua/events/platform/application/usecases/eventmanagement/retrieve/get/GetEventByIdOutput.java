package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get;

import com.kaua.events.platform.domain.eventmanagement.Event;

import java.time.Instant;

public record GetEventByIdOutput(
        String eventId,
        String organizationId,
        String title,
        String description,
        String status,
        String type,
        GetEventByIdAddressOutput address,
        String imageUrl,
        String categoryId,
        Instant startAt,
        Instant finishAt,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {

    public static GetEventByIdOutput from(final Event aEvent) {
        return new GetEventByIdOutput(
                aEvent.getId().value().toString(),
                aEvent.getOrganizationId().value().toString(),
                aEvent.getTitle(),
                aEvent.getDescription().orElse(null),
                aEvent.getStatus().name(),
                aEvent.getType().name(),
                aEvent.getAddress().map(GetEventByIdAddressOutput::from).orElse(null),
                aEvent.getImageUrl().orElse(null),
                aEvent.getCategoryId(),
                aEvent.getStartAt(),
                aEvent.getFinishAt(),
                aEvent.getCreatedAt(),
                aEvent.getUpdatedAt(),
                aEvent.getDeletedAt().orElse(null)
        );
    }
}
