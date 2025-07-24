package com.kaua.events.platform.infrastructure.ticket.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.ticket.retrieve.get.GetTicketByIdOutput;

import java.math.BigDecimal;
import java.time.Instant;

public record GetTicketByIdResponse(
        @JsonProperty("ticket_id") String ticketId,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("sold") int sold,
        @JsonProperty("type") String ticketType,
        @JsonProperty("status") String ticketStatus,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {

    public static GetTicketByIdResponse from(final GetTicketByIdOutput aTicket) {
        return new GetTicketByIdResponse(
                aTicket.ticketId(),
                aTicket.eventId(),
                aTicket.name(),
                aTicket.description(),
                aTicket.price(),
                aTicket.quantity(),
                aTicket.sold(),
                aTicket.ticketType(),
                aTicket.ticketStatus(),
                aTicket.createdAt(),
                aTicket.updatedAt()
        );
    }
}
