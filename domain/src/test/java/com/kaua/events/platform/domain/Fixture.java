package com.kaua.events.platform.domain;

import com.kaua.events.platform.domain.eventmanagement.*;
import com.kaua.events.platform.domain.organizations.Organization;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.organizations.OrganizationMember;
import com.kaua.events.platform.domain.organizations.OrganizationMemberRole;
import com.kaua.events.platform.domain.ticket.Ticket;
import com.kaua.events.platform.domain.ticket.TicketStatus;
import com.kaua.events.platform.domain.ticket.TicketType;
import com.kaua.events.platform.domain.users.*;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

public final class Fixture {

    private static final Faker faker = new Faker();

    private Fixture() {
    }


    public static final class UserFixture {
        private UserFixture() {
        }

        public static User newUser() {
            final var aFirstName = faker.name().firstName().length() < 3 ? "TESSSSSS"
                    : faker.name().firstName();
            final var aLastName = faker.name().lastName().length() < 3 ? "TESSSSSSLAST"
                    : faker.name().lastName();
            return User.newUser(
                    new Name(aFirstName, aLastName),
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

        public static Event newEventWithAddress(final OrganizationID aOrganizationId, final String categoryId) {
            return Event.newEvent(
                    aOrganizationId,
                    faker.lorem().sentence(3),
                    "test event description",
                    EventType.IN_PERSON,
                    Address.newAddress(
                            "rua x",
                            "0000",
                            null,
                            "bairro tst",
                            "city ts",
                            "ts",
                            "00000000",
                            "non"
                    ),
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

        public static Event withType(final Event event, final EventType type) {
            return Event.with(
                    event.getId(),
                    event.getVersion(),
                    event.getOrganizationId(),
                    event.getTitle(),
                    event.getCategoryId(),
                    event.getStatus(),
                    type,
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

    public static final class TicketFixture {
        private TicketFixture() {
        }

        public static Ticket newTicket() {
            return Ticket.newTicket(
                    new EventID(ULID.random()),
                    "test-ticket",
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    10,
                    TicketType.STANDARD,
                    TicketStatus.AVAILABLE
            );
        }

        public static Ticket newTicket(final EventID aEventId) {
            return Ticket.newTicket(
                    aEventId,
                    "test-ticket",
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    10,
                    TicketType.STANDARD,
                    TicketStatus.AVAILABLE
            );
        }

        public static Ticket withStatus(final EventID aEventId, final TicketStatus aStatus) {
            return Ticket.newTicket(
                    aEventId,
                    "test-ticket",
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    10,
                    TicketType.STANDARD,
                    aStatus
            );
        }

        public static Ticket withType(final EventID aEventId, final TicketType aType) {
            return Ticket.newTicket(
                    aEventId,
                    "test-ticket",
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    10,
                    aType,
                    TicketStatus.AVAILABLE
            );
        }

        public static Ticket withName(final EventID aEventId, final String aName) {
            return Ticket.newTicket(
                    aEventId,
                    aName,
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    10,
                    TicketType.STANDARD,
                    TicketStatus.AVAILABLE
            );
        }

        public static Ticket withQuantity(final EventID aEventId, final int aQuantity) {
            return Ticket.newTicket(
                    aEventId,
                    "test-ticket",
                    "test-ticket-description",
                    new BigDecimal("10.00"),
                    aQuantity,
                    TicketType.STANDARD,
                    TicketStatus.AVAILABLE
            );
        }
    }
}
