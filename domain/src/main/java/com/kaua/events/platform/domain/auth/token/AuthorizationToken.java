package com.kaua.events.platform.domain.auth.token;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Optional;

public class AuthorizationToken extends AggregateRoot<AuthorizationTokenID> {

    private String tokenJTI;
    private AuthorizationTokenType type;
    private Instant expiresIn;
    private Instant issuedAt;
    private boolean revoked;
    private String clientId;
    private String userId;

    private AuthorizationToken(
            final AuthorizationTokenID aAuthorizationTokenID,
            final long aVersion,
            final String aTokenJTI,
            final AuthorizationTokenType aType,
            final Instant aExpiresIn,
            final Instant aIssuedAt,
            final boolean aRevoked,
            final String aClientId,
            final String aUserId
    ) {
        super(aAuthorizationTokenID, aVersion);
        setTokenJTI(aTokenJTI);
        setType(aType);
        setExpiresIn(aExpiresIn);
        setIssuedAt(aIssuedAt);
        setRevoked(aRevoked);
        setClientId(aClientId);
        setUserId(aUserId);
    }

    public static AuthorizationToken newAuthToken(
            final String aTokenJTI,
            final AuthorizationTokenType aType,
            final Instant aExpiresIn,
            final Instant aIssuedAt,
            final String aClientId,
            final String aUserId
    ) {
        final var aId = new AuthorizationTokenID(ULID.random());

        return new AuthorizationToken(
                aId,
                0L,
                aTokenJTI,
                aType,
                aExpiresIn,
                aIssuedAt,
                false,
                aClientId,
                aUserId
        );
    }

    public static AuthorizationToken with(
            final AuthorizationTokenID anId,
            final long aVersion,
            final String aTokenJTI,
            final AuthorizationTokenType aType,
            final Instant aExpiresIn,
            final Instant aIssuedAt,
            final boolean aRevoked,
            final String aClientId,
            final String aUserId
    ) {
        return new AuthorizationToken(
                anId,
                aVersion,
                aTokenJTI,
                aType,
                aExpiresIn,
                aIssuedAt,
                aRevoked,
                aClientId,
                aUserId
        );
    }

    public boolean isExpired() {
        return this.expiresIn.isBefore(Instant.now());
    }

    public String getTokenJTI() {
        return tokenJTI;
    }

    public AuthorizationTokenType getType() {
        return type;
    }

    public Instant getExpiresIn() {
        return expiresIn;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public String getClientId() {
        return clientId;
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

    private void setTokenJTI(final String tokenJTI) {
        this.tokenJTI = this.assertArgumentNotEmpty(tokenJTI, "tokenJTI", "should not be empty");
    }

    private void setType(final AuthorizationTokenType type) {
        this.type = this.assertArgumentNotNull(type, "type", "should not be null");
    }

    private void setExpiresIn(final Instant expiresIn) {
        this.expiresIn = this.assertArgumentNotNull(expiresIn, "expiresIn", "should not be null");
    }

    private void setIssuedAt(final Instant issuedAt) {
        this.issuedAt = this.assertArgumentNotNull(issuedAt, "issuedAt", "should not be null");
    }

    private void setRevoked(final boolean revoked) {
        this.revoked = revoked;
    }

    private void setClientId(final String clientId) {
        this.clientId = this.assertArgumentNotEmpty(clientId, "clientId", "should not be empty");
    }

    private void setUserId(final String userId) {
        this.userId = userId;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "AuthorizationToken(" +
                "tokenJTI='" + tokenJTI + '\'' +
                ", type=" + type.name() +
                ", expiresIn=" + expiresIn +
                ", issuedAt=" + issuedAt +
                ", revoked=" + revoked +
                ", clientId=" + clientId +
                ", userId='" + userId + '\'' +
                ')';
    }
}
