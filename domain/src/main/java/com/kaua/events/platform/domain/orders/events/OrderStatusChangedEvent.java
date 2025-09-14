package com.kaua.events.platform.domain.orders.events;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String status,
        String eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        long aggregateVersion,
        String source,
        String traceId
) implements DomainEvent {

    public OrderStatusChangedEvent(
            final String orderId,
            final long aggregateVersion,
            final String status
    ) {
        this(
                status,
                IdentifierUtils.generateNewULID().toString(),
                "OrderStatusChanged",
                InstantUtils.now(),
                orderId,
                aggregateVersion,
                "OrderService",
                orderId
        );
    }
}
