package com.kaua.events.platform.infrastructure.eventmanagement.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.update.UpdateEventOutput;

public record UpdateEventResponse(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("organization_id") String organizationId
) {

    public static UpdateEventResponse from(final UpdateEventOutput aEvent) {
        return new UpdateEventResponse(
                aEvent.eventId(),
                aEvent.organizationId()
        );
    }
}
