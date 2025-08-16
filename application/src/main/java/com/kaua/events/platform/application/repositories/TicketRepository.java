package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.ticket.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    Optional<Ticket> ticketOfId(String id);

    Pagination<Ticket> listAll(SearchQuery query);

    Ticket save(Ticket ticket);

    List<Ticket> saveAll(List<Ticket> tickets);
}
