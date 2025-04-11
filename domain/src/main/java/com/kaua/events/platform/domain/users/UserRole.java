package com.kaua.events.platform.domain.users;

import java.util.Arrays;
import java.util.Optional;

public enum UserRole {

    ORGANIZER_OWNER,
    ORGANIZER_ADMIN,
    USER;

    public static Optional<UserRole> from(final String aRole) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(aRole))
                .findFirst();
    }
}
