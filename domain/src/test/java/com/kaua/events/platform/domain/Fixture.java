package com.kaua.events.platform.domain;

import com.kaua.events.platform.domain.eventmanagement.Event;
import com.kaua.events.platform.domain.eventmanagement.EventStatus;
import com.kaua.events.platform.domain.eventmanagement.EventType;
import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.users.*;
import com.kaua.events.platform.domain.utils.InstantUtils;
import net.datafaker.Faker;

import java.time.temporal.ChronoUnit;

public final class Fixture {

    private static final Faker faker = new Faker();

    private Fixture() {
    }


    public static final class UserFixture {
        private UserFixture() {
        }

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
        private OrganizationFixture() {
        }

        public static Organization newOrganization() {
            return Organization.newOrganization(
                    "test-org",
                    "test-org-description"
            );
        }
    }

    public static final class OrganizationMemberFixture {
        private OrganizationMemberFixture() {
        }

        public static OrganizationMember newOwnerMember(OrganizationID organizationID, UserID userID) {
            return OrganizationMember.newOwnerMember(organizationID, userID);
        }

        public static OrganizationMember newMember(OrganizationID organizationID, UserID userID, OrganizationMemberRole role) {
            return OrganizationMember.newMember(organizationID, userID, role);
        }
    }

    public static final class EventFixture {
        private EventFixture() {
        }

        public static Event newEvent(final OrganizationID aOrganizationId, final String categoryId) {
            return Event.newEvent(
                    aOrganizationId,
                    faker.lorem().sentence(3),
                    "test event description",
                    EventType.REMOTE,
                    null,
                    categoryId,
                    InstantUtils.now().plus(10, ChronoUnit.MINUTES),
                    InstantUtils.now().plus(10, ChronoUnit.DAYS)
            );
        }

        public static Event newEvent(final String aTitle, final OrganizationID aOrganizationId, final String categoryId) {
            return Event.newEvent(
                    aOrganizationId,
                    aTitle,
                    "test event description",
                    EventType.REMOTE,
                    null,
                    categoryId,
                    InstantUtils.now().plus(10, ChronoUnit.MINUTES),
                    InstantUtils.now().plus(10, ChronoUnit.DAYS)
            );
        }

        public static Event withStatus(final Event event, final EventStatus status) {
            return Event.with(
                    event.getId(),
                    event.getVersion(),
                    event.getOrganizationId(),
                    event.getTitle(),
                    event.getCategoryId(),
                    status,
                    event.getType(),
                    event.getAddress().orElse(null),
                    event.getImageUrl().orElse(null),
                    event.getCategoryId(),
                    event.getStartAt(),
                    event.getFinishAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt(),
                    event.getDeletedAt().orElse(null)
            );
        }
    }
}
