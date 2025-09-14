package com.kaua.events.platform.infrastructure.listeners;

import com.kaua.events.platform.application.usecases.orders.update.status.UpdateOrderStatusInput;
import com.kaua.events.platform.application.usecases.orders.update.status.UpdateOrderStatusUseCase;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentInput;
import com.kaua.events.platform.application.usecases.payments.create.CreatePaymentUseCase;
import com.kaua.events.platform.domain.orders.events.OrderCreatedEvent;
import com.kaua.events.platform.domain.payments.events.PaymentCreatedEvent;
import com.kaua.events.platform.domain.payments.events.PaymentStatusChangedEvent;
import com.kaua.events.platform.infrastructure.configurations.json.Json;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventListener.class);

    private final CreatePaymentUseCase createPaymentUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public InMemoryEventListener(
            final CreatePaymentUseCase createPaymentUseCase,
            final UpdateOrderStatusUseCase updateOrderStatusUseCase
    ) {
        this.createPaymentUseCase = Objects.requireNonNull(createPaymentUseCase);
        this.updateOrderStatusUseCase = Objects.requireNonNull(updateOrderStatusUseCase);
    }

    @EventListener
    public void handleEvents(OutboxMessage aOutboxMessage) {
        log.info("OutboxMessage received: {}", aOutboxMessage);
        switch (aOutboxMessage.eventType()) {
            case "OrderCreated" ->
                    this.handleOrderCreatedEvent(Json.readValue(aOutboxMessage.payload(), OrderCreatedEvent.class));
            case "PaymentCreated" ->
                    this.handlePaymentCreatedEvent(Json.readValue(aOutboxMessage.payload(), PaymentCreatedEvent.class));
            case "PaymentStatusChanged" ->
                    this.handlePaymentStatusChanged(Json.readValue(aOutboxMessage.payload(), PaymentStatusChangedEvent.class));
            case "OrderStatusChanged" -> log.warn("OrderStatusChanged listener");
            default -> throw new IllegalArgumentException("Event type not recognized: " + aOutboxMessage.eventType());
        }
    }

    private void handlePaymentStatusChanged(final PaymentStatusChangedEvent paymentStatusChangedEvent) {
        log.info("Payment status changed Event received: {}", paymentStatusChangedEvent);
        final var aPaymentStatusChangedProcessed = this.updateOrderStatusUseCase.execute(
                UpdateOrderStatusInput.with(
                        paymentStatusChangedEvent.orderId(),
                        paymentStatusChangedEvent.status(),
                        paymentStatusChangedEvent.aggregateId())
        );
        log.info("Order status changed by payment status changed event {}", aPaymentStatusChangedProcessed);
    }

    private void handlePaymentCreatedEvent(final PaymentCreatedEvent paymentCreatedEvent) {
        log.info("Payment Event received: {}", paymentCreatedEvent);
    }

    private void handleOrderCreatedEvent(final OrderCreatedEvent aEvent) {
        log.info("Order Event received: {}", aEvent);
        final var aPaymentProcess = this.createPaymentUseCase.execute(CreatePaymentInput.with(
                aEvent.paymentDetails(),
                aEvent.aggregateId(),
                aEvent.traceId()
        ));
        log.info("Payment process created: {}", aPaymentProcess);
    }
}
