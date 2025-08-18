package com.kaua.events.platform.application.usecases.orders.retrieve.get;

import com.kaua.events.platform.domain.orders.OrderItem;

import java.math.BigDecimal;

public record GetOrderItemsByIdOutput(
        String itemId,
        String eventId,
        String ticketId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {

    public static GetOrderItemsByIdOutput from(final OrderItem item) {
        return new GetOrderItemsByIdOutput(
                item.getId().toString(),
                item.getEventId().value().toString(),
                item.getTicketId().value().toString(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
