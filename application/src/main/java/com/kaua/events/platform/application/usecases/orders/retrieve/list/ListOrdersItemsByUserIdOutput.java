package com.kaua.events.platform.application.usecases.orders.retrieve.list;

import com.kaua.events.platform.domain.orders.OrderItem;

public record ListOrdersItemsByUserIdOutput(
        String itemId,
        String eventId,
        String ticketId,
        int quantity
) {

    public static ListOrdersItemsByUserIdOutput from(final OrderItem item) {
        return new ListOrdersItemsByUserIdOutput(
                item.getId().toString(),
                item.getEventId().value().toString(),
                item.getTicketId().value().toString(),
                item.getQuantity()
        );
    }
}
