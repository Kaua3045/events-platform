package com.kaua.events.platform.infrastructure.ticket.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.ticket.update.UpdateTicketInput;

public record UpdateTicketRequest(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("price") String price,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("type") String type,
        @JsonProperty("status") String status
) {

    public UpdateTicketInput toInput(final String ticketId, final String userId) {
        return new UpdateTicketInput(
                ticketId,
                userId,
                eventId,
                name,
                description,
                price,
                quantity,
                type,
                status
        );
    }
}
