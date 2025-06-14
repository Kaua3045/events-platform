package com.kaua.events.platform.application.usecases.organizations.update.member;

public record UpdateMemberInput(
        String authenticatedUserId,
        String userId,
        String roleName
) {

    public static UpdateMemberInput with(final String authenticatedUserId, final String userId, final String roleName) {
        return new UpdateMemberInput(authenticatedUserId, userId, roleName);
    }
}
