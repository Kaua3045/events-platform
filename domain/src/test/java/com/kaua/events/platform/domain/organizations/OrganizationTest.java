package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrganizationTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewOrganization_thenReturnOrganization() {
        final var aName = "organization-test";
        final var aDescription = "teste";

        final var aOrganization = Organization.newOrganization(
                aName,
                aDescription
        );

        Assertions.assertNotNull(aOrganization.getId());
        Assertions.assertEquals(aName, aOrganization.getName());
        Assertions.assertEquals(aDescription, aOrganization.getDescription().get());
        Assertions.assertFalse(aOrganization.isDeleted());
        Assertions.assertNotNull(aOrganization.getCreatedAt());
        Assertions.assertNotNull(aOrganization.getUpdatedAt());
        Assertions.assertTrue(aOrganization.getDeletedAt().isEmpty());
        Assertions.assertDoesNotThrow(() -> aOrganization.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWith_thenReturnOrganization() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aVersion = 0L;
        final var aName = "organization-test";
        final var aDescription = "teste";
        final var aIsDeleted = false;
        final var aNow = InstantUtils.now();

        final var aOrganization = Organization.with(
                aOrganizationId,
                aVersion,
                aName,
                aDescription,
                aIsDeleted,
                aNow,
                aNow,
                null
        );

        Assertions.assertEquals(aOrganizationId, aOrganization.getId());
        Assertions.assertEquals(aVersion, aOrganization.getVersion());
        Assertions.assertEquals(aName, aOrganization.getName());
        Assertions.assertEquals(aDescription, aOrganization.getDescription().get());
        Assertions.assertEquals(aIsDeleted, aOrganization.isDeleted());
        Assertions.assertEquals(aNow, aOrganization.getCreatedAt());
        Assertions.assertEquals(aNow, aOrganization.getUpdatedAt());
        Assertions.assertTrue(aOrganization.getDeletedAt().isEmpty());
    }

    @Test
    void testCallToStringOrganization() {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aVersion = 0L;
        final var aName = "organization-test";
        final var aDescription = "teste";
        final var aIsDeleted = false;
        final var aNow = InstantUtils.now();

        final var aOrganization = Organization.with(
                aOrganizationId,
                aVersion,
                aName,
                aDescription,
                aIsDeleted,
                aNow,
                aNow,
                null
        );

        final var aOrganizationToString = aOrganization.toString();

        Assertions.assertTrue(aOrganizationToString.contains("id='" + aOrganizationId.value().toString()));
        Assertions.assertTrue(aOrganizationToString.contains("version='" + aVersion));
        Assertions.assertTrue(aOrganizationToString.contains("name='" + aName));
        Assertions.assertTrue(aOrganizationToString.contains("description='" + aDescription));
        Assertions.assertTrue(aOrganizationToString.contains("isDeleted=" + aIsDeleted));
        Assertions.assertTrue(aOrganizationToString.contains("createdAt=" + aNow));
        Assertions.assertTrue(aOrganizationToString.contains("updatedAt=" + aNow));
        Assertions.assertTrue(aOrganizationToString.contains("deletedAt="));
    }
}
