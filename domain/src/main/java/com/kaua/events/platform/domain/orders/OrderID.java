package com.kaua.events.platform.domain.orders;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record OrderID(ULID value) implements Identifier<ULID> {

    public OrderID {
        this.assertArgumentNotNull(value, "value", "should not be null");
    }
}
