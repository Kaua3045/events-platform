package com.kaua.events.platform.domain.auth.token;

import java.util.Arrays;
import java.util.Optional;

public enum AuthorizationTokenType {

    ACCESS_TOKEN,
    REFRESH_TOKEN,
    ID_TOKEN;

    public static Optional<AuthorizationTokenType> from(final String aType) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(aType))
                .findFirst();
    }
}
