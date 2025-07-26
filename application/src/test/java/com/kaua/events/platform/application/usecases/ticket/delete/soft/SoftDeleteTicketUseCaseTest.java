package com.kaua.events.platform.application.usecases.ticket.delete.soft;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.ticket.TicketStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

class SoftDeleteTicketUseCaseTest extends UseCaseTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultSoftDeleteTicketUseCase useCase;

    @Test
    void givenAValidInput_whenCallSoftDeleteTicketUseCase_thenReturnVoid() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.of(aTicket));
        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aTicket.getEventId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        Mockito.when(ticketRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(ticketRepository, Mockito.times(1)).save(argThat(cmd ->
                Objects.equals(cmd.getId().value().toString(), aTicketId)
                        && Objects.equals(cmd.getStatus(), TicketStatus.DELETED)
                        && cmd.getDeletedAt().isPresent()));
    }

    @Test
    void givenAnInvalidTicketId_whenCallSoftDeleteTicketUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));

        final var aTicketId = ULID.random().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Ticket with id " + aTicketId + " was not found";

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnDeletedTicket_whenCallSoftDeleteTicketUseCase_thenReturnVoid() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.withStatus(aEvent.getId(), TicketStatus.DELETED);
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.of(aTicket));

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidUserId_whenCallSoftDeleteTicketUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = ULID.random().toString();

        final var aExpectedErrorMessage = "OrganizationMember with id " + aUserId + " was not found";

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidNotOwnerOrAdminMember_whenCallSoftDeleteTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());
        final var aMember = Fixture.OrganizationMemberFixture.newMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()), OrganizationMemberRole.ADMIN);

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Only owners can delete tickets";

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.of(aTicket));
        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventId_whenCallSoftDeleteTicketUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(aOrganizationId), new UserID(ULID.random()));
        final var aTicket = Fixture.TicketFixture.newTicket();

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "Event with id " + aTicket.getEventId().value() + " was not found";

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.of(aTicket));
        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aTicket.getEventId().value().toString()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aTicket.getEventId().value().toString());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventDoesNotBelongToOrganization_whenCallSoftDeleteTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(aOrganizationId), ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(new OrganizationID(ULID.random()), new UserID(ULID.random()));

        final var aTicketId = aTicket.getId().value().toString();
        final var aUserId = aMember.getUserId().value().toString();

        final var aExpectedErrorMessage = "You cannot delete a ticket from an event that does not belong to your organization";

        final var aInput = SoftDeleteTicketInput.with(aTicketId, aUserId);

        Mockito.when(ticketRepository.ticketOfId(aTicketId))
                .thenReturn(Optional.of(aTicket));
        Mockito.when(organizationMemberRepository.memberOfUserId(aUserId))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(aTicket.getEventId().value().toString()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1)).memberOfUserId(aUserId);
        Mockito.verify(ticketRepository, Mockito.times(1)).ticketOfId(aTicketId);
        Mockito.verify(eventRepository, Mockito.times(1)).eventOfId(aTicket.getEventId().value().toString());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidNullInput_whenCallSoftDeleteTicketUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to SoftDeleteTicketUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).ticketOfId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }
}
