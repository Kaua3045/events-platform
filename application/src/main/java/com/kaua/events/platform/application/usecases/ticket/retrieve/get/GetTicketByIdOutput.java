package com.kaua.events.platform.application.usecases.ticket.retrieve.get;

import com.kaua.events.platform.domain.ticket.Ticket;

import java.math.BigDecimal;
import java.time.Instant;

public record GetTicketByIdOutput(
        String ticketId,
        String eventId,
        String name,
        String description,
        BigDecimal price,
        int quantity,
        int sold,
        String ticketType,
        String ticketStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static GetTicketByIdOutput from(final Ticket aTicket) {
        return new GetTicketByIdOutput(
                aTicket.getId().value().toString(),
                aTicket.getEventId().value().toString(),
                aTicket.getName(),
                aTicket.getDescription().orElse(null),
                aTicket.getPrice(),
                aTicket.getQuantity(),
                aTicket.getSold(),
                aTicket.getType().name(),
                aTicket.getStatus().name(),
                aTicket.getCreatedAt(),
                aTicket.getUpdatedAt()
        );
    }
}
