package com.kaua.events.platform.application.usecases.ticket.create;

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

class CreateTicketUseCaseTest extends UseCaseTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DefaultCreateTicketUseCase useCase;

    @Test
    void givenAValidValues_whenCallCreateTicketUseCase_thenReturnTicketId() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.of(aEvent));
        Mockito.when(ticketRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.ticketId());
        Assertions.assertEquals(anEventId, aOutput.eventId());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.times(1)).save(argThat(aCmd ->
                Objects.equals(aName, aCmd.getName())
                        && Objects.equals(aDescription, aCmd.getDescription().get())
                        && Objects.equals(anEventId, aCmd.getEventId().value().toString())
                        && Objects.equals(aPrice, aCmd.getPrice().toString())
                        && Objects.equals(aQuantity, aCmd.getQuantity())
                        && Objects.equals(aType, aCmd.getType().name().toLowerCase())
                        && Objects.equals(aStatus, aCmd.getStatus().name().toLowerCase())));
    }

    @Test
    void givenAnInvalidInput_whenCallCreateTicketUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateTicketUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.never()).memberOfUserId(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidTicketType_whenCallCreateTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "invalid";
        final var aStatus = "available";

        final var aExpectedErrorMessage = "Invalid ticket type: invalid";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidTicketStatus_whenCallCreateTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "invalid";

        final var aExpectedErrorMessage = "Invalid ticket status: invalid";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidUserId_whenCallCreateTicketUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var anInvalidUserId = ULID.random().toString();

        final var aInput = CreateTicketInput.with(
                anInvalidUserId,
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals("OrganizationMember with id " + anInvalidUserId + " was not found", aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(anInvalidUserId));
        Mockito.verify(eventRepository, Mockito.never()).eventOfId(Mockito.any());
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnInvalidEventId_whenCallCreateTicketUseCase_thenThrowNotFoundException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var anEventId = ULID.random().toString();
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals("Event with id " + anEventId + " was not found", aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAnEventOfAnotherOrganization_whenCallCreateTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newOwnerMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random())
        );
        final var anotherOrganizationId = ULID.random();
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(anotherOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals("User is not a member of the organization that owns the event", aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAMemberThatIsNotAnOwner_whenCallCreateTicketUseCase_thenThrowDomainException() {
        final var aOrganizationId = ULID.random();
        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(aOrganizationId),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );
        final var aEvent = Fixture.EventFixture.newEvent(
                "event-name",
                new OrganizationID(aOrganizationId),
                ULID.random().toString()
        );
        final var aName = "ticket-name";
        final var aDescription = "ticket-description";
        final var anEventId = aEvent.getId().value().toString();
        final var aPrice = "100.00";
        final var aQuantity = 10;
        final var aType = "standard";
        final var aStatus = "available";

        final var aInput = CreateTicketInput.with(
                aMember.getUserId().value().toString(),
                aName,
                aDescription,
                anEventId,
                aPrice,
                aQuantity,
                aType,
                aStatus
        );

        Mockito.when(organizationMemberRepository.memberOfUserId(Mockito.any()))
                .thenReturn(Optional.of(aMember));
        Mockito.when(eventRepository.eventOfId(Mockito.any()))
                .thenReturn(Optional.of(aEvent));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals("User is not an owner of the organization that owns the event", aException.getMessage());

        Mockito.verify(organizationMemberRepository, Mockito.times(1))
                .memberOfUserId(Mockito.eq(aMember.getUserId().value().toString()));
        Mockito.verify(eventRepository, Mockito.times(1))
                .eventOfId(Mockito.eq(anEventId));
        Mockito.verify(ticketRepository, Mockito.never()).save(Mockito.any());
    }
}
