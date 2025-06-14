package com.kaua.events.platform.application.usecases.organizations.update.member;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;

import java.util.Objects;

public class DefaultUpdateMemberUseCase extends UpdateMemberUseCase {

    private final OrganizationMemberRepository organizationMemberRepository;

    public DefaultUpdateMemberUseCase(final OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    @Override
    public UpdateMemberOutput execute(final UpdateMemberInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(UpdateMemberUseCase.class);

        final var aMemberAuthenticated = this.organizationMemberRepository.memberOfUserId(input.authenticatedUserId())
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.authenticatedUserId()));

        final var aMemberToUpdate = this.organizationMemberRepository.memberOfUserId(input.userId())
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));

        if (aMemberAuthenticated.getMemberRole().equals(OrganizationMemberRole.MEMBER)) {
            throw DomainException.with("You cannot update a member with MEMBER role.");
        }

        if (!aMemberAuthenticated.getOrganizationId().equals(aMemberToUpdate.getOrganizationId())) {
            throw DomainException.with("You cannot update a member that is not part of your organization.");
        }

        final var aMemberRole = OrganizationMemberRole.from(input.roleName())
                .orElseThrow(() -> NotFoundException.with("Member role with name %s not found.".formatted(input.roleName())));

        if (aMemberRole.equals(OrganizationMemberRole.OWNER)) {
            throw DomainException.with("You cannot update a member to OWNER role.");
        }

        if (!aMemberAuthenticated.getMemberRole().equals(OrganizationMemberRole.OWNER)) {
            throw DomainException.with("Only the owner can update a member to a different role.");
        }

        final var aUpdatedMember = aMemberToUpdate.changeRole(aMemberRole);

        this.organizationMemberRepository.save(aUpdatedMember);
        return UpdateMemberOutput.from(aUpdatedMember);
    }
}
