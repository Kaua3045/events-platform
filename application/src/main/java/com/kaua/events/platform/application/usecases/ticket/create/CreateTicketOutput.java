package com.kaua.events.platform.application.usecases.ticket.create;

import com.kaua.events.platform.domain.ticket.Ticket;

public record CreateTicketOutput(
        String ticketId,
        String eventId
) {

    public static CreateTicketOutput from(final Ticket aTicket) {
        return new CreateTicketOutput(
                aTicket.getId().value().toString(),
                aTicket.getEventId().value().toString()
        );
    }
}
