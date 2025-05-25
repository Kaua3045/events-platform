package com.kaua.events.platform.application.usecases.organizations.retrieve.get;

import com.kaua.events.platform.domain.organizations.Organization;

import java.time.Instant;

public record GetOrganizationByIdOutput(
        String id,
        long version,
        String name,
        String description,
        boolean isDeleted,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {

    public static GetOrganizationByIdOutput from(final Organization aOrganization) {
        return new GetOrganizationByIdOutput(
                aOrganization.getId().value().toString(),
                aOrganization.getVersion(),
                aOrganization.getName(),
                aOrganization.getDescription().orElse(null),
                aOrganization.isDeleted(),
                aOrganization.getCreatedAt(),
                aOrganization.getUpdatedAt(),
                aOrganization.getDeletedAt().orElse(null)
        );
    }
}
