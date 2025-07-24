package com.kaua.events.platform.application.usecases.ticket.update;

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
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateTicketUseCaseTest extends UseCaseTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private DefaultUpdateTicketUseCase useCase;

    @Test
    void givenAValidInput_whenCallUpdateTicketUseCase_thenUpdateTicketSuccessfully() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                aOrganizationId,
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());

        final var aNewName = "New Ticket Name";
        final var aNewDescription = "New Ticket Description";
        final var aNewPrice = BigDecimal.valueOf(100.00);
        final var aNewType = TicketType.STUDENT;
        final var aNewStatus = TicketStatus.AVAILABLE;

        final var aInput = UpdateTicketInput.with(
                aTicket.getId().value().toString(),
                aMember.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                aNewName,
                aNewDescription,
                aNewPrice.toString(),
                aTicket.getQuantity(),
                aNewType.name(),
                aNewStatus.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(aMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aMember));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aTicket.getId().value().toString(), aOutput.ticketId());
        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());

        verify(ticketRepository, times(1)).ticketOfId(aTicket.getId().value().toString());
        verify(eventRepository, times(1)).eventOfId(aEvent.getId().value().toString());
        verify(organizationMemberRepository, times(1)).memberOfUserId(aMember.getUserId().value().toString());
        verify(ticketRepository, times(1)).save(argThat(aCmd ->
                Objects.equals(aCmd.getName(), aNewName)
                        && Objects.equals(aCmd.getDescription().get(), aNewDescription)
                        && Objects.equals(aCmd.getPrice(), aNewPrice)
                        && Objects.equals(aCmd.getQuantity(), aTicket.getQuantity())
                        && Objects.equals(aCmd.getType(), aNewType)
                        && Objects.equals(aCmd.getStatus(), aNewStatus)
                        && Objects.equals(aCmd.getEventId().value(), aEvent.getId().value())
        ));
    }

    @Test
    void givenAValidSameInput_whenCallUpdateTicketUseCase_thenReturnNotUpdatedTicket() {
        final var aOrganizationId = new OrganizationID(ULID.random());

        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                aOrganizationId,
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(aOrganizationId, ULID.random().toString());
        final var aTicket = Fixture.TicketFixture.newTicket(aEvent.getId());

        final var aInput = UpdateTicketInput.with(
                aTicket.getId().value().toString(),
                aMember.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                aTicket.getName(),
                aTicket.getDescription().orElse(null),
                aTicket.getPrice().toString(),
                aTicket.getQuantity(),
                aTicket.getType().name(),
                aTicket.getStatus().name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(aMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aMember));

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertEquals(aTicket.getId().value().toString(), aOutput.ticketId());
        Assertions.assertEquals(aEvent.getId().value().toString(), aOutput.eventId());

        verify(ticketRepository, times(1)).ticketOfId(aTicket.getId().value().toString());
        verify(eventRepository, times(1)).eventOfId(aEvent.getId().value().toString());
        verify(organizationMemberRepository, times(1)).memberOfUserId(aMember.getUserId().value().toString());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenNullInput_whenCallUpdateTicketUseCase_thenThrowException() {
        final var expectedMessage = "Input to UpdateTicketUseCase cannot be null";

        final var exception = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void givenAnInvalidTicketId_whenCallUpdateTicketUseCase_thenThrowNotFoundException() {
        final var aTicketId = ULID.random().toString();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );

        final var expectedErrorMessage = "Ticket with id " + aTicketId + " was not found";

        final var aInput = UpdateTicketInput.with(
                aTicketId,
                aMember.getUserId().value().toString(),
                ULID.random().toString(),
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(0)).eventOfId(any());
        verify(organizationMemberRepository, times(0)).memberOfUserId(any());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidEventId_whenCallUpdateTicketUseCase_thenThrowNotFoundException() {
        final var aEventId = ULID.random().toString();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random())
        );

        final var expectedErrorMessage = "Event with id " + aEventId + " was not found";

        final var aInput = UpdateTicketInput.with(
                aMember.getUserId().value().toString(),
                ULID.random().toString(),
                aEventId,
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(Fixture.TicketFixture.newTicket()));
        when(eventRepository.eventOfId(any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(organizationMemberRepository, times(0)).memberOfUserId(any());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidUserId_whenCallUpdateTicketUseCase_thenThrowNotFoundException() {
        final var aUserId = ULID.random().toString();
        final var aTicket = Fixture.TicketFixture.newTicket();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());

        final var expectedErrorMessage = "OrganizationMember with id " + aUserId + " was not found";

        final var aInput = UpdateTicketInput.with(
                aTicket.getId().value().toString(),
                aUserId,
                ULID.random().toString(),
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(any()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidMemberDoesNotBelongToEventOrganization_whenCallUpdateTicketUseCase_thenThrowDomainException() {
        final var aTicket = Fixture.TicketFixture.newTicket();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.ADMIN
        );

        final var expectedErrorMessage = "User is not a member of the organization that owns the event";

        final var aInput = UpdateTicketInput.with(
                aTicket.getId().value().toString(),
                aMember.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(aMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aMember));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidMemberDoesNotOwnerOrAdmin_whenCallUpdateTicketUseCase_thenThrowDomainException() {
        final var aTicket = Fixture.TicketFixture.newTicket();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );

        final var expectedErrorMessage = "User is not an owner of the organization that owns the event";

        final var aInput = UpdateTicketInput.with(
                aTicket.getId().value().toString(),
                aMember.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(aMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aMember));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(ticketRepository, times(0)).save(any());
    }

    @Test
    void givenAnInvalidTicketIdNotBelongToEvent_whenCallUpdateTicketUseCase_thenThrowDomainException() {
        final var aTicket = Fixture.TicketFixture.newTicket();
        final var aEvent = Fixture.EventFixture.newEvent(new OrganizationID(ULID.random()), ULID.random().toString());
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                aEvent.getOrganizationId(),
                new UserID(ULID.random())
        );

        final var expectedErrorMessage = "Ticket does not belong to the event";

        final var aInput = UpdateTicketInput.with(
                ULID.random().toString(),
                aMember.getUserId().value().toString(),
                aEvent.getId().value().toString(),
                "New Ticket Name",
                "New Ticket Description",
                "100.00",
                10,
                TicketType.STUDENT.name(),
                TicketStatus.AVAILABLE.name()
        );

        when(ticketRepository.ticketOfId(any()))
                .thenReturn(Optional.of(aTicket));
        when(eventRepository.eventOfId(aEvent.getId().value().toString()))
                .thenReturn(Optional.of(aEvent));
        when(organizationMemberRepository.memberOfUserId(aMember.getUserId().value().toString()))
                .thenReturn(Optional.of(aMember));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        verify(ticketRepository, times(1)).ticketOfId(any());
        verify(eventRepository, times(1)).eventOfId(any());
        verify(organizationMemberRepository, times(1)).memberOfUserId(any());
        verify(ticketRepository, times(0)).save(any());
    }
}