package com.kaua.events.platform.domain.organizations;

import java.util.Arrays;
import java.util.Optional;

public enum OrganizationMemberRole {

    OWNER,
    ADMIN,
    MEMBER;

    public static Optional<OrganizationMemberRole> from(final String value) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
