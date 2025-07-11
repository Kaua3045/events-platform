package com.kaua.events.platform.infrastructure.ticket.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketInput;

public record CreateTicketRequest(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("price") String price,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("type") String type,
        @JsonProperty("status") String status
) {

    public CreateTicketInput toInput(final String userId) {
        return new CreateTicketInput(
                userId,
                name,
                description,
                eventId,
                price,
                quantity,
                type,
                status
        );
    }
}
