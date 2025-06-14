package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrganizationMemberTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewOwnerMember_thenReturnOrganizationMemberOwner() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());

        final var aMember = OrganizationMember.newOwnerMember(aOrganizationId, aUserId);

        Assertions.assertNotNull(aMember);
        Assertions.assertNotNull(aMember.getId());
        Assertions.assertEquals(0, aMember.getVersion());
        Assertions.assertEquals(aOrganizationId, aMember.getOrganizationId());
        Assertions.assertEquals(aUserId, aMember.getUserId());
        Assertions.assertEquals(OrganizationMemberRole.OWNER, aMember.getMemberRole());
        Assertions.assertNotNull(aMember.getCreatedAt());
        Assertions.assertNotNull(aMember.getUpdatedAt());
        Assertions.assertDoesNotThrow(() -> aMember.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallNewMember_thenReturnOrganizationMember() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());
        final var aMemberRole = OrganizationMemberRole.ADMIN;

        final var aMember = OrganizationMember.newMember(aOrganizationId, aUserId, aMemberRole);

        Assertions.assertNotNull(aMember);
        Assertions.assertNotNull(aMember.getId());
        Assertions.assertEquals(0, aMember.getVersion());
        Assertions.assertEquals(aOrganizationId, aMember.getOrganizationId());
        Assertions.assertEquals(aUserId, aMember.getUserId());
        Assertions.assertEquals(aMemberRole, aMember.getMemberRole());
        Assertions.assertNotNull(aMember.getCreatedAt());
        Assertions.assertNotNull(aMember.getUpdatedAt());
        Assertions.assertDoesNotThrow(() -> aMember.validate(NotificationHandler.create()));
    }

    @Test
    void givenAnInvalidOwnerRole_whenCallNewMember_thenThrowsDomainException() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());
        final var aMemberRole = OrganizationMemberRole.OWNER;

        final var expectedErrorMessage = "To create a owner member use the specific method";

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> OrganizationMember.newMember(aOrganizationId, aUserId, aMemberRole));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidValues_whenCallWith_thenReturnOrganizationMember() {
        final var aMemberId = new OrganizationMemberID(IdentifierUtils.generateNewMonotonicULID());
        final var aVersion = 0L;
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());
        final var aMemberRole = OrganizationMemberRole.ADMIN;
        final var aNow = InstantUtils.now();

        final var aMember = OrganizationMember.with(
                aMemberId,
                aVersion,
                aOrganizationId,
                aUserId,
                aMemberRole,
                aNow,
                aNow
        );

        Assertions.assertNotNull(aMember);
        Assertions.assertEquals(aMemberId, aMember.getId());
        Assertions.assertEquals(aVersion, aMember.getVersion());
        Assertions.assertEquals(aOrganizationId, aMember.getOrganizationId());
        Assertions.assertEquals(aUserId, aMember.getUserId());
        Assertions.assertEquals(aMemberRole, aMember.getMemberRole());
        Assertions.assertEquals(aNow, aMember.getCreatedAt());
        Assertions.assertEquals(aNow, aMember.getUpdatedAt());
    }

    @Test
    void testCallToStringMethodInOrganizationMember() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());

        final var aMember = OrganizationMember.newOwnerMember(aOrganizationId, aUserId);

        final var aMemberToString = aMember.toString();

        Assertions.assertTrue(aMemberToString.contains("id=" + aMember.getId().value().toString()));
        Assertions.assertTrue(aMemberToString.contains("version=" + aMember.getVersion()));
        Assertions.assertTrue(aMemberToString.contains("organizationId=" + aOrganizationId.value().toString()));
        Assertions.assertTrue(aMemberToString.contains("userId=" + aUserId.value().toString()));
        Assertions.assertTrue(aMemberToString.contains("memberRole=" + aMember.getMemberRole().name()));
        Assertions.assertTrue(aMemberToString.contains("createdAt=" + aMember.getCreatedAt()));
        Assertions.assertTrue(aMemberToString.contains("updatedAt=" + aMember.getUpdatedAt()));
    }

    @Test
    void givenAValidMemberRoleString_whenCallFrom_thenReturnOrganizationMemberRole() {
        final var aMemberRoleString = "ADMIN";

        final var aMemberRole = OrganizationMemberRole.from(aMemberRoleString);

        Assertions.assertNotNull(aMemberRole);
        Assertions.assertEquals(OrganizationMemberRole.ADMIN, aMemberRole.get());
    }

    @Test
    void givenAValidMemberRole_whenCallChangeRole_thenReturnUpdatedOrganizationMember() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());
        final var aMemberRole = OrganizationMemberRole.ADMIN;

        final var aMember = OrganizationMember.newMember(aOrganizationId, aUserId, aMemberRole);
        final var newRole = OrganizationMemberRole.MEMBER;

        final var aUpdatedAt = aMember.getUpdatedAt();

        final var updatedMember = aMember.changeRole(newRole);

        Assertions.assertNotNull(updatedMember);
        Assertions.assertEquals(newRole, updatedMember.getMemberRole());
        Assertions.assertTrue(aUpdatedAt.isBefore(updatedMember.getUpdatedAt()));
    }

    @Test
    void givenAnInvalidMemberRole_whenCallChangeRole_thenThrowsDomainException() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());
        final var aMemberRole = OrganizationMemberRole.ADMIN;

        final var aMember = OrganizationMember.newMember(aOrganizationId, aUserId, aMemberRole);
        final OrganizationMemberRole invalidRole = null; // Simulating an invalid role

        final var expectedErrorMessage = "should not be null";
        final var expectedErrorProperty = "memberRole";

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> aMember.changeRole(invalidRole));

        Assertions.assertEquals(expectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(expectedErrorProperty, aException.getErrors().getFirst().property());
    }
}
