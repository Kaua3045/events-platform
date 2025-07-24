package com.kaua.events.platform.infrastructure.ticket;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.Map;

class TicketJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(ticketRepository());
    }

    @Test
    void givenAValidNewTicket_whenCallSave_thenTicketIsPersisted() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = ULID.random();
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var aPrice = BigDecimal.valueOf(100.00);
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aTicket = Ticket.newTicket(
                new EventID(aEventId),
                aName,
                aDescription,
                aPrice,
                aQuantity,
                TicketType.from(aType).get(),
                TicketStatus.from(aStatus).get()
        );

        final var aActualTicket = this.ticketRepository().save(aTicket);

        Assertions.assertEquals(1, countTickets());
        Assertions.assertEquals(aTicket.getId(), aActualTicket.getId());
        Assertions.assertEquals(aTicket.getVersion(), aActualTicket.getVersion());
        Assertions.assertEquals(aTicket.getName(), aActualTicket.getName());
        Assertions.assertEquals(aTicket.getDescription().get(), aActualTicket.getDescription().get());
        Assertions.assertEquals(aTicket.getPrice(), aActualTicket.getPrice());
        Assertions.assertEquals(aTicket.getQuantity(), aActualTicket.getQuantity());
        Assertions.assertEquals(aTicket.getType(), aActualTicket.getType());
        Assertions.assertEquals(aTicket.getStatus(), aActualTicket.getStatus());
        Assertions.assertEquals(aTicket.getCreatedAt(), aActualTicket.getCreatedAt());
        Assertions.assertEquals(aTicket.getUpdatedAt(), aActualTicket.getUpdatedAt());
    }

    @Test
    void givenAValidPersistedTicket_whenCallSave_thenReturnUpdatedTicket() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = ULID.random();
        final var aTicket = Fixture.TicketFixture.newTicket(new EventID(aEventId));

        this.ticketRepository().save(aTicket);

        final var aUpdatedTicket = aTicket.update(
                "updated-ticket-name",
                "updated-ticket-description",
                BigDecimal.valueOf(150.00),
                20,
                TicketType.VIP,
                TicketStatus.INACTIVE
        );

        final var aActualTicket = this.ticketRepository().save(aUpdatedTicket);

        Assertions.assertEquals(aTicket.getId(), aActualTicket.getId());
        Assertions.assertEquals(aTicket.getVersion() + 1, aActualTicket.getVersion());
        Assertions.assertEquals(aUpdatedTicket.getName(), aActualTicket.getName());
        Assertions.assertEquals(aUpdatedTicket.getDescription().get(), aActualTicket.getDescription().get());
        Assertions.assertEquals(aUpdatedTicket.getPrice(), aActualTicket.getPrice());
        Assertions.assertEquals(aUpdatedTicket.getQuantity(), aActualTicket.getQuantity());
        Assertions.assertEquals(aUpdatedTicket.getType(), aActualTicket.getType());
        Assertions.assertEquals(aUpdatedTicket.getStatus(), aActualTicket.getStatus());
        Assertions.assertEquals(aUpdatedTicket.getCreatedAt(), aActualTicket.getCreatedAt());
        Assertions.assertTrue(aActualTicket.getUpdatedAt().isAfter(aUpdatedTicket.getCreatedAt()));
    }

    @Test
    void givenAValidExistsEvent_whenCallSaveButVersionIsNotMatch_thenThrowsConflictException() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = ULID.random();
        final var aTicket = Fixture.TicketFixture.newTicket(new EventID(aEventId));

        final var aTicketSaved = this.ticketRepository().save(aTicket);

        final var expectedErrorMessage = "Ticket with identifier %s and version 2 does not match, ticket was updated by another transaction"
                .formatted(aTicket.getId().value());

        Assertions.assertEquals(1, countTickets());

        final var aSavedTicketSearched = this.ticketRepository()
                .ticketOfId(aTicketSaved.getId().value().toString())
                .orElseThrow();

        final var aUpdatedTicket = aSavedTicketSearched.update(
                "updated-ticket-name",
                "updated-ticket-description",
                BigDecimal.valueOf(150.00),
                20,
                TicketType.VIP,
                TicketStatus.INACTIVE
        );
        aUpdatedTicket.incrementVersion(); // Simulate version mismatch

        final var aTicketRepositoryVariable = this.ticketRepository(); // Variable to use in lambda, because this.ticketRepository() is not allowed in lambda
        // this is a way to test if the exception is thrown, THIS IS A SCAM, in future disable this rule in sonar

        final var aException = Assertions.assertThrows(ConflictException.class,
                () -> aTicketRepositoryVariable.save(aUpdatedTicket));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidTicketId_whenCallTickerOfId_thenReturnTicket() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = ULID.random();
        final var aTicket = Fixture.TicketFixture.newTicket(new EventID(aEventId));

        this.ticketRepository().save(aTicket);

        Assertions.assertEquals(1, countTickets());

        final var aActualTicket = this.ticketRepository().ticketOfId(aTicket.getId().value().toString())
                .orElseThrow();

        Assertions.assertEquals(aTicket.getId(), aActualTicket.getId());
        Assertions.assertEquals(aTicket.getVersion(), aActualTicket.getVersion());
        Assertions.assertEquals(aTicket.getName(), aActualTicket.getName());
        Assertions.assertEquals(aTicket.getDescription().get(), aActualTicket.getDescription().get());
        Assertions.assertEquals(aTicket.getPrice(), aActualTicket.getPrice());
        Assertions.assertEquals(aTicket.getQuantity(), aActualTicket.getQuantity());
        Assertions.assertEquals(aTicket.getSold(), aActualTicket.getSold());
        Assertions.assertEquals(aTicket.getType(), aActualTicket.getType());
        Assertions.assertEquals(aTicket.getStatus(), aActualTicket.getStatus());
        Assertions.assertEquals(aTicket.getCreatedAt(), aActualTicket.getCreatedAt());
        Assertions.assertEquals(aTicket.getUpdatedAt(), aActualTicket.getUpdatedAt());
    }

    @Test
    void givenAnInvalidTicketId_whenCallTickerOfId_thenReturnEmpty() {
        Assertions.assertEquals(0, countTickets());

        final var aActualTicket = this.ticketRepository().ticketOfId("invalid-id");

        Assertions.assertTrue(aActualTicket.isEmpty());
    }

    @Test
    void givenAValidValues_whenCallListAll_thenReturnPaginatedTickets() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.newTicket(aEventId);
        final var aTicketTwo = Fixture.TicketFixture.newTicket(aEventId);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "";
        final var aDirection = "asc";
        final var aSort = "created_at";
        final var aTotalPages = 1;
        final var aTotalItems = 2;

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        Assertions.assertEquals(2, countTickets());

        final var aActualResponse = this.ticketRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertTrue(aActualResponse.items().contains(aTicketOne));
    }

    @Test
    void givenAValidValuesButNoHasData_whenCallListAll_thenReturnEmptyPaginated() {
        Assertions.assertEquals(0, countTickets());

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "";
        final var aDirection = "asc";
        final var aSort = "created_at";
        final var aTotalPages = 0;
        final var aTotalItems = 0;

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        Assertions.assertEquals(0, countTickets());

        final var aActualResponse = this.ticketRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertTrue(aActualResponse.items().isEmpty());
    }

    @Test
    void givenAValidValuesWithTerms_whenCallListAll_thenReturnPaginatedTickets() {
        Assertions.assertEquals(0, countTickets());

        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.withName(aEventId, "vip-teste");
        final var aTicketTwo = Fixture.TicketFixture.newTicket(aEventId);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);
        this.ticketRepository().save(Fixture.TicketFixture.newTicket(aEventId));

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "vi";
        final var aDirection = "asc";
        final var aSort = "created_at";
        final var aTotalPages = 1;
        final var aTotalItems = 1;

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        Assertions.assertEquals(3, countTickets());

        final var aActualResponse = this.ticketRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertTrue(aActualResponse.items().contains(aTicketOne));
    }

    @Test
    @Sql(statements = {
            "INSERT INTO tickets (id, event_id, name, description, price, quantity, type, status, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '01JRP066XMA9GZZZZHAZZZZZYD', 'ticket-name', 'ticket-description', 100.00, 10, 'non-exists', 'AVAILABLE', NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidTicketType_whenCallListAll_thenThrowsDomainException() {
        Assertions.assertEquals(1, countTickets());

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "ticket-name";
        final var aDirection = "asc";
        final var aSort = "created_at";

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        final var aExpectedErrorMessage = "cannot be null";
        final var aExpectedErrorProperty = "type";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> this.ticketRepository().listAll(aSearchQuery));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO tickets (id, event_id, name, description, price, quantity, type, status, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '01JRP066XMA9GZZZZHAZZZZZYD', 'ticket-name', 'ticket-description', 100.00, 10, 'standard', 'non-exists', NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidTicketStatus_whenCallListAll_thenThrowsDomainException() {
        Assertions.assertEquals(1, countTickets());

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "ticket-name";
        final var aDirection = "asc";
        final var aSort = "created_at";

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        final var aExpectedErrorMessage = "cannot be null";
        final var aExpectedErrorProperty = "status";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> this.ticketRepository().listAll(aSearchQuery));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    void givenAValidStatusFilter_whenCallListAll_thenReturnOnlyMatchingStatus() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.withStatus(
                aEventId,
                TicketStatus.AVAILABLE);
        final var aTicketTwo = Fixture.TicketFixture.withStatus(aEventId, TicketStatus.INACTIVE);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        final var filters = Map.of("status", "AVAILABLE");

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = this.ticketRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aTicketOne.getId(), response.items().getFirst().getId());
    }

    @Test
    void givenAValidMultipleFilters_whenCallListAll_thenReturnFilteredResults() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.withType(
                aEventId,
                TicketType.VIP);
        final var aTicketTwo = Fixture.TicketFixture.withStatus(aEventId, TicketStatus.INACTIVE);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        final var filters = Map.of(
                "type", "VIP",
                "status", "AVAILABLE"
        );

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", filters);

        final var response = this.ticketRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aTicketOne.getId(), response.items().getFirst().getId());
    }

    @Test
    void givenAnInvalidFilterKey_whenCallListAll_thenFilterIsIgnoredAndAllTicketsReturned() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.newTicket(aEventId);
        final var aTicketTwo = Fixture.TicketFixture.newTicket(aEventId);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        // Invalid filter key (not in allowedFilters)
        final var filters = Map.of("invalid_key", "someValue");

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                null,                     // period
                filters
        );

        final var response = this.ticketRepository().listAll(query);

        // Filter is ignored, so all tickets are returned
        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertTrue(response.items().stream()
                .anyMatch(t -> t.getId().value().equals(aTicketOne.getId().value())));
        Assertions.assertTrue(response.items().stream()
                .anyMatch(t -> t.getId().value().equals(aTicketTwo.getId().value())));
    }

    @Test
    void givenAFilterValueAsBlank_whenCallListAll_thenFilterIsIgnoredAndAllTicketsReturned() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.newTicket(aEventId);
        final var aTicketTwo = Fixture.TicketFixture.newTicket(aEventId);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        // Allowed filter, but value is blank
        final var filters = Map.of("status", "   ");

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                null,                     // period
                filters
        );

        final var response = this.ticketRepository().listAll(query);

        // Filter is ignored, so all tickets are returned
        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertTrue(response.items().stream()
                .anyMatch(t -> t.getId().value().equals(aTicketOne.getId().value())));
        Assertions.assertTrue(response.items().stream()
                .anyMatch(t -> t.getId().value().equals(aTicketTwo.getId().value())));
    }

    @Test
    void givenAValidEventIdFilter_whenCallListAll_thenReturnOnlyTicketsForEventId() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.newTicket(aEventId);
        final var aTicketTwo = Fixture.TicketFixture.newTicket(new EventID(ULID.random()));

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        final var filters = Map.of("eventId", aEventId.value().toString());

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                filters
        );

        final var response = this.ticketRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aEventId.value(), response.items().getFirst().getEventId().value());
    }

    @Test
    void givenAValidSortByQuantity_whenCallListAll_thenReturnTicketsSortedBySold() {
        final var aEventId = new EventID(ULID.random());

        final var aTicketOne = Fixture.TicketFixture.withQuantity(aEventId, 10);
        final var aTicketTwo = Fixture.TicketFixture.withQuantity(aEventId, 5);

        this.ticketRepository().save(aTicketOne);
        this.ticketRepository().save(aTicketTwo);

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "quantity",                   // sort
                "desc"                     // direction
        );

        final var response = this.ticketRepository().listAll(query);

        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertEquals(aTicketOne.getId(), response.items().getFirst().getId());
    }
}
