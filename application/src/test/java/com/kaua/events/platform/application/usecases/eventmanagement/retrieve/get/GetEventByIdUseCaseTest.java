package com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

class GetEventByIdUseCaseTest extends UseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DefaultGetEventByIdUseCase useCase;

    @Test
    void givenAValidEventId_whenCallGetEventByIdUseCase_thenReturnEvent() {
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());
        final var aEventId = aEvent.getId().value().toString();

        final var aInput = GetEventByIdInput.with(aEventId, aEvent.getOrganizationId().value().toString());

        Mockito.when(eventRepository.eventOfId(aEventId))
                .thenReturn(Optional.of(aEvent));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertEquals(aEventId, aOutput.eventId());
        Assertions.assertEquals(aEvent.getOrganizationId().value().toString(), aOutput.organizationId());
        Assertions.assertEquals(aEvent.getTitle(), aOutput.title());
        Assertions.assertEquals(aEvent.getDescription().get(), aOutput.description());
        Assertions.assertEquals(aEvent.getStatus().name(), aOutput.status());
        Assertions.assertEquals(aEvent.getType().name(), aOutput.type());
        Assertions.assertNull(aOutput.address());
        Assertions.assertNull(aOutput.imageUrl());
        Assertions.assertEquals(aEvent.getCategoryId(), aOutput.categoryId());
        Assertions.assertEquals(aEvent.getStartAt(), aOutput.startAt());
        Assertions.assertEquals(aEvent.getFinishAt(), aOutput.finishAt());
        Assertions.assertEquals(aEvent.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aEvent.getUpdatedAt(), aOutput.updatedAt());
        Assertions.assertNull(aOutput.deletedAt());

        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aEventId);
    }

    @Test
    void givenAValidEventId_whenCallGetEventByIdUseCase_thenReturnEventWithAddress() {
        final var aOrganizationId = new OrganizationID(ULID.random());
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = EventType.IN_PERSON;
        final var aAddress = Address.newAddress(
                "test street",
                "12345A",
                "teste",
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

        final var aEventId = aEvent.getId().value().toString();

        final var aInput = GetEventByIdInput.with(aEventId, aEvent.getOrganizationId().value().toString());

        Mockito.when(eventRepository.eventOfId(aEventId))
                .thenReturn(Optional.of(aEvent));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertEquals(aEventId, aOutput.eventId());
        Assertions.assertEquals(aEvent.getOrganizationId().value().toString(), aOutput.organizationId());
        Assertions.assertEquals(aEvent.getTitle(), aOutput.title());
        Assertions.assertEquals(aEvent.getDescription().get(), aOutput.description());
        Assertions.assertEquals(aEvent.getStatus().name(), aOutput.status());
        Assertions.assertEquals(aEvent.getType().name(), aOutput.type());
        Assertions.assertEquals(aAddress.getStreet(), aOutput.address().street());
        Assertions.assertEquals(aAddress.getNumber(), aOutput.address().number());
        Assertions.assertEquals(aAddress.getComplement().get(), aOutput.address().complement());
        Assertions.assertEquals(aAddress.getNeighborhood(), aOutput.address().neighborhood());
        Assertions.assertEquals(aAddress.getCity(), aOutput.address().city());
        Assertions.assertEquals(aAddress.getState(), aOutput.address().state());
        Assertions.assertEquals(aAddress.getPostalCode(), aOutput.address().postalCode());
        Assertions.assertEquals(aAddress.getCountry(), aOutput.address().country());
        Assertions.assertNull(aOutput.imageUrl());
        Assertions.assertEquals(aEvent.getCategoryId(), aOutput.categoryId());
        Assertions.assertEquals(aEvent.getStartAt(), aOutput.startAt());
        Assertions.assertEquals(aEvent.getFinishAt(), aOutput.finishAt());
        Assertions.assertEquals(aEvent.getCreatedAt(), aOutput.createdAt());
        Assertions.assertEquals(aEvent.getUpdatedAt(), aOutput.updatedAt());
        Assertions.assertNull(aOutput.deletedAt());

        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aEventId);
    }

    @Test
    void givenAnInvalidNullInput_whenCallGetEventByIdUseCase_thenThrowsUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to GetEventByIdUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
    }

    @Test
    void givenAnInvalidEventId_whenCallGetEventByIdUseCase_thenThrowsNotFoundException() {
        final var aId = "1234567890";

        final var expectedErrorMessage = "Event with id 1234567890 was not found";

        final var aInput = GetEventByIdInput.with(aId, ULID.random().toString());

        Mockito.when(eventRepository.eventOfId(aId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aId);
    }
}
