package com.kaua.events.platform.infrastructure.rest.controllers;

import com.kaua.events.platform.application.usecases.eventmanagement.create.CreateEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.delete.SoftDeleteEventInput;
import com.kaua.events.platform.application.usecases.eventmanagement.delete.SoftDeleteEventUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get.GetEventByIdInput;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.get.GetEventByIdUseCase;
import com.kaua.events.platform.application.usecases.eventmanagement.retrieve.list.ListEventsUseCase;
import com.kaua.events.platform.domain.pagination.Pagination;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.domain.utils.Period;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.eventmanagement.req.CreateEventRequest;
import com.kaua.events.platform.infrastructure.eventmanagement.res.CreateEventResponse;
import com.kaua.events.platform.infrastructure.eventmanagement.res.GetEventByIdResponse;
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
    private final SoftDeleteEventUseCase softDeleteEventUseCase;
    private final GetEventByIdUseCase getEventByIdUseCase;

    public EventRestController(
            final CreateEventUseCase createEventUseCase,
            final ListEventsUseCase listEventsUseCase,
            final SoftDeleteEventUseCase softDeleteEventUseCase,
            final GetEventByIdUseCase getEventByIdUseCase
    ) {
        this.createEventUseCase = Objects.requireNonNull(createEventUseCase);
        this.listEventsUseCase = Objects.requireNonNull(listEventsUseCase);
        this.softDeleteEventUseCase = Objects.requireNonNull(softDeleteEventUseCase);
        this.getEventByIdUseCase = Objects.requireNonNull(getEventByIdUseCase);
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

    @Override
    public ResponseEntity<GetEventByIdResponse> getEventById(
            final AuthenticatedUser user,
            final String eventId
    ) {
        final var aInput = GetEventByIdInput.with(eventId, user.id());

        final var aOutput = this.getEventByIdUseCase.execute(aInput);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GetEventByIdResponse.from(aOutput));
    }

    @Override
    public void softDeleteEvent(final String eventId, final AuthenticatedUser authenticatedUser) {
        final var aInput = SoftDeleteEventInput.with(eventId, authenticatedUser.id());

        this.softDeleteEventUseCase.execute(aInput);
    }
}
