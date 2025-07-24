package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.get.GetTicketByIdInput;
import com.kaua.events.platform.application.usecases.ticket.retrieve.get.GetTicketByIdUseCase;
import com.kaua.events.platform.application.usecases.ticket.retrieve.list.ListTicketsUseCase;
import com.kaua.events.platform.application.usecases.ticket.update.UpdateTicketUseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.Period;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.rest.TicketAPI;
import com.kaua.events.platform.infrastructure.ticket.req.CreateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.req.UpdateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.res.CreateTicketResponse;
import com.kaua.events.platform.infrastructure.ticket.res.GetTicketByIdResponse;
import com.kaua.events.platform.infrastructure.ticket.res.ListTicketsResponse;
import com.kaua.events.platform.infrastructure.ticket.res.UpdateTicketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

@RestController
public class TicketRestController implements TicketAPI {

    private final CreateTicketUseCase createTicketUseCase;
    private final ListTicketsUseCase listTicketsUseCase;
    private final UpdateTicketUseCase updateTicketUseCase;
    private final GetTicketByIdUseCase getTicketByIdUseCase;

    public TicketRestController(
            final CreateTicketUseCase createTicketUseCase,
            final ListTicketsUseCase listTicketsUseCase,
            final UpdateTicketUseCase updateTicketUseCase,
            final GetTicketByIdUseCase getTicketByIdUseCase
    ) {
        this.createTicketUseCase = Objects.requireNonNull(createTicketUseCase);
        this.listTicketsUseCase = Objects.requireNonNull(listTicketsUseCase);
        this.updateTicketUseCase = Objects.requireNonNull(updateTicketUseCase);
        this.getTicketByIdUseCase = Objects.requireNonNull(getTicketByIdUseCase);
    }

    @Override
    public ResponseEntity<CreateTicketResponse> createTicket(
            final AuthenticatedUser authenticatedUser,
            final CreateTicketRequest request
    ) {
        final var aInput = request.toInput(authenticatedUser.id());

        final var aOutput = this.createTicketUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateTicketResponse.from(aOutput));
    }

    @Override
    public Pagination<ListTicketsResponse> listTickets(
            final Map<String, String> filters,
            final String search,
            final int page,
            final int perPage,
            final String sort,
            final String direction,
            final String startDate,
            final String endDate
    ) {
        final var aQuery = SearchQuery.newSearchQuery(
                page,
                perPage,
                search,
                sort,
                direction,
                new Period(Period.startValidate(
                        startDate,
                        30,
                        ChronoUnit.DAYS
                ), Period.endValidate(
                        endDate,
                        30,
                        ChronoUnit.DAYS
                )),
                filters
        );

        final var aOutput = this.listTicketsUseCase.execute(aQuery);

        return aOutput.map(ListTicketsResponse::from);
    }

    @Override
    public ResponseEntity<GetTicketByIdResponse> getTicketById(final String ticketId) {
        final var aInput = GetTicketByIdInput.with(ticketId);

        final var aOutput = this.getTicketByIdUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GetTicketByIdResponse.from(aOutput));
    }

    @Override
    public ResponseEntity<UpdateTicketResponse> updateTicket(
            final String ticketId,
            final AuthenticatedUser authenticatedUser,
            final UpdateTicketRequest request
    ) {
        final var aInput = request.toInput(ticketId, authenticatedUser.id());

        final var aOutput = this.updateTicketUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UpdateTicketResponse.from(aOutput));
    }
}
