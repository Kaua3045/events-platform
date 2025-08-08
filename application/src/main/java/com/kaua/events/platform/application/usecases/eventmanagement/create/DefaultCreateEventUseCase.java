package com.kaua.events.platform.application.usecases.eventmanagement.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultCreateEventUseCase extends CreateEventUseCase {

    private final EventRepository eventRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultCreateEventUseCase(
            final EventRepository eventRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreateEventOutput execute(final CreateEventInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createEventUseCase",
                (trace) -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(CreateEventUseCase.class);

                    trace.runInSpan("checkForDuplicateTitle", () -> {
                        if (this.eventRepository.existsByTitleAndOrganizationId(input.title(), input.organizationId())) {
                            throw DomainException.with("Already exists other event using this name");
                        }
                    });

                    final var aOrganizationId = new OrganizationID(ULID.fromString(input.organizationId()));
                    final var aEventType = EventType.from(input.eventType())
                            .orElseThrow(() -> NotFoundException.with("Event type %s was not found".formatted(input.eventType())));

                    trace.setAttribute("organizationId", input.organizationId());
                    trace.setAttribute("categoryId", input.categoryId());
                    trace.setAttribute("event.title", input.title());
                    trace.setAttribute("event.type", aEventType.name());
                    trace.setAttribute("event.startAt", input.startAt().toString());
                    trace.setAttribute("event.finishAt", input.finishAt().toString());

                    if (aEventType.equals(EventType.REMOTE)) {
                        trace.addEvent("Creating REMOTE event");
                        final var aEvent = Event.newEvent(
                                aOrganizationId,
                                input.title(),
                                input.description(),
                                aEventType,
                                null,
                                input.categoryId(),
                                input.startAt(),
                                input.finishAt()
                        );

                        trace.runInSpan("saveRemoteEvent", () -> this.eventRepository.save(aEvent));

                        trace.addEvent("Event created successfully");
                        return CreateEventOutput.from(aEvent);
                    }

                    if (input.address() == null) {
                        throw DomainException.with("Address required on event type is IN_PERSON");
                    }

                    final var aAddress = Address.newAddress(
                            input.address().street(),
                            input.address().number(),
                            input.address().complement(),
                            input.address().neighborhood(),
                            input.address().city(),
                            input.address().state(),
                            input.address().postalCode(),
                            input.address().country()
                    );

                    final var aEvent = Event.newEvent(
                            aOrganizationId,
                            input.title(),
                            input.description(),
                            aEventType,
                            aAddress,
                            input.categoryId(),
                            input.startAt(),
                            input.finishAt()
                    );

                    trace.addEvent("Creating IN_PERSON event");

                    trace.setAttribute("event.address.street", input.address().street());
                    trace.setAttribute("event.address.number", input.address().number());
                    trace.setAttribute("event.address.neighborhood", input.address().neighborhood());
                    trace.setAttribute("event.address.city", input.address().city());
                    trace.setAttribute("event.address.state", input.address().state());
                    trace.setAttribute("event.address.postalCode", input.address().postalCode());
                    trace.setAttribute("event.address.country", input.address().country());

                    trace.runInSpan("saveInPersonEvent", () -> this.eventRepository.save(aEvent));

                    trace.addEvent("Event created successfully");
                    return CreateEventOutput.from(aEvent);
                }
        );
    }
}
