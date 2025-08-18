package com.kaua.events.platform.application.usecases.orders.retrieve.get;

import com.kaua.events.platform.domain.orders.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GetOrderByIdOutput(
        String orderId,
        String userId,
        List<GetOrderItemsByIdOutput> items,
        BigDecimal totalAmount,
        String paymentId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant failedAt
) {

    public static GetOrderByIdOutput from(final Order aOrder) {
        return new GetOrderByIdOutput(
                aOrder.getId().value().toString(),
                aOrder.getUserId().value().toString(),
                aOrder.getItems().stream().map(GetOrderItemsByIdOutput::from)
                        .toList(),
                aOrder.getTotalAmount(),
                aOrder.getPaymentId().map(it -> it.value().toString())
                        .orElse(null),
                aOrder.getStatus().name(),
                aOrder.getCreatedAt(),
                aOrder.getUpdatedAt(),
                aOrder.getFailedAt().orElse(null)
        );
    }
}
