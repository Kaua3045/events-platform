package com.kaua.events.platform.infrastructure.ticket;

import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

@Component
public class TicketJdbcRepository implements TicketRepository {

    private static final Logger log = LoggerFactory.getLogger(TicketJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public TicketJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Ticket save(final Ticket ticket) {
        if (ticket.getVersion() == 0) {
            log.debug("Creating new ticket: {}", ticket);
            create(ticket);
            log.info("Created new ticket: {}", ticket);
        }

        ticket.incrementVersion();
        return ticket;
    }

    private void create(final Ticket aTicket) {
        final var aSql = """
                INSERT INTO tickets (
                id,
                version,
                event_id,
                name,
                description,
                price,
                quantity,
                sold,
                type,
                status,
                created_at,
                updated_at
                )
                VALUES (
                :id,
                (:version + 1),
                :eventId,
                :name,
                :description,
                :price,
                :quantity,
                :sold,
                :type,
                :status,
                :createdAt,
                :updatedAt
                )
                """;

        executeUpdate(aSql, aTicket);
    }

    private int executeUpdate(final String aSql, final Ticket aTicket) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aTicket.getId().value().toString());
        aParams.put("version", aTicket.getVersion());
        aParams.put("eventId", aTicket.getEventId().value().toString());
        aParams.put("name", aTicket.getName());
        aParams.put("description", aTicket.getDescription().orElse(null));
        aParams.put("price", aTicket.getPrice());
        aParams.put("quantity", aTicket.getQuantity());
        aParams.put("sold", aTicket.getSold());
        aParams.put("type", aTicket.getType().name());
        aParams.put("status", aTicket.getStatus().name());
        aParams.put("createdAt", aTicket.getCreatedAt());
        aParams.put("updatedAt", aTicket.getUpdatedAt());

        return this.databaseClient.update(aSql, aParams);
    }
}
