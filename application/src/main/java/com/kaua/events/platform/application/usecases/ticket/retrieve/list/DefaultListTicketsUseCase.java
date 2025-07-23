package com.kaua.events.platform.application.usecases.ticket.retrieve.list;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.Objects;

public class DefaultListTicketsUseCase extends ListTicketsUseCase {

    private final TicketRepository ticketRepository;

    public DefaultListTicketsUseCase(final TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    @Override
    public Pagination<ListTicketsOutput> execute(final SearchQuery input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(ListTicketsUseCase.class);

        return this.ticketRepository.listAll(input)
                .map(ListTicketsOutput::from);
    }
}
