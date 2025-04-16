package com.kaua.events.platform.infrastructure.oauth.token;

import com.kaua.events.platform.AbstractRepositoryTest;
import com.kaua.events.platform.domain.auth.token.AuthorizationToken;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.time.temporal.ChronoUnit;

class AuthorizationTokenJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(authorizationTokenRepository());
    }

    @Test
    void givenAValidNewAuthorizationToken_whenCallSave_thenAuthorizationTokenIsPersisted() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aTokenJti = "token-jti";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;
        final var aExpiresIn = InstantUtils.now().plus(5, ChronoUnit.MINUTES);
        final var aIssuedAt = InstantUtils.now();
        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());

        final var aAuthorizationToken = AuthorizationToken.newAuthToken(
                aTokenJti,
                aType,
                aExpiresIn,
                aIssuedAt,
                aClientId,
                aUserId.value().toString()
        );

        final var aActualAuthorizationToken = this.authorizationTokenRepository().save(aAuthorizationToken);

        Assertions.assertEquals(1, countAuthorizationTokens());

        Assertions.assertEquals(aAuthorizationToken.getId(), aActualAuthorizationToken.getId());
        Assertions.assertEquals(aAuthorizationToken.getVersion(), aActualAuthorizationToken.getVersion());
        Assertions.assertEquals(aAuthorizationToken.getTokenJTI(), aActualAuthorizationToken.getTokenJTI());
        Assertions.assertEquals(aAuthorizationToken.getType(), aActualAuthorizationToken.getType());
        Assertions.assertEquals(aAuthorizationToken.getExpiresIn(), aActualAuthorizationToken.getExpiresIn());
        Assertions.assertEquals(aAuthorizationToken.getIssuedAt(), aActualAuthorizationToken.getIssuedAt());
        Assertions.assertEquals(aAuthorizationToken.isRevoked(), aActualAuthorizationToken.isRevoked());
        Assertions.assertEquals(aAuthorizationToken.getClientId(), aActualAuthorizationToken.getClientId());
        Assertions.assertEquals(aAuthorizationToken.getUserId().get(), aActualAuthorizationToken.getUserId().get());
    }

    @Test
    void givenAValidUpdatedAuthorizationToken_whenCallSave_thenAuthorizationTokenIsUpdated() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aTokenJti = "token-jti";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;
        final var aExpiresIn = InstantUtils.now().plus(5, ChronoUnit.MINUTES);
        final var aIssuedAt = InstantUtils.now();
        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());

        final var aAuthorizationToken = AuthorizationToken.newAuthToken(
                "123",
                aType,
                aExpiresIn.plus(5, ChronoUnit.MINUTES),
                aIssuedAt.plus(10, ChronoUnit.MINUTES),
                aClientId,
                aUserId.value().toString()
        );

        this.authorizationTokenRepository().save(aAuthorizationToken);

        Assertions.assertEquals(1, countAuthorizationTokens());

        final var aUpdatedToken = aAuthorizationToken.update(
                aTokenJti,
                aExpiresIn,
                aIssuedAt
        );

        final var aActualUpdatedToken = this.authorizationTokenRepository().save(aUpdatedToken);

        Assertions.assertEquals(1, countAuthorizationTokens());

        Assertions.assertEquals(aUpdatedToken.getId(), aActualUpdatedToken.getId());
        Assertions.assertEquals(aUpdatedToken.getVersion(), aActualUpdatedToken.getVersion());
        Assertions.assertEquals(aUpdatedToken.getTokenJTI(), aActualUpdatedToken.getTokenJTI());
        Assertions.assertEquals(aUpdatedToken.getType(), aActualUpdatedToken.getType());
        Assertions.assertEquals(aUpdatedToken.getExpiresIn(), aActualUpdatedToken.getExpiresIn());
        Assertions.assertEquals(aUpdatedToken.getIssuedAt(), aActualUpdatedToken.getIssuedAt());
        Assertions.assertEquals(aUpdatedToken.isRevoked(), aActualUpdatedToken.isRevoked());
        Assertions.assertEquals(aUpdatedToken.getClientId(), aActualUpdatedToken.getClientId());
        Assertions.assertEquals(aUpdatedToken.getUserId().get(), aActualUpdatedToken.getUserId().get());
    }

    @Test
    void givenAnInvalidUpdatedAuthorizationTokenOnVersionsDoesNotMatch_whenCallSave_thenThrowsConflictException() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aTokenJti = "token-jti";
        final var aType = AuthorizationTokenType.ACCESS_TOKEN;
        final var aExpiresIn = InstantUtils.now().plus(5, ChronoUnit.MINUTES);
        final var aIssuedAt = InstantUtils.now();
        final var aClientId = "client-id";
        final var aUserId = new UserID(IdentifierUtils.generateNewULID());

        final var aExpectedErrorMessage = "Conflict on update of authorization token";

        final var aAuthorizationToken = AuthorizationToken.newAuthToken(
                "123",
                aType,
                aExpiresIn.plus(5, ChronoUnit.MINUTES),
                aIssuedAt.plus(10, ChronoUnit.MINUTES),
                aClientId,
                aUserId.value().toString()
        );

        this.authorizationTokenRepository().save(aAuthorizationToken);

        Assertions.assertEquals(1, countAuthorizationTokens());

        final var aUpdatedToken = aAuthorizationToken.update(
                aTokenJti,
                aExpiresIn,
                aIssuedAt
        );
        aUpdatedToken.incrementVersion();

        final var aException = Assertions.assertThrows(InternalErrorException.class,
                () -> this.authorizationTokenRepository().save(aUpdatedToken));

        Assertions.assertEquals(1, countAuthorizationTokens());
        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());
    }

    @Test
    void givenAValidSub_whenCallTokensOfSub_thenReturnsTokensForSub() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aAccessToken = AuthorizationToken.newAuthToken(
                IdentifierUtils.generateNewIdWithoutHyphen(),
                AuthorizationTokenType.ACCESS_TOKEN,
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now(),
                "client-id",
                "user-id"
        );
        final var aRefreshToken = AuthorizationToken.newAuthToken(
                IdentifierUtils.generateNewIdWithoutHyphen(),
                AuthorizationTokenType.REFRESH_TOKEN,
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now(),
                "client-id",
                "user-id"
        );

        this.authorizationTokenRepository().save(aAccessToken);
        this.authorizationTokenRepository().save(aRefreshToken);

        Assertions.assertEquals(2, countAuthorizationTokens());

        final var aActualTokens = this.authorizationTokenRepository().tokensOfSub("user-id");

        Assertions.assertEquals(2, aActualTokens.size());
        Assertions.assertTrue(aActualTokens.contains(aAccessToken));
        Assertions.assertTrue(aActualTokens.contains(aRefreshToken));
    }

    @Test
    void givenAnInvalidSub_whenCallTokensOfSub_thenReturnsEmptyList() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aActualTokens = this.authorizationTokenRepository().tokensOfSub("invalid-sub");

        Assertions.assertEquals(0, aActualTokens.size());
    }

    @Test
    void givenAValidJTI_whenCallTokenOfJti_thenReturnToken() {
        Assertions.assertEquals(0, countAuthorizationTokens());

        final var aAccessToken = AuthorizationToken.newAuthToken(
                IdentifierUtils.generateNewIdWithoutHyphen(),
                AuthorizationTokenType.ACCESS_TOKEN,
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now(),
                "client-id",
                "user-id"
        );

        this.authorizationTokenRepository().save(aAccessToken);

        Assertions.assertEquals(1, countAuthorizationTokens());

        final var aActualToken = this.authorizationTokenRepository().tokenOfJti(aAccessToken.getTokenJTI()).get();

        Assertions.assertEquals(aAccessToken.getId(), aActualToken.getId());
        Assertions.assertEquals(aAccessToken.getVersion(), aActualToken.getVersion());
        Assertions.assertEquals(aAccessToken.getTokenJTI(), aActualToken.getTokenJTI());
        Assertions.assertEquals(aAccessToken.getType(), aActualToken.getType());
        Assertions.assertEquals(aAccessToken.getExpiresIn(), aActualToken.getExpiresIn());
        Assertions.assertEquals(aAccessToken.getIssuedAt(), aActualToken.getIssuedAt());
        Assertions.assertEquals(aAccessToken.isRevoked(), aActualToken.isRevoked());
        Assertions.assertEquals(aAccessToken.getClientId(), aActualToken.getClientId());
        Assertions.assertEquals(aAccessToken.getUserId().get(), aActualToken.getUserId().get());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO authorization_tokens (id, jti, client_id, user_id, type, revoked, expires_in, issued_at, version) " +
                    "VALUES ('01JRP066XMA9GZZZZHAZZZZZYF', '123456787899', 'client-id', 'user-id', 'INVALID_TYPE', false, DATEADD('MINUTE', 5, NOW()), NOW(), 0)"
    })
    void givenAnStoredInvalidType_whenCallTokenOfJti_thenThrowsNotFoundException() {
        Assertions.assertEquals(1, countAuthorizationTokens());

        final var aExpectedErrorMessage = "Token type was not found";

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.authorizationTokenRepository().tokenOfJti("123456787899"));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());
    }
}
