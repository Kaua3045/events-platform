package com.kaua.events.platform.infrastructure.listeners;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryEventListenerTest extends UnitTest {

    private InMemoryEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new InMemoryEventListener();
    }

    @Test
    void givenOrderCreatedEvent_whenHandleOrderEvent_thenProcessedSuccessfully() {
        String payload = """
                {
                    "status": "CREATED",
                    "total_amount": 100.0,
                    "payment_details": {"method": "PIX", "amount": 100.0},
                    "event_id": "evt-123",
                    "event_type": "OrderCreated",
                    "occurred_on": "2025-08-30T16:27:19.481562Z",
                    "aggregate_id": "agg-123",
                    "aggregate_version": 0,
                    "source": "OrderService",
                    "trace_id": null
                }
                """;

        OutboxMessage message = new OutboxMessage(
                "evt-123",
                "Order",
                "agg-123",
                0L,
                "OrderCreated",
                payload,
                "2025-08-30T16:27:19.481562Z",
                "PENDING",
                null
        );

        listener.handleOrderEvent(message);
    }

    @Test
    void givenUnknownEventType_whenHandleOrderEvent_thenThrowsException() {
        String payload = "{}";

        OutboxMessage message = new OutboxMessage(
                "evt-456",
                "Order",
                "agg-456",
                0L,
                "UnknownEvent",
                payload,
                "2025-08-30T16:27:19.481562Z",
                "PENDING",
                null
        );

        assertThrows(IllegalArgumentException.class, () -> listener.handleOrderEvent(message));
    }
}
