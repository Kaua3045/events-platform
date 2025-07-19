package com.kaua.events.platform.application.usecases.eventmanagement.update;

import com.kaua.events.platform.domain.eventmanagement.Event;

public record UpdateEventOutput(
        String eventId,
        String organizationId
) {

    public static UpdateEventOutput from(final Event aEvent) {
        return new UpdateEventOutput(
                aEvent.getId().value().toString(),
                aEvent.getOrganizationId().value().toString()
        );
    }
}
