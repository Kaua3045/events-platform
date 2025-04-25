package com.kaua.events.platform.application.usecases.organizations.create;

import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationMember;

public record CreateOrganizationOutput(
        String organizationId,
        String userId
) {

    public static CreateOrganizationOutput from(
            final Organization aOrganization,
            final OrganizationMember aOrganizationMember
    ) {
        return new CreateOrganizationOutput(
                aOrganization.getId().value().toString(),
                aOrganizationMember.getUserId().value().toString()
        );
    }
}
