package com.kaua.events.platform.domain.auth.code;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;

public class AuthorizationCode extends AggregateRoot<AuthorizationCodeID> {

    private String code;
    private String clientId;
    private UserID userId;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private boolean isUsed;
    private Instant expirationDate;
    private Instant createdAt;
    private Instant updatedAt;

    private AuthorizationCode(
            final AuthorizationCodeID aAuthorizationCodeID,
            long aVersion,
            final String aCode,
            final String aClientId,
            final UserID aUserId,
            final String aRedirectUri,
            final String aCodeChallenge,
            final String aCodeChallengeMethod,
            final boolean aIsUsed,
            final Instant aExpirationDate,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        super(aAuthorizationCodeID, aVersion);
        setCode(aCode);
        setClientId(aClientId);
        setUserId(aUserId);
        setRedirectUri(aRedirectUri);
        setCodeChallenge(aCodeChallenge);
        setCodeChallengeMethod(aCodeChallengeMethod);
        setUsed(aIsUsed);
        setExpirationDate(aExpirationDate);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
    }

    public static AuthorizationCode newAuthCode(
            final String aClientId,
            final UserID aUserId,
            final String aRedirectUri,
            final String aCodeChallenge,
            final String aCodeChallengeMethod
    ) {
        final var aAuthorizationCodeID = new AuthorizationCodeID(ULID.random());
        final var aCode = ULID.random().toString(); // TODO in future change to a more secure code generation
        final var aExpirationDate = InstantUtils.now().plusSeconds(60 * 5); // 5 minutes
        final var aNow = InstantUtils.now();

        return new AuthorizationCode(
                aAuthorizationCodeID,
                0L,
                aCode,
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod,
                false,
                aExpirationDate,
                aNow,
                aNow
        );
    }

    public static AuthorizationCode with(
            final AuthorizationCodeID aAuthorizationCodeID,
            final long aVersion,
            final String aCode,
            final String aClientId,
            final UserID aUserId,
            final String aRedirectUri,
            final String aCodeChallenge,
            final String aCodeChallengeMethod,
            final boolean aIsUsed,
            final Instant aExpirationDate,
            final Instant aCreatedAt,
            final Instant aUpdatedAt
    ) {
        return new AuthorizationCode(
                aAuthorizationCodeID,
                aVersion,
                aCode,
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod,
                aIsUsed,
                aExpirationDate,
                aCreatedAt,
                aUpdatedAt
        );
    }

    public void markAsUsed() {
        this.setUsed(true);
        this.setUpdatedAt(InstantUtils.now());
    }

    public boolean isExpired() {
        return InstantUtils.now().isAfter(this.expirationDate);
    }

    public String getCode() {
        return code;
    }

    public String getClientId() {
        return clientId;
    }

    public UserID getUserId() {
        return userId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void setCode(final String code) {
        this.code = this.assertArgumentNotEmpty(code, "code", "should not be empty");
    }

    private void setClientId(final String clientId) {
        this.clientId = this.assertArgumentNotEmpty(clientId, "clientId", "should not be empty");
    }

    private void setUserId(final UserID userId) {
        this.userId = this.assertArgumentNotNull(userId, "userId", "should not be null");
    }

    private void setRedirectUri(final String redirectUri) {
        this.redirectUri = this.assertArgumentNotEmpty(redirectUri, "redirectUri", "should not be empty");
    }

    private void setCodeChallenge(final String codeChallenge) {
        this.codeChallenge = this.assertArgumentNotEmpty(codeChallenge, "codeChallenge", "should not be empty");
    }

    private void setCodeChallengeMethod(final String codeChallengeMethod) {
        this.codeChallengeMethod = this.assertArgumentNotEmpty(codeChallengeMethod, "codeChallengeMethod", "should not be empty");
    }

    private void setUsed(final boolean used) {
        this.isUsed = used;
    }

    private void setExpirationDate(final Instant expirationDate) {
        this.expirationDate = this.assertArgumentNotNull(expirationDate, "expirationDate", "should not be null");
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "AuthorizationCode(" +
                "id='" + getId().value().toString() + '\'' +
                ", code='" + code + '\'' +
                ", version=" + getVersion() + '\'' +
                ", clientId='" + clientId + '\'' +
                ", userId='" + userId.value().toString() +
                ", redirectUri='" + redirectUri + '\'' +
                ", codeChallenge='" + codeChallenge + '\'' +
                ", codeChallengeMethod='" + codeChallengeMethod + '\'' +
                ", isUsed=" + isUsed +
                ", expirationDate=" + expirationDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ')';
    }
}
