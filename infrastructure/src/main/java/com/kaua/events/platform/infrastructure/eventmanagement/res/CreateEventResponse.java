package com.kaua.events.platform.infrastructure.eventmanagement.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventOutput;

public record CreateEventResponse(
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("event_id") String eventId
) {

    public static CreateEventResponse from(final CreateEventOutput aOutput) {
        return new CreateEventResponse(
                aOutput.organizationId(),
                aOutput.eventId()
        );
    }
}
