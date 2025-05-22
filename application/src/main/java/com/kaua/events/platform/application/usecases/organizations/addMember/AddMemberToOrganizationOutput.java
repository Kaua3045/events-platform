package com.kaua.events.platform.application.usecases.organizations.addMember;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

public record AddMemberToOrganizationOutput(
        String organizationId,
        String addedUserId
) {

    public static AddMemberToOrganizationOutput from(final OrganizationMember aMember) {
        return new AddMemberToOrganizationOutput(
                aMember.getOrganizationId().value().toString(),
                aMember.getUserId().value().toString()
        );
    }
}
