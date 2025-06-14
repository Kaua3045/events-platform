package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.retrieve.list.ListOrganizationMembersOutput;

import java.time.Instant;

public record ListOrganizationMembersResponse(
        @JsonProperty("member_id") String memberId,
        @JsonProperty("role") String role,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("version") long version
) {

    public static ListOrganizationMembersResponse from(final ListOrganizationMembersOutput aMember) {
        return new ListOrganizationMembersResponse(
                aMember.memberId(),
                aMember.role(),
                aMember.createdAt(),
                aMember.version()
        );
    }
}
