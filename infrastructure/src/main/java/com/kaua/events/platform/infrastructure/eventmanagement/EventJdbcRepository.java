package com.kaua.events.platform.infrastructure.eventmanagement;

import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.eventmanagement.*;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import com.kaua.events.platform.infrastructure.jdbc.JdbcUtils;
import com.kaua.events.platform.infrastructure.jdbc.RowMap;
import com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public Pagination<Event> listAll(final SearchQuery query) {
        final var allowedFilters = Map.of(
                "categoryId", "category_id",
                "status", "status",
                "eventType", "type",
                "address_city", "address_city"
        );

        final var allowedSortFields = List.of("title", "start_at", "finish_at", "created_at");

        var spec = Optional.ofNullable(query.terms())
                .filter(terms -> !terms.isBlank())
                .map(this::assembleSpecification)
                .map(DynamicQueryListBuilder.Specification::where);

        // Add filtersSpec if present
        spec = spec.map(it -> buildFiltersSpecification(query.filters(), allowedFilters)
                        .map(it::and)
                        .orElse(it))
                .or(() -> buildFiltersSpecification(query.filters(), allowedFilters));

        // Add periodSpec if present
        spec = spec
                .map(it -> query.getPeriod()
                        .map(period -> com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.between(
                                "start_at", "start", "end", period.start(), period.end()))
                        .map(it::and)
                        .orElse(it))
                .or(() -> query.getPeriod()
                        .map(period -> com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.between(
                                "start_at", "start", "end", period.start(), period.end())));

        // Use empty spec if nothing was built
        var finalSpec = spec.orElse(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.Specification.where(null));

        final var dynamicQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "events",
                query,
                finalSpec,
                allowedSortFields
        );

        final var items = this.databaseClient.query(dynamicQuery.sql(), dynamicQuery.params(), eventMapper());
        final var totalPages = (int) Math.ceil((double) items.size() / query.perPage());

        final var metadata = new PaginationMetadata(
                query.page(),
                query.perPage(),
                totalPages,
                items.size()
        );

        return new Pagination<>(metadata, items);
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

    private DynamicQueryListBuilder.Specification assembleSpecification(final String terms) {
        return com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("title", "terms_title", terms)
                .or(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("description", "terms_description", terms));
    }

    private Optional<DynamicQueryListBuilder.Specification> buildFiltersSpecification(
            Map<String, String> filters,
            Map<String, String> allowedFilters
    ) {
        if (filters.isEmpty()) {
            return Optional.empty();
        }

        return filters.entrySet().stream()
                .filter(entry -> {
                    var value = entry.getValue();
                    return allowedFilters.containsKey(entry.getKey()) && !value.isBlank();
                })
                .map(entry -> com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal(
                        allowedFilters.get(entry.getKey()),
                        entry.getKey().replace(".", "_"),
                        entry.getValue()
                ))
                .reduce(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.Specification::and);
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

    private RowMap<Event> eventMapper() {
        return rs ->
                Event.with(
                        new EventID(ULID.fromString(rs.getString("id"))),
                        rs.getLong("version"),
                        new OrganizationID(ULID.fromString(rs.getString("organization_id"))),
                        rs.getString("title"),
                        rs.getString("description"),
                        EventStatus.from(rs.getString("status")).orElse(null),
                        EventType.from(rs.getString("type")).orElse(null),
                        rs.getString("address_street") == null ? null : Address.newAddress(
                                rs.getString("address_street"),
                                rs.getString("address_number"),
                                rs.getString("address_complement"),
                                rs.getString("address_neighborhood"),
                                rs.getString("address_city"),
                                rs.getString("address_state"),
                                rs.getString("address_postal_code"),
                                rs.getString("address_country")
                        ),
                        rs.getString("image_url"),
                        rs.getString("category_id"),
                        JdbcUtils.getInstant(rs, "start_at"),
                        JdbcUtils.getInstant(rs, "finish_at"),
                        JdbcUtils.getInstant(rs, "created_at"),
                        JdbcUtils.getInstant(rs, "updated_at"),
                        JdbcUtils.getInstant(rs, "deleted_at")
                );
    }
}
