package com.kaua.events.platform.infrastructure.listeners;

import com.kaua.events.platform.application.usecases.orders.update.status.UpdateOrderStatusUseCase;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentUseCase;
import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryEventListenerTest extends UnitTest {

    private InMemoryEventListener listener;

    @Mock
    private CreatePaymentUseCase createPaymentUseCase;

    @Mock
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @BeforeEach
    void setUp() {
        listener = new InMemoryEventListener(createPaymentUseCase, updateOrderStatusUseCase);
    }

    @Test
    void givenOrderCreatedEvent_whenHandleOrderEvent_thenProcessedSuccessfully() {
        String payload = """
                {
                    "status": "CREATED",
                    "total_amount": 100.0,
                    "payment_details": {"method": "PIX", "amount": 100.0, "qr_code": null, "qr_code_image_url": null, "expires_in": 0},
                    "event_id": "evt-123",
                    "event_type": "OrderCreated",
                    "occurred_on": "2025-08-30T16:27:19.481562Z",
                    "aggregate_id": "agg-123",
                    "aggregate_version": 0,
                    "source": "OrderService",
                    "trace_id": "trace-123"
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

        listener.handleEvents(message);

        verify(createPaymentUseCase, times(1)).execute(any());
    }

    @Test
    void givenPaymentCreatedEvent_whenHandleOrderEvent_thenProcessedSuccessfully() {
        String payload = """
                {
                    "payment_id": "pay-123",
                    "status": "COMPLETED",
                    "amount": 100.0,
                    "aggregate_id": "agg-123",
                    "event_id": "evt-124",
                    "event_type": "PaymentCreated",
                    "occurred_on": "2025-08-30T16:27:19.481562Z",
                    "trace_id": "trace-124"
                }
                """;

        OutboxMessage message = new OutboxMessage(
                "evt-124",
                "Payment",
                "agg-123",
                0L,
                "PaymentCreated",
                payload,
                "2025-08-30T16:27:19.481562Z",
                "PENDING",
                null
        );

        listener.handleEvents(message);

        verifyNoInteractions(createPaymentUseCase);
    }

    @Test
    void givenPaymentStatusChangedEvent_whenHandlePaymentEvent_thenProcessedSuccessfully() {
        String payload = """
                {
                    "order_id": "order-123",
                    "status": "PAID",
                    "aggregate_id": "agg-123",
                    "event_id": "evt-124",
                    "event_type": "PaymentStatusChanged",
                    "occurred_on": "2025-08-30T16:27:19.481562Z",
                    "trace_id": "trace-124"
                }
                """;

        OutboxMessage message = new OutboxMessage(
                "evt-124",
                "Payment",
                "agg-123",
                0L,
                "PaymentStatusChanged",
                payload,
                "2025-08-30T16:27:19.481562Z",
                "PENDING",
                null
        );

        listener.handleEvents(message);

        verifyNoInteractions(createPaymentUseCase);
        verify(updateOrderStatusUseCase, times(1)).execute(any());
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

        assertThrows(IllegalArgumentException.class, () -> listener.handleEvents(message));
        verifyNoInteractions(createPaymentUseCase);
    }
}
