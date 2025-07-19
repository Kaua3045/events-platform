package com.kaua.events.platform.application.usecases.eventmanagement.update;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
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

    public DefaultUpdateEventUseCase(
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    @Override
    public UpdateEventOutput execute(final UpdateEventInput input) {
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

        this.eventRepository.save(aUpdatedEvent);

        return UpdateEventOutput.from(aUpdatedEvent);
    }
}
