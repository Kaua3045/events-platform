package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationOutput;

public record AddMemberToOrganizationResponse(
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("added_user_id") String addedUserId
) {

    public static AddMemberToOrganizationResponse from(final AddMemberToOrganizationOutput aOutput) {
        return new AddMemberToOrganizationResponse(aOutput.organizationId(), aOutput.addedUserId());
    }
}
