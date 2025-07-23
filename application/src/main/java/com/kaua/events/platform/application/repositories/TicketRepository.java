package com.kaua.events.platform.application.repositories;

import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.ticket.Ticket;

public interface TicketRepository {

    Pagination<Ticket> listAll(SearchQuery query);

    Ticket save(Ticket ticket);
}
