package com.kaua.events.platform.application.usecases.orders.update.status;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.orders.OrderStatus;
import com.kaua.events.platform.domain.payments.PaymentID;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class UpdateOrderStatusUseCaseTest extends UseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultUpdateOrderStatusUseCase useCase;

    @Test
    void givenANullInput_whenCallsExecute_thenThrowException() {
        final UpdateOrderStatusInput aInput = null;

        final var expectedMessage = "Input to UpdateOrderStatusUseCase cannot be null";

        final var actualException = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> useCase.execute(aInput)
        );

        Assertions.assertEquals(expectedMessage, actualException.getMessage());

        Mockito.verify(orderRepository, Mockito.never()).orderOfId(any());
    }

    @Test
    void givenANonExistentOrder_whenCallsExecute_thenThrowNotFoundException() {
        final var aOrderId = ULID.random().toString();
        final var aInput = UpdateOrderStatusInput.with(aOrderId, null, "PAID");

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.empty());

        final var expectedMessage = "Order with id " + aOrderId + " was not found";

        final var actualException = Assertions.assertThrows(
                NotFoundException.class,
                () -> useCase.execute(aInput)
        );

        Assertions.assertEquals(expectedMessage, actualException.getMessage());

        Mockito.verify(orderRepository, Mockito.times(1)).orderOfId(any());
        Mockito.verify(orderRepository, Mockito.never()).save(any());
    }

    @Test
    void givenAValidInputWithStatusWaitingAndPaymentIdFromInput_whenCallsExecute_thenUpdateOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );
        final var aOrderId = aOrder.getId().value().toString();
        final var aPaymentId = ULID.random().toString();

        final var aInput = UpdateOrderStatusInput.with(aOrderId, "WAITING", aPaymentId);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = Assertions.assertDoesNotThrow(() -> useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aOrderId, aOutput.orderId());
        Assertions.assertEquals("PAYMENT_PENDING", aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAValidInputWithStatusWaitingAndPaymentIdNull_whenCallsExecute_thenUseOrderPaymentId() {
        final var aPaymentId = new PaymentID(ULID.random());
        final var aUserId = new UserID(ULID.random());
        final var aOrder = Fixture.OrderFixture.newOrderWithPaymentId(aUserId, aPaymentId);

        final var aOrderId = aOrder.getId().value().toString();
        final var aInput = UpdateOrderStatusInput.with(aOrderId, "WAITING", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = Assertions.assertDoesNotThrow(() -> useCase.execute(aInput));

        Assertions.assertEquals("PAYMENT_PENDING", aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAValidInputWithStatusApproved_whenCallsExecute_thenUpdateOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aInput = UpdateOrderStatusInput.with(aOrder.getId().value().toString(), "APPROVED", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = useCase.execute(aInput);

        Assertions.assertEquals(OrderStatus.PAYMENT_APPROVED.name(), aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAValidInputWithStatusIdentified_whenCallsExecute_thenUpdateOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aInput = UpdateOrderStatusInput.with(aOrder.getId().value().toString(), "IDENTIFIED", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = useCase.execute(aInput);

        Assertions.assertEquals(OrderStatus.PAYMENT_APPROVED.name(), aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAValidInputWithStatusPaid_whenCallsExecute_thenUpdateOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aInput = UpdateOrderStatusInput.with(aOrder.getId().value().toString(), "PAID", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = useCase.execute(aInput);

        Assertions.assertEquals(OrderStatus.PAID.name(), aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAValidInputWithStatusFailed_whenCallsExecute_thenUpdateOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aInput = UpdateOrderStatusInput.with(aOrder.getId().value().toString(), "FAILED", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = useCase.execute(aInput);

        Assertions.assertEquals(OrderStatus.FAILED.name(), aOutput.status());

        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    void givenAnInvalidStatus_whenCallsExecute_thenThrowDomainException() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aInput = UpdateOrderStatusInput.with(aOrder.getId().value().toString(), "INVALID_STATUS", null);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var actualException = Assertions.assertThrows(DomainException.class,
                () -> useCase.execute(aInput));

        Assertions.assertEquals("Invalid order status INVALID_STATUS", actualException.getMessage());

        Mockito.verify(orderRepository, Mockito.never()).save(any());
    }
}
