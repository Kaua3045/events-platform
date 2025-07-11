package com.kaua.events.platform.application.usecases.organizations.retrieve.members.get;

import com.kaua.events.platform.domain.organizations.OrganizationMember;

import java.time.Instant;

public record GetOrganizationMemberByUserIdOutput(
        String organizationMemberId,
        long version,
        String organizationId,
        String userId,
        String memberRole,
        Instant createdAt,
        Instant updatedAt
) {

    public static GetOrganizationMemberByUserIdOutput from(final OrganizationMember aMember) {
        return new GetOrganizationMemberByUserIdOutput(
                aMember.getId().value().toString(),
                aMember.getVersion(),
                aMember.getOrganizationId().value().toString(),
                aMember.getUserId().value().toString(),
                aMember.getMemberRole().name(),
                aMember.getCreatedAt(),
                aMember.getUpdatedAt()
        );
    }
}
