package com.kaua.events.platform.application.usecases.orders.retrive.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.usecases.orders.retrieve.get.DefaultGetOrderByIdUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.get.GetOrderByIdInput;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class GetOrderByIdUseCaseTest extends UseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultGetOrderByIdUseCase useCase;

    @Test
    void givenAValidInput_whenCallsGetOrderById_thenReturnOrder() {
        final var aOrder = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );
        final var aOrderId = aOrder.getId().value().toString();

        final var aInput = GetOrderByIdInput.with(aOrderId);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.of(aOrder));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aOrderId, aOutput.orderId());
        Assertions.assertEquals(aOrder.getUserId().value().toString(), aOutput.userId());
        Assertions.assertEquals(aOrder.getItems().getFirst().getId().toString(), aOutput.items().getFirst().itemId());
        Assertions.assertEquals(aOrder.getTotalAmount(), aOutput.totalAmount());
        Assertions.assertNull(aOutput.paymentId());
        Assertions.assertEquals(aOrder.getStatus().name(), aOutput.status());
        Assertions.assertEquals(aOrder.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aOrder.getUpdatedAt(), aOutput.updatedAt());
        Assertions.assertNull(aOutput.failedAt());

        Mockito.verify(orderRepository, Mockito.times(1)).orderOfId(any());
    }

    @Test
    void givenAnInvalidId_whenCallsGetOrderById_thenThrowNotFoundException() {
        final var aOrderId = ULID.random().toString();

        final var expectedErrorMessage = "Order with id " + aOrderId + " was not found";

        final var aInput = GetOrderByIdInput.with(aOrderId);

        Mockito.when(orderRepository.orderOfId(any()))
                .thenReturn(Optional.empty());

        final var actualException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, actualException.getMessage());

        Mockito.verify(orderRepository, Mockito.times(1)).orderOfId(any());
    }

    @Test
    void givenAInvalidInput_whenCallsGetOrderById_thenThrowUseCaseInputCannotBeNullException() {
        final GetOrderByIdInput aInput = null;

        final var expectedErrorMessage = "Input to GetOrderByIdUseCase cannot be null";

        final var actualException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, actualException.getMessage());

        Mockito.verify(orderRepository, Mockito.times(0)).orderOfId(any());
    }
}
