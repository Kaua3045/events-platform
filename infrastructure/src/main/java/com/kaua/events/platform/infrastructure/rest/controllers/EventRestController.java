package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
import com.kaua.events.platform.infrastructure.eventmanagement.req.CreateEventRequest;
import com.kaua.events.platform.infrastructure.eventmanagement.res.CreateEventResponse;
import com.kaua.events.platform.infrastructure.rest.EventAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class EventRestController implements EventAPI {

    private final CreateEventUseCase createEventUseCase;

    public EventRestController(final CreateEventUseCase createEventUseCase) {
        this.createEventUseCase = Objects.requireNonNull(createEventUseCase);
    }

    @Override
    public ResponseEntity<CreateEventResponse> createEvent(final CreateEventRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.createEventUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateEventResponse.from(aOutput));
    }
}
