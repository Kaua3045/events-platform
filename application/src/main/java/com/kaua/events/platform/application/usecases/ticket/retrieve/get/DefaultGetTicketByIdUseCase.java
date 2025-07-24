package com.kaua.events.platform.application.usecases.ticket.retrieve.get;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.TicketRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.ticket.Ticket;

import java.util.Objects;

public class DefaultGetTicketByIdUseCase extends GetTicketByIdUseCase {

    private final TicketRepository ticketRepository;

    public DefaultGetTicketByIdUseCase(final TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    @Override
    public GetTicketByIdOutput execute(final GetTicketByIdInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(GetTicketByIdUseCase.class);

        return this.ticketRepository.ticketOfId(input.ticketId())
                .map(GetTicketByIdOutput::from)
                .orElseThrow(NotFoundException.with(Ticket.class, input.ticketId()));
    }
}
