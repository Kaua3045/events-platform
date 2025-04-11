package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record UserID(ULID value) implements Identifier<ULID> {

    public UserID {
        this.assertArgumentNotNull(value, "value", "UserID cannot be null");
    }
}
