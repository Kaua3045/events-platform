package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.Fixture;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

class OrganizationMemberJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(organizationMemberRepository());
    }

    @Test
    void givenAValidNewOrganizationMember_whenCallSave_thenOrganizationMemberIsPersisted() {
        Assertions.assertEquals(0, countOrganizationMembers());

        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aUserId = new UserID(IdentifierUtils.generateNewMonotonicULID());

        final var aOrganizationMember = OrganizationMember.newOwnerMember(
                aOrganizationId,
                aUserId
        );

        final var aActualOrganizationMember = this.organizationMemberRepository().save(aOrganizationMember);

        Assertions.assertEquals(1, countOrganizationMembers());
        Assertions.assertEquals(aOrganizationMember.getId(), aActualOrganizationMember.getId());
        Assertions.assertEquals(aOrganizationMember.getVersion(), aActualOrganizationMember.getVersion());
        Assertions.assertEquals(aOrganizationMember.getOrganizationId(), aActualOrganizationMember.getOrganizationId());
        Assertions.assertEquals(aOrganizationMember.getUserId(), aActualOrganizationMember.getUserId());
        Assertions.assertEquals(aOrganizationMember.getMemberRole(), aActualOrganizationMember.getMemberRole());
        Assertions.assertEquals(aOrganizationMember.getCreatedAt(), aActualOrganizationMember.getCreatedAt());
        Assertions.assertEquals(aOrganizationMember.getUpdatedAt(), aActualOrganizationMember.getUpdatedAt());
    }

    @Test
    void givenAValidUserId_whenCallMemberOfUserId_thenReturnMember() {
        Assertions.assertEquals(0, countOrganizationMembers());

        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );

        this.organizationMemberRepository().save(aMember);

        Assertions.assertEquals(1, countOrganizationMembers());

        final var aActualMember = this.organizationMemberRepository().memberOfUserId(aMember.getUserId().value().toString()).orElseThrow();

        Assertions.assertEquals(aMember.getId(), aActualMember.getId());
        Assertions.assertEquals(aMember.getVersion(), aActualMember.getVersion());
        Assertions.assertEquals(aMember.getOrganizationId(), aActualMember.getOrganizationId());
        Assertions.assertEquals(aMember.getUserId(), aActualMember.getUserId());
        Assertions.assertEquals(aMember.getMemberRole(), aActualMember.getMemberRole());
        Assertions.assertEquals(aMember.getCreatedAt(), aActualMember.getCreatedAt());
        Assertions.assertEquals(aMember.getUpdatedAt(), aActualMember.getUpdatedAt());
    }

    @Test
    void givenAnNonExistsUserId_whenCallMemberOfUserId_thenReturnEmpty() {
        Assertions.assertEquals(0, countOrganizationMembers());

        final var aUserId = "123456";

        final var aActualMember = this.organizationMemberRepository().memberOfUserId(aUserId);

        Assertions.assertTrue(aActualMember.isEmpty());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO organization_members (id, organization_id, user_id, member_role, created_at, updated_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '01JRP066XMA9GZZZZHAZZZZZYA', '01JRP066XMA9GZZZZHAZZZZZYD', 'non-exists', NOW(), NOW(), 0)"
    })
    void givenAValidIdButInvalidMemberRole_whenCallMemberOfUserId_thenThrowsDomainException() {
        Assertions.assertEquals(1, countOrganizationMembers());

        final var aId = "01JRP066XMA9GZZZZHAZZZZZYD";

        final var aExpectedErrorMessage = "should not be null";
        final var aExpectedErrorProperty = "memberRole";

        final var aException = Assertions.assertThrows(ValidationException.class,
                () -> this.organizationMemberRepository().memberOfUserId(aId));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aException.getErrors().getFirst().property());
    }

    @Test
    void givenAValidExistsMember_whenCallSave_thenReturnUpdatedMember() {
        Assertions.assertEquals(0, countOrganizationMembers());

        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );

        this.organizationMemberRepository().save(aMember);

        Assertions.assertEquals(1, countOrganizationMembers());

        final var aUpdatedAt = aMember.getUpdatedAt();

        final var aUpdatedMember = aMember.changeRole(OrganizationMemberRole.ADMIN);

        final var aActualMember = this.organizationMemberRepository().save(aUpdatedMember);

        Assertions.assertEquals(1, countOrganizationMembers());

        Assertions.assertEquals(aMember.getId(), aActualMember.getId());
        Assertions.assertEquals(aMember.getVersion(), aActualMember.getVersion());
        Assertions.assertEquals(aMember.getOrganizationId(), aActualMember.getOrganizationId());
        Assertions.assertEquals(aMember.getUserId(), aActualMember.getUserId());
        Assertions.assertEquals(OrganizationMemberRole.ADMIN, aActualMember.getMemberRole());
        Assertions.assertEquals(aMember.getCreatedAt(), aActualMember.getCreatedAt());
        Assertions.assertNotEquals(aUpdatedAt, aActualMember.getUpdatedAt());
        Assertions.assertTrue(aActualMember.getUpdatedAt().isAfter(aUpdatedAt));
    }

    @Test
    void givenAValidExistsMember_whenCallSaveButVersionIsNotMatch_thenThrowsConflictException() {
        Assertions.assertEquals(0, countOrganizationMembers());

        final var aMember = Fixture.OrganizationMemberFixture.newMember(
                new OrganizationID(ULID.random()),
                new UserID(ULID.random()),
                OrganizationMemberRole.MEMBER
        );

        final var aMemberSaved = this.organizationMemberRepository().save(aMember);

        final var expectedErrorMessage = "Organization member with identifier %s and version 2 does not match, organization member was updated by another transaction"
                .formatted(aMember.getId().value());

        Assertions.assertEquals(1, countOrganizationMembers());

        final var aSavedMemberSearched = this.organizationMemberRepository()
                .memberOfUserId(aMemberSaved.getUserId().value().toString())
                .orElseThrow();

        final var aUpdatedMember = aSavedMemberSearched.changeRole(OrganizationMemberRole.ADMIN);
        aUpdatedMember.incrementVersion(); // Simulate version mismatch

        final var aOrganizationMemberVariable = this.organizationMemberRepository(); // Variable to use in lambda, because this.customerRepository() is not allowed in lambda
        // this is a way to test if the exception is thrown, THIS IS A SCAM, in future disable this rule in sonar
        final var aException = Assertions.assertThrows(ConflictException.class,
                () -> aOrganizationMemberVariable.save(aUpdatedMember));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());
    }
}
