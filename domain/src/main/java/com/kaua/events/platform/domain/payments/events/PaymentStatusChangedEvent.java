package com.kaua.events.platform.domain.payments.events;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;

import java.time.Instant;

public record PaymentStatusChangedEvent(
        String orderId,
        String status,
        String eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        long aggregateVersion,
        String source,
        String traceId
) implements DomainEvent {

    public PaymentStatusChangedEvent(
            final String orderId,
            final String paymentId,
            final long aggregateVersion,
            final String status,
            final String traceId
    ) {
        this(
                orderId,
                status,
                IdentifierUtils.generateNewULID().toString(),
                "PaymentStatusChanged",
                InstantUtils.now(),
                paymentId,
                aggregateVersion,
                "PaymentService",
                traceId
        );
    }
}
