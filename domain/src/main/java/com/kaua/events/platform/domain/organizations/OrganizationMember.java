package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;

public class OrganizationMember extends AggregateRoot<OrganizationMemberID> {

    private OrganizationID organizationId;
    private UserID userId;
    private OrganizationMemberRole memberRole;
    private Instant createdAt;
    private Instant updatedAt;

    private OrganizationMember(
            final OrganizationMemberID aOrganizationMemberId,
            final long aVersion,
            final OrganizationID aOrganizationId,
            final UserID aUserId,
            final OrganizationMemberRole aMemberRole,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        super(aOrganizationMemberId, aVersion);
        setOrganizationId(aOrganizationId);
        setUserId(aUserId);
        setMemberRole(aMemberRole);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
    }

    public static OrganizationMember newOwnerMember(
            final OrganizationID aOrganizationId,
            final UserID aUserId
    ) {
        final var aOrganizationMemberId = new OrganizationMemberID(IdentifierUtils.generateNewMonotonicULID());
        final var aNow = InstantUtils.now();

        return new OrganizationMember(
                aOrganizationMemberId,
                0L,
                aOrganizationId,
                aUserId,
                OrganizationMemberRole.OWNER,
                aNow,
                aNow
        );
    }

    public static OrganizationMember newMember(
            final OrganizationID aOrganizationId,
            final UserID aUserId,
            final OrganizationMemberRole aMemberRole
    ) {
        if (aMemberRole == OrganizationMemberRole.OWNER) {
            throw DomainException.with("To create a owner member use the specific method");
        }

        final var aOrganizationMemberId = new OrganizationMemberID(IdentifierUtils.generateNewMonotonicULID());
        final var aNow = InstantUtils.now();

        return new OrganizationMember(
                aOrganizationMemberId,
                0L,
                aOrganizationId,
                aUserId,
                aMemberRole,
                aNow,
                aNow
        );
    }

    public static OrganizationMember with(
            final OrganizationMemberID aOrganizationMemberId,
            final long aVersion,
            final OrganizationID aOrganizationId,
            final UserID aUserId,
            final OrganizationMemberRole aMemberRole,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        return new OrganizationMember(
                aOrganizationMemberId,
                aVersion,
                aOrganizationId,
                aUserId,
                aMemberRole,
                aCreatedAt,
                aUpdatedAt
        );
    }

    public OrganizationMember changeRole(final OrganizationMemberRole aRole) {
        this.setMemberRole(this.assertArgumentNotNull(aRole, "memberRole", "should not be null"));
        this.setUpdatedAt(InstantUtils.now());
        return this;
    }

    public OrganizationID getOrganizationId() {
        return organizationId;
    }

    public UserID getUserId() {
        return userId;
    }

    public OrganizationMemberRole getMemberRole() {
        return memberRole;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void setOrganizationId(final OrganizationID organizationId) {
        this.organizationId = this.assertArgumentNotNull(organizationId, "organizationId", "should not be null");
    }

    private void setUserId(final UserID userId) {
        this.userId = this.assertArgumentNotNull(userId, "userId", "should not be null");
    }

    private void setMemberRole(final OrganizationMemberRole memberRole) {
        this.memberRole = this.assertArgumentNotNull(memberRole, "memberRole", "should not be null");
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "OrganizationMember(" +
                "id=" + getId().value().toString() +
                ", version=" + getVersion() +
                ", organizationId=" + organizationId.value().toString() +
                ", userId=" + userId.value().toString() +
                ", memberRole=" + memberRole.name() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ')';
    }
}
