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
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Test
    void givenAValidPersistedOrder_whenCallSave_thenReturnUpdatedOrderAndItems() {
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
        Assertions.assertEquals(2, countOrderItems());
        Assertions.assertEquals(1, savedOrder.getVersion());

        final var updatedItemOne = OrderItem.with(
                itemOne.getId(),
                itemOne.getEventId(),
                itemOne.getTicketId(),
                5,
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(600)
        );

        final var updatedItemTwo = OrderItem.with(
                itemTwo.getId(),
                itemTwo.getEventId(),
                itemTwo.getTicketId(),
                3,
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(210)
        );

        final var updatedOrder = Order.with(
                savedOrder.getId(),
                savedOrder.getVersion(),
                savedOrder.getUserId(),
                List.of(updatedItemOne, updatedItemTwo),
                BigDecimal.valueOf(810),
                savedOrder.getPaymentId().orElse(null),
                OrderStatus.PAID,
                savedOrder.getCreatedAt(),
                savedOrder.getUpdatedAt(),
                null
        );

        final var actualOrder = orderRepository().save(updatedOrder);

        Assertions.assertEquals(savedOrder.getId(), actualOrder.getId());
        Assertions.assertEquals(savedOrder.getVersion() + 1, actualOrder.getVersion());
        Assertions.assertEquals(OrderStatus.PAID, actualOrder.getStatus());

        Assertions.assertEquals(2, countOrderItems());

        final var reloadedOrder = orderRepository()
                .orderOfId(actualOrder.getId().value().toString())
                .orElseThrow();

        Assertions.assertEquals(2, reloadedOrder.getItems().size());

        final var reloadedItemOne = reloadedOrder.getItems().stream()
                .filter(it -> it.getId().equals(updatedItemOne.getId()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(5, reloadedItemOne.getQuantity());
        Assertions.assertEquals(BigDecimal.valueOf(120).setScale(2, RoundingMode.HALF_UP), reloadedItemOne.getUnitPrice());
        Assertions.assertEquals(BigDecimal.valueOf(600).setScale(2, RoundingMode.HALF_UP), reloadedItemOne.getTotalPrice());

        final var reloadedItemTwo = reloadedOrder.getItems().stream()
                .filter(it -> it.getId().equals(updatedItemTwo.getId()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(3, reloadedItemTwo.getQuantity());
        Assertions.assertEquals(BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_UP), reloadedItemTwo.getUnitPrice());
        Assertions.assertEquals(BigDecimal.valueOf(210).setScale(2, RoundingMode.HALF_UP), reloadedItemTwo.getTotalPrice());
    }

    @Test
    void givenAValidOrderButVersionMismatch_whenCallSave_thenThrowsConflictException() {
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

        this.orderRepository().save(aOrder);

        final var aUpdatedOrder = Order.with(
                aOrder.getId(),
                aOrder.getVersion(),
                aOrder.getUserId(),
                aOrder.getItems(),
                aOrder.getTotalAmount(),
                aOrder.getPaymentId().orElse(null),
                OrderStatus.PAID,
                aOrder.getCreatedAt(),
                aOrder.getUpdatedAt(),
                null
        );

        aUpdatedOrder.incrementVersion();

        final var expectedMessage = "Order with identifier %s and version %d does not match, order was updated by another transaction"
                .formatted(aUpdatedOrder.getId().value().toString(), aUpdatedOrder.getVersion());

        final var actualException = Assertions.assertThrows(
                ConflictException.class,
                () -> this.orderRepository().save(aUpdatedOrder)
        );

        Assertions.assertEquals(expectedMessage, actualException.getMessage());
    }
}
