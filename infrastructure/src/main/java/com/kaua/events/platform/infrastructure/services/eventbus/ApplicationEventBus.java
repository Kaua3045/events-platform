package com.kaua.events.platform.infrastructure.services.eventbus;

import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class ApplicationEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventBus.class);

    private final ApplicationContext applicationContext;

    public ApplicationEventBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void publish(OutboxMessage aOutboxMessage) {
        log.info("Publishing event to the application context: {}", aOutboxMessage);
        this.applicationContext.publishEvent(aOutboxMessage);
        log.info("Event published to the application context: {}", aOutboxMessage);
    }
}
