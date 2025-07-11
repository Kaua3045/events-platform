package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.ticket.Ticket;

public interface TicketRepository {

    Ticket save(Ticket ticket);
}
