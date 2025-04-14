package com.kaua.events.platform.infrastructure.services.encryption;

import com.kaua.events.platform.IntegrationTest;
import com.kaua.events.platform.domain.users.PasswordEncryption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class PasswordEncryptionTest {

    @Autowired
    private PasswordEncryption passwordEncryption;

    @Test
    void givenAValidPassword_whenCallEncrypt_thenReturnEncryptedPassword() {
        final var password = "password";
        final var encryptedPassword = this.passwordEncryption.encrypt(password);

        Assertions.assertNotNull(encryptedPassword);
        Assertions.assertNotEquals(password, encryptedPassword);
    }

    @Test
    void givenAValidPassword_whenCallMatches_thenReturnTrue() {
        final var password = "password";
        final var encryptedPassword = this.passwordEncryption.encrypt(password);

        Assertions.assertTrue(this.passwordEncryption.matches(password, encryptedPassword));
    }

    @Test
    void givenAnInvalidPassword_whenCallMatches_thenReturnFalse() {
        final var password = "password";
        final var encryptedPassword = this.passwordEncryption.encrypt(password);

        Assertions.assertFalse(this.passwordEncryption.matches("invalidPassword", encryptedPassword));
    }
}
