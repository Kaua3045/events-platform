package com.kaua.events.platform.infrastructure.services.eventbus;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ApplicationEventBusTest extends UnitTest {

    @Mock
    private ApplicationContext applicationContext;

    private ApplicationEventBus eventBus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventBus = new ApplicationEventBus(applicationContext);
    }

    @Test
    void givenOutboxMessage_whenPublish_thenDelegatesToApplicationContext() {
        final var message = new OutboxJdbcRepository.OutboxMessage(
                "id-123",
                "aggregate-type",
                "aggregate-id",
                1L,
                "event-type",
                "{}",
                "2024-01-01T00:00:00Z",
                "PENDING",
                null
        );

        eventBus.publish(message);

        verify(applicationContext, times(1)).publishEvent(message.payload());
    }
}
