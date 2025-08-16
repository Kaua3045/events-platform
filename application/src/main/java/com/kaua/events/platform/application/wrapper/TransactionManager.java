package com.kaua.events.platform.application.wrapper;

import com.kaua.events.platform.domain.utils.Generated;

import java.util.function.Supplier;

@Generated
public interface TransactionManager {
    <T> TransactionResult<T> execute(Supplier<T> action);
}
