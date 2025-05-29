package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.retrieve.get.GetOrganizationByIdOutput;

import java.time.Instant;

public record GetOrganizationByIdResponse(
        @JsonProperty("id") String id,
        @JsonProperty("version") long version,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("is_deleted") boolean isDeleted,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt
) {

    public static GetOrganizationByIdResponse from(final GetOrganizationByIdOutput aOutput) {
        return new GetOrganizationByIdResponse(
                aOutput.id(),
                aOutput.version(),
                aOutput.name(),
                aOutput.description(),
                aOutput.isDeleted(),
                aOutput.createdAt(),
                aOutput.updatedAt(),
                aOutput.deletedAt()
        );
    }
}
