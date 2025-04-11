package com.kaua.events.platform.domain;

import com.kaua.events.platform.domain.events.DomainEvent;
import com.kaua.events.platform.domain.validation.AssertionConcern;

import java.util.List;

public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> implements AssertionConcern {

    protected AggregateRoot(final ID id) {
        super(id, 0);
    }

    protected AggregateRoot(final ID id, final long version) {
        super(id, version);
    }

    protected AggregateRoot(final ID id, final long version, final List<DomainEvent> domainEvents) {
        super(id, version, domainEvents);
    }
}
