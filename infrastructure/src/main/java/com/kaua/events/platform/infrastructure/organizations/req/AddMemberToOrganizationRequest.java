package com.kaua.events.platform.infrastructure.organizations.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.addMember.AddMemberToOrganizationInput;

public record AddMemberToOrganizationRequest(
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("authenticated_user_id") String authenticatedUserId,
        @JsonProperty("add_user_id") String addUserId,
        @JsonProperty("role") String role
) {

    public AddMemberToOrganizationInput toInput() {
        return AddMemberToOrganizationInput.with(
                organizationId,
                authenticatedUserId,
                addUserId,
                role
        );
    }
}
