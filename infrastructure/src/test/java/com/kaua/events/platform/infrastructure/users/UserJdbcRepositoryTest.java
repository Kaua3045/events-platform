package com.kaua.events.platform.infrastructure.users;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.users.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(userRepository());
    }

    @Test
    void givenAValidNewUser_whenCallSave_thenUserIsPersisted() {
        Assertions.assertEquals(0, countUsers());

        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "123456Amq@";
        final var aRole = UserRole.USER;

        final var aUser = User.newUser(
                new Name(aFirstName, aLastName),
                new Email(aEmail),
                Password.of(aPassword),
                aRole
        );

        final var aActualUser = this.userRepository().save(aUser);

        Assertions.assertEquals(1, countUsers());
        Assertions.assertEquals(aUser.getId(), aActualUser.getId());
        Assertions.assertEquals(aUser.getVersion(), aActualUser.getVersion());
        Assertions.assertEquals(aUser.getName(), aActualUser.getName());
        Assertions.assertEquals(aUser.getEmail(), aActualUser.getEmail());
        Assertions.assertEquals(aUser.getPassword(), aActualUser.getPassword());
        Assertions.assertEquals(aUser.getRole(), aActualUser.getRole());
        Assertions.assertEquals(aUser.getCreatedAt(), aActualUser.getCreatedAt());
        Assertions.assertEquals(aUser.getUpdatedAt(), aActualUser.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsEmail_whenCallExistsByEmail_thenReturnFalse() {
        Assertions.assertEquals(0, countUsers());
        final var aEmail = "testes.tess@teste.com";

        final var aActualResponse = this.userRepository().existsByEmail(aEmail);

        Assertions.assertFalse(aActualResponse);
    }

    @Test
    void givenAnExistsEmail_whenCallExistsByEmail_thenReturnTrue() {
        Assertions.assertEquals(0, countUsers());

        final var aFirstName = "John";
        final var aLastName = "Doe";
        final var aEmail = "john.doe@teste.com";
        final var aPassword = "123456Amq@";
        final var aRole = UserRole.USER;

        final var aUser = User.newUser(
                new Name(aFirstName, aLastName),
                new Email(aEmail),
                Password.of(aPassword),
                aRole
        );
        this.userRepository().save(aUser);

        Assertions.assertEquals(1, countUsers());

        final var aActualResponse = this.userRepository().existsByEmail(aEmail);

        Assertions.assertTrue(aActualResponse);
    }
}
