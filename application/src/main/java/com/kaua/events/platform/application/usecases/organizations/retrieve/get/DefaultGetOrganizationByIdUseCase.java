package com.kaua.events.platform.application.usecases.organizations.retrieve.get;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.OrganizationRepository;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.organizations.Organization;

import java.util.Objects;

public class DefaultGetOrganizationByIdUseCase extends GetOrganizationByIdUseCase {

    private final OrganizationRepository organizationRepository;

    public DefaultGetOrganizationByIdUseCase(final OrganizationRepository organizationRepository) {
        this.organizationRepository = Objects.requireNonNull(organizationRepository);
    }

    @Override
    public GetOrganizationByIdOutput execute(final GetOrganizationByIdInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(GetOrganizationByIdUseCase.class);

        return this.organizationRepository.organizationOfId(input.id())
                .map(GetOrganizationByIdOutput::from)
                .orElseThrow(NotFoundException.with(Organization.class, input.id()));
    }
}
