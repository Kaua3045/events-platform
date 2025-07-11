package com.kaua.events.platform.application.usecases.organizations.retrieve.members.get;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.OrganizationMember;

import java.util.Objects;

public class DefaultGetOrganizationMemberByUserIdUseCase extends GetOrganizationMemberByUserIdUseCase {

    private final OrganizationMemberRepository organizationMemberRepository;

    public DefaultGetOrganizationMemberByUserIdUseCase(final OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    @Override
    public GetOrganizationMemberByUserIdOutput execute(final GetOrganizationMemberByUserIdInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(GetOrganizationMemberByUserIdUseCase.class);

        return this.organizationMemberRepository.memberOfUserId(input.userId())
                .map(GetOrganizationMemberByUserIdOutput::from)
                .orElseThrow(NotFoundException.with(OrganizationMember.class, input.userId()));
    }
}
