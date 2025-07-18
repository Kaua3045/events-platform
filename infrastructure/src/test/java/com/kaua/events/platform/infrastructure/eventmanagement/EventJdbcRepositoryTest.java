package com.kaua.events.platform.infrastructure.eventmanagement;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventStatus;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.Period;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

class EventJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(eventRepository());
    }

    @Test
    void givenAValidNewEventOnIsTypeIsRemote_whenCallSave_thenEventIsPersisted() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = ULID.random();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                new OrganizationID(aOrganizationId),
                aTitle,
                aDescription,
                EventType.from(aType).get(),
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        final var aActualEvent = this.eventRepository().save(aEvent);

        Assertions.assertEquals(1, countEvents());
        Assertions.assertEquals(aEvent.getId(), aActualEvent.getId());
        Assertions.assertEquals(aEvent.getVersion(), aActualEvent.getVersion());
        Assertions.assertEquals(aEvent.getTitle(), aActualEvent.getTitle());
        Assertions.assertEquals(aEvent.getDescription().get(), aActualEvent.getDescription().get());
        Assertions.assertEquals(aEvent.getStatus(), aActualEvent.getStatus());
        Assertions.assertEquals(aEvent.getType(), aActualEvent.getType());
        Assertions.assertTrue(aActualEvent.getAddress().isEmpty());
        Assertions.assertEquals(aEvent.getCategoryId(), aActualEvent.getCategoryId());
        Assertions.assertEquals(aEvent.getStartAt(), aActualEvent.getStartAt());
        Assertions.assertEquals(aEvent.getFinishAt(), aActualEvent.getFinishAt());
        Assertions.assertEquals(aEvent.getCreatedAt(), aActualEvent.getCreatedAt());
        Assertions.assertEquals(aEvent.getUpdatedAt(), aActualEvent.getUpdatedAt());
    }

    @Test
    void givenAValidNewEventOnIsTypeIsInPerson_whenCallSave_thenEventIsPersisted() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = ULID.random();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "in_person";
        final var aStreet = "event-street";
        final var aNumber = "12345B";
        final var aComplement = "home";
        final var aNeighborhood = "baiiro";
        final var aCity = "city-test";
        final var aState = "state-test";
        final var aPostalCode = "120292831288";
        final var aCountry = "br-tes";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                new OrganizationID(aOrganizationId),
                aTitle,
                aDescription,
                EventType.from(aType).get(),
                Address.newAddress(
                        aStreet,
                        aNumber,
                        aComplement,
                        aNeighborhood,
                        aCity,
                        aState,
                        aPostalCode,
                        aCountry
                ),
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        final var aActualEvent = this.eventRepository().save(aEvent);

        Assertions.assertEquals(1, countEvents());
        Assertions.assertEquals(aEvent.getId(), aActualEvent.getId());
        Assertions.assertEquals(aEvent.getVersion(), aActualEvent.getVersion());
        Assertions.assertEquals(aEvent.getTitle(), aActualEvent.getTitle());
        Assertions.assertEquals(aEvent.getDescription().get(), aActualEvent.getDescription().get());
        Assertions.assertEquals(aEvent.getStatus(), aActualEvent.getStatus());
        Assertions.assertEquals(aEvent.getType(), aActualEvent.getType());
        Assertions.assertEquals(aEvent.getAddress().get(), aActualEvent.getAddress().get());
        Assertions.assertEquals(aEvent.getCategoryId(), aActualEvent.getCategoryId());
        Assertions.assertEquals(aEvent.getStartAt(), aActualEvent.getStartAt());
        Assertions.assertEquals(aEvent.getFinishAt(), aActualEvent.getFinishAt());
        Assertions.assertEquals(aEvent.getCreatedAt(), aActualEvent.getCreatedAt());
        Assertions.assertEquals(aEvent.getUpdatedAt(), aActualEvent.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsTitleInOrganizationId_whenCallExistsByTitleAndOrganizationId_thenReturnFalse() {
        Assertions.assertEquals(0, countEvents());

        final var aTitle = "test";
        final var aOrganizationId = ULID.random().toString();

        final var aResult = this.eventRepository().existsByTitleAndOrganizationId(aTitle, aOrganizationId);

        Assertions.assertFalse(aResult);
    }

    @Test
    void givenAnExistsTitleInOrganizationId_whenCallExistsByTitleAndOrganizationId_thenReturnTrue() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = ULID.random();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                new OrganizationID(aOrganizationId),
                aTitle,
                aDescription,
                EventType.from(aType).get(),
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        this.eventRepository().save(aEvent);

        final var aResult = this.eventRepository().existsByTitleAndOrganizationId(aTitle, aOrganizationId.toString());

        Assertions.assertTrue(aResult);
    }

    @Test
    void givenAValidValues_whenCallListAll_thenReturnPaginatedEvents() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

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

        Assertions.assertEquals(2, countEvents());

        final var aActualResponse = this.eventRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertTrue(aActualResponse.items().contains(aEventOne));
    }

    @Test
    void givenAValidValuesButNoHasData_whenCallListAll_thenReturnEmptyPaginated() {
        Assertions.assertEquals(0, countEvents());

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

        Assertions.assertEquals(0, countEvents());

        final var aActualResponse = this.eventRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertTrue(aActualResponse.items().isEmpty());
    }

    @Test
    void givenAValidValuesWithTerm_whenCallListAll_thenReturnPaginatedEvents() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent("aaaaa", aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent("bbbbbbb", aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);
        this.eventRepository().save(Fixture.EventFixture.newEvent("cccccccc", aOrganizationId, ULID.random().toString()));

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "a";
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

        Assertions.assertEquals(3, countEvents());

        final var aActualResponse = this.eventRepository().listAll(aSearchQuery);

        Assertions.assertEquals(aTotalPages, aActualResponse.metadata().totalPages());
        Assertions.assertEquals(aTotalItems, aActualResponse.metadata().totalItems());
        Assertions.assertEquals(aPage, aActualResponse.metadata().currentPage());
        Assertions.assertEquals(aPerPage, aActualResponse.metadata().perPage());
        Assertions.assertEquals(aEventOne.getId().value(), aActualResponse.items().getFirst().getId().value());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO events (id, organization_id, title, description, status, type, category_id, start_at, finish_at, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '01JRP066XMA9GZZZZHAZZZZZYA', 'title', 'description', 'non-exists', 'REMOTE', '01JRP066XMA9GZZZZHAZZZZZYD', NOW(), DATEADD('MINUTE', 10, NOW()), NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidEventStatus_whenCallListAll_thenThrowsDomainException() {
        Assertions.assertEquals(1, countEvents());

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "title";
        final var aDirection = "asc";
        final var aSort = "created_at";

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        final var aExpectedErrorMessage = "should not be null";
        final var aExpectedErrorProperty = "status";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> this.eventRepository().listAll(aSearchQuery));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO events (id, organization_id, title, description, status, type, category_id, start_at, finish_at, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '01JRP066XMA9GZZZZHAZZZZZYA', 'title', 'description', 'SCHEDULED', 'non-exists', '01JRP066XMA9GZZZZHAZZZZZYD', NOW(), DATEADD('MINUTE', 10, NOW()), NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidEventType_whenCallListAll_thenThrowsDomainException() {
        Assertions.assertEquals(1, countEvents());

        final var aPage = 0;
        final var aPerPage = 10;
        final var aTerms = "title";
        final var aDirection = "asc";
        final var aSort = "created_at";

        final var aSearchQuery = SearchQuery.newSearchQuery(
                aPage,
                aPerPage,
                aTerms,
                aSort,
                aDirection
        );

        final var aExpectedErrorMessage = "should not be null";
        final var aExpectedErrorProperty = "type";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> this.eventRepository().listAll(aSearchQuery));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aException.getErrors().getFirst().property());
    }

    // TODO daqui pra baixo precisa de refactor nos tests

    @Test
    void givenAValidStatusFilter_whenCallListAll_thenReturnOnlyMatchingStatus() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.withStatus(
                Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString()),
                EventStatus.STARTED);
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        final var filters = Map.of("status", "STARTED");

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", null, filters);

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aEventOne.getId().value(), response.items().getFirst().getId().value());
    }

    @Test
    void givenAValidAddressCityFilter_whenCallListAll_thenReturnMatchingCity() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Event.newEvent(
                aOrganizationId,
                "Event One",
                "Description One",
                EventType.REMOTE,
                Address.newAddress(
                        "Street One",
                        "123",
                        "Apt 1",
                        "Neighborhood One",
                        "São Paulo",
                        "SP",
                        "01000-000",
                        "BR"
                ),
                ULID.random().toString(),
                InstantUtils.now().plus(1, ChronoUnit.DAYS),
                InstantUtils.now().plus(2, ChronoUnit.DAYS)
        );
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        final var filters = Map.of("address_city", "São Paulo");

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", null, filters);

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals("São Paulo", response.items().getFirst().getAddress().get().getCity());
    }

    @Test
    void givenAValidMultipleFilters_whenCallListAll_thenReturnFilteredResults() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aCategoryId = ULID.random().toString();
        final var aEventType = "REMOTE";

        final var aEventOne = Event.newEvent(
                aOrganizationId,
                "Tech Event",
                "Description of Tech Event",
                EventType.from(aEventType).get(),
                null,
                aCategoryId,
                InstantUtils.now().plus(1, ChronoUnit.DAYS),
                InstantUtils.now().plus(2, ChronoUnit.DAYS)
        );
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        final var filters = Map.of(
                "categoryId", aCategoryId,
                "eventType", aEventType
        );

        final var query = SearchQuery.newSearchQuery(0, 10, "", "created_at", "asc", null, filters);

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aEventOne.getId().value(), response.items().getFirst().getId().value());
    }

    @Test
    void givenAValidPeriodFilter_whenCallListAll_thenReturnEventsWithinPeriod() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        // Evento dentro do período
        final var aEventInside = Event.newEvent(
                aOrganizationId,
                "Inside Event",
                "Description",
                EventType.REMOTE,
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(5, ChronoUnit.DAYS),     // start_at
                InstantUtils.now().plus(6, ChronoUnit.DAYS)      // finish_at
        );

        // Evento fora do período
        final var aEventOutside = Event.newEvent(
                aOrganizationId,
                "Outside Event",
                "Description",
                EventType.REMOTE,
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(40, ChronoUnit.DAYS),    // start_at
                InstantUtils.now().plus(41, ChronoUnit.DAYS)
        );

        this.eventRepository().save(aEventInside);
        this.eventRepository().save(aEventOutside);

        // Definindo o período entre hoje e +10 dias
        final var period = new Period(
                InstantUtils.now(),
                InstantUtils.now().plus(10, ChronoUnit.DAYS)
        );

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                period                   // period
        );

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aEventInside.getId().value(), response.items().getFirst().getId().value());
    }

    // TODO tesdt
    @Test
    void givenAnInvalidFilterKey_whenCallListAll_thenFilterIsIgnoredAndAllEventsReturned() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        // Filtro não permitido (não existe no allowedFilters)
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

        final var response = this.eventRepository().listAll(query);

        // O filtro não é aplicado, então retorna todos os eventos
        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertTrue(response.items().stream()
                .anyMatch(e -> e.getId().value().equals(aEventOne.getId().value())));
        Assertions.assertTrue(response.items().stream()
                .anyMatch(e -> e.getId().value().equals(aEventTwo.getId().value())));
    }

    @Test
    void givenAFilterValueAsBlank_whenCallListAll_thenFilterIsIgnoredAndAllEventsReturned() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        // Filtro permitido, mas valor blank
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

        final var response = this.eventRepository().listAll(query);

        // O filtro é ignorado, então retorna todos os eventos
        Assertions.assertEquals(2, response.metadata().totalItems());
        Assertions.assertTrue(response.items().stream()
                .anyMatch(e -> e.getId().value().equals(aEventOne.getId().value())));
        Assertions.assertTrue(response.items().stream()
                .anyMatch(e -> e.getId().value().equals(aEventTwo.getId().value())));
    }

    @Test
    void givenAValidAllPossibleFiltersAndPeriod_whenCallListAll_thenReturnFilteredEvents() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(aEventTwo);

        final var filters = new HashMap<String, String>();
        filters.put("status", "SCHEDULED");
        filters.put("eventType", aEventOne.getType().name());

        final var period = new Period(
                InstantUtils.now().minus(10, ChronoUnit.MINUTES),
                InstantUtils.now().plus(30, ChronoUnit.DAYS)
        );

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                period,                   // period
                filters
        );

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(2, response.metadata().totalItems());
    }

    @Test
    void givenAValidEventId_whenCallEventOfId_thenReturnEvent() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());

        this.eventRepository().save(aEvent);

        final var aActualEvent = this.eventRepository().eventOfId(aEvent.getId().value().toString());

        Assertions.assertTrue(aActualEvent.isPresent());
        Assertions.assertEquals(aEvent.getId(), aActualEvent.get().getId());
    }

    @Test
    void givenAnInvalidEventId_whenCallEventOfId_thenReturnEmpty() {
        final var aEventId = ULID.random().toString();

        final var aActualEvent = this.eventRepository().eventOfId(aEventId);

        Assertions.assertTrue(aActualEvent.isEmpty());
    }

    @Test
    void givenAValidPersistedEvent_whenCallSave_thenReturnUpdatedEvent() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());

        this.eventRepository().save(aEvent);

        final var aUpdatedEvent = aEvent.markAsDeleted();

        final var aActualEvent = this.eventRepository().save(aUpdatedEvent);

        Assertions.assertEquals(aEvent.getId(), aActualEvent.getId());
        Assertions.assertEquals(aEvent.getTitle(), aActualEvent.getTitle());
        Assertions.assertEquals(aEvent.getDescription().get(), aActualEvent.getDescription().get());
        Assertions.assertEquals(aEvent.getStatus(), aActualEvent.getStatus());
        Assertions.assertEquals(aEvent.getType(), aActualEvent.getType());
        Assertions.assertEquals(aEvent.getAddress(), aActualEvent.getAddress());
        Assertions.assertEquals(aEvent.getCategoryId(), aActualEvent.getCategoryId());
        Assertions.assertEquals(aEvent.getStartAt(), aActualEvent.getStartAt());
        Assertions.assertEquals(aEvent.getFinishAt(), aActualEvent.getFinishAt());
        Assertions.assertEquals(aEvent.getCreatedAt(), aActualEvent.getCreatedAt());
        Assertions.assertEquals(aEvent.getUpdatedAt(), aActualEvent.getUpdatedAt());
        Assertions.assertTrue(aActualEvent.getDeletedAt().isPresent());
    }

    @Test
    void givenAValidExistsEvent_whenCallSaveButVersionIsNotMatch_thenThrowsConflictException() {
        Assertions.assertEquals(0, countEvents());

        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );

        final var aEventSaved = this.eventRepository().save(aEvent);

        final var expectedErrorMessage = "Event with identifier %s and version 2 does not match, event was updated by another transaction"
                .formatted(aEvent.getId().value());

        Assertions.assertEquals(1, countEvents());

        final var aSavedEventSearched = this.eventRepository()
                .eventOfId(aEventSaved.getId().value().toString())
                .orElseThrow();

        final var aUpdatedEvent = aSavedEventSearched.markAsDeleted();
        aUpdatedEvent.incrementVersion(); // Simulate version mismatch

        final var aEventRepositoryVariable = this.eventRepository(); // Variable to use in lambda, because this.eventRepository() is not allowed in lambda
        // this is a way to test if the exception is thrown, THIS IS A SCAM, in future disable this rule in sonar

        final var aException = Assertions.assertThrows(ConflictException.class,
                () -> aEventRepositoryVariable.save(aUpdatedEvent));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidOrganizationIdFilter_whenCallListAll_thenReturnOnlyAllEventsForOrganizationId() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aEventOne = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aEventTwo = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());

        this.eventRepository().save(aEventOne);
        this.eventRepository().save(Fixture.EventFixture.withStatus(aEventTwo, EventStatus.FINISHED));

        final var filters = Map.of("filters.organizationId", aOrganizationId.value().toString());

        final var query = SearchQuery.newSearchQuery(
                0,                        // page
                10,                       // perPage
                "",                       // terms
                "created_at",             // sort
                "asc",                    // direction
                filters
        );

        final var response = this.eventRepository().listAll(query);

        Assertions.assertEquals(1, response.metadata().totalItems());
        Assertions.assertEquals(aOrganizationId.value(), response.items().getFirst().getOrganizationId().value());
    }
}
