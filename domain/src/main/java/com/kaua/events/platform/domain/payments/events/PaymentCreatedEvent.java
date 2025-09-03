package com.kaua.events.platform.domain.payments.events;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreatedEvent(
        String orderId,
        String status,
        BigDecimal totalAmount,
        String eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        long aggregateVersion,
        String source,
        String traceId
) implements DomainEvent {

    public PaymentCreatedEvent(
            final String orderId,
            final String paymentId,
            final long aggregateVersion,
            final String status,
            final BigDecimal totalAmount,
            final String traceId
    ) {
        this(
                orderId,
                status,
                totalAmount,
                IdentifierUtils.generateNewULID().toString(),
                "PaymentCreated",
                InstantUtils.now(),
                paymentId,
                aggregateVersion,
                "PaymentService",
                traceId
        );
    }
}
