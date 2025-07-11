package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.retrieve.members.get.GetOrganizationMemberByUserIdOutput;

import java.time.Instant;

public record GetOrganizationMemberByUserIdResponse(
        @JsonProperty("organization_member_id") String organizationMemberId,
        @JsonProperty("version") long version,
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("member_role") String memberRole,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {

    public static GetOrganizationMemberByUserIdResponse from(final GetOrganizationMemberByUserIdOutput aOutput) {
        return new GetOrganizationMemberByUserIdResponse(
                aOutput.organizationMemberId(),
                aOutput.version(),
                aOutput.organizationId(),
                aOutput.userId(),
                aOutput.memberRole(),
                aOutput.createdAt(),
                aOutput.updatedAt()
        );
    }
}
