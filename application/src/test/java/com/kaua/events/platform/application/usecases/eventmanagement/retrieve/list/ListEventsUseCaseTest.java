package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.PaginationMetadata;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

class ListEventsUseCaseTest extends UseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DefaultListEventsUseCase useCase;

    @Test
    void givenAnInvalidNullInput_whenCallListEventsUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var expectedErrorMessage = "Input to ListEventsUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verifyNoInteractions(eventRepository);
    }

    @Test
    void givenAValidInput_whenCallListEventsUseCase_thenReturnPagination() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aCategoryId = ULID.random().toString();

        final var aEventOne = Fixture.EventFixture.newEvent(
                aOrganizationId,
                aCategoryId
        );
        final var aEventTwo = Fixture.EventFixture.newEvent(
                aOrganizationId,
                aCategoryId
        );

        final var aEvents = List.of(aEventOne, aEventTwo);

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "title";
        final var aDirection = "asc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, aEvents.size());
        final var aPagination = new Pagination<>(aMetadata, aEvents);

        final var aItemsCount = 2;
        final var aResult = aPagination.map(ListEventsOutput::from);

        Mockito.when(eventRepository.listAll(aSearchQuery)).thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertEquals(aResult.items(), aOutput.items());
        Assertions.assertEquals(aResult.metadata(), aOutput.metadata());

        Mockito.verify(eventRepository, Mockito.times(1)).listAll(aSearchQuery);
    }

    @Test
    void givenAValidInputButHasNoData_whenCallListEventsUseCase_thenReturnEmptyData() {
        final var aPage = 0;
        final var aPerPage = 10;
        final var aTotalPages = 1;
        final var aTerms = "";
        final var aSort = "title";
        final var aDirection = "asc";

        final var aSearchQuery = SearchQuery.newSearchQuery(aPage, aPerPage, aTerms, aSort, aDirection);

        final var aMetadata = new PaginationMetadata(aPage, aPerPage, aTotalPages, 0);
        final var aPagination = new Pagination<Event>(aMetadata, List.of());

        final var aItemsCount = 0;

        Mockito.when(eventRepository.listAll(aSearchQuery))
                .thenReturn(aPagination);

        final var aOutput = this.useCase.execute(aSearchQuery);

        Assertions.assertEquals(aItemsCount, aOutput.metadata().totalItems());
        Assertions.assertTrue(aOutput.items().isEmpty());
        Assertions.assertEquals(aMetadata, aOutput.metadata());

        Mockito.verify(eventRepository, Mockito.times(1)).listAll(aSearchQuery);
    }
}
