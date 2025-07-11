package com.kaua.events.platform.infrastructure.ticket.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketOutput;

public record CreateTicketResponse(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ticket_id") String ticketId
) {

    public static CreateTicketResponse from(final CreateTicketOutput aOutput) {
        return new CreateTicketResponse(
                aOutput.eventId(),
                aOutput.ticketId()
        );
    }
}
