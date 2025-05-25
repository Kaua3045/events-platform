package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.organizations.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrganizationJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(organizationRepository());
    }

    @Test
    void givenAValidNewOrganization_whenCallSave_thenOrganizationIsPersisted() {
        Assertions.assertEquals(0, countOrganizations());

        final var aName = "organization-test";
        final var aDescription = "teste";

        final var aOrganization = Organization.newOrganization(
                aName,
                aDescription
        );

        final var aActualOrganization = this.organizationRepository().save(aOrganization);

        Assertions.assertEquals(1, countOrganizations());
        Assertions.assertEquals(aOrganization.getId(), aActualOrganization.getId());
        Assertions.assertEquals(aOrganization.getVersion(), aActualOrganization.getVersion());
        Assertions.assertEquals(aOrganization.getName(), aActualOrganization.getName());
        Assertions.assertEquals(aOrganization.getDescription(), aActualOrganization.getDescription());
        Assertions.assertEquals(aOrganization.getCreatedAt(), aActualOrganization.getCreatedAt());
        Assertions.assertEquals(aOrganization.getUpdatedAt(), aActualOrganization.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsName_whenCallExistsByName_thenReturnFalse() {
        Assertions.assertEquals(0, countOrganizations());
        final var aName = "organization-test";

        final var aActualResponse = this.organizationRepository().existsByName(aName);

        Assertions.assertFalse(aActualResponse);
    }

    @Test
    void givenAnExistsName_whenCallExistsByName_thenReturnTrue() {
        Assertions.assertEquals(0, countOrganizations());

        final var aName = "organization-test";
        final var aDescription = "teste";

        final var aOrganization = Organization.newOrganization(
                aName,
                aDescription
        );
        this.organizationRepository().save(aOrganization);

        Assertions.assertEquals(1, countOrganizations());

        final var aActualResponse = this.organizationRepository().existsByName(aName);

        Assertions.assertTrue(aActualResponse);
    }

    @Test
    void givenAnNonExistsId_whenCallExistsById_thenReturnFalse() {
        Assertions.assertEquals(0, countOrganizations());
        final var aId = "organization-id";

        final var aActualResponse = this.organizationRepository().existsById(aId);

        Assertions.assertFalse(aActualResponse);
    }

    @Test
    void givenAnExistsId_whenCallExistsById_thenReturnTrue() {
        Assertions.assertEquals(0, countOrganizations());

        final var aName = "organization-test";
        final var aDescription = "teste";

        final var aOrganization = Organization.newOrganization(
                aName,
                aDescription
        );

        this.organizationRepository().save(aOrganization);

        Assertions.assertEquals(1, countOrganizations());

        final var aActualResponse = this.organizationRepository().existsById(aOrganization.getId().value().toString());

        Assertions.assertTrue(aActualResponse);
    }
}
