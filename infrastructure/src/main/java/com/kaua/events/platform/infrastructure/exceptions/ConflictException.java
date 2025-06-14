package com.kaua.events.platform.infrastructure.exceptions;

import com.kaua.events.platform.domain.exceptions.NoStackTraceException;

public class ConflictException extends NoStackTraceException {

    private ConflictException(final String message) {
        super(message);
    }

    public static ConflictException with(final String message) {
        return new ConflictException(message);
    }
}
