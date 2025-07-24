package com.kaua.events.platform.infrastructure.ticket.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.ticket.update.UpdateTicketOutput;

public record UpdateTicketResponse(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ticket_id") String ticketId
) {

    public static UpdateTicketResponse from(final UpdateTicketOutput aOutput) {
        return new UpdateTicketResponse(
                aOutput.eventId(),
                aOutput.ticketId()
        );
    }
}
