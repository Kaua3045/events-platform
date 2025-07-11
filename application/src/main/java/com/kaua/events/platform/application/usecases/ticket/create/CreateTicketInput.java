package com.kaua.events.platform.application.usecases.ticket.create;

public record CreateTicketInput(
        String userId,
        String name,
        String description,
        String eventId,
        String price,
        int quantity,
        String type,
        String status
) {

    public static CreateTicketInput with(
            final String userId,
            final String name,
            final String description,
            final String eventId,
            final String price,
            final int quantity,
            final String type,
            final String status
    ) {
        return new CreateTicketInput(userId, name, description, eventId, price, quantity, type, status);
    }
}
