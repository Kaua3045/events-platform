package com.kaua.events.platform.infrastructure.listeners;

import com.kaua.events.platform.domain.orders.events.OrderCreatedEvent;
import com.kaua.events.platform.infrastructure.configurations.json.Json;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventListener.class);

    @EventListener
    public void handleOrderEvent(OutboxMessage aOutboxMessage) {
        log.info("OutboxMessage received: {}", aOutboxMessage);
        switch (aOutboxMessage.eventType()) {
            case "OrderCreated" -> this.handleOrderCreatedEvent(Json.readValue(aOutboxMessage.payload(), OrderCreatedEvent.class));
            default -> throw new IllegalArgumentException("Event type not recognized: " + aOutboxMessage.eventType());
        }
    }

    private void handleOrderCreatedEvent(final OrderCreatedEvent aEvent) {
        log.info("Event received: {}", aEvent);
    }
}
