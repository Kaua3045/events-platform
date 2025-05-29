package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
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

    @Test
    void givenAValidOrganizationId_whenCallOrganizationOfId_thenReturnOrganization() {
        Assertions.assertEquals(0, countOrganizations());

        final var aOrganization = Fixture.OrganizationFixture.newOrganization();

        this.organizationRepository().save(aOrganization);

        Assertions.assertEquals(1, countOrganizations());

        final var aActualOrganization = this.organizationRepository().organizationOfId(aOrganization.getId().value().toString()).orElseThrow();

        Assertions.assertEquals(aOrganization.getId(), aActualOrganization.getId());
        Assertions.assertEquals(aOrganization.getVersion(), aActualOrganization.getVersion());
        Assertions.assertEquals(aOrganization.getName(), aActualOrganization.getName());
        Assertions.assertEquals(aOrganization.getDescription(), aActualOrganization.getDescription());
        Assertions.assertEquals(aOrganization.isDeleted(), aActualOrganization.isDeleted());
        Assertions.assertEquals(aOrganization.getCreatedAt(), aActualOrganization.getCreatedAt());
        Assertions.assertEquals(aOrganization.getUpdatedAt(), aActualOrganization.getUpdatedAt());
        Assertions.assertEquals(aOrganization.getDeletedAt(), aActualOrganization.getDeletedAt());
    }

    @Test
    void givenAnNonExistsOrganizationId_whenCallOrganizationOfId_thenReturnEmpty() {
        Assertions.assertEquals(0, countOrganizations());

        final var aOrganizationId = "123456";

        final var aActualOrganization = this.organizationRepository().organizationOfId(aOrganizationId);

        Assertions.assertTrue(aActualOrganization.isEmpty());
    }
}
