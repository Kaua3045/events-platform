package com.kaua.events.platform.application.usecases.organizations.update.member;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

public record UpdateMemberOutput(
        String userId
) {

    public static UpdateMemberOutput from(final OrganizationMember aMember) {
        return new UpdateMemberOutput(aMember.getUserId().value().toString());
    }
}
