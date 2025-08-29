package com.kaua.events.platform.infrastructure.exceptions;

import com.kaua.events.platform.domain.exceptions.NoStackTraceException;
import com.kaua.events.platform.domain.validation.Error;

import java.util.Collections;
import java.util.List;

public class TooManyRequestsException extends NoStackTraceException {

    private final List<Error> errors;

    private TooManyRequestsException(final String message, final List<Error> errors) {
        super(message);
        this.errors = errors;
    }

    public static TooManyRequestsException with(final String message) {
        return new TooManyRequestsException(message, Collections.emptyList());
    }

    public static TooManyRequestsException with(final List<Error> errors) {
        return new TooManyRequestsException("TooManyRequestsException", errors);
    }

    public List<Error> getErrors() {
        return errors;
    }
}
