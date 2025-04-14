package com.kaua.events.platform.domain.auth.code;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthorizationCodeTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewAuthCode_thenReturnAuthorizationCode() {
        final var aClientId = "clientId";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";

        final var aAuthorizationCode = AuthorizationCode.newAuthCode(
                aClientId,
                aUserId,
                aRedirectUri,
                aCodeChallenge,
                aCodeChallengeMethod
        );

        Assertions.assertNotNull(aAuthorizationCode);
        Assertions.assertNotNull(aAuthorizationCode.getId());
        Assertions.assertNotNull(aAuthorizationCode.getCode());
        Assertions.assertEquals(aClientId, aAuthorizationCode.getClientId());
        Assertions.assertEquals(aUserId, aAuthorizationCode.getUserId());
        Assertions.assertEquals(aRedirectUri, aAuthorizationCode.getRedirectUri());
        Assertions.assertEquals(aCodeChallenge, aAuthorizationCode.getCodeChallenge());
        Assertions.assertEquals(aCodeChallengeMethod, aAuthorizationCode.getCodeChallengeMethod());
        Assertions.assertFalse(aAuthorizationCode.isUsed());
        Assertions.assertFalse(aAuthorizationCode.isExpired());
        Assertions.assertNotNull(aAuthorizationCode.getExpirationDate());
        Assertions.assertNotNull(aAuthorizationCode.getCreatedAt());
        Assertions.assertNotNull(aAuthorizationCode.getUpdatedAt());
    }

    @Test
    void givenAValidValues_whenCallWith_thenReturnAuthorizationCode() {
        final var aAuthorizationCodeId = new AuthorizationCodeID(IdentifierUtils.generateNewULID());
        final var aVersion = 0L;
        final var aCode = "code";
        final var aClientId = "clientId";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aIsUsed = false;
        final var aExpirationDate = InstantUtils.now().plusSeconds(3600);
        final var aCreatedAt = InstantUtils.now();
        final var aUpdatedAt = InstantUtils.now();

        final var aAuthorizationCode = AuthorizationCode.with(
                aAuthorizationCodeId,
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

        Assertions.assertNotNull(aAuthorizationCode);
        Assertions.assertEquals(aAuthorizationCodeId, aAuthorizationCode.getId());
        Assertions.assertEquals(aVersion, aAuthorizationCode.getVersion());
        Assertions.assertEquals(aCode, aAuthorizationCode.getCode());
        Assertions.assertEquals(aClientId, aAuthorizationCode.getClientId());
        Assertions.assertEquals(aUserId, aAuthorizationCode.getUserId());
        Assertions.assertEquals(aRedirectUri, aAuthorizationCode.getRedirectUri());
        Assertions.assertEquals(aCodeChallenge, aAuthorizationCode.getCodeChallenge());
        Assertions.assertEquals(aCodeChallengeMethod, aAuthorizationCode.getCodeChallengeMethod());
        Assertions.assertEquals(aIsUsed, aAuthorizationCode.isUsed());
        Assertions.assertEquals(aExpirationDate, aAuthorizationCode.getExpirationDate());
        Assertions.assertEquals(aCreatedAt, aAuthorizationCode.getCreatedAt());
        Assertions.assertEquals(aUpdatedAt, aAuthorizationCode.getUpdatedAt());
        Assertions.assertDoesNotThrow(() -> aAuthorizationCode.validate(NotificationHandler.create()));
    }

    @Test
    void testCallToStringInAuthorizationCode() {
        final var aAuthorizationCodeId = new AuthorizationCodeID(IdentifierUtils.generateNewULID());
        final var aVersion = 0L;
        final var aCode = "code";
        final var aClientId = "clientId";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aIsUsed = false;
        final var aExpirationDate = InstantUtils.now().plusSeconds(3600);
        final var aCreatedAt = InstantUtils.now();
        final var aUpdatedAt = InstantUtils.now();

        final var aAuthorizationCode = AuthorizationCode.with(
                aAuthorizationCodeId,
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

        final var aAuthorizationCodeToString = aAuthorizationCode.toString();

        Assertions.assertNotNull(aAuthorizationCode);
        Assertions.assertNotNull(aAuthorizationCodeToString);
        Assertions.assertTrue(aAuthorizationCodeToString.contains("AuthorizationCode"));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("id='" + aAuthorizationCodeId.value().toString()));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("version=" + aVersion));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("code='" + aCode));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("clientId='" + aClientId));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("userId='" + aUserId.value().toString()));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("redirectUri='" + aRedirectUri));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("codeChallenge='" + aCodeChallenge));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("codeChallengeMethod='" + aCodeChallengeMethod));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("isUsed=" + aIsUsed));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("expirationDate=" + aExpirationDate));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("createdAt=" + aCreatedAt));
        Assertions.assertTrue(aAuthorizationCodeToString.contains("updatedAt=" + aUpdatedAt));
    }

    @Test
    void givenAValidAuthorizationCode_whenCallMarkAsUsed_thenMarkAsUsed() {
        final var aAuthorizationCodeId = new AuthorizationCodeID(IdentifierUtils.generateNewULID());
        final var aVersion = 0L;
        final var aCode = "code";
        final var aClientId = "clientId";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());
        final var aRedirectUri = "http://localhost:8080/callback";
        final var aCodeChallenge = "codeChallenge";
        final var aCodeChallengeMethod = "S256";
        final var aIsUsed = false;
        final var aExpirationDate = InstantUtils.now().plusSeconds(3600);
        final var aCreatedAt = InstantUtils.now();
        final var aUpdatedAt = InstantUtils.now();

        final var aAuthorizationCode = AuthorizationCode.with(
                aAuthorizationCodeId,
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

        Assertions.assertFalse(aAuthorizationCode.isUsed());

        aAuthorizationCode.markAsUsed();

        Assertions.assertTrue(aAuthorizationCode.isUsed());
    }
}
