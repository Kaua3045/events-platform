package com.kaua.events.platform.application.usecases.ticket.retrieve.get;

public record GetTicketByIdInput(String ticketId) {

    public static GetTicketByIdInput with(final String aTicketId) {
        return new GetTicketByIdInput(aTicketId);
    }
}
