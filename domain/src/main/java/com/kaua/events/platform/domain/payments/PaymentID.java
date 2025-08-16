package com.kaua.events.platform.domain.payments;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record PaymentID(ULID value) implements Identifier<ULID> {

    public PaymentID {
        this.assertArgumentNotNull(value, "value", "should not be null");
    }
}
