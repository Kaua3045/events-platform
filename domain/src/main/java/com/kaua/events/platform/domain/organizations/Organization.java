package com.kaua.events.platform.domain.organizations;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Optional;

public class Organization extends AggregateRoot<OrganizationID> {

    private String name; // VALUE OBJECT
    private String description; // VALUE OBJECT
    private boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Organization(
            final OrganizationID aOrganizationID,
            final long aVersion,
            final String aName,
            final String aDescription,
            final boolean aIsDeleted,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        super(aOrganizationID, aVersion);
        setName(aName);
        setDescription(aDescription);
        setDeleted(aIsDeleted);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setDeletedAt(aDeletedAt);
    }

    public static Organization newOrganization(
            String aName,
            String aDescription
    ) {
        final var aOrganizationId = new OrganizationID(IdentifierUtils.generateNewMonotonicULID());
        final var aNow = InstantUtils.now();

        return new Organization(
                aOrganizationId,
                0L,
                aName,
                aDescription,
                false,
                aNow,
                aNow,
                null
        );
    }

    public static Organization with(
            OrganizationID aOrganizationId,
            long aVersion,
            String aName,
            String aDescription,
            boolean aIsDeleted,
            Instant aCreatedAt,
            Instant aUpdatedAt,
            Instant aDeletedAt
    ) {
        return new Organization(
                aOrganizationId,
                aVersion,
                aName,
                aDescription,
                aIsDeleted,
                aCreatedAt,
                aUpdatedAt,
                aDeletedAt
        );
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    private void setName(final String name) {
        this.assertArgumentNotEmpty(name, "name", "should not be null");
        this.assertArgumentMinLength(name, 2, "name", "must have at least 2 characters");
        this.assertArgumentMaxLength(name, 255, "name", "can only have a maximum of 255 characters");
        this.name = name;
    }

    private void setDescription(final String description) {
        this.assertArgumentMaxLength(name, 255, "name", "can only have a maximum of 255 characters");
        this.description = description;
    }

    private void setDeleted(final boolean deleted) {
        isDeleted = deleted;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
    }

    private void setDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "Organization(" +
                "id='" + getId().value().toString() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + getDescription().orElse(null) + '\'' +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + getDeletedAt().orElse(null) +
                ')';
    }
}
