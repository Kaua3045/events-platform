package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsUseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.Period;
import com.kaua.events.platform.infrastructure.eventmanagement.req.CreateEventRequest;
import com.kaua.events.platform.infrastructure.eventmanagement.res.CreateEventResponse;
import com.kaua.events.platform.infrastructure.eventmanagement.res.ListEventsResponse;
import com.kaua.events.platform.infrastructure.rest.EventAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

@RestController
public class EventRestController implements EventAPI {

    private final CreateEventUseCase createEventUseCase;
    private final ListEventsUseCase listEventsUseCase;

    public EventRestController(
            final CreateEventUseCase createEventUseCase,
            final ListEventsUseCase listEventsUseCase
    ) {
        this.createEventUseCase = Objects.requireNonNull(createEventUseCase);
        this.listEventsUseCase = Objects.requireNonNull(listEventsUseCase);
    }

    @Override
    public ResponseEntity<CreateEventResponse> createEvent(final CreateEventRequest request) {
        final var aInput = request.toInput();

        final var aOutput = this.createEventUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateEventResponse.from(aOutput));
    }

    @Override
    public Pagination<ListEventsResponse> listEvents(
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

        final var aOutput = this.listEventsUseCase.execute(aQuery);

        return aOutput.map(ListEventsResponse::from);
    }
}
