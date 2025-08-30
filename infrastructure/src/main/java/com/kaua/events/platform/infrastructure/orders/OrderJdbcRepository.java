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
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderJdbcRepository implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderJdbcRepository.class);

    private final DatabaseClient databaseClient;
    private final OutboxJdbcRepository outboxRepository;

    public OrderJdbcRepository(
            final DatabaseClient databaseClient,
            OutboxJdbcRepository outboxRepository
    ) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Order> orderOfId(final String id) {
        final var aOrderSql = "SELECT * FROM orders WHERE id = :id";

        final var aOrder = this.databaseClient.queryOne(
                aOrderSql,
                Map.of("id", id),
                orderRowMap()
        );

        if (aOrder.isEmpty()) {
            return Optional.empty();
        }

        final var aOrderItemsSql = "SELECT * FROM order_items WHERE order_id = :orderId";

        final var aOrderItems = this.databaseClient.query(
                aOrderItemsSql,
                Map.of("orderId", id),
                orderItemRowMap()
        ).stream().map(it -> OrderItem.with(
                it.orderItemId(),
                it.eventId(),
                it.ticketId(),
                it.quantity(),
                it.unitPrice(),
                it.totalPrice()
        )).toList();

        aOrder.get().addAllItem(aOrderItems);

        return aOrder;
    }

    @Override
    public Pagination<Order> listAll(final SearchQuery query) {
        final var allowedFilters = Map.of(
                "status", "status",
                "userId", "user_id"
        );

        final var allowedSortFields = List.of("created_at", "updated_at", "total_amount");

        var spec = buildFiltersSpecification(query.filters(), allowedFilters)
                .orElse(DynamicQueryListBuilder.Specification.where(null));

        final var dynamicQuery = DynamicQueryListBuilder.build(
                "orders",
                query,
                spec,
                allowedSortFields
        );

        final var countSql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1");
        final Map<String, Object> countParams = new HashMap<>();
        spec.apply(countSql, countParams);

        final var itemsCount = this.databaseClient.count(countSql.toString(), countParams);
        final var totalPages = (int) Math.ceil((double) itemsCount / query.perPage());

        final var aOrders = this.databaseClient.query(dynamicQuery.sql(), dynamicQuery.params(), orderRowMap());

        final var metadata = new PaginationMetadata(
                query.page(),
                query.perPage(),
                totalPages,
                itemsCount
        );

        if (aOrders.isEmpty()) {
            return new Pagination<>(metadata, List.of());
        }

        final var aOrderIds = aOrders.stream().map(it -> it.getId().value().toString())
                .toList();

        final var itemsSql = "SELECT * FROM order_items WHERE order_id IN (:order_ids)";

        Map<String, Object> params = Map.of("order_ids", aOrderIds);

        final var aItems = this.databaseClient.query(
                itemsSql,
                params,
                orderItemRowMap()
        );

        final var aItemsByOrder = aItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItemDTO::orderId,
                        Collectors.mapping(dto -> OrderItem.with(
                                dto.orderItemId,
                                dto.eventId,
                                dto.ticketId,
                                dto.quantity,
                                dto.unitPrice,
                                dto.totalPrice
                        ), Collectors.toList())
                ));

        aOrders.forEach(order -> {
            var its = aItemsByOrder.getOrDefault(order.getId(), List.of());
            order.addAllItem(its);
        });

        return new Pagination<>(metadata, aOrders);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Order save(final Order order) {
        if (order.getVersion() == 0) {
            log.debug("Creating new order: {}", order);
            create(order);
            batchInsertItems(order.getId().value().toString(), order.getItems());
            this.outboxRepository.save(order.getDomainEvents());
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

    private RowMap<Order> orderRowMap() {
        return rs -> Order.with(
                new OrderID(ULID.fromString(rs.getString("id"))),
                rs.getLong("version"),
                new UserID(ULID.fromString(rs.getString("user_id"))),
                new ArrayList<>(),
                rs.getBigDecimal("total_amount"),
                rs.getString("payment_id") != null
                        ? new PaymentID(ULID.fromString(rs.getString("payment_id")))
                        : null,
                OrderStatus.from(rs.getString("status")).orElse(null),
                JdbcUtils.getInstant(rs, "created_at"),
                JdbcUtils.getInstant(rs, "updated_at"),
                JdbcUtils.getInstant(rs, "failed_at")
        );
    }

    private RowMap<OrderItemDTO> orderItemRowMap() {
        return rs -> new OrderItemDTO(
                new OrderID(ULID.fromString(rs.getString("order_id"))),
                ULID.fromString(rs.getString("id")),
                new EventID(ULID.fromString(rs.getString("event_id"))),
                new TicketID(ULID.fromString(rs.getString("ticket_id"))),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("total_price")
        );
    }

    public record OrderItemDTO(
            OrderID orderId,
            ULID orderItemId,
            EventID eventId,
            TicketID ticketId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
    }
}
