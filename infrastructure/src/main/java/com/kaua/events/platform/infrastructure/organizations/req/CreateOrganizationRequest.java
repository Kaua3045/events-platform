package com.kaua.events.platform.infrastructure.organizations.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.create.CreateOrganizationInput;

public record CreateOrganizationRequest(
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password,
        @JsonProperty("organization_name") String organizationName,
        @JsonProperty("description") String description
) {

    public CreateOrganizationInput toInput() {
        return CreateOrganizationInput.with(
                this.firstName,
                this.lastName,
                this.email,
                this.password,
                this.organizationName,
                this.description
        );
    }
}
