package com.kaua.events.platform.domain.eventmanagement;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record EventID(ULID value) implements Identifier<ULID> {

    public EventID {
        this.assertArgumentNotNull(value, "value", "should not be null");
    }
}
