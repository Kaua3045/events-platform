package com.kaua.events.platform.domain.eventmanagement;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

class EventTest extends UnitTest {

    @Test
    void givenAValidValuesAndTypeIsRemote_whenCallNewEvent_thenReturnEvent() {
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = EventType.REMOTE;
        final var aCategoryId = UUID.randomUUID().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.HOURS);
        final var aFinishAt = InstantUtils.now().plus(5, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Assertions.assertNotNull(aEvent.getId());
        Assertions.assertEquals(0, aEvent.getVersion());
        Assertions.assertEquals(aOrganizationId, aEvent.getOrganizationId());
        Assertions.assertEquals(aTitle, aEvent.getTitle());
        Assertions.assertEquals(aDescription, aEvent.getDescription().get());
        Assertions.assertEquals(EventStatus.SCHEDULED, aEvent.getStatus());
        Assertions.assertEquals(aType, aEvent.getType());
        Assertions.assertTrue(aEvent.getAddress().isEmpty());
        Assertions.assertTrue(aEvent.getImageUrl().isEmpty());
        Assertions.assertEquals(aCategoryId, aEvent.getCategoryId());
        Assertions.assertEquals(aStartAt, aEvent.getStartAt());
        Assertions.assertEquals(aFinishAt, aEvent.getFinishAt());
        Assertions.assertNotNull(aEvent.getCreatedAt());
        Assertions.assertNotNull(aEvent.getUpdatedAt());
        Assertions.assertTrue(aEvent.getDeletedAt().isEmpty());
        Assertions.assertDoesNotThrow(() -> aEvent.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValuesAndTypeIsInPerson_whenCallNewEvent_thenReturnEvent() {
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = EventType.IN_PERSON;
        final var aAddress = Address.newAddress(
                "test street",
                "12345A",
                null,
                "Bairro test",
                "POA TEST",
                "RS",
                "101010101010000",
                "BR"
        );
        final var aCategoryId = UUID.randomUUID().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.HOURS);
        final var aFinishAt = InstantUtils.now().plus(5, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                aAddress,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Assertions.assertNotNull(aEvent.getId());
        Assertions.assertEquals(0, aEvent.getVersion());
        Assertions.assertEquals(aOrganizationId, aEvent.getOrganizationId());
        Assertions.assertEquals(aTitle, aEvent.getTitle());
        Assertions.assertEquals(aDescription, aEvent.getDescription().get());
        Assertions.assertEquals(EventStatus.SCHEDULED, aEvent.getStatus());
        Assertions.assertEquals(aType, aEvent.getType());
        Assertions.assertEquals(aAddress, aEvent.getAddress().get());
        Assertions.assertTrue(aEvent.getImageUrl().isEmpty());
        Assertions.assertEquals(aCategoryId, aEvent.getCategoryId());
        Assertions.assertEquals(aStartAt, aEvent.getStartAt());
        Assertions.assertEquals(aFinishAt, aEvent.getFinishAt());
        Assertions.assertNotNull(aEvent.getCreatedAt());
        Assertions.assertNotNull(aEvent.getUpdatedAt());
        Assertions.assertTrue(aEvent.getDeletedAt().isEmpty());
        Assertions.assertDoesNotThrow(() -> aEvent.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWith_thenReturnEvent() {
        final var aId = new EventID(ULID.random());
        final var aVersion = 0L;
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aStatus = EventStatus.SCHEDULED;
        final var aType = EventType.IN_PERSON;
        final var aAddress = Address.newAddress(
                "test street",
                "12345A",
                null,
                "Bairro test",
                "POA TEST",
                "RS",
                "101010101010000",
                "BR"
        );
        final var aCategoryId = UUID.randomUUID().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.HOURS);
        final var aFinishAt = InstantUtils.now().plus(5, ChronoUnit.DAYS);
        final var aNow = InstantUtils.now();

        final var aEvent = Event.with(
                aId,
                aVersion,
                aOrganizationId,
                aTitle,
                aDescription,
                aStatus,
                aType,
                aAddress,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt,
                aNow,
                aNow,
                null
        );

        Assertions.assertEquals(aId, aEvent.getId());
        Assertions.assertEquals(aVersion, aEvent.getVersion());
        Assertions.assertEquals(aOrganizationId, aEvent.getOrganizationId());
        Assertions.assertEquals(aTitle, aEvent.getTitle());
        Assertions.assertEquals(aDescription, aEvent.getDescription().get());
        Assertions.assertEquals(EventStatus.SCHEDULED, aEvent.getStatus());
        Assertions.assertEquals(aType, aEvent.getType());
        Assertions.assertEquals(aAddress, aEvent.getAddress().get());
        Assertions.assertTrue(aEvent.getImageUrl().isEmpty());
        Assertions.assertEquals(aCategoryId, aEvent.getCategoryId());
        Assertions.assertEquals(aStartAt, aEvent.getStartAt());
        Assertions.assertEquals(aFinishAt, aEvent.getFinishAt());
        Assertions.assertNotNull(aEvent.getCreatedAt());
        Assertions.assertNotNull(aEvent.getUpdatedAt());
        Assertions.assertTrue(aEvent.getDeletedAt().isEmpty());
        Assertions.assertDoesNotThrow(() -> aEvent.validate(NotificationHandler.create()));
    }

    @Test
    void testCallToStringInEventClass() {
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = EventType.IN_PERSON;
        final var aAddress = Address.newAddress(
                "test street",
                "12345A",
                null,
                "Bairro test",
                "POA TEST",
                "RS",
                "101010101010000",
                "BR"
        );
        final var aCategoryId = UUID.randomUUID().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.HOURS);
        final var aFinishAt = InstantUtils.now().plus(5, ChronoUnit.DAYS);

        final var aEvent = Event.newEvent(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                aAddress,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        final var aEventToString = Assertions.assertDoesNotThrow(aEvent::toString);

        Assertions.assertNotNull(aEventToString);
    }

    @Test
    void testCallFromInEventStatus() {
        final var aStatus = "SCHEDULED";

        Assertions.assertTrue(() -> EventStatus.from(aStatus).isPresent());
    }

    @Test
    void testCallFromInEventType() {
        final var aType = "REMOTE";

        Assertions.assertTrue(() -> EventType.from(aType).isPresent());
    }
}
