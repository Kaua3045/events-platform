package com.kaua.events.platform.infrastructure.exceptions;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.validation.Error;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TooManyRequestsExceptionTest extends UnitTest {

    @Test
    void givenMessage_whenWithMessage_thenExceptionCreated() {
        String message = "Rate limit exceeded";
        TooManyRequestsException ex = TooManyRequestsException.with(message);

        Assertions.assertNotNull(ex);
        Assertions.assertEquals(message, ex.getMessage());
        Assertions.assertNotNull(ex.getErrors());
        Assertions.assertTrue(ex.getErrors().isEmpty());
    }

    @Test
    void givenErrors_whenWithErrors_thenExceptionCreated() {
        List<Error> errors = List.of(
                new Error("field1", "must not be null"),
                new Error("field2", "invalid value")
        );

        TooManyRequestsException ex = TooManyRequestsException.with(errors);

        Assertions.assertNotNull(ex);
        Assertions.assertEquals("TooManyRequestsException", ex.getMessage());
        Assertions.assertNotNull(ex.getErrors());
        Assertions.assertEquals(2, ex.getErrors().size());
        Assertions.assertEquals("field1", ex.getErrors().get(0).property());
        Assertions.assertEquals("must not be null", ex.getErrors().get(0).message());
        Assertions.assertEquals("field2", ex.getErrors().get(1).property());
        Assertions.assertEquals("invalid value", ex.getErrors().get(1).message());
    }

    @Test
    void givenEmptyErrors_whenWithErrors_thenExceptionHasEmptyErrorsList() {
        TooManyRequestsException ex = TooManyRequestsException.with(List.of());

        Assertions.assertNotNull(ex);
        Assertions.assertEquals("TooManyRequestsException", ex.getMessage());
        Assertions.assertNotNull(ex.getErrors());
        Assertions.assertTrue(ex.getErrors().isEmpty());
    }
}
