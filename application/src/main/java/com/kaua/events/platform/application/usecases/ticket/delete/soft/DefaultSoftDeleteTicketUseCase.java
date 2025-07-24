package com.kaua.events.platform.application.usecases.ticket.delete.soft;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;

import java.util.Objects;

public class DefaultSoftDeleteTicketUseCase extends SoftDeleteTicketUseCase {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public DefaultSoftDeleteTicketUseCase(
            final TicketRepository ticketRepository,
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    @Override
    public void execute(final SoftDeleteTicketInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(SoftDeleteTicketUseCase.class);

        final var aTicket = this.ticketRepository.ticketOfId(input.ticketId())
                .orElseThrow(NotFoundException.with(Ticket.class, input.ticketId()));

        if (aTicket.getStatus().equals(TicketStatus.DELETED)) {
            return;
        }

        final var aMember = this.organizationMemberRepository.memberOfUserId(input.userId())
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

        if (!aMember.getMemberRole().equals(OrganizationMemberRole.OWNER)) {
            throw DomainException.with("Only owners can delete tickets");
        }

        final var aEvent = this.eventRepository.eventOfId(aTicket.getEventId().value().toString())
                .orElseThrow(NotFoundException.with(Event.class, aTicket.getEventId()));

        if (!aEvent.getOrganizationId().equals(aMember.getOrganizationId())) {
            throw DomainException.with("You cannot delete a ticket from an event that does not belong to your organization");
        }

        final var aDeletedTicket = aTicket.markAsDeleted();

        this.ticketRepository.save(aDeletedTicket);
    }
}
