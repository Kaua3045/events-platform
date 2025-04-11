package com.kaua.events.platform.application.exceptions;

import com.kaua.events.platform.domain.exceptions.NoStackTraceException;

public class UseCaseInputCannotBeNullException extends NoStackTraceException {

    public UseCaseInputCannotBeNullException(Class<?> clazz) {
        super("Input to %s cannot be null".formatted(clazz.getSimpleName()));
    }
}
