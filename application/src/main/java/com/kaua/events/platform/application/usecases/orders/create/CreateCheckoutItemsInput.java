package com.kaua.events.platform.application.usecases.orders.create;

public record CreateCheckoutItemsInput(
        String eventId,
        String ticketId,
        int quantity
) {

    public static CreateCheckoutItemsInput with(
            final String eventId,
            final String ticketId,
            final int quantity
    ) {
        return new CreateCheckoutItemsInput(eventId, ticketId, quantity);
    }
}
