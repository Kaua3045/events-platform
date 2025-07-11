package com.kaua.events.platform.application.usecases.ticket.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventID;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.utils.ULID;

import java.math.BigDecimal;
import java.util.Objects;

public class DefaultCreateTicketUseCase extends CreateTicketUseCase {

    private final TicketRepository ticketRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final EventRepository eventRepository;

    public DefaultCreateTicketUseCase(
            final TicketRepository ticketRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final EventRepository eventRepository
    ) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.eventRepository = Objects.requireNonNull(eventRepository);
    }

    @Override
    public CreateTicketOutput execute(final CreateTicketInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(CreateTicketUseCase.class);

        final var aMember = this.organizationMemberRepository.memberOfUserId(input.userId())
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

        final var aEvent = this.eventRepository.eventOfId(input.eventId())
                .orElseThrow(NotFoundException.with(Event.class, input.eventId()));

        if (!aMember.getOrganizationId().value().equals(aEvent.getOrganizationId().value())) {
            throw DomainException.with("User is not a member of the organization that owns the event");
        }

        if (!aMember.getMemberRole().equals(OrganizationMemberRole.OWNER)) {
            throw DomainException.with("User is not an owner of the organization that owns the event");
        }

        final var aType = TicketType.from(input.type())
                .orElseThrow(() -> DomainException.with("Invalid ticket type: " + input.type()));

        final var aStatus = TicketStatus.from(input.status())
                .orElseThrow(() -> DomainException.with("Invalid ticket status: " + input.status()));

        final var aTicket = Ticket.newTicket(
                new EventID(ULID.fromString(input.eventId())),
                input.name(),
                input.description(),
                new BigDecimal(input.price()),
                input.quantity(),
                aType,
                aStatus
        );

        this.ticketRepository.save(aTicket);

        return CreateTicketOutput.from(aTicket);
    }
}
