package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.Identifier;
import com.kaua.events.platform.domain.utils.ULID;

public record OrganizationMemberID(ULID value) implements Identifier<ULID> {

    public OrganizationMemberID {
        this.assertArgumentNotNull(value, "value", "should not be null");
    }
}
