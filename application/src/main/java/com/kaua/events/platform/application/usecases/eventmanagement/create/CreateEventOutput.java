package com.kaua.events.platform.application.usecases.eventmanagement.create;

import com.kaua.events.platform.domain.eventmanagement.Event;

public record CreateEventOutput(
        String organizationId,
        String eventId
) {

    public static CreateEventOutput from(final Event aEvent) {
        return new CreateEventOutput(
                aEvent.getOrganizationId().value().toString(),
                aEvent.getId().value().toString()
        );
    }
}
