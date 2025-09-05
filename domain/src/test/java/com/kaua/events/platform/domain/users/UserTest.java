package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.person.DocumentFactory;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewUser_thenShouldReturnUser() {
        final var aName = new Name("John", "Doe");
        final var aEmail = new Email("testes@test.com");
        final var aPassword = Password.of("123455Am@");
        final var aRole = UserRole.USER;

        final var aUser = User.newUser(aName, aEmail, aPassword, aRole);

        Assertions.assertNotNull(aUser);
        Assertions.assertNotNull(aUser.getId());
        Assertions.assertEquals(0L, aUser.getVersion());
        Assertions.assertEquals(aName, aUser.getName());
        Assertions.assertEquals(aEmail, aUser.getEmail());
        Assertions.assertEquals(aPassword, aUser.getPassword());
        Assertions.assertEquals(aRole, aUser.getRole());
        Assertions.assertTrue(aUser.getDocument().isEmpty());
        Assertions.assertNotNull(aUser.getCreatedAt());
        Assertions.assertNotNull(aUser.getUpdatedAt());
    }

    @Test
    void givenAValidValues_whenCallWith_thenShouldReturnUser() {
        final var aUserId = new UserID(ULID.random());
        final var aVersion = 1L;
        final var aName = new Name("John", "Doe");
        final var aEmail = new Email("testes@test.com");
        final var aPassword = Password.of("123455Am@");
        final var aRole = UserRole.USER;
        final var aDocument = DocumentFactory.create("217.641.740-20", "cpf");
        final var aNow = InstantUtils.now();

        final var aUser = User.with(
                aUserId,
                aVersion,
                aName,
                aEmail,
                aPassword,
                aRole,
                aDocument,
                aNow,
                aNow
        );

        Assertions.assertNotNull(aUser);
        Assertions.assertEquals(aUserId, aUser.getId());
        Assertions.assertEquals(aVersion, aUser.getVersion());
        Assertions.assertEquals(aName, aUser.getName());
        Assertions.assertEquals(aEmail, aUser.getEmail());
        Assertions.assertEquals(aPassword, aUser.getPassword());
        Assertions.assertEquals(aRole, aUser.getRole());
        Assertions.assertEquals(aDocument, aUser.getDocument().get());
        Assertions.assertNotNull(aUser.getCreatedAt());
        Assertions.assertNotNull(aUser.getUpdatedAt());
        Assertions.assertDoesNotThrow(() -> aUser.validate(NotificationHandler.create()));
    }

    @Test
    void testCallUserToString() {
        final var aUserId = new UserID(ULID.random());
        final var aVersion = 1L;
        final var aName = new Name("John", "Doe");
        final var aEmail = new Email("testes@test.com");
        final var aPassword = Password.of("123455Am@");
        final var aRole = UserRole.from("user").get();
        final var aDocument = DocumentFactory.create("217.641.740-20", "cpf");
        final var aNow = InstantUtils.now();

        final var aUser = User.with(
                aUserId,
                aVersion,
                aName,
                aEmail,
                aPassword,
                aRole,
                aDocument,
                aNow,
                aNow
        );

        final var aUserToString = aUser.toString();

        Assertions.assertNotNull(aUserToString);
        Assertions.assertTrue(aUserToString.contains("userId=" + aUserId.value().toString()));
        Assertions.assertTrue(aUserToString.contains("version=" + aVersion));
        Assertions.assertTrue(aUserToString.contains("name=" + aName.fullName()));
        Assertions.assertTrue(aUserToString.contains("email=" + aEmail.value()));
        Assertions.assertTrue(aUserToString.contains("role=" + aRole.name()));
        Assertions.assertTrue(aUserToString.contains("document=" + aDocument));
        Assertions.assertTrue(aUserToString.contains("createdAt=" + aNow));
        Assertions.assertTrue(aUserToString.contains("updatedAt=" + aNow));
    }

    @Test
    void givenAValidValues_whenCallUpdateDocument_thenReturnUpdatedUser() {
        final var aName = new Name("John", "Doe");
        final var aEmail = new Email("testes@test.com");
        final var aPassword = Password.of("123455Am@");
        final var aRole = UserRole.USER;
        final var aDocument = DocumentFactory.create("217.641.740-20", "cpf");

        final var aUser = User.newUser(
                aName,
                aEmail,
                aPassword,
                aRole
        );

        final var aUpdatedUser = aUser.updateDocument(aDocument);

        Assertions.assertEquals(aDocument, aUpdatedUser.getDocument().get());
        Assertions.assertTrue(aUser.getUpdatedAt().isBefore(aUpdatedUser.getUpdatedAt()));
    }
}
