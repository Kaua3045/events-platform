package com.kaua.events.platform.infrastructure.jobs;

import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.application.wrapper.TransactionResult;
import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.events.platform.infrastructure.services.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class PublishOutboxEventsJobTest extends UnitTest {

    @Mock
    private OutboxJdbcRepository outboxRepository;

    @Mock
    private EventBus eventBus;

    @Mock
    private TransactionManager transactionManager;

    @InjectMocks
    private PublishOutboxEventsJob job;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(transactionManager.execute(Mockito.any()))
                .thenAnswer(invocation -> {
                    final var supplier = invocation.getArgument(0, Supplier.class);
                    try {
                        Object result = supplier.get();
                        return TransactionResult.success(result);
                    } catch (RuntimeException e) {
                        return TransactionResult.failure(e);
                    }
                });
    }

    @Test
    void givenNoPendingEvents_whenPublish_thenDoNothing() {
        when(outboxRepository.findTop50ByStatusIsPendingOrderByOccurredOnAsc())
                .thenReturn(List.of());

        job.publish();

        verify(outboxRepository, times(1))
                .findTop50ByStatusIsPendingOrderByOccurredOnAsc();
        verifyNoInteractions(eventBus);
    }

    @Test
    void givenPendingEvents_whenPublish_thenPublishAndMarkAsPublished() {
        var event = new OutboxJdbcRepository.OutboxMessage("id-123", "agg", "agg-id", 1L,
                "type", "{}", "2024-01-01T00:00:00Z", "PENDING", null);

        when(outboxRepository.findTop50ByStatusIsPendingOrderByOccurredOnAsc())
                .thenReturn(List.of(event))
                .thenReturn(List.of());

        job.publish();

        verify(eventBus, times(1)).publish(event);
        verify(outboxRepository, times(1)).markAsPublished("id-123");
    }

    @Test
    void givenPublishThrowsException_whenPublish_thenStillMarkAsPublished() {
        var event = new OutboxJdbcRepository.OutboxMessage("id-456", "agg", "agg-id", 1L,
                "type", "{}", "2024-01-01T00:00:00Z", "PENDING", null);

        when(outboxRepository.findTop50ByStatusIsPendingOrderByOccurredOnAsc())
                .thenReturn(List.of(event))
                .thenReturn(List.of());

        doThrow(new RuntimeException("boom")).when(eventBus).publish(event);

        job.publish();

        verify(eventBus, times(1)).publish(event);
        verify(outboxRepository, times(1)).markAsPublished("id-456");
    }

    @Test
    void givenTransactionFails_whenPublish_thenStopsLoop() {
        when(transactionManager.execute(any()))
                .thenReturn(TransactionResult.failure(new RuntimeException("DB error")));

        job.publish();

        verifyNoInteractions(outboxRepository, eventBus);
    }
}
