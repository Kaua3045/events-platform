package com.kaua.events.platform.application.usecases.organizations.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.application.usecases.users.create.CreateUserInput;
import com.kaua.events.platform.application.usecases.users.create.CreateUserUseCase;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.ULID;

import java.util.Objects;

public class DefaultCreateOrganizationUseCase extends CreateOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final CreateUserUseCase createUserUseCase;

    public DefaultCreateOrganizationUseCase(
            final OrganizationRepository organizationRepository,
            final OrganizationMemberRepository organizationMemberRepository,
            final CreateUserUseCase createUserUseCase
    ) {
        this.organizationRepository = Objects.requireNonNull(organizationRepository);
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
        this.createUserUseCase = Objects.requireNonNull(createUserUseCase);
    }

    @Override
    public CreateOrganizationOutput execute(final CreateOrganizationInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(CreateOrganizationUseCase.class);

        if (this.organizationRepository.existsByName(input.organizationName())) {
            throw DomainException.with("The organization name %s already exists".formatted(input.organizationName()));
        }

        final var aUserOutput = this.createUserUseCase.execute(CreateUserInput.with(
                input.firstName(),
                input.lastName(),
                input.email(),
                input.password()
        ));

        final var aUserId = aUserOutput.userId();

        final var aOrganization = Organization.newOrganization(
                input.organizationName(),
                input.description()
        );

        final var aOrganizationOwnerMember = OrganizationMember.newOwnerMember(
                aOrganization.getId(),
                new UserID(ULID.fromString(aUserId))
        );

        this.organizationRepository.save(aOrganization);
        this.organizationMemberRepository.save(aOrganizationOwnerMember);

        return CreateOrganizationOutput.from(
                aOrganization,
                aOrganizationOwnerMember
        );
    }
}
