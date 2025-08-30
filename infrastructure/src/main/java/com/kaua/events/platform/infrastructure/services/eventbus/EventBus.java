package com.kaua.events.platform.infrastructure.services.eventbus;

import static com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository.OutboxMessage;

public interface EventBus {

    void publish(OutboxMessage aOutboxMessage);
}
