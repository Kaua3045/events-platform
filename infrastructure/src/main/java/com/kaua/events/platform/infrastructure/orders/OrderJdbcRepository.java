package com.kaua.events.platform.infrastructure.orders;

import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OrderJdbcRepository implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OrderJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
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
}
