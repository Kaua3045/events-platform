package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationOutput;

public record CreateOrganizationResponse(
        @JsonProperty("organization_id") String organizationId,
        @JsonProperty("owner_id") String ownerId
) {

    public static CreateOrganizationResponse from(final CreateOrganizationOutput aOutput) {
        return new CreateOrganizationResponse(
                aOutput.organizationId(),
                aOutput.userId()
        );
    }
}
