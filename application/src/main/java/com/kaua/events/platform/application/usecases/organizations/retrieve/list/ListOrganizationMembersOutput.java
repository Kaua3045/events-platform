package com.kaua.events.platform.application.usecases.organizations.retrieve.list;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

import java.time.Instant;

public record ListOrganizationMembersOutput(
        String memberId,
        String userId,
        String role,
        Instant createdAt,
        long version
) {

    public static ListOrganizationMembersOutput from(final OrganizationMember aMember) {
        return new ListOrganizationMembersOutput(
                aMember.getId().value().toString(),
                aMember.getUserId().value().toString(),
                aMember.getMemberRole().name(),
                aMember.getCreatedAt(),
                aMember.getVersion()
        );
    }
}
