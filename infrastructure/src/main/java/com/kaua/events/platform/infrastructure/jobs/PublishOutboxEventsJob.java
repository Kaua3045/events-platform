package com.kaua.events.platform.infrastructure.jobs;

import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.events.platform.infrastructure.services.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Profile("!test-integration")
public class PublishOutboxEventsJob {

    private static final Logger log = LoggerFactory.getLogger(PublishOutboxEventsJob.class);

    private final OutboxJdbcRepository outboxRepository;
    private final EventBus eventBus;
    private final TransactionManager transactionManager;

    public PublishOutboxEventsJob(
            final OutboxJdbcRepository outboxRepository,
            final EventBus eventBus,
            final TransactionManager transactionManager
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Scheduled(
            fixedRateString = "${jobs.outbox.publish-rate-minutes}",
            initialDelayString = "${jobs.outbox.publish-initial-delay-minutes}",
            timeUnit = TimeUnit.MINUTES
    )
    public void publish() {
        log.info("Publishing outbox events");

        boolean pending = true;
        while (pending) {
            final var aResult = this.transactionManager.execute(() -> {
                final var aOutboxEvents = this.outboxRepository
                        .findTop50ByStatusIsPendingOrderByOccurredOnAsc();

                if (aOutboxEvents.isEmpty()) {
                    return false;
                }

                aOutboxEvents.forEach(aOutboxEvent -> {
                    try {
                        this.eventBus.publish(aOutboxEvent);
                        this.outboxRepository.markAsPublished(aOutboxEvent.id());
                        log.info("Outbox event published: {}", aOutboxEvent);
                    } catch (final RuntimeException ex) {
                        log.error("Error publishing outbox event: {}", aOutboxEvent, ex);
                        this.outboxRepository.markAsPublished(aOutboxEvent.id());
                    }
                });

                return true;
            });

            if (aResult.isFailure()) {
                log.error("Error on execute publish outbox events jobs with error: {}", aResult.getException().getMessage());
                pending = false;
            } else {
                pending = aResult.getValue();
            }
        }
    }
}
