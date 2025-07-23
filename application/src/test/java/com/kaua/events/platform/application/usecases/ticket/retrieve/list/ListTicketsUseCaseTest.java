package com.kaua.events.platform.application.usecases.ticket.retrieve.list;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.ticket.Ticket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

class ListTicketsUseCaseTest extends UseCaseTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private DefaultListTicketsUseCase useCase;

    @Test
    void givenAnInvalidNullInput_whenCallListTicketsUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var expectedErrorMessage = "Input to ListTicketsUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verifyNoInteractions(ticketRepository);
    }

    @Test
    void givenAValidInput_whenCallListTicketsUseCase_thenReturnPagination() {
        final var aTicketOne = Fixture.TicketFixture.newTicket();
        final var aTicketTwo = Fixture.TicketFixture.newTicket();

        final var aTickets = List.of(aTicketOne, aTicketTwo);

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "name";
        final var aDirection = "asc";
        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);
        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, aTickets.size());
        final var aPagination = new Pagination<>(aMetadata, aTickets);

        final var aItemsCount = 2;
        final var aResult = aPagination.map(ListTicketsOutput::from);

        Mockito.when(ticketRepository.listAll(aSearchQuery)).thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertEquals(aResult.items(), aOutput.items());
        Assertions.assertEquals(aResult.metadata(), aOutput.metadata());

        Mockito.verify(ticketRepository, Mockito.times(1)).listAll(aSearchQuery);
    }

    @Test
    void givenAValidInputButHasNoData_whenCallListTicketsUseCase_thenReturnEmptyData() {
        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "name";
        final var aDirection = "asc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, 0);
        final var aPagination = new Pagination<Ticket>(aMetadata, List.of());

        final var aItemsCount = 0;

        Mockito.when(ticketRepository.listAll(aSearchQuery))
                .thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertTrue(aOutput.items().isEmpty());
        Assertions.assertEquals(aMetadata, aOutput.metadata());

        Mockito.verify(ticketRepository, Mockito.times(1)).listAll(aSearchQuery);
    }
}
