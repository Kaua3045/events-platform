package com.kaua.events.platform.domain.orders;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.orders.events.OrderCreatedEvent;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.payments.PixPaymentDetails;
import com.kaua.events.platform.domain.ticket.TicketID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class OrderTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewOrder_thenInstantiateAnOrder() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aTotalPriceItem = BigDecimal.valueOf(105.50);
        final var aQuantity = 10;

        final var aOrderItemOne = OrderItem.newItem(
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem
        );

        final var aItems = List.of(aOrderItemOne, OrderItem.newItem(
                aEventId,
                new TicketID(ULID.random()),
                5,
                BigDecimal.valueOf(5.55)
        ));

        final var aOrder = Order.newOrder(
                aUserId,
                aItems
        );

        Assertions.assertNotNull(aOrder);
        Assertions.assertNotNull(aOrder.getId());
        Assertions.assertEquals(aUserId, aOrder.getUserId());
        Assertions.assertTrue(aOrder.getItems().containsAll(aItems));
        Assertions.assertEquals(BigDecimal.valueOf(133.25), aOrder.getTotalAmount());
        Assertions.assertTrue(aOrder.getPaymentId().isEmpty());
        Assertions.assertEquals(OrderStatus.CREATED, aOrder.getStatus());
        Assertions.assertNotNull(aOrder.getCreatedAt());
        Assertions.assertNotNull(aOrder.getUpdatedAt());
        Assertions.assertTrue(aOrder.getFailedAt().isEmpty());
        Assertions.assertEquals(aTotalPriceItem.setScale(2, RoundingMode.HALF_UP), aOrderItemOne.getTotalPrice());
        Assertions.assertDoesNotThrow(() -> aOrder.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWith_thenInstantiateAnOrder() {
        final var aOrderId = new OrderID(ULID.random());
        final var aVersion = 0L;
        final var aUserId = new UserID(ULID.random());
        final var aNow = InstantUtils.now();
        final var aOrderStatus = OrderStatus.CREATED;
        final var aPaymentId = new PaymentID(ULID.random());

        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aTotalPriceItem = BigDecimal.valueOf(105.5);
        final var aQuantity = 10;
        final var aOrderItemId = ULID.random();

        final var aItems = List.of(OrderItem.with(
                aOrderItemId,
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem,
                aTotalPriceItem
        ));

        final var aOrder = Order.with(
                aOrderId,
                aVersion,
                aUserId,
                aItems,
                aTotalPriceItem,
                aPaymentId,
                aOrderStatus,
                aNow,
                aNow,
                null
        );

        Assertions.assertNotNull(aOrder);
        Assertions.assertEquals(aOrderId, aOrder.getId());
        Assertions.assertEquals(aUserId, aOrder.getUserId());
        Assertions.assertEquals(BigDecimal.valueOf(105.50).setScale(2, RoundingMode.HALF_UP), aOrder.getTotalAmount());
        Assertions.assertEquals(aPaymentId, aOrder.getPaymentId().get());
        Assertions.assertEquals(OrderStatus.CREATED, aOrder.getStatus());
        Assertions.assertEquals(aNow, aOrder.getCreatedAt());
        Assertions.assertEquals(aNow, aOrder.getUpdatedAt());
        Assertions.assertTrue(aOrder.getFailedAt().isEmpty());
        Assertions.assertEquals(aTotalPriceItem.setScale(2, RoundingMode.HALF_UP), aItems.getFirst().getTotalPrice());
        Assertions.assertEquals(aUnitPriceItem.setScale(2, RoundingMode.HALF_UP), aItems.getFirst().getUnitPrice());
    }

    @Test
    void testCallToStringInOrder() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aQuantity = 10;

        final var aOrderItemOne = OrderItem.newItem(
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem
        );

        final var aItems = List.of(aOrderItemOne, OrderItem.newItem(
                aEventId,
                new TicketID(ULID.random()),
                5,
                BigDecimal.valueOf(5.55)
        ));

        final var aOrder = Order.newOrder(
                aUserId,
                aItems
        );

        Assertions.assertDoesNotThrow(aOrderItemOne::toString);
        Assertions.assertDoesNotThrow(aOrder::toString);
    }

    @Test
    void givenAValidValue_whenCallOrderStatusFrom_thenReturnOrderStatus() {
        final var aOrderStatus = OrderStatus.from("CREATED");

        Assertions.assertNotNull(aOrderStatus);
        Assertions.assertEquals(OrderStatus.CREATED, aOrderStatus.get());
    }

    @Test
    void givenAnInvalidValue_whenCallOrderStatusFrom_thenReturnEmpty() {
        final var aOrderStatus = OrderStatus.from("INVALID");

        Assertions.assertTrue(aOrderStatus.isEmpty());
    }

    @Test
    void givenAnNegativeTotalAmount_whenCallNewOrder_thenThrowValidationException() {
        final var expectedErrorMessage = "cannot be negative";
        final var expectedErrorProperty = "totalAmount";

        final var aOrderId = new OrderID(ULID.random());
        final var aVersion = 0L;
        final var aUserId = new UserID(ULID.random());
        final var aNow = InstantUtils.now();
        final var aOrderStatus = OrderStatus.CREATED;
        final var aPaymentId = new PaymentID(ULID.random());

        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aTotalPriceItem = BigDecimal.valueOf(105.5);
        final var aQuantity = 10;
        final var aOrderItemId = ULID.random();

        final var aItems = List.of(OrderItem.with(
                aOrderItemId,
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem,
                aTotalPriceItem
        ));

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> Order.with(
                        aOrderId,
                        aVersion,
                        aUserId,
                        aItems,
                        BigDecimal.valueOf(-1),
                        aPaymentId,
                        aOrderStatus,
                        aNow,
                        aNow,
                        null
                ));

        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    void givenAnNegativeUnitPrice_whenCallNewOrderItem_thenThrowValidationException() {
        final var expectedErrorMessage = "cannot be negative";
        final var expectedErrorProperty = "unitPrice";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> OrderItem.with(
                        ULID.random(),
                        new EventID(ULID.random()),
                        new TicketID(ULID.random()),
                        1,
                        BigDecimal.valueOf(-1),
                        BigDecimal.valueOf(10)
                ));

        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    void givenAnNegativeTotalPrice_whenCallNewOrderItem_thenThrowValidationException() {
        final var expectedErrorMessage = "cannot be negative";
        final var expectedErrorProperty = "totalPrice";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> OrderItem.with(
                        ULID.random(),
                        new EventID(ULID.random()),
                        new TicketID(ULID.random()),
                        1,
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(-1)
                ));

        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    void givenAValidValues_whenCallNewOrderItem_thenInstantiateOrderItem() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aTotalPriceItem = BigDecimal.valueOf(105.5);
        final var aQuantity = 10;
        final var aOrderItemId = ULID.random();

        final var aItem = OrderItem.with(
                aOrderItemId,
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem,
                aTotalPriceItem
        );

        Assertions.assertEquals(aOrderItemId, aItem.getId());
        Assertions.assertEquals(aEventId, aItem.getEventId());
        Assertions.assertEquals(aTicketId, aItem.getTicketId());
        Assertions.assertEquals(aQuantity, aItem.getQuantity());
    }

    @Test
    void givenAValidOrderCreatedEvent_whenCallRegisterEvent_thenReturnOrderWithEvents() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aQuantity = 10;

        final var aOrderItemOne = OrderItem.newItem(
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem
        );

        final var aItems = List.of(aOrderItemOne, OrderItem.newItem(
                aEventId,
                new TicketID(ULID.random()),
                5,
                BigDecimal.valueOf(5.55)
        ));

        final var aOrder = Order.newOrder(
                aUserId,
                aItems
        );

        final var aEvent = new OrderCreatedEvent(
                aOrder.getId().value().toString(),
                aOrder.getVersion(),
                aOrder.getStatus().name(),
                aOrder.getTotalAmount(),
                new PixPaymentDetails(aOrder.getTotalAmount()),
                "1234-traceId"
        );

        aOrder.registerEvent(aEvent);

        Assertions.assertEquals(1, aOrder.getDomainEvents().size());
        Assertions.assertTrue(aOrder.getDomainEvents().contains(aEvent));
    }

    @Test
    void givenAValidPaymentId_whenCallUpdatePaymentId_thenReturnOrderUpdated() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aQuantity = 10;

        final var aOrderItemOne = OrderItem.newItem(
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem
        );

        final var aItems = List.of(aOrderItemOne, OrderItem.newItem(
                aEventId,
                new TicketID(ULID.random()),
                5,
                BigDecimal.valueOf(5.55)
        ));

        final var aOrder = Order.newOrder(
                aUserId,
                aItems
        );

        final var aPaymentId = new PaymentID(ULID.random());
        final var anUpdatedAt = aOrder.getUpdatedAt();

        final var anOrderUpdated = aOrder.updatePaymentId(aPaymentId);

        Assertions.assertNotNull(anOrderUpdated);
        Assertions.assertEquals(aOrder.getId(), anOrderUpdated.getId());
        Assertions.assertEquals(aOrder.getUserId(), anOrderUpdated.getUserId());
        Assertions.assertEquals(aOrder.getItems(), anOrderUpdated.getItems());
        Assertions.assertEquals(aOrder.getTotalAmount(), anOrderUpdated.getTotalAmount());
        Assertions.assertEquals(aPaymentId, anOrderUpdated.getPaymentId().get());
        Assertions.assertEquals(aOrder.getStatus(), anOrderUpdated.getStatus());
        Assertions.assertEquals(aOrder.getCreatedAt(), anOrderUpdated.getCreatedAt());
        Assertions.assertTrue(anUpdatedAt.isBefore(anOrderUpdated.getUpdatedAt()));
        Assertions.assertEquals(aOrder.getFailedAt(), anOrderUpdated.getFailedAt());
    }

    @Test
    void givenAValidOrder_whenCallMarkAsFailed_thenReturnOrderFailed() {
        final var aEventId = new EventID(ULID.random());
        final var aTicketId = new TicketID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aUnitPriceItem = BigDecimal.valueOf(10.55);
        final var aQuantity = 10;

        final var aOrderItemOne = OrderItem.newItem(
                aEventId,
                aTicketId,
                aQuantity,
                aUnitPriceItem
        );

        final var aItems = List.of(aOrderItemOne, OrderItem.newItem(
                aEventId,
                new TicketID(ULID.random()),
                5,
                BigDecimal.valueOf(5.55)
        ));

        final var aOrder = Order.newOrder(
                aUserId,
                aItems
        );

        final var anUpdatedAt = aOrder.getUpdatedAt();

        final var anOrderFailed = aOrder.markAsFailed();

        Assertions.assertNotNull(anOrderFailed);
        Assertions.assertEquals(aOrder.getId(), anOrderFailed.getId());
        Assertions.assertEquals(aOrder.getUserId(), anOrderFailed.getUserId());
        Assertions.assertEquals(aOrder.getItems(), anOrderFailed.getItems());
        Assertions.assertEquals(aOrder.getTotalAmount(), anOrderFailed.getTotalAmount());
        Assertions.assertTrue(anOrderFailed.getPaymentId().isEmpty());
        Assertions.assertEquals(OrderStatus.FAILED, anOrderFailed.getStatus());
        Assertions.assertEquals(aOrder.getCreatedAt(), anOrderFailed.getCreatedAt());
        Assertions.assertTrue(anUpdatedAt.isBefore(anOrderFailed.getUpdatedAt()));
        Assertions.assertTrue(anOrderFailed.getFailedAt().isPresent());
    }
}