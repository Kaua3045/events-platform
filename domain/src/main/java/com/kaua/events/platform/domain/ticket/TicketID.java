package com.kaua.events.platform.domain.ticket;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record TicketID(ULID value) implements Identifier<ULID> {

    public TicketID {
        this.assertArgumentNotNull(value, "value", "should not be null");
    }
}
