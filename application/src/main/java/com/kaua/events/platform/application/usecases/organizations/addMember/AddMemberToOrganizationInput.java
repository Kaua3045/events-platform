package com.kaua.events.platform.application.usecases.organizations.addMember;

public record AddMemberToOrganizationInput(
        String organizationId,
        String authenticatedUserId,
        String userId,
        String role
) {

    public static AddMemberToOrganizationInput with(
            final String aOrganizationId,
            final String aAuthenticatedUserId,
            final String aUserId,
            final String aRole
    ) {
        return new AddMemberToOrganizationInput(
                aOrganizationId,
                aAuthenticatedUserId,
                aUserId,
                aRole
        );
    }
}
