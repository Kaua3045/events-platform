package com.kaua.events.platform.application.usecases.eventmanagement.delete;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.EventRepository;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;

import java.util.Objects;

public class DefaultSoftDeleteEventUseCase extends SoftDeleteEventUseCase {

    private final EventRepository eventRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public DefaultSoftDeleteEventUseCase(
            final EventRepository eventRepository,
            final OrganizationMemberRepository organizationMemberRepository
    ) {
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    // TODO adicionar um if verificando se o evento existe ou se nao foi deletado
    @Override
    public void execute(final SoftDeleteEventInput input) {
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

        final var aDeletedEvent = aEvent.markAsDeleted();

        this.eventRepository.save(aDeletedEvent);
    }
}
