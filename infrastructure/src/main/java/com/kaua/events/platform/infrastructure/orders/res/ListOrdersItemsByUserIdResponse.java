package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersItemsByUserIdOutput;

public record ListOrdersItemsByUserIdResponse(
        @JsonProperty("item_id") String itemId,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ticket_id") String ticketId,
        @JsonProperty("quantity") int quantity
) {

    public static ListOrdersItemsByUserIdResponse from(final ListOrdersItemsByUserIdOutput item) {
        return new ListOrdersItemsByUserIdResponse(
                item.itemId(),
                item.eventId(),
                item.ticketId(),
                item.quantity()
        );
    }
}
