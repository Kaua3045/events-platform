package com.kaua.events.platform.infrastructure.organizations;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
