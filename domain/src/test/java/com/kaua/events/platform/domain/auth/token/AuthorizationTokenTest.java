package com.kaua.events.platform.domain.auth.token;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class AuthorizationTokenTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewAuthToken_thenReturnAuthorizationToken() {
        final var aTokenJTI = "aTokenJti";
        final var aType = AuthorizationTokenType.from("ACCESS_TOKEN").get();
        final var aExpiresIn = Instant.now().plusSeconds(3600);
        final var aIssuedAt = Instant.now();
        final var aClientId = "aClientId";
        final var aUserId = "aUserId";

        final var aToken = AuthorizationToken.newAuthToken(
                aTokenJTI,
                aType,
                aExpiresIn,
                aIssuedAt,
                aClientId,
                aUserId
        );

        Assertions.assertNotNull(aToken);
        Assertions.assertNotNull(aToken.getId());
        Assertions.assertEquals(aTokenJTI, aToken.getTokenJTI());
        Assertions.assertEquals(aType, aToken.getType());
        Assertions.assertEquals(aExpiresIn, aToken.getExpiresIn());
        Assertions.assertEquals(aIssuedAt, aToken.getIssuedAt());
        Assertions.assertFalse(aToken.isRevoked());
        Assertions.assertEquals(aClientId, aToken.getClientId());
        Assertions.assertEquals(aUserId, aToken.getUserId().get());
        Assertions.assertFalse(aToken.isExpired());
        Assertions.assertDoesNotThrow(() -> aToken.validate(NotificationHandler.create()));
    }

    @Test
    void givenAValidValues_whenCallWith_thenReturnAuthorizationToken() {
        final var aAuthorizationTokenId = new AuthorizationTokenID(IdentifierUtils.generateNewULID());
        final var aVersion = 0L;
        final var aTokenJTI = "aTokenJti";
        final var aType = AuthorizationTokenType.from("REFRESH_TOKEN").get();
        final var aExpiresIn = Instant.now().plusSeconds(3600);
        final var aIssuedAt = Instant.now();
        final var aRevoked = false;
        final var aClientId = "aClientId";
        final var aUserId = "aUserId";

        final var aToken = AuthorizationToken.with(
                aAuthorizationTokenId,
                aVersion,
                aTokenJTI,
                aType,
                aExpiresIn,
                aIssuedAt,
                aRevoked,
                aClientId,
                aUserId
        );

        Assertions.assertNotNull(aToken);
        Assertions.assertEquals(aAuthorizationTokenId, aToken.getId());
        Assertions.assertEquals(aVersion, aToken.getVersion());
        Assertions.assertEquals(aTokenJTI, aToken.getTokenJTI());
        Assertions.assertEquals(aType, aToken.getType());
        Assertions.assertEquals(aExpiresIn, aToken.getExpiresIn());
        Assertions.assertEquals(aIssuedAt, aToken.getIssuedAt());
        Assertions.assertEquals(aRevoked, aToken.isRevoked());
        Assertions.assertEquals(aClientId, aToken.getClientId());
        Assertions.assertEquals(aUserId, aToken.getUserId().get());
    }

    @Test
    void testCallToStringAuthorizationToken() {
        final var aAuthorizationTokenId = new AuthorizationTokenID(IdentifierUtils.generateNewULID());
        final var aVersion = 0L;
        final var aTokenJTI = "aTokenJti";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;
        final var aExpiresIn = Instant.now().plusSeconds(3600);
        final var aIssuedAt = Instant.now();
        final var aRevoked = false;
        final var aClientId = "aClientId";
        final var aUserId = "aUserId";

        final var aToken = AuthorizationToken.with(
                aAuthorizationTokenId,
                aVersion,
                aTokenJTI,
                aType,
                aExpiresIn,
                aIssuedAt,
                aRevoked,
                aClientId,
                aUserId
        );

        final var aToString = aToken.toString();

        Assertions.assertNotNull(aToString);
        Assertions.assertTrue(aToString.contains("AuthorizationToken("));
        Assertions.assertTrue(aToString.contains("id='" + aAuthorizationTokenId.value().toString()));
        Assertions.assertTrue(aToString.contains("version=" + aVersion));
        Assertions.assertTrue(aToString.contains("tokenJTI=" + aTokenJTI));
        Assertions.assertTrue(aToString.contains("type=" + aType.name()));
        Assertions.assertTrue(aToString.contains("expiresIn=" + aExpiresIn));
        Assertions.assertTrue(aToString.contains("issuedAt=" + aIssuedAt));
        Assertions.assertTrue(aToString.contains("revoked=" + aRevoked));
        Assertions.assertTrue(aToString.contains("clientId=" + aClientId));
        Assertions.assertTrue(aToString.contains("userId='" + aUserId));
    }

    @Test
    void givenAValidValues_whenCallUpdate_thenReturnUpdatedAuthorizationToken() {
        final var aNewTokenJti = "newTokenJti";
        final var aNewExpiresIn = Instant.now().plus(10, ChronoUnit.MINUTES);
        final var aNewIssuedAt = Instant.now();

        final var aOriginalToken = AuthorizationToken.newAuthToken(
                "originalTokenJti",
                AuthorizationTokenType.ACCESS_TOKEN,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now(),
                "clientId",
                "userId"
        );

        final var aUpdatedToken = aOriginalToken.update(
                aNewTokenJti,
                aNewExpiresIn,
                aNewIssuedAt
        );

        Assertions.assertNotNull(aUpdatedToken);
        Assertions.assertEquals(aOriginalToken.getId(), aUpdatedToken.getId());
        Assertions.assertEquals(aOriginalToken.getVersion(), aUpdatedToken.getVersion());
        Assertions.assertEquals(aNewTokenJti, aUpdatedToken.getTokenJTI());
        Assertions.assertEquals(aOriginalToken.getType(), aUpdatedToken.getType());
        Assertions.assertEquals(aNewExpiresIn, aUpdatedToken.getExpiresIn());
        Assertions.assertEquals(aNewIssuedAt, aUpdatedToken.getIssuedAt());
        Assertions.assertFalse(aUpdatedToken.isRevoked());
        Assertions.assertEquals(aOriginalToken.getClientId(), aUpdatedToken.getClientId());
        Assertions.assertEquals(aOriginalToken.getUserId().get(), aUpdatedToken.getUserId().get());
        Assertions.assertFalse(aUpdatedToken.isExpired());
        Assertions.assertDoesNotThrow(() -> aUpdatedToken.validate(NotificationHandler.create()));
    }
}
