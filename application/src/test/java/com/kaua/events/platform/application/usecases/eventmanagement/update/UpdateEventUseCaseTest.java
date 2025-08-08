package com.kaua.events.platform.application.usecases.eventmanagement.update;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateEventUseCaseTest extends UseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultUpdateEventUseCase useCase;

    @Test
    void givenAValidInputChangingTypeToInPerson_whenCallUpdateEventUseCase_thenUpdateEventSuccessfully() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.REMOTE
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newOwnerMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random())
        );

        Assertions.assertTrue(aEvent.getAddress().isEmpty());

        final var aInputAddress = UpdateEventAddressInput.with(
                "rua teste",
                "1234",
                "aaa",
                "bairro x",
                "cidade teste",
                "sp",
                "13210000",
                "BR"
        );

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Title",
                "New Description",
                EventType.IN_PERSON.name(),
                aInputAddress,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(1)).save(argThat(aCmd ->
                Objects.equals(aCmd.getTitle(), aInput.title())
                        && Objects.equals(aCmd.getDescription().get(), aInput.description())
                        && Objects.equals(aCmd.getType(), EventType.IN_PERSON)
                        && Objects.equals(aCmd.getCategoryId(), aInput.categoryId())
                        && Objects.equals(aCmd.getAddress().get().getStreet(), aInputAddress.street())
                        && Objects.equals(aCmd.getAddress().get().getNumber(), aInputAddress.number())
                        && Objects.equals(aCmd.getAddress().get().getComplement().get(), aInputAddress.complement())
                        && Objects.equals(aCmd.getAddress().get().getNeighborhood(), aInputAddress.neighborhood())
                        && Objects.equals(aCmd.getAddress().get().getCity(), aInputAddress.city())
                        && Objects.equals(aCmd.getAddress().get().getState(), aInputAddress.state())
                        && Objects.equals(aCmd.getAddress().get().getPostalCode(), aInputAddress.postalCode())
                        && Objects.equals(aCmd.getAddress().get().getCountry(), aInputAddress.country())
                        && Objects.equals(aCmd.getStartAt(), aInput.startAt())
                        && Objects.equals(aCmd.getFinishAt(), aInput.finishAt())));
    }

    @Test
    void givenAValidInputChangingTypeToRemote_whenCallUpdateEventUseCase_thenUpdateEventSuccessfully() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEventWithAddress(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.IN_PERSON
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newOwnerMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random())
        );

        Assertions.assertTrue(aEvent.getAddress().isPresent());

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(1)).save(argThat(aCmd ->
                Objects.equals(aCmd.getTitle(), aInput.title())
                        && Objects.equals(aCmd.getDescription().get(), aInput.description())
                        && Objects.equals(aCmd.getType(), EventType.REMOTE)
                        && Objects.equals(aCmd.getCategoryId(), aInput.categoryId())
                        && aCmd.getAddress().isEmpty()
                        && Objects.equals(aCmd.getStartAt(), aInput.startAt())
                        && Objects.equals(aCmd.getFinishAt(), aInput.finishAt())));
    }

    @Test
    void givenAValidInputChangingTypeToRemoteAndPassOldAddress_whenCallUpdateEventUseCase_thenUpdateEventSuccessfully() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEventWithAddress(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.IN_PERSON
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newOwnerMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random())
        );

        Assertions.assertTrue(aEvent.getAddress().isPresent());

        final var aInputAddress = UpdateEventAddressInput.with(
                "rua teste",
                "1234",
                "aaa",
                "bairro x",
                "cidade teste",
                "sp",
                "13210000",
                "BR"
        );

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                aInputAddress,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(1)).save(argThat(aCmd ->
                Objects.equals(aCmd.getTitle(), aInput.title())
                        && Objects.equals(aCmd.getDescription().get(), aInput.description())
                        && Objects.equals(aCmd.getType(), EventType.REMOTE)
                        && Objects.equals(aCmd.getCategoryId(), aInput.categoryId())
                        && aCmd.getAddress().isEmpty()
                        && Objects.equals(aCmd.getStartAt(), aInput.startAt())
                        && Objects.equals(aCmd.getFinishAt(), aInput.finishAt())));
    }

    @Test
    void givenNullInput_whenCallUpdateEventUseCase_thenThrowException() {
        final var expectedMessage = "Input to UpdateEventUseCase cannot be null";

        final var exception = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedMessage, exception.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
    }

    @Test
    void givenAnInvalidUserId_whenCallUpdateEventUseCase_thenThrowNotFoundException() {
        final var aUserId = ULID.random().toString();

        final var expectedErrorMessage = "OrganizationMember with id " + aUserId + " was not found";

        final var aInput = UpdateEventInput.with(
                aUserId,
                ULID.random().toString(),
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(0)).eventOfId(any());
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidEventId_whenCallUpdateEventUseCase_thenThrowNotFoundException() {
        final var aEventId = ULID.random().toString();
        final var aOwner = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );

        final var expectedErrorMessage = "Event with id " + aEventId + " was not found";

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEventId,
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(any()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidMemberThisMemberIsNotAdminOrOwner_whenCallUpdateEventUseCase_thenThrowDomainException() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEventWithAddress(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.IN_PERSON
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );

        final var expectedErrorMessage = "Only owner and admins can edit this event";

        Assertions.assertTrue(aEvent.getAddress().isPresent());

        final var aInputAddress = UpdateEventAddressInput.with(
                "rua teste",
                "1234",
                "aaa",
                "bairro x",
                "cidade teste",
                "sp",
                "13210000",
                "BR"
        );

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                aInputAddress,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidMemberThisMemberIsNotBelongToEventOrganization_whenCallUpdateEventUseCase_thenThrowDomainException() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEventWithAddress(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.IN_PERSON
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.ADMIN
        );

        final var expectedErrorMessage = "Event does not belong to the organization of the user";

        Assertions.assertTrue(aEvent.getAddress().isPresent());

        final var aInputAddress = UpdateEventAddressInput.with(
                "rua teste",
                "1234",
                "aaa",
                "bairro x",
                "cidade teste",
                "sp",
                "13210000",
                "BR"
        );

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Title",
                "New Description",
                EventType.REMOTE.name(),
                aInputAddress,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void givenASameValuesInInput_whenCallUpdateEventUseCase_thenReturnNotUpdatedEvent() {
        final var aEvent = Fixture.EventFixture.withType(
                Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString()),
                EventType.REMOTE
        );
        final var aOwner = Fixture.OrganizationMemberFixture.newMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.ADMIN
        );

        final var aInput = UpdateEventInput.with(
                aOwner.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                aEvent.getTitle(),
                aEvent.getDescription().orElse(null),
                aEvent.getType().name(),
                null,
                aEvent.getCategoryId(),
                aEvent.getStartAt(),
                aEvent.getFinishAt()
        );

        when(organizationMemberRepository.memberOfUserId(aOwner.getUserId().value().toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());
        Assertions.assertEquals(aEvent.getOrganizationId().value().toString(), aOutput.organizationId());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidInputWithDuplicatedTitle_whenCallUpdateEventUseCase_thenThrowDomainException() {
        final var organizationId = new OrganizationID(ULID.random());
        final var eventId = ULID.random().toString();
        final var userId = ULID.random();
        final var duplicatedTitle = "Duplicated Title";

        final var aEvent = Fixture.EventFixture.newEvent("Old title", organizationId, ULID.random().toString());

        final var aOwner = Fixture.OrganizationMemberFixture.newOwnerMember(
                organizationId,
                new UserID(userId)
        );

        final var expectedErrorMessage = "Already exists other event using this name";

        final var aInput = UpdateEventInput.with(
                userId.toString(),
                eventId,
                duplicatedTitle,
                "Updated Description",
                EventType.REMOTE.name(),
                null,
                ULID.random().toString(),
                InstantUtils.now().plus(30, ChronoUnit.MINUTES),
                InstantUtils.now().plus(60, ChronoUnit.DAYS)
        );

        when(organizationMemberRepository.memberOfUserId(userId.toString()))
                .thenReturn(Optional.of(aOwner));
        when(eventRepository.eventOfId(eventId))
                .thenReturn(Optional.of(aEvent));
        when(eventRepository.existsByTitleAndOrganizationId(
                duplicatedTitle, organizationId.value().toString()))
                .thenReturn(true);

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(tracerWrapper, Mockito.times(1))
                .traceWithReturn(Mockito.eq("updateEventUseCase"), Mockito.any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(eventRepository, times(1))
                .existsByTitleAndOrganizationId(duplicatedTitle, organizationId.value().toString());
        verify(eventRepository, times(0)).save(any());
    }
}
