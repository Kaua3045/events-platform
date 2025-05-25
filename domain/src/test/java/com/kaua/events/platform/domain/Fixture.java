package com.kaua.events.platform.domain;

import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.*;
import net.datafaker.Faker;

public final class Fixture {

    private static final Faker faker = new Faker();

    private Fixture() {}


    public static final class UserFixture {
        private UserFixture() {}

        public static User newUser() {
            return User.newUser(
                    new Name(faker.name().firstName(), faker.name().lastName()),
                    new Email(faker.internet().emailAddress()),
                    Password.of("12345678Am*"),
                    UserRole.USER
            );
        }
    }

    public static final class OrganizationFixture {
        private OrganizationFixture() {}

        public static Organization newOrganization() {
            return Organization.newOrganization(
                    "test-org",
                    "test-org-description"
            );
        }
    }

    public static final class OrganizationMemberFixture {
        private OrganizationMemberFixture() {}

        public static OrganizationMember newOwnerMember(OrganizationID organizationID, UserID userID) {
            return OrganizationMember.newOwnerMember(organizationID, userID);
        }

        public static OrganizationMember newMember(OrganizationID organizationID, UserID userID, OrganizationMemberRole role) {
            return OrganizationMember.newMember(organizationID, userID, role);
        }
    }
}
