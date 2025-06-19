package com.kaua.events.platform.application.usecases.eventmanagement.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
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

    public DefaultCreateEventUseCase(final EventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
    }

    @Override
    public CreateEventOutput execute(final CreateEventInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(CreateEventUseCase.class);

        if (this.eventRepository.existsByTitleAndOrganizationId(input.title(), input.organizationId())) {
            throw DomainException.with("Already exists other event using this name");
        }

        final var aOrganizationId = new OrganizationID(ULID.fromString(input.organizationId()));
        final var aEventType = EventType.from(input.eventType())
                .orElseThrow(() -> NotFoundException.with("Event type %s was not found".formatted(input.eventType())));

        if (aEventType.equals(EventType.REMOTE)) {
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

            this.eventRepository.save(aEvent);

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

        this.eventRepository.save(aEvent);

        return CreateEventOutput.from(aEvent);
    }
}
