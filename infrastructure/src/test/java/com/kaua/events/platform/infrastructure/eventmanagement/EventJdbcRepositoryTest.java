package com.kaua.events.platform.infrastructure.eventmanagement;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

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
}
