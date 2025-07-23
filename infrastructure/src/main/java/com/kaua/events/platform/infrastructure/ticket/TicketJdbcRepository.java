package com.kaua.events.platform.infrastructure.ticket;

import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
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
public class TicketJdbcRepository implements TicketRepository {

    private static final Logger log = LoggerFactory.getLogger(TicketJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public TicketJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public Pagination<Ticket> listAll(final SearchQuery query) {
        final var allowedFilters = Map.of(
                "status", "status",
                "type", "type",
                "eventId", "event_id"
        );

        final var allowedSortFields = List.of("name", "price", "sold", "quantity", "created_at");

        var spec = Optional.ofNullable(query.terms())
                .filter(terms -> !terms.isBlank())
                .map(this::assembleSpecification)
                .map(DynamicQueryListBuilder.Specification::where);

        spec = spec.map(it -> buildFiltersSpecification(query.filters(), allowedFilters)
                        .map(it::and)
                        .orElse(it))
                .or(() -> buildFiltersSpecification(query.filters(), allowedFilters));

        var finalSpec = spec.orElse(DynamicQueryListBuilder.Specification.where(null));

        final var dynamicQuery = DynamicQueryListBuilder.build(
                "tickets",
                query,
                finalSpec,
                allowedSortFields
        );

        final var countSql = new StringBuilder("SELECT COUNT(*) FROM tickets WHERE 1=1");
        final Map<String, Object> countParams = new HashMap<>();
        finalSpec.apply(countSql, countParams);

        final var items = this.databaseClient.count(countSql.toString(), countParams);
        final var totalPages = (int) Math.ceil((double) items / query.perPage());

        final var aPaginatedItems = this.databaseClient.query(dynamicQuery.sql(), dynamicQuery.params(), ticketMapper());

        final var metadata = new PaginationMetadata(
                query.page(),
                query.perPage(),
                totalPages,
                items
        );

        return new Pagination<>(metadata, aPaginatedItems);
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

    private DynamicQueryListBuilder.Specification assembleSpecification(final String terms) {
        return DynamicQueryListBuilder.like("name", "terms_name", terms)
                .or(DynamicQueryListBuilder.like("description", "terms_description", terms));
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
                .map(entry -> DynamicQueryListBuilder.equal(
                        allowedFilters.get(entry.getKey()),
                        entry.getKey().replace(".", "_"),
                        entry.getValue()
                ))
                .reduce(DynamicQueryListBuilder.Specification::and);
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

    private RowMap<Ticket> ticketMapper() {
        return rs -> Ticket.with(
                new TicketID(ULID.fromString(rs.getString("id"))),
                rs.getLong("version"),
                rs.getString("name"),
                rs.getString("description"),
                new EventID(ULID.fromString(rs.getString("event_id"))),
                rs.getBigDecimal("price"),
                rs.getInt("quantity"),
                rs.getInt("sold"),
                TicketType.from(rs.getString("type")).orElse(null),
                TicketStatus.from(rs.getString("status")).orElse(null),
                JdbcUtils.getInstant(rs, "created_at"),
                JdbcUtils.getInstant(rs, "updated_at")
        );
    }
}
