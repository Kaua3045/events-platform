package com.kaua.events.platform.domain.auth.token;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record AuthorizationTokenID(ULID value) implements Identifier<ULID> {

    public AuthorizationTokenID {
        this.assertArgumentNotNull(value, "value", "cannot be null");
    }
}
