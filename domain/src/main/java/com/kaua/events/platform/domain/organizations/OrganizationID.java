package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record OrganizationID(ULID value) implements Identifier<ULID> {

    public OrganizationID {
        this.assertArgumentNotNull(value, "value", "OrganizationID cannot be null");
    }
}
