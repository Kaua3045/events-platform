package com.kaua.events.platform.application.wrapper;

import com.kaua.events.platform.domain.utils.Generated;

@Generated
public class TransactionResult<T> {

    private final T value;
    private final RuntimeException exception;

    private TransactionResult(T value, RuntimeException exception) {
        this.value = value;
        this.exception = exception;
    }

    public static <T> TransactionResult<T> success(T value) {
        return new TransactionResult<>(value, null);
    }

    public static <T> TransactionResult<T> failure(RuntimeException exception) {
        return new TransactionResult<>(null, exception);
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public boolean isFailure() {
        return exception != null;
    }

    public T getValue() {
        if (isFailure()) throw new IllegalStateException("Transaction failed", exception);
        return value;
    }

    public RuntimeException getException() {
        return exception;
    }
}
