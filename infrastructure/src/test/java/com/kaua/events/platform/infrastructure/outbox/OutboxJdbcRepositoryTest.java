package com.kaua.events.platform.infrastructure.outbox;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.orders.OrderStatus;
import com.kaua.events.platform.domain.orders.events.OrderCreatedEvent;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

class OutboxJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(outboxRepository());
    }

    @Test
    void givenAValidDomainEvent_whenCallSave_thenOutboxEventIsPersisted() {
        Assertions.assertEquals(0, countOutboxMessages());

        final var aEvent = new OrderCreatedEvent(
                IdentifierUtils.generateNewULID().toString(),
                0,
                OrderStatus.CREATED.name(),
                new BigDecimal("10.00"),
                new PixPaymentDetails(new BigDecimal("10.00")),
                IdentifierUtils.generateNewULID().toString()
        );

        outboxRepository().save(List.of(aEvent));

        Assertions.assertEquals(1, countOutboxMessages());

        final var persisted = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc();
        Assertions.assertEquals(1, persisted.size());

        final var outboxMessage = persisted.getFirst();
        Assertions.assertEquals(aEvent.eventId(), outboxMessage.id());
        Assertions.assertEquals("PENDING", outboxMessage.status());
        Assertions.assertEquals(aEvent.getClass().getSimpleName(), outboxMessage.aggregateType());
        Assertions.assertEquals(aEvent.aggregateId(), outboxMessage.aggregateId());
        Assertions.assertEquals(aEvent.aggregateVersion(), outboxMessage.aggregateVersion());
        Assertions.assertEquals(aEvent.eventType(), outboxMessage.eventType());
        Assertions.assertNotNull(outboxMessage.payload());
        Assertions.assertNull(outboxMessage.lastAttemptAt());
    }

    @Test
    void givenMultiplePendingEvents_whenCallFindTop50_thenReturnOrderedByOccurredOn() {
        Assertions.assertEquals(0, countOutboxMessages());

        final var olderEvent = new TestDomainEvent("agg-1", 1, InstantUtils.now().minusSeconds(60));
        final var newerEvent = new TestDomainEvent("agg-2", 1, InstantUtils.now());

        outboxRepository().save(List.of(newerEvent));
        outboxRepository().save(List.of(olderEvent));

        final var events = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc();

        Assertions.assertEquals(2, events.size());
        Assertions.assertEquals(olderEvent.eventId(), events.get(0).id());
        Assertions.assertEquals(newerEvent.eventId(), events.get(1).id());
    }

    @Test
    void givenNoPendingEvents_whenCallFindTop50_thenReturnEmptyList() {
        Assertions.assertEquals(0, countOutboxMessages());

        final var result = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void givenAnExistingEvent_whenMarkAsPublished_thenStatusIsUpdated() {
        final var aEvent = new TestDomainEvent("agg-123", 1, InstantUtils.now());

        outboxRepository().save(List.of(aEvent));

        Assertions.assertEquals(1, countOutboxMessages());
        final var before = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc().getFirst();
        Assertions.assertEquals("PENDING", before.status());

        outboxRepository().markAsPublished(aEvent.eventId());

        final var aPendingMessages = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc();
        Assertions.assertTrue(aPendingMessages.isEmpty());
    }

    @Test
    void givenAnExistingEvent_whenMarkAsFailed_thenStatusIsUpdated() {
        final var aEvent = new TestDomainEvent("agg-456", 1, InstantUtils.now());

        outboxRepository().save(List.of(aEvent));

        Assertions.assertEquals(1, countOutboxMessages());
        final var before = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc().getFirst();
        Assertions.assertEquals("PENDING", before.status());

        outboxRepository().markAsFailed(aEvent.eventId());

        final var aPendingMessages = outboxRepository().findTop50ByStatusIsPendingOrderByOccurredOnAsc();
        Assertions.assertTrue(aPendingMessages.isEmpty());
    }

    private record TestDomainEvent(
            String eventId,
            String eventType,
            Instant occurredOn,
            String aggregateId,
            long aggregateVersion,
            String source,
            String traceId,
            String additionalData
    ) implements DomainEvent {

        public TestDomainEvent(final String aggregateId, final long aggregateVersion, final Instant occurredOn) {
            this(
                    IdentifierUtils.generateNewULID().toString(),
                    "TestDomainEvent",
                    occurredOn,
                    aggregateId,
                    aggregateVersion,
                    "TestSource",
                    IdentifierUtils.generateNewULID().toString(),
                    "Some additional data"
            );
        }
    }
}
