package com.kaua.events.platform.infrastructure.oauth.code;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthorizationCodeJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(authorizationCodeRepository());
    }

    @Test
    void givenAValidNewAuthorizationCode_whenCallSave_thenAuthorizationCodeIsPersisted() {
        Assertions.assertEquals(0, countAuthorizationCodes());

        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "code-challenge";
        final var aCodeChallengeMethod = "S256";

        final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod
        );

        final var aActualAuthorizationCode = this.authorizationCodeRepository().save(aAuthorizationCode);

        Assertions.assertEquals(1, countAuthorizationCodes());
        Assertions.assertEquals(aAuthorizationCode.getId(), aActualAuthorizationCode.getId());
        Assertions.assertEquals(aAuthorizationCode.getVersion(), aActualAuthorizationCode.getVersion());
        Assertions.assertEquals(aAuthorizationCode.getCode(), aActualAuthorizationCode.getCode());
        Assertions.assertEquals(aAuthorizationCode.getClientId(), aActualAuthorizationCode.getClientId());
        Assertions.assertEquals(aAuthorizationCode.getUserId(), aActualAuthorizationCode.getUserId());
        Assertions.assertEquals(aAuthorizationCode.getRedirectUri(), aActualAuthorizationCode.getRedirectUri());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallenge(), aActualAuthorizationCode.getCodeChallenge());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallengeMethod(), aActualAuthorizationCode.getCodeChallengeMethod());
        Assertions.assertEquals(aAuthorizationCode.isUsed(), aActualAuthorizationCode.isUsed());
        Assertions.assertEquals(aAuthorizationCode.getExpirationDate(), aActualAuthorizationCode.getExpirationDate());
        Assertions.assertEquals(aAuthorizationCode.getCreatedAt(), aActualAuthorizationCode.getCreatedAt());
        Assertions.assertEquals(aAuthorizationCode.getUpdatedAt(), aActualAuthorizationCode.getUpdatedAt());
    }

    @Test
    void givenAValidUpdatedAuthorizationCode_whenCallSave_thenAuthorizationCodeIsUpdated() {
        Assertions.assertEquals(0, countAuthorizationCodes());

        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "code-challenge";
        final var aCodeChallengeMethod = "S256";

        final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod
        );

        this.authorizationCodeRepository().save(aAuthorizationCode);

        Assertions.assertEquals(1, countAuthorizationCodes());

        aAuthorizationCode.markAsUsed();

        this.authorizationCodeRepository().save(aAuthorizationCode);

        Assertions.assertEquals(1, countAuthorizationCodes());

        final var aActualAuthorizationCode = this.authorizationCodeRepository().authorizationCodeOfCode(aAuthorizationCode.getCode()).get();

        Assertions.assertEquals(aAuthorizationCode.getId(), aActualAuthorizationCode.getId());
        Assertions.assertEquals(aAuthorizationCode.getVersion(), aActualAuthorizationCode.getVersion());
        Assertions.assertEquals(aAuthorizationCode.getCode(), aActualAuthorizationCode.getCode());
        Assertions.assertEquals(aAuthorizationCode.getClientId(), aActualAuthorizationCode.getClientId());
        Assertions.assertEquals(aAuthorizationCode.getUserId(), aActualAuthorizationCode.getUserId());
        Assertions.assertEquals(aAuthorizationCode.getRedirectUri(), aActualAuthorizationCode.getRedirectUri());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallenge(), aActualAuthorizationCode.getCodeChallenge());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallengeMethod(), aActualAuthorizationCode.getCodeChallengeMethod());
        Assertions.assertEquals(aAuthorizationCode.isUsed(), aActualAuthorizationCode.isUsed());
        Assertions.assertEquals(aAuthorizationCode.getExpirationDate(), aActualAuthorizationCode.getExpirationDate());
        Assertions.assertEquals(aAuthorizationCode.getCreatedAt(), aActualAuthorizationCode.getCreatedAt());
        Assertions.assertEquals(aAuthorizationCode.getUpdatedAt(), aActualAuthorizationCode.getUpdatedAt());
    }

    @Test
    void givenAnInvalidUpdatedAuthorizationCodeOnVersionsDoesNotMatch_whenCallSave_thenThrowsConflictException() {
        Assertions.assertEquals(0, countAuthorizationCodes());

        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "code-challenge";
        final var aCodeChallengeMethod = "S256";

        final var aExpectedErrorMessage = "Conflict on update of authorization code";

        final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod
        );

        this.authorizationCodeRepository().save(aAuthorizationCode);

        Assertions.assertEquals(1, countAuthorizationCodes());

        aAuthorizationCode.markAsUsed();
        aAuthorizationCode.incrementVersion();

        final var aException = Assertions.assertThrows(InternalErrorException.class, () -> {
            this.authorizationCodeRepository().save(aAuthorizationCode);
        });

        Assertions.assertEquals(1, countAuthorizationCodes());
        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidCode_whenCallAuthorizationCodeOfCode_thenReturnAuthorizationCode() {
        Assertions.assertEquals(0, countAuthorizationCodes());

        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "code-challenge";
        final var aCodeChallengeMethod = "S256";

        final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod
        );

        this.authorizationCodeRepository().save(aAuthorizationCode);

        Assertions.assertEquals(1, countAuthorizationCodes());

        final var aActualAuthorizationCode = this.authorizationCodeRepository().authorizationCodeOfCode(aAuthorizationCode.getCode()).get();

        Assertions.assertEquals(aAuthorizationCode.getId(), aActualAuthorizationCode.getId());
        Assertions.assertEquals(aAuthorizationCode.getVersion(), aActualAuthorizationCode.getVersion());
        Assertions.assertEquals(aAuthorizationCode.getCode(), aActualAuthorizationCode.getCode());
        Assertions.assertEquals(aAuthorizationCode.getClientId(), aActualAuthorizationCode.getClientId());
        Assertions.assertEquals(aAuthorizationCode.getUserId(), aActualAuthorizationCode.getUserId());
        Assertions.assertEquals(aAuthorizationCode.getRedirectUri(), aActualAuthorizationCode.getRedirectUri());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallenge(), aActualAuthorizationCode.getCodeChallenge());
        Assertions.assertEquals(aAuthorizationCode.getCodeChallengeMethod(), aActualAuthorizationCode.getCodeChallengeMethod());
        Assertions.assertEquals(aAuthorizationCode.isUsed(), aActualAuthorizationCode.isUsed());
        Assertions.assertEquals(aAuthorizationCode.getExpirationDate(), aActualAuthorizationCode.getExpirationDate());
        Assertions.assertEquals(aAuthorizationCode.getCreatedAt(), aActualAuthorizationCode.getCreatedAt());
        Assertions.assertEquals(aAuthorizationCode.getUpdatedAt(), aActualAuthorizationCode.getUpdatedAt());
    }

    @Test
    void givenAnInvalidNonExistsCode_whenCallAuthorizationCodeOfCode_thenReturnOptionalEmpty() {
        Assertions.assertEquals(0, countAuthorizationCodes());

        final var aCode = "invalid-code";

        final var aActualAuthorizationCode = this.authorizationCodeRepository().authorizationCodeOfCode(aCode);

        Assertions.assertTrue(aActualAuthorizationCode.isEmpty());
    }
}
