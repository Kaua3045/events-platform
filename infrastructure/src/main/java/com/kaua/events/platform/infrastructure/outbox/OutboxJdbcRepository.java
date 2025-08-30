package com.kaua.events.platform.infrastructure.outbox;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.infrastructure.configurations.json.Json;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class OutboxJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(OutboxJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OutboxJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    public void save(final DomainEvent aEvent) {
        log.info("Saving outbox event with identifier {} and event {}", aEvent.eventId(), aEvent);
        final var aSql = """
                INSERT INTO outbox (
                 id,
                 aggregate_type,
                 aggregate_id,
                 aggregate_version,
                 event_type,
                 payload,
                 occurred_on,
                 status,
                 last_attempt_at
                )
                VALUES (
                 :id,
                 :aggregateType,
                 :aggregateId,
                 :aggregateVersion,
                 :eventType,
                 :payload,
                 :occurredOn,
                 :status,
                 :lastAttemptAt
                )
                """;

        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aEvent.eventId());
        aParams.put("aggregateType", aEvent.getClass().getSimpleName());
        aParams.put("aggregateId", aEvent.aggregateId());
        aParams.put("aggregateVersion", aEvent.aggregateVersion());
        aParams.put("eventType", aEvent.eventType());
        aParams.put("payload", Json.writeValueAsString(aEvent));
        aParams.put("occurredOn", aEvent.occurredOn());
        aParams.put("status", "PENDING");
        aParams.put("lastAttemptAt", null);

        this.databaseClient.update(aSql, aParams);
        log.info("Outbox event with identifier {} saved {}", aEvent.eventId(), aEvent);
    }

    public List<OutboxMessage> findTop50ByStatusIsPendingOrderByOccurredOnAsc() {
        final var aSql = """
                SELECT
                 id,
                 aggregate_type,
                 aggregate_id,
                 aggregate_version,
                 event_type,
                 payload,
                 occurred_on,
                 status,
                 last_attempt_at
                FROM outbox
                WHERE status = 'PENDING'
                ORDER BY occurred_on
                LIMIT 50
                FOR UPDATE SKIP LOCKED
                """;

        log.info("Fetching up to 50 pending outbox messages");

        return this.databaseClient.query(aSql, (rs) -> new OutboxMessage(
                rs.getString("id"),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getInt("aggregate_version"),
                rs.getString("event_type"),
                rs.getString("payload"),
                rs.getString("occurred_on"),
                rs.getString("status"),
                rs.getString("last_attempt_at")
        ));
    }

    public void markAsPublished(final String id) {
        log.info("Marking outbox message with id {} as PUBLISHED", id);
        final var aSql = """
                UPDATE outbox
                SET status = 'PUBLISHED',
                    last_attempt_at = :lastAttemptAt
                WHERE id = :id
                """;

        final var aParams = new HashMap<String, Object>();
        aParams.put("id", id);
        aParams.put("lastAttemptAt", InstantUtils.now());

        this.databaseClient.update(aSql, aParams);
        log.info("Outbox message with id {} marked as PUBLISHED", id);
    }

    public void markAsFailed(final String id) {
        log.info("Marking outbox message with id {} as FAILED", id);
        final var aSql = """
                UPDATE outbox
                SET status = 'FAILED',
                    last_attempt_at = :lastAttemptAt
                WHERE id = :id
                """;

        final var aParams = new HashMap<String, Object>();
        aParams.put("id", id);
        aParams.put("lastAttemptAt", InstantUtils.now());

        this.databaseClient.update(aSql, aParams);
        log.info("Outbox message with id {} marked as FAILED", id);
    }

    public record OutboxMessage(
            String id,
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            String eventType,
            String payload,
            String occurredOn,
            String status,
            String lastAttemptAt
    ) {}
}
