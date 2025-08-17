package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ListOrdersByUserIdResponse(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("items") List<ListOrdersItemsByUserIdResponse> items,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("failed_at") Instant failedAt
) {

    public static ListOrdersByUserIdResponse from(final ListOrdersByUserIdOutput aOrder) {
        return new ListOrdersByUserIdResponse(
                aOrder.orderId(),
                aOrder.userId(),
                aOrder.items().stream().map(ListOrdersItemsByUserIdResponse::from)
                        .toList(),
                aOrder.totalAmount(),
                aOrder.status(),
                aOrder.createdAt(),
                aOrder.updatedAt(),
                aOrder.failedAt()
        );
    }
}
