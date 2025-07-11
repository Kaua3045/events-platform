package com.kaua.events.platform.application.usecases.organizations.retrieve.members.get;

public record GetOrganizationMemberByUserIdInput(String userId) {

    public static GetOrganizationMemberByUserIdInput with(final String userId) {
        return new GetOrganizationMemberByUserIdInput(userId);
    }
}
