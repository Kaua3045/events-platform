package com.kaua.events.platform.application.usecases.eventmanagement.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class CreateEventUseCaseTest extends UseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DefaultCreateEventUseCase useCase;

    @Test
    void givenAValidValuesWithTypeIsRemote_whenCallCreateEventUseCase_thenReturnOrgIdAndEventId() {
        final var aOrganizationId = ULID.random().toString();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aInput = CreateEventInput.with(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Mockito.when(eventRepository.existsByTitleAndOrganizationId(aTitle, aOrganizationId))
                .thenReturn(false);
        Mockito.when(eventRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.eventId());
        Assertions.assertEquals(aOrganizationId, aOutput.organizationId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("createEventUseCase"), Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(1)).existsByTitleAndOrganizationId(aTitle, aOrganizationId);
        Mockito.verify(eventRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.equals(aOrganizationId, aCmd.getOrganizationId().value().toString())
                        && Objects.equals(aTitle, aCmd.getTitle())
                        && Objects.equals(aDescription, aCmd.getDescription().get())
                        && Objects.equals(aType, aCmd.getType().name().toLowerCase())
                        && Objects.equals(aCategoryId, aCmd.getCategoryId())
                        && Objects.equals(aStartAt, aCmd.getStartAt())
                        && Objects.equals(aFinishAt, aCmd.getFinishAt())));
    }

    @Test
    void givenAValidValuesWithTypeIsInPerson_whenCallCreateEventUseCase_thenReturnOrgIdAndEventId() {
        final var aOrganizationId = ULID.random().toString();
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

        final var aAddressInput = CreateEventAddressInput.with(
                aStreet,
                aNumber,
                aComplement,
                aNeighborhood,
                aCity,
                aState,
                aPostalCode,
                aCountry
        );

        final var aInput = CreateEventInput.with(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                aAddressInput,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Mockito.when(eventRepository.existsByTitleAndOrganizationId(aTitle, aOrganizationId))
                .thenReturn(false);
        Mockito.when(eventRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.eventId());
        Assertions.assertEquals(aOrganizationId, aOutput.organizationId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("createEventUseCase"), Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(1)).existsByTitleAndOrganizationId(aTitle, aOrganizationId);
        Mockito.verify(eventRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.equals(aOrganizationId, aCmd.getOrganizationId().value().toString())
                        && Objects.equals(aTitle, aCmd.getTitle())
                        && Objects.equals(aDescription, aCmd.getDescription().get())
                        && Objects.equals(aType, aCmd.getType().name().toLowerCase())
                        && Objects.equals(aStreet, aCmd.getAddress().get().getStreet())
                        && Objects.equals(aCategoryId, aCmd.getCategoryId())
                        && Objects.equals(aStartAt, aCmd.getStartAt())
                        && Objects.equals(aFinishAt, aCmd.getFinishAt())));
    }

    @Test
    void givenAnInvalidExistsTitleInOrganization_whenCallCreateEventUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random().toString();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "remote";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aExpectedErrorMessage = "Already exists other event using this name";

        final var aInput = CreateEventInput.with(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Mockito.when(eventRepository.existsByTitleAndOrganizationId(aTitle, aOrganizationId))
                .thenReturn(true);

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1)).existsByTitleAndOrganizationId(aTitle, aOrganizationId);
        Mockito.verify(eventRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventType_whenCallCreateEventUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random().toString();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "invalid";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aExpectedErrorMessage = "Event type invalid was not found";

        final var aInput = CreateEventInput.with(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Mockito.when(eventRepository.existsByTitleAndOrganizationId(aTitle, aOrganizationId))
                .thenReturn(false);

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1)).existsByTitleAndOrganizationId(aTitle, aOrganizationId);
        Mockito.verify(eventRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidNullAddressOnEventTypeIsInPerson_whenCallCreateEventUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random().toString();
        final var aTitle = "event-title";
        final var aDescription = "event-description";
        final var aType = "in_person";
        final var aCategoryId = ULID.random().toString();
        final var aStartAt = InstantUtils.now().plus(10, ChronoUnit.MINUTES);
        final var aFinishAt = InstantUtils.now().plus(10, ChronoUnit.DAYS);

        final var aExpectedErrorMessage = "Address required on event type is IN_PERSON";

        final var aInput = CreateEventInput.with(
                aOrganizationId,
                aTitle,
                aDescription,
                aType,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt
        );

        Mockito.when(eventRepository.existsByTitleAndOrganizationId(aTitle, aOrganizationId))
                .thenReturn(false);

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1)).existsByTitleAndOrganizationId(aTitle, aOrganizationId);
        Mockito.verify(eventRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void givenAnInvalidInput_whenCallCreateEventUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateEventUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(eventRepository, Mockito.never()).existsByTitleAndOrganizationId(Mockito.any(), Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }
}
