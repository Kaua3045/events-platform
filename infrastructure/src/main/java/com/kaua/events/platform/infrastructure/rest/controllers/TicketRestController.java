package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.ticket.create.CreateTicketUseCase;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.rest.TicketAPI;
import com.kaua.events.platform.infrastructure.ticket.req.CreateTicketRequest;
import com.kaua.events.platform.infrastructure.ticket.res.CreateTicketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class TicketRestController implements TicketAPI {

    private final CreateTicketUseCase createTicketUseCase;

    public TicketRestController(final CreateTicketUseCase createTicketUseCase) {
        this.createTicketUseCase = Objects.requireNonNull(createTicketUseCase);
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
}
