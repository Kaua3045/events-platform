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
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
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
    public Optional<Ticket> ticketOfId(final String id) {
        final var aSql = "SELECT * FROM tickets WHERE id = :id";
        return this.databaseClient.queryOne(aSql, Map.of("id", id), ticketMapper());
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

        boolean isSearchingForDeleted = "DELETED".equalsIgnoreCase(query.filters().get("status"));

        if (!isSearchingForDeleted) {
            spec = Optional.of(
                    spec.orElse(DynamicQueryListBuilder.Specification.where(null))
                            .and(DynamicQueryListBuilder.notEqual("status", "status_deleted", "DELETED"))
            );
        }

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
        } else {
            log.debug("Updating ticket: {}", ticket);
            update(ticket);
            log.info("Updated ticket: {}", ticket);
        }

        ticket.incrementVersion();
        return ticket;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Ticket> saveAll(final List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return Collections.emptyList();
        }

        final var sql = """
                UPDATE tickets
                SET
                    version = (:version + 1),
                    event_id = :eventId,
                    name = :name,
                    description = :description,
                    price = :price,
                    quantity = :quantity,
                    sold = :sold,
                    type = :type,
                    status = :status,
                    created_at = :createdAt,
                    updated_at = :updatedAt,
                    deleted_at = :deletedAt
                WHERE id = :id AND version = :version
                """;

        var batchParams = tickets.stream().map(aTicket -> {
            Map<String, Object> aParams = new HashMap<>();
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
            aParams.put("deletedAt", aTicket.getDeletedAt().orElse(null));
            return aParams;
        }).toList();

        log.debug("Updating {} tickets", tickets.size());

        int[] updateResults = this.databaseClient.batchUpdate(sql, batchParams);

        // Verifica se algum update falhou
        for (int i = 0; i < updateResults.length; i++) {
            if (updateResults[i] == 0) {
                var ticket = tickets.get(i);
                throw ConflictException.with(
                        "Ticket with identifier %s and version %d does not match, ticket was updated by another transaction"
                                .formatted(ticket.getId().value(), ticket.getVersion())
                );
            }
        }

        tickets.forEach(Ticket::incrementVersion); // TODO in future change this to use in update method in aggregaste

        return tickets;
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
                updated_at,
                deleted_at
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
                :updatedAt,
                :deletedAt
                )
                """;

        executeUpdate(aSql, aTicket);
    }

    private void update(final Ticket aTicket) {
        final var aSql = """
                UPDATE tickets
                SET
                version = (:version + 1),
                event_id = :eventId,
                name = :name,
                description = :description,
                price = :price,
                quantity = :quantity,
                sold = :sold,
                type = :type,
                status = :status,
                created_at = :createdAt,
                updated_at = :updatedAt,
                deleted_at = :deletedAt
                WHERE id = :id AND version = :version
                """;

        if (executeUpdate(aSql, aTicket) == 0) {
            throw ConflictException.with("Ticket with identifier %s and version %d does not match, ticket was updated by another transaction"
                    .formatted(aTicket.getId().value(), aTicket.getVersion()));
        }
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
        aParams.put("deletedAt", aTicket.getDeletedAt().orElse(null));

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
                JdbcUtils.getInstant(rs, "updated_at"),
                JdbcUtils.getInstant(rs, "deleted_at")
        );
    }
}
