package com.kaua.events.platform.infrastructure.orders;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderID;
import com.kaua.events.platform.domain.orders.OrderItem;
import com.kaua.events.platform.domain.orders.OrderStatus;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Test
    void givenAValidValues_whenCallListAll_thenReturnPaginatedOrders() {
        Assertions.assertEquals(0, countOrders());

        final var userId = new UserID(ULID.random());
        final var orderOne = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                ), Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                )));
        final var orderTwo = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())));

        orderRepository().save(orderOne);
        orderRepository().save(orderTwo);

        final var page = 0;
        final var perPage = 10;
        final var sort = "created_at";
        final var direction = "asc";

        final var query = SearchQuery.newSearchQuery(page, perPage, "", sort, direction);

        Assertions.assertEquals(2, countOrders());

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalPages());
        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertEquals(page, response.metadata().currentPage());
        Assertions.assertEquals(perPage, response.metadata().perPage());
        Assertions.assertTrue(response.items().stream().anyMatch(o -> o.getId().equals(orderOne.getId())));
    }

    @Test
    void givenNoOrders_whenCallListAll_thenReturnEmptyPaginated() {
        Assertions.assertEquals(0, countOrders());

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc");

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(0, response.metadata().totalItems());
        Assertions.assertEquals(0, response.metadata().totalPages());
        Assertions.assertTrue(response.items().isEmpty());
    }

    @Test
    void givenAValidStatusFilter_whenCallListAll_thenReturnOnlyMatchingStatus() {
        final var userId = new UserID(ULID.random());

        final var orderOne = Fixture.OrderFixture.withStatus(userId, OrderStatus.PAID);
        final var orderTwo = Fixture.OrderFixture.withStatus(userId, OrderStatus.CANCELED);

        orderRepository().save(orderOne);
        orderRepository().save(orderTwo);

        final var filters = Map.of("status", "PAID");
        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(orderOne.getId(), response.items().getFirst().getId());
    }

    @Test
    void givenMultipleOrdersWithSameUser_whenCallListAll_thenReturnAllOrders() {
        final var userId = new UserID(ULID.random());

        final var orderOne = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                ), Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                )));
        final var orderTwo = Order.with(
                new OrderID(ULID.random()),
                0L,
                userId,
                List.of(Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                )),
                BigDecimal.valueOf(10),
                new PaymentID(ULID.random()),
                OrderStatus.CREATED,
                InstantUtils.now(),
                InstantUtils.now(),
                null
        );
        final var orderThree = Fixture.OrderFixture.newOrder(new UserID(ULID.random()),
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())));

        orderRepository().save(orderOne);
        orderRepository().save(orderTwo);
        orderRepository().save(orderThree);

        final var filters = Map.of("userId", userId.value().toString());
        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertTrue(response.items().stream().anyMatch(o -> o.getId().equals(orderOne.getId())));
        Assertions.assertTrue(response.items().stream().anyMatch(o -> o.getId().equals(orderTwo.getId())));
        Assertions.assertTrue(response.items().stream().noneMatch(o -> o.getId().equals(orderThree.getId())));
    }

    @Test
    void givenInvalidFilterKey_whenCallListAll_thenReturnAllOrders() {
        final var userId = new UserID(ULID.random());

        final var orderOne = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                ), Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                )));
        final var orderTwo = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())));

        orderRepository().save(orderOne);
        orderRepository().save(orderTwo);

        final var filters = Map.of("invalidKey", "value");
        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(2, response.metadata().totalItems());
    }

    @Test
    void givenBlankFilterValue_whenCallListAll_thenFilterIsIgnored() {
        final var userId = new UserID(ULID.random());

        final var orderOne = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                ), Fixture.OrderFixture.newOrderItem(
                        ULID.random(),
                        ULID.random()
                )));
        final var orderTwo = Fixture.OrderFixture.newOrder(userId,
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random())));

        orderRepository().save(orderOne);
        orderRepository().save(orderTwo);

        final var filters = Map.of("status", "   "); // blank value
        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = orderRepository().listAll(query);

        Assertions.assertEquals(2, response.metadata().totalItems());
    }

    @Test
    void givenAValidOrderId_whenCallOrderOfId_thenOrderAndItemsAreReturned() {
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
        orderRepository().save(aOrder);

        final var aRetrievedOrder = orderRepository().orderOfId(aOrder.getId().value().toString())
                .orElseThrow();

        Assertions.assertEquals(1, countOrders());
        Assertions.assertEquals(aOrder.getId(), aRetrievedOrder.getId());
        Assertions.assertEquals(1, aRetrievedOrder.getVersion());
        Assertions.assertEquals(aUserId, aRetrievedOrder.getUserId());
        Assertions.assertEquals(OrderStatus.CREATED, aRetrievedOrder.getStatus());
        Assertions.assertTrue(aOrder.getItems().containsAll(aRetrievedOrder.getItems()));

        Assertions.assertEquals(2, countOrderItems());
    }

    @Test
    void givenAnInvalidOrderId_whenCallOrderOfId_thenReturnedEmpty() {
        Assertions.assertEquals(0, countOrders());
        Assertions.assertEquals(0, countOrderItems());

        final var aOrderId = ULID.random().toString();

        final var aRetrievedOrder = orderRepository().orderOfId(aOrderId);

        Assertions.assertTrue(aRetrievedOrder.isEmpty());
        Assertions.assertEquals(0, countOrders());
        Assertions.assertEquals(0, countOrderItems());
    }
}
