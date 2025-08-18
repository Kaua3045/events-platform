package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.retrieve.get.GetOrderItemsByIdOutput;

import java.math.BigDecimal;

public record GetOrderItemsByIdResponse(
        @JsonProperty("item_id") String itemId,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ticket_id") String ticketId,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("unit_price") BigDecimal unitPrice,
        @JsonProperty("total_price") BigDecimal totalPrice
) {

    public static GetOrderItemsByIdResponse from(final GetOrderItemsByIdOutput item) {
        return new GetOrderItemsByIdResponse(
                item.itemId(),
                item.eventId(),
                item.ticketId(),
                item.quantity(),
                item.unitPrice(),
                item.totalPrice()
        );
    }
}
