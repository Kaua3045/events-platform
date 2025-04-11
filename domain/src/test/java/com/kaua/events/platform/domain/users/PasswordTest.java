package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.utils.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PasswordTest extends UnitTest {

    @ParameterizedTest
    @CsvSource({
            "123456Am*",
            "Am@123456",
            "123456Am@",
            "12345678mA!@",
    })
    void givenAValidPassword_whenCreatePassword_thenPasswordIsCreated(String password) {
        final var userPassword = Password.create(password, getPasswordEncryption());
        Assertions.assertEquals(password, userPassword.value());
    }

    @Test
    void givenAValidPassword_whenCallOfPassword_thenReturnPasswordObject() {
        final var aPassword = "12345678Am@";
        final var userPassword = Password.of(aPassword);

        Assertions.assertEquals(aPassword, userPassword.value());
        Assertions.assertTrue(userPassword.matches(aPassword, getPasswordEncryption()));
    }

    @ParameterizedTest
    @CsvSource({
            "12345678",
            "12345678a",
            "12345678A",
            "12345678@",
            "12345678aA",
            "12345678a@",
            "12345678A@"
    })
    void givenAnInvalidPassword_whenCreatePassword_thenThrowDomainException(String password) {
        final var aProperty = "password";
        final var aMessage = "should have at least one lowercase letter, one uppercase letter, one digit and one special character";

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> Password.create(password, getPasswordEncryption()));

        Assertions.assertEquals(aProperty, exception.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, exception.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidNullPassword_whenCreatePassword_thenThrowDomainException() {
        final var aProperty = "password";
        final var aMessage = "should not be empty";

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> Password.create(null, getPasswordEncryption()));

        Assertions.assertEquals(aProperty, exception.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, exception.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMinLengthPassword_whenCreatePassword_thenThrowDomainException() {
        final var aProperty = "password";
        final var aMessage = "should have at least 8 characters";

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> Password.create("1234567", getPasswordEncryption()));

        Assertions.assertEquals(aProperty, exception.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, exception.getErrors().get(0).message());
    }

    @Test
    void givenAnInvalidMaxLengthPassword_whenCreatePassword_thenThrowDomainException() {
        final var aPassword = RandomStringUtils.generateValue(256);
        final var aProperty = "password";
        final var aMessage = "should have at most 255 characters";

        final var exception = Assertions.assertThrows(DomainException.class,
                () -> Password.create(aPassword, getPasswordEncryption()));

        Assertions.assertEquals(aProperty, exception.getErrors().get(0).property());
        Assertions.assertEquals(aMessage, exception.getErrors().get(0).message());
    }

    private PasswordEncryption getPasswordEncryption() {
        return new PasswordEncryption() {
            @Override
            public String encrypt(String rawPassword) {
                return rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String encryptedPassword) {
                return rawPassword.equals(encryptedPassword);
            }
        };
    }
}
