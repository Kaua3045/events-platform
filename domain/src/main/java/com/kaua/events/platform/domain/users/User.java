package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.person.Document;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Optional;

public class User extends AggregateRoot<UserID> {

    private Name name;
    private Email email;
    private Password password;
    private UserRole role;
    private Document document;
    private String phoneNumber;
    private Instant createdAt;
    private Instant updatedAt;

    private User(
            final UserID aUserID,
            final long aVersion,
            final Name aName,
            final Email aEmail,
            final Password aPassword,
            final UserRole aRole,
            final Document aDocument,
            final String aPhoneNumber,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        super(aUserID, aVersion);
        this.setName(aName);
        this.setEmail(aEmail);
        this.setPassword(aPassword);
        this.setRole(aRole);
        this.setDocument(aDocument);
        this.setPhoneNumber(aPhoneNumber);
        this.setCreatedAt(aCreatedAt);
        this.setUpdatedAt(aUpdatedAt);
    }

    public static User newUser(
            final Name aName,
            final Email aEmail,
            final Password aPassword,
            final UserRole aRole
    ) {
        final var aUserId = new UserID(ULID.random());
        final var aNow = InstantUtils.now();
        return new User(
                aUserId,
                0L,
                aName,
                aEmail,
                aPassword,
                aRole,
                null,
                null,
                aNow,
                aNow
        );
    }

    public static User with(
            final UserID aUserID,
            final long aVersion,
            final Name aName,
            final Email aEmail,
            final Password aPassword,
            final UserRole aRole,
            final Document aDocument,
            final String aPhoneNumber,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        return new User(
                aUserID,
                aVersion,
                aName,
                aEmail,
                aPassword,
                aRole,
                aDocument,
                aPhoneNumber,
                aCreatedAt,
                aUpdatedAt
        );
    }

    public User updateDocument(final Document aDocument) {
        return new User(
                getId(),
                getVersion(),
                getName(),
                getEmail(),
                getPassword(),
                getRole(),
                aDocument,
                getPhoneNumber().orElse(null),
                getCreatedAt(),
                InstantUtils.now()
        );
    }

    public User updatePhoneNumber(final String aPhoneNumber) {
        return new User(
                getId(),
                getVersion(),
                getName(),
                getEmail(),
                getPassword(),
                getRole(),
                getDocument().orElse(null),
                aPhoneNumber,
                getCreatedAt(),
                InstantUtils.now()
        );
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public Optional<Document> getDocument() {
        return Optional.ofNullable(document);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    private void setName(final Name name) {
        this.name = this.assertArgumentNotNull(name, "name", "should not be null");
    }

    private void setEmail(final Email email) {
        this.email = this.assertArgumentNotNull(email, "email", "should not be null");
    }

    private void setPassword(final Password password) {
        this.password = this.assertArgumentNotNull(password, "password", "should not be null");
    }

    private void setRole(final UserRole role) {
        this.role = this.assertArgumentNotNull(role, "role", "should not be null");
    }

    private void setDocument(final Document document) {
        this.document = document;
    }

    private void setPhoneNumber(final String phoneNumber) {
        if (phoneNumber != null) {
            this.assertArgumentPattern(
                    phoneNumber,
                    "^\\+[1-9]\\d{1,14}$",
                    "phoneNumber",
                    "must be in E.164 format"
            );
        }
        this.phoneNumber = phoneNumber;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
    }

    @Override
    public String toString() {
        return "User(" +
                "userId=" + getId().value().toString() +
                ", version=" + getVersion() +
                ", name=" + name.fullName() + ", email=" + email.value() +
                ", role=" + role.name() +
                ", document=" + getDocument().orElse(null) +
                ", phoneNumber=" + getPhoneNumber().orElse(null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ')';
    }
}
