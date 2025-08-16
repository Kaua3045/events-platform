package com.kaua.events.platform.infrastructure.orders;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.domain.orders.OrderStatus;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class OrderJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(orderRepository());
    }

    @Test
    void givenAValidNewOrder_whenCallSave_thenOrderAndItemsArePersisted() {
        Assertions.assertEquals(0, countOrders());
        Assertions.assertEquals(0, countOrderItems());

        final var aUserId = new UserID(ULID.random());

        final var itemOne = OrderItem.newItem(
                new EventID(ULID.random()),
                new TicketID(ULID.random()),
                2,
                BigDecimal.valueOf(100)
        );
        final var itemTwo = OrderItem.newItem(
                new EventID(ULID.random()),
                new TicketID(ULID.random()),
                1,
                BigDecimal.valueOf(50)
        );

        final var aOrder = Order.newOrder(aUserId, List.of(itemOne, itemTwo));

        final var savedOrder = orderRepository().save(aOrder);

        Assertions.assertEquals(1, countOrders());
        Assertions.assertEquals(aOrder.getId(), savedOrder.getId());
        Assertions.assertEquals(1, savedOrder.getVersion());
        Assertions.assertEquals(aUserId, savedOrder.getUserId());
        Assertions.assertEquals(OrderStatus.CREATED, savedOrder.getStatus());

        Assertions.assertEquals(2, countOrderItems());
    }

    @Test
    void givenAValidNewOrderWithPaymentId_whenCallSave_thenOrderAndItemsArePersisted() {
        Assertions.assertEquals(0, countOrders());
        Assertions.assertEquals(0, countOrderItems());

        final var aUserId = new UserID(ULID.random());

        final var itemOne = OrderItem.newItem(
                new EventID(ULID.random()),
                new TicketID(ULID.random()),
                2,
                BigDecimal.valueOf(100)
        );
        final var itemTwo = OrderItem.newItem(
                new EventID(ULID.random()),
                new TicketID(ULID.random()),
                1,
                BigDecimal.valueOf(50)
        );

        final var aOrder = Order.newOrder(aUserId, List.of(itemOne, itemTwo));
        final var aOrderWithPaymentId = Order.with(
                aOrder.getId(),
                aOrder.getVersion(),
                aOrder.getUserId(),
                aOrder.getItems(),
                aOrder.getTotalAmount(),
                new PaymentID(ULID.random()),
                aOrder.getStatus(),
                aOrder.getCreatedAt(),
                aOrder.getUpdatedAt(),
                aOrder.getFailedAt().orElse(null)
        );

        final var savedOrder = orderRepository().save(aOrderWithPaymentId);

        Assertions.assertEquals(1, countOrders());
        Assertions.assertEquals(aOrder.getId(), savedOrder.getId());
        Assertions.assertEquals(1, savedOrder.getVersion());
        Assertions.assertEquals(aUserId, savedOrder.getUserId());
        Assertions.assertEquals(OrderStatus.CREATED, savedOrder.getStatus());

        Assertions.assertEquals(2, countOrderItems());
    }
}
