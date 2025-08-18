package com.kaua.events.platform.application.usecases.orders.retrieve.list;

import com.kaua.events.platform.domain.orders.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ListOrdersByUserIdOutput(
        String orderId,
        String userId,
        List<ListOrdersItemsByUserIdOutput> items,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant failedAt
) {

    public static ListOrdersByUserIdOutput from(final Order aOrder) {
        return new ListOrdersByUserIdOutput(
                aOrder.getId().value().toString(),
                aOrder.getUserId().value().toString(),
                aOrder.getItems().stream().map(ListOrdersItemsByUserIdOutput::from)
                        .toList(),
                aOrder.getTotalAmount(),
                aOrder.getStatus().name(),
                aOrder.getCreatedAt(),
                aOrder.getUpdatedAt(),
                aOrder.getFailedAt().orElse(null)
        );
    }
}
