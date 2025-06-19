package com.kaua.events.platform.infrastructure.eventmanagement;

import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class EventJdbcRepository implements EventRepository {

    private static final Logger log = LoggerFactory.getLogger(EventJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public EventJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public boolean existsByTitleAndOrganizationId(final String title, final String id) {
        final var aSql = "SELECT COUNT(*) FROM events WHERE organization_id = :organizationId AND title = :title";
        return this.databaseClient.count(aSql, Map.of(
                "organizationId", id,
                "title", title
        )) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Event save(final Event event) {
        if (event.getVersion() == 0) {
            log.debug("Creating new event: {}", event);
            create(event);
            log.info("Created new event: {}", event);
        }

        event.incrementVersion();
        return event;
    }

    private void create(final Event aEvent) {
        final var aSql = """
                INSERT INTO events (
                id,
                version,
                organization_id,
                title,
                description,
                status,
                type,
                image_url,
                category_id,
                start_at,
                finish_at,
                created_at,
                updated_at,
                deleted_at,
                address_street,
                address_number,
                address_complement,
                address_neighborhood,
                address_city,
                address_state,
                address_postal_code,
                address_country
                )
                VALUES (
                :id,
                (:version + 1),
                :organization_id,
                :title,
                :description,
                :status,
                :type,
                :image_url,
                :category_id,
                :start_at,
                :finish_at,
                :created_at,
                :updated_at,
                :deleted_at,
                :address_street,
                :address_number,
                :address_complement,
                :address_neighborhood,
                :address_city,
                :address_state,
                :address_postal_code,
                :address_country
                )
                """;

        executeUpdate(aSql, aEvent);
    }

    private int executeUpdate(final String aSql, final Event aEvent) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aEvent.getId().value().toString());
        aParams.put("version", aEvent.getVersion());
        aParams.put("organization_id", aEvent.getOrganizationId().value().toString());
        aParams.put("title", aEvent.getTitle());
        aParams.put("description", aEvent.getDescription().orElse(null));
        aParams.put("status", aEvent.getStatus().name());
        aParams.put("type", aEvent.getType().name());
        aParams.put("image_url", aEvent.getImageUrl().orElse(null));
        aParams.put("category_id", aEvent.getCategoryId());
        aParams.put("start_at", aEvent.getStartAt());
        aParams.put("finish_at", aEvent.getFinishAt());
        aParams.put("created_at", aEvent.getCreatedAt());
        aParams.put("updated_at", aEvent.getUpdatedAt());
        aParams.put("deleted_at", aEvent.getDeletedAt().orElse(null));
        aParams.put("address_street", aEvent.getAddress().map(Address::getStreet).orElse(null));
        aParams.put("address_number", aEvent.getAddress().map(Address::getNumber).orElse(null));
        aParams.put("address_complement", aEvent.getAddress().flatMap(Address::getComplement).orElse(null));
        aParams.put("address_neighborhood", aEvent.getAddress().map(Address::getNeighborhood).orElse(null));
        aParams.put("address_city", aEvent.getAddress().map(Address::getCity).orElse(null));
        aParams.put("address_state", aEvent.getAddress().map(Address::getState).orElse(null));
        aParams.put("address_postal_code", aEvent.getAddress().map(Address::getPostalCode).orElse(null));
        aParams.put("address_country", aEvent.getAddress().map(Address::getCountry).orElse(null));

        return this.databaseClient.update(aSql, aParams);
    }
}
