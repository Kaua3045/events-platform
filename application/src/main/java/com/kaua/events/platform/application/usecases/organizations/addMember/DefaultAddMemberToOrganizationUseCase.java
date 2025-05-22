package com.kaua.events.platform.application.usecases.organizations.addMember;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.User;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultAddMemberToOrganizationUseCase extends AddMemberToOrganizationUseCase {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public DefaultAddMemberToOrganizationUseCase(
            final OrganizationMemberRepository organizationMemberRepository,
            final OrganizationRepository organizationRepository,
            final UserRepository userRepository
    ) {
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.organizationRepository = Objects.requireNonNull(organizationRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Override
    public AddMemberToOrganizationOutput execute(final AddMemberToOrganizationInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(AddMemberToOrganizationUseCase.class);

        if (!this.organizationRepository.existsById(input.organizationId())) {
            throw NotFoundException.with(Organization.class, input.organizationId()).get();
        }

        if (!this.userRepository.existsById(input.userId())) {
            throw NotFoundException.with(User.class, input.userId()).get();
        }

        final var aAuthenticatedMember = this.organizationMemberRepository.memberOfUserId(input.authenticatedUserId())
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.authenticatedUserId()));

        if (!aAuthenticatedMember.getOrganizationId().value().toString().equals(input.organizationId())) {
            throw DomainException.with("The authenticated user must be from the same organization");
        }

        final var aRole = OrganizationMemberRole.from(input.role())
                .orElseThrow(() -> NotFoundException.with("Role %s was not found".formatted(input.role())));

        if (aRole.equals(OrganizationMemberRole.OWNER)) {
            throw DomainException.with("Only one owner per organization permitted");
        }

        if (aAuthenticatedMember.getMemberRole().equals(OrganizationMemberRole.MEMBER)) {
            throw DomainException.with("Member does not permitted to add other member to organization");
        }

        if (aAuthenticatedMember.getMemberRole().equals(aRole)) {
            throw DomainException.with("Does not permitted add member with same role");
        }

        final var aMember = OrganizationMember.newMember(
                new OrganizationID(ULID.fromString(input.organizationId())),
                new UserID(ULID.fromString(input.userId())),
                aRole
        );

        this.organizationMemberRepository.save(aMember);

        return AddMemberToOrganizationOutput.from(aMember);
    }
}
