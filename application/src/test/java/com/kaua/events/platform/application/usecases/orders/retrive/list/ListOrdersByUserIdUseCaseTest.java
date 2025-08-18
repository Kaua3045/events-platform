package com.kaua.events.platform.application.usecases.orders.retrive.list;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrderRepository;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.DefaultListOrdersByUserIdUseCase;
import com.kaua.events.platform.application.usecases.orders.retrieve.list.ListOrdersByUserIdOutput;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.orders.Order;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

class ListOrdersByUserIdUseCaseTest extends UseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultListOrdersByUserIdUseCase useCase;

    @Test
    void givenAnInvalidNullInput_whenCallListOrdersByUserId_thenThrowUseCaseInputCannotBeNullException() {
        final var expectedErrorMessage = "Input to ListOrdersByUserIdUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void givenAValidInput_whenCallListOrdersByUserId_thenReturnPagination() {
        final var aOrderOne = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );
        final var aOrderTwo = Fixture.OrderFixture.newOrder(
                List.of(Fixture.OrderFixture.newOrderItem(ULID.random(), ULID.random()))
        );

        final var aOrders = List.of(aOrderOne, aOrderTwo);

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "createdAt";
        final var aDirection = "desc";
        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, aOrders.size());
        final var aPagination = new Pagination<>(aMetadata, aOrders);

        final var aItemsCount = 2;
        final var aResult = aPagination.map(ListOrdersByUserIdOutput::from);

        Mockito.when(orderRepository.listAll(aSearchQuery)).thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertEquals(aResult.items(), aOutput.items());
        Assertions.assertEquals(aResult.metadata(), aOutput.metadata());

        Mockito.verify(orderRepository, Mockito.times(1)).listAll(aSearchQuery);
    }

    @Test
    void givenAValidInputButHasNoData_whenCallListOrdersByUserId_thenReturnEmptyData() {
        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "createdAt";
        final var aDirection = "asc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, 0);
        final var aPagination = new Pagination<Order>(aMetadata, List.of());

        final var aItemsCount = 0;

        Mockito.when(orderRepository.listAll(aSearchQuery))
                .thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertTrue(aOutput.items().isEmpty());
        Assertions.assertEquals(aMetadata, aOutput.metadata());

        Mockito.verify(orderRepository, Mockito.times(1)).listAll(aSearchQuery);
    }
}
