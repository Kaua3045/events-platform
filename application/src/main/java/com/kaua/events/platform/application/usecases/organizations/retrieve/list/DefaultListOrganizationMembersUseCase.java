package com.kaua.events.platform.application.usecases.organizations.retrieve.list;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationMemberRepository;
import com.kaua.events.platform.domain.pagination.Pagination;

import java.util.Objects;

public class DefaultListOrganizationMembersUseCase extends ListOrganizationMembersUseCase {

    private final OrganizationMemberRepository organizationMemberRepository;

    public DefaultListOrganizationMembersUseCase(final OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = Objects.requireNonNull(organizationMemberRepository);
    }

    @Override
    public Pagination<ListOrganizationMembersOutput> execute(final ListOrganizationMembersInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(ListOrganizationMembersUseCase.class);

        return this.organizationMemberRepository.membersOfOrganizationId(
                input.organizationId(),
                input.query()
        ).map(ListOrganizationMembersOutput::from);
    }
}
