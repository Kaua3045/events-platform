package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.utils.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NameTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewName_thenReturnNameObject() {
        final var aFirstName = "John";
        final var aLastName = "Doe";

        final var aName = new Name(aFirstName, aLastName);

        Assertions.assertEquals(aFirstName, aName.firstName());
        Assertions.assertEquals(aLastName, aName.lastName());
        Assertions.assertEquals(aFirstName + " " + aLastName, aName.fullName());
    }

    @Test
    void givenAnInvalidFirstName_whenCallNewName_thenThrowValidationException() {
        final String firstName = null;
        final String lastName = "Doe";

        final var aProperty = "firstName";
        final var aMessage = "should not be empty";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidLastName_whenCallNewName_thenThrowValidationException() {
        final String firstName = "John";
        final String lastName = null;

        final var aProperty = "lastName";
        final var aMessage = "should not be empty";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMinLengthFirstName_whenCallNewName_thenThrowValidationException() {
        final String firstName = "J";
        final String lastName = "Doe";

        final var aProperty = "firstName";
        final var aMessage = "should have at least 3 characters";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMinLengthLastName_whenCallNewName_thenThrowValidationException() {
        final String firstName = "John";
        final String lastName = "D";

        final var aProperty = "lastName";
        final var aMessage = "should have at least 3 characters";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMaxLengthFirstName_whenCallNewName_thenThrowValidationException() {
        final String firstName = RandomStringUtils.generateValue(101);
        final String lastName = "Doe";

        final var aProperty = "firstName";
        final var aMessage = "should have at most 100 characters";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMaxLengthLastName_whenCallNewName_thenThrowValidationException() {
        final String firstName = "John";
        final String lastName = RandomStringUtils.generateValue(101);

        final var aProperty = "lastName";
        final var aMessage = "should have at most 100 characters";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> new Name(firstName, lastName));

        Assertions.assertEquals(aProperty, aException.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, aException.getErrors().get(0).message());
    }
}
