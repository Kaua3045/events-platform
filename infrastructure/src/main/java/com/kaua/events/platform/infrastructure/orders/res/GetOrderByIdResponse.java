package com.kaua.events.platform.infrastructure.orders.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.orders.retrieve.get.GetOrderByIdOutput;
import com.kaua.events.platform.domain.orders.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GetOrderByIdResponse(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("items") List<GetOrderItemsByIdResponse> items,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("payment_id") String paymentId,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("failed_at") Instant failedAt
) {

    public static GetOrderByIdResponse from(final GetOrderByIdOutput aOutput) {
        return new GetOrderByIdResponse(
                aOutput.orderId(),
                aOutput.userId(),
                aOutput.items().stream().map(GetOrderItemsByIdResponse::from)
                        .toList(),
                aOutput.totalAmount(),
                aOutput.paymentId(),
                aOutput.status(),
                aOutput.createdAt(),
                aOutput.updatedAt(),
                aOutput.failedAt()
        );
    }
}
