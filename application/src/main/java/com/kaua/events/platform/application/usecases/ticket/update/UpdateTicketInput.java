package com.kaua.events.platform.application.usecases.ticket.update;

public record UpdateTicketInput(
        String ticketId,
        String userId,
        String eventId,
        String name,
        String description,
        String price,
        int quantity,
        String type,
        String status
) {

    public static UpdateTicketInput with(
            final String ticketId,
            final String userId,
            final String eventId,
            final String name,
            final String description,
            final String price,
            final int quantity,
            final String type,
            final String status
    ) {
        return new UpdateTicketInput(ticketId, userId, eventId, name, description, price, quantity, type, status);
    }
}
