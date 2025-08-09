package com.kaua.events.platform.application.usecases.eventmanagement.update;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.eventmanagement.Address;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;

import java.util.Objects;
import java.util.Optional;

public class DefaultUpdateEventUseCase extends UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultUpdateEventUseCase(
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public UpdateEventOutput execute(final UpdateEventInput input) {
        return this.tracerWrapper.traceWithReturn(
                "updateEventUseCase",
                span -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(UpdateEventUseCase.class);

                    final var aMember = this.organizationMemberRepository.memberOfUserId(input.userId())
                            .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

                    final var aEvent = this.eventRepository.eventOfId(input.eventId())
                            .orElseThrow(NotFoundException.with(Event.class, input.eventId()));

                    if (aMember.getMemberRole().equals(OrganizationMemberRole.MEMBER)) {
                        throw DomainException.with("Only owner and admins can edit this event");
                    }

                    if (!aMember.getOrganizationId().value().equals(aEvent.getOrganizationId().value())) {
                        throw DomainException.with("Event does not belong to the organization of the user");
                    }

                    final var aTitle = Optional.ofNullable(input.title())
                            .map(it -> {
                                if (!it.equals(aEvent.getTitle())) {
                                    span.runInSpan("checkForDuplicateTitle", () -> {
                                        if (this.eventRepository.existsByTitleAndOrganizationId(input.title(), aMember.getOrganizationId().value().toString())) {
                                            throw DomainException.with("Already exists other event using this name");
                                        }
                                    });
                                }
                                return it;
                            })
                            .orElse(aEvent.getTitle());
                    final var aDescription = Optional.ofNullable(input.description())
                            .orElse(aEvent.getDescription().orElse(null));
                    final var aCategoryId = Optional.ofNullable(input.categoryId())
                            .orElse(aEvent.getCategoryId());
                    final var aStartAt = Optional.ofNullable(input.startAt())
                            .orElse(aEvent.getStartAt());
                    final var aFinishAt = Optional.ofNullable(input.finishAt())
                            .orElse(aEvent.getFinishAt());

                    final var aEventType = EventType.from(input.eventType())
                            .orElse(aEvent.getType());

                    final var aAddress = Optional.ofNullable(input.address())
                            .filter(address -> aEventType.equals(EventType.IN_PERSON))
                            .map(address -> Address.newAddress(
                                    address.street(),
                                    address.number(),
                                    address.complement(),
                                    address.neighborhood(),
                                    address.city(),
                                    address.state(),
                                    address.postalCode(),
                                    address.country()
                            ))
                            .orElse(null);

                    final Event aUpdatedEvent = aEvent.update(
                            aTitle,
                            aDescription,
                            aEventType,
                            aAddress,
                            aCategoryId,
                            aStartAt,
                            aFinishAt
                    );

                    span.setAttribute("eventId", input.eventId());
                    span.setAttribute("organizationId", aEvent.getOrganizationId().value().toString());
                    span.setAttribute("userId", aMember.getUserId().value().toString());

                    if (aEvent.equals(aUpdatedEvent)) {
                        span.addEvent("Event not updated because received same data");
                        return UpdateEventOutput.from(aUpdatedEvent);
                    }

                    span.runInSpan("updateEvent", () -> this.eventRepository.save(aUpdatedEvent));

                    span.addEvent("Event updated successfully");
                    return UpdateEventOutput.from(aUpdatedEvent);
                }
        );
    }
}
