package com.kaua.events.platform.application.usecases.ticket.delete.soft;

public record SoftDeleteTicketInput(String ticketId, String userId) {

    public static SoftDeleteTicketInput with(final String ticketId, final String userId) {
        return new SoftDeleteTicketInput(ticketId, userId);
    }
}
