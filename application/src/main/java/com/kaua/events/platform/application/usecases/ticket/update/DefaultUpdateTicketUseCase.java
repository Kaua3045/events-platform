package com.kaua.events.platform.application.usecases.ticket.update;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class DefaultUpdateTicketUseCase extends UpdateTicketUseCase {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultUpdateTicketUseCase(
            final TicketRepository ticketRepository,
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public UpdateTicketOutput execute(final UpdateTicketInput input) {
        return this.tracerWrapper.traceWithReturn(
                "updateTicketUseCase",
                span -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(UpdateTicketUseCase.class);

                    final var aTicket = this.ticketRepository.ticketOfId(input.ticketId())
                            .orElseThrow(NotFoundException.with(Ticket.class, input.ticketId()));

                    final var aEvent = this.eventRepository.eventOfId(input.eventId())
                            .orElseThrow(NotFoundException.with(Event.class, input.eventId()));

                    final var aMember = this.organizationMemberRepository.memberOfUserId(input.userId())
                            .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

                    if (!aMember.getOrganizationId().value().equals(aEvent.getOrganizationId().value())) {
                        throw DomainException.with("User is not a member of the organization that owns the event");
                    }

                    if (!aMember.getMemberRole().equals(OrganizationMemberRole.OWNER)) {
                        throw DomainException.with("User is not an owner of the organization that owns the event");
                    }

                    if (!aTicket.getEventId().value().equals(aEvent.getId().value())) {
                        throw DomainException.with("Ticket does not belong to the event");
                    }

                    final var aName = Optional.ofNullable(input.name())
                            .orElse(aTicket.getName());
                    final var aDescription = Optional.ofNullable(input.description())
                            .orElse(aTicket.getDescription().orElse(null));
                    final var aPrice = Optional.ofNullable(input.price())
                            .map(BigDecimal::new)
                            .orElse(aTicket.getPrice());

                    final var aTicketType = TicketType.from(input.type())
                            .orElse(aTicket.getType());
                    final var aTicketStatus = TicketStatus.from(input.status())
                            .orElse(aTicket.getStatus());

                    final Ticket aUpdatedTicket = aTicket.update(
                            aName,
                            aDescription,
                            aPrice,
                            input.quantity(),
                            aTicketType,
                            aTicketStatus
                    );

                    span.setAttribute("userId", aMember.getUserId().value().toString());
                    span.setAttribute("organizationId", aEvent.getOrganizationId().value().toString());
                    span.setAttribute("eventId", aEvent.getId().value().toString());
                    span.setAttribute("ticketId", aTicket.getId().value().toString());

                    if (aTicket.equals(aUpdatedTicket)) {
                        span.addEvent("Ticket not updated because received same data");
                        return UpdateTicketOutput.from(aUpdatedTicket);
                    }

                    span.runInSpan("updateTicket", () -> this.ticketRepository.save(aUpdatedTicket));
                    span.addEvent("Ticket successfully updated");
                    return UpdateTicketOutput.from(aUpdatedTicket);
                }
        );
    }
}
