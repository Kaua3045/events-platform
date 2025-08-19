package com.kaua.events.platform.domain.orders.events;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.payments.PaymentDetails;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String status,
        BigDecimal totalAmount,
        PaymentDetails paymentDetails,
        String eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        long aggregateVersion,
        String source,
        String traceId
) implements DomainEvent {

    public OrderCreatedEvent(
            final String orderId,
            final long aggregateVersion,
            final String status,
            final BigDecimal totalAmount,
            final PaymentDetails paymentDetails,
            final String traceId
    ) {
        this(
                status,
                totalAmount,
                paymentDetails,
                IdentifierUtils.generateNewULID().toString(),
                "OrderCreated",
                InstantUtils.now(),
                orderId,
                aggregateVersion,
                "OrderService",
                traceId
        );
    }
}
