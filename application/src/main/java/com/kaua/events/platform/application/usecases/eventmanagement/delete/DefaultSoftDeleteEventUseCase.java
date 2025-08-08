package com.kaua.events.platform.application.usecases.eventmanagement.delete;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;

import java.util.Objects;

public class DefaultSoftDeleteEventUseCase extends SoftDeleteEventUseCase {

    private final EventRepository eventRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultSoftDeleteEventUseCase(
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    // TODO adicionar um if verificando se o evento existe ou se nao foi deletado
    @Override
    public void execute(final SoftDeleteEventInput input) {
        this.tracerWrapper.trace(
                "softDeleteEventUseCase",
                span -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(SoftDeleteEventUseCase.class);

                    final var aMember = this.organizationMemberRepository.memberOfUserId(input.userId())
                            .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

                    if (!aMember.getMemberRole().equals(OrganizationMemberRole.OWNER)) {
                        throw DomainException.with("Only owners can delete events");
                    }

                    final var aEvent = this.eventRepository.eventOfId(input.eventId())
                            .orElseThrow(NotFoundException.with(Event.class, input.eventId()));

                    if (!aMember.getOrganizationId().value().equals(aEvent.getOrganizationId().value())) {
                        throw DomainException.with("Event does not belong to the organization of the user");
                    }

                    span.setAttribute("userId", input.userId());
                    span.setAttribute("eventId", input.eventId());
                    span.setAttribute("organizationId", aEvent.getOrganizationId().value().toString());

                    final var aDeletedEvent = aEvent.markAsDeleted();

                    span.setAttribute("event.status", aDeletedEvent.getStatus().name());

                    span.runInSpan("updateDeletedEvent", () -> this.eventRepository.save(aDeletedEvent));
                });
    }
}
