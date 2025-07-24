package com.kaua.events.platform.application.usecases.ticket.update;

import com.kaua.events.platform.domain.ticket.Ticket;

public record UpdateTicketOutput(
        String ticketId,
        String eventId
) {

    public static UpdateTicketOutput from(final Ticket aTicket) {
        return new UpdateTicketOutput(
                aTicket.getId().value().toString(),
                aTicket.getEventId().value().toString()
        );
    }
}
