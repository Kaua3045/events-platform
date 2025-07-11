package com.kaua.events.platform.application.usecases.eventmanagement.delete;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.eventmanagement.EventStatus;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;

class SoftDeleteEventUseCaseTest extends UseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultSoftDeleteEventUseCase useCase;

    @Test
    void givenAValidInput_whenCallSoftDeleteEventUseCase_thenReturnVoid() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));

        final var aEventId = aEvent.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aInput = SoftDeleteEventInput.with(aEventId, aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aEventId))
                .thenReturn(Optional.of(aEvent));
        Mockito.when(eventRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aEventId);
        Mockito.verify(eventRepository, Mockito.times(1)).save(argThat(cmd ->
                Objects.equals(cmd.getId().value().toString(), aEventId)
                        && Objects.equals(cmd.getStatus(), EventStatus.DELETED)
                        && cmd.getDeletedAt().isPresent()));
    }

    @Test
    void givenAnInvalidUserId_whenCallSoftDeleteEventUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());

        final var aEventId = aEvent.getId().value().toString();
        final var aUserId = ULID.random().toString();

        final var aExpectedErrorMessage = "OrganizationMember with id " + aUserId + " was not found";

        final var aInput = SoftDeleteEventInput.with(aEventId, aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventId_whenCallSoftDeleteEventUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));

        final var aEventId = ULID.random().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Event with id " + aEventId + " was not found";

        final var aInput = SoftDeleteEventInput.with(aEventId, aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aEventId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aEventId);
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventDoesNotBelongToOrganization_whenCallSoftDeleteEventUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(ULID.random()), new UserID(ULID.random()));

        final var aEventId = aEvent.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Event does not belong to the organization of the user";

        final var aInput = SoftDeleteEventInput.with(aEventId, aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aEventId))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aEventId);
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidUserIdIsNotOwner_whenCallSoftDeleteEventUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()), OrganizationMemberRole.ADMIN);

        final var aEventId = aEvent.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Only owners can delete events";

        final var aInput = SoftDeleteEventInput.with(aEventId, aUserId);

        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(eventRepository, Mockito.times(0)).eventOfId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidInput_whenCallSoftDeleteEventUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to SoftDeleteEventUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any());
    }
}
