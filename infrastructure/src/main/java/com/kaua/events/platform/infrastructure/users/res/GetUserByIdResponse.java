package com.kaua.events.platform.infrastructure.users.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.users.retrive.get.GetUserByIdOutput;

import java.time.Instant;

public record GetUserByIdResponse(
        @JsonProperty("id") String id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("email") String email,
        @JsonProperty("role") String role,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("version") long version
) {

    public static GetUserByIdResponse from(final GetUserByIdOutput aOutput) {
        return new GetUserByIdResponse(
                aOutput.id(),
                aOutput.firstName(),
                aOutput.lastName(),
                aOutput.email(),
                aOutput.role(),
                aOutput.createdAt(),
                aOutput.updatedAt(),
                aOutput.version()
        );
    }
}
