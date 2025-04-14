package com.kaua.events.platform.infrastructure.users;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

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

    @Test
    void givenAValidEmail_whenCallUserOfEmail_thenReturnUser() {
        Assertions.assertEquals(0, countUsers());

        final var aUser = User.newUser(
                new Name("John", "Doe"),
                new Email("john.doe@test.com"),
                Password.of("123456Amq@"),
                UserRole.USER
        );

        this.userRepository().save(aUser);

        Assertions.assertEquals(1, countUsers());

        final var aActualUser = this.userRepository().userOfEmail(aUser.getEmail().value()).orElseThrow();

        Assertions.assertEquals(aUser.getId(), aActualUser.getId());
        Assertions.assertEquals(aUser.getVersion(), aActualUser.getVersion());
        Assertions.assertEquals(aUser.getName(), aActualUser.getName());
        Assertions.assertEquals(aUser.getEmail().value(), aActualUser.getEmail().value());
        Assertions.assertEquals(aUser.getPassword().value(), aActualUser.getPassword().value());
        Assertions.assertEquals(aUser.getRole(), aActualUser.getRole());
        Assertions.assertEquals(aUser.getCreatedAt(), aActualUser.getCreatedAt());
        Assertions.assertEquals(aUser.getUpdatedAt(), aActualUser.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsEmail_whenCallUserOfEmail_thenReturnEmpty() {
        Assertions.assertEquals(0, countUsers());

        final var aEmail = "john.doe@gmail.ts";

        final var aActualUser = this.userRepository().userOfEmail(aEmail);

        Assertions.assertTrue(aActualUser.isEmpty());
    }

    @Test
    void givenAValidId_whenCallUserOfId_thenReturnUser() {
        Assertions.assertEquals(0, countUsers());

        final var aUser = User.newUser(
                new Name("John", "Doe"),
                new Email("john.doe@test.com"),
                Password.of("123456Amq@"),
                UserRole.USER
        );

        this.userRepository().save(aUser);

        Assertions.assertEquals(1, countUsers());

        final var aActualUser = this.userRepository().userOfId(aUser.getId().value().toString()).orElseThrow();

        Assertions.assertEquals(aUser.getId(), aActualUser.getId());
        Assertions.assertEquals(aUser.getVersion(), aActualUser.getVersion());
        Assertions.assertEquals(aUser.getName(), aActualUser.getName());
        Assertions.assertEquals(aUser.getEmail().value(), aActualUser.getEmail().value());
        Assertions.assertEquals(aUser.getPassword().value(), aActualUser.getPassword().value());
        Assertions.assertEquals(aUser.getRole(), aActualUser.getRole());
        Assertions.assertEquals(aUser.getCreatedAt(), aActualUser.getCreatedAt());
        Assertions.assertEquals(aUser.getUpdatedAt(), aActualUser.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsId_whenCallUserOfId_thenReturnEmpty() {
        Assertions.assertEquals(0, countUsers());

        final var aId = "non-exists-id";

        final var aActualUser = this.userRepository().userOfId(aId);

        Assertions.assertTrue(aActualUser.isEmpty());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO users (id, first_name, last_name, email, password, role, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', 'john', 'doe', 'john.doe@teste.com', '1234656Am@', 'INVALID_ROLE', NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidRoleTypeStored_whenCallUserOfId_thenThrowsNotFoundException() {
        Assertions.assertEquals(1, countUsers());

        final var aId = "01JRP066XMA9GZZZZHAZZZZZYF";

        final var aExpectedErrorMessage = "UserRole not found";

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.userRepository().userOfId(aId));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());
    }
}
