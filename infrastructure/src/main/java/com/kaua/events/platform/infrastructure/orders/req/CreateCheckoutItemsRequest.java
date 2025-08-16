package com.kaua.events.platform.infrastructure.orders.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.create.CreateCheckoutItemsInput;

public record CreateCheckoutItemsRequest(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ticket_id") String ticketId,
        @JsonProperty("quantity") int quantity
) {

    public CreateCheckoutItemsInput toInput() {
        return new CreateCheckoutItemsInput(
                eventId(),
                ticketId(),
                quantity()
        );
    }
}
