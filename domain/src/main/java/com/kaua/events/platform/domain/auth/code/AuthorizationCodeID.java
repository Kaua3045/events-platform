package com.kaua.events.platform.domain.auth.code;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record AuthorizationCodeID(ULID value) implements Identifier<ULID> {

    public AuthorizationCodeID {
        this.assertArgumentNotNull(value, "value", "cannot be null");
    }
}
