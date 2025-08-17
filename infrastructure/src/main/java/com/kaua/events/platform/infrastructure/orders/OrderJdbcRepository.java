package com.kaua.events.platform.infrastructure.orders;

import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.domain.orders.OrderStatus;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.users.UserID;
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
public class OrderJdbcRepository implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OrderJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public Pagination<Order> listAll(final SearchQuery query) {
        final var allowedFilters = Map.of(
                "status", "status",
                "userId", "user_id"
        );

        List<String> columns = List.of(
                "o.id AS order_id",
                "o.version AS order_version",
                "o.user_id AS order_user_id",
                "o.total_amount AS order_total_amount",
                "o.payment_id AS order_payment_id",
                "o.status AS order_status",
                "o.created_at AS order_created_at",
                "o.updated_at AS order_updated_at",
                "o.failed_at AS order_failed_at",
                "i.id AS item_id",
                "i.event_id AS item_event_id",
                "i.ticket_id AS item_ticket_id",
                "i.quantity AS item_quantity",
                "i.unit_price AS item_unit_price",
                "i.total_price AS item_total_price"
        );

        final var allowedSortFields = List.of("o.created_at", "o.updated_at", "o.total_amount");

        var spec = buildFiltersSpecification(query.filters(), allowedFilters)
                .orElse(DynamicQueryListBuilder.Specification.where(null));

        final var joinSpec = DynamicQueryListBuilder.leftJoin(
                "order_items",
                "i",
                "o.id = i.order_id"
        );

        final var finalSpec = spec.and(joinSpec);

        final var dynamicQuery = DynamicQueryListBuilder.build(
                "orders o",
                query,
                finalSpec,
                allowedSortFields,
                columns
        );

        final var countSql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1");
        final Map<String, Object> countParams = new HashMap<>();
        finalSpec.apply(countSql, countParams);

        final var items = this.databaseClient.count(countSql.toString(), countParams);
        final var totalPages = (int) Math.ceil((double) items / query.perPage());

        final var aPaginatedItems = this.databaseClient.query(dynamicQuery.sql(), dynamicQuery.params(), orderMapper());

        final var metadata = new PaginationMetadata(
                query.page(),
                query.perPage(),
                totalPages,
                items
        );

        return new Pagination<>(metadata, consolidateOrders(aPaginatedItems));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Order save(final Order order) {
        if (order.getVersion() == 0) {
            log.debug("Creating new order: {}", order);
            create(order);
            batchInsertItems(order.getId().value().toString(), order.getItems());
            log.info("Created new order: {}", order);
        }

        order.incrementVersion();
        return order;
    }

    private void create(final Order aOrder) {
        final var aSql = """
                INSERT INTO orders (
                id,
                version,
                user_id,
                total_amount,
                payment_id,
                status,
                created_at,
                updated_at,
                failed_at
                )
                VALUES (
                :id,
                (:version + 1),
                :user_id,
                :total_amount,
                :payment_id,
                :status,
                :createdAt,
                :updatedAt,
                :failed_at
                )
                """;

        executeUpdateOrder(aSql, aOrder);
    }

    private int executeUpdateOrder(final String aSql, final Order aOrder) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aOrder.getId().value().toString());
        aParams.put("version", aOrder.getVersion());
        aParams.put("user_id", aOrder.getUserId().value().toString());
        aParams.put("total_amount", aOrder.getTotalAmount());
        aParams.put("payment_id", aOrder.getPaymentId()
                .map(it -> it.value().toString()).orElse(null));
        aParams.put("status", aOrder.getStatus().name());
        aParams.put("createdAt", aOrder.getCreatedAt());
        aParams.put("updatedAt", aOrder.getUpdatedAt());
        aParams.put("failed_at", aOrder.getFailedAt().orElse(null));

        return this.databaseClient.update(aSql, aParams);
    }

    private void batchInsertItems(final String orderId, List<OrderItem> items) {
        var sql = """
                    INSERT INTO order_items (
                        id,
                        order_id,
                        event_id,
                        ticket_id,
                        quantity,
                        unit_price,
                        total_price
                    ) VALUES (
                        :id,
                        :order_id,
                        :event_id,
                        :ticket_id,
                        :quantity,
                        :unit_price,
                        :total_price
                    )
                """;

        var batchParams = items.stream().map(item -> {
            Map<String, Object> params = new HashMap<>();
            params.put("id", item.getId().toString());
            params.put("order_id", orderId);
            params.put("event_id", item.getEventId().value().toString());
            params.put("ticket_id", item.getTicketId().value().toString());
            params.put("quantity", item.getQuantity());
            params.put("unit_price", item.getUnitPrice());
            params.put("total_price", item.getTotalPrice());
            return params;
        }).toList();

        log.debug("Creating {} items", items.size());

        this.databaseClient.batchUpdate(sql, batchParams);
    }

    private RowMap<Order> orderMapper() {
        return rs -> {
            final var aItemId = rs.getString("item_id");
            List<OrderItem> aItems = new ArrayList<>();

            aItems.add(OrderItem.with(
                    ULID.fromString(aItemId),
                    new EventID(ULID.fromString(rs.getString("item_event_id"))),
                    new TicketID(ULID.fromString(rs.getString("item_ticket_id"))),
                    rs.getInt("item_quantity"),
                    rs.getBigDecimal("item_unit_price"),
                    rs.getBigDecimal("item_total_price")
            ));

            return Order.with(
                    new OrderID(ULID.fromString(rs.getString("order_id"))),
                    rs.getLong("order_version"),
                    new UserID(ULID.fromString(rs.getString("order_user_id"))),
                    aItems,
                    rs.getBigDecimal("order_total_amount"),
                    rs.getString("order_payment_id") != null ? new PaymentID(ULID.fromString(rs.getString("order_payment_id")))
                            : null,
                    OrderStatus.from(rs.getString("order_status")).orElse(null),
                    JdbcUtils.getInstant(rs, "order_created_at"),
                    JdbcUtils.getInstant(rs, "order_updated_at"),
                    JdbcUtils.getInstant(rs, "order_failed_at"));
        };
    }

    private List<Order> consolidateOrders(List<Order> rows) {
        Map<OrderID, Order> grouped = new LinkedHashMap<>();

        for (Order row : rows) {
            grouped.compute(row.getId(), (id, existing) -> {
                if (existing == null) return row; // primeira vez: adiciona o Order
                existing.addAllItem(row.getItems()); // acumula os itens
                return existing;
            });
        }

        return new ArrayList<>(grouped.values());
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
}
