package com.kaua.events.platform.application.usecases.auth.token.create;

import com.kaua.events.platform.application.UseCaseTest;
import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
import com.kaua.events.platform.application.repositories.OAuthClientRepository;
import com.kaua.events.platform.domain.auth.OAuthClient;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.auth.code.AuthorizationCodeID;
import com.kaua.events.platform.domain.auth.token.AuthorizationToken;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenID;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.UserID;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.AdditionalAnswers.returnsFirstArg;

class CreateAuthorizationTokenUseCaseTest extends UseCaseTest {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private AuthorizationTokenRepository authorizationTokenRepository;

    @Mock
    private OAuthClientRepository oAuthClientRepository;

    @Mock
    private TokenGeneratorGateway tokenGeneratorGateway;

    @InjectMocks
    private DefaultCreateAuthorizationTokenUseCase useCase;

    @Test
    void givenAValidValuesWithCodeInput_whenCallCreateAuthorizationTokenUseCase_thenReturnTokens() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Assertions.assertEquals(aClientId, aInput.clientId());
        Assertions.assertEquals(aCode, aInput.code());
        Assertions.assertEquals(aCodeVerifier, aInput.codeVerifier());
        Assertions.assertEquals("authorization_code", aInput.grantType());

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.newAuthCode(
                        aClientId,
                        aSub,
                        "http://localhost:8080/callback",
                        "N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928",
                        "S256"
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationCodeRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAValidValuesWithCodeInputOnNonExistsOtherTokens_whenCallCreateAuthorizationTokenUseCase_thenReturnTokens() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.newAuthCode(
                        aClientId,
                        aSub,
                        "http://localhost:8080/callback",
                        "N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928",
                        "S256"
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationCodeRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidNonExistsCode_whenCallCreateAuthorizationTokenUseCase_thenThrowNotFoundException() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "AuthorizationCode with code code was not found";

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidUsedCodeInput_whenCallCreateAuthorizationCode_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "The authorization code has already been used";

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.with(
                        new AuthorizationCodeID(IdentifierUtils.generateNewULID()),
                        0L,
                        aCode,
                        aClientId,
                        new UserID(IdentifierUtils.generateNewULID()),
                        "http://localhost:8080/callback",
                        "N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928",
                        "S256",
                        true,
                        InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        InstantUtils.now()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidExpiredCodeInput_whenCallCreateAuthorizationCode_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "The authorization code has expired";

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.with(
                        new AuthorizationCodeID(IdentifierUtils.generateNewULID()),
                        0L,
                        aCode,
                        aClientId,
                        new UserID(IdentifierUtils.generateNewULID()),
                        "http://localhost:8080/callback",
                        "N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928",
                        "S256",
                        false,
                        InstantUtils.now().minus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        InstantUtils.now()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidNullInput_whenCallCreateAuthorizationTokenUseCase_thenThrowUseCaseInputCannotBeNullException() {
        final var aExpectedErrorMessage = "Input to CreateAuthorizationTokenUseCase cannot be null";

        final var aException = Assertions.assertThrows(UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.never()).authorizationCodeOfCode(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidNotMatchClientId_whenCallCreateAuthorizationTokenUseCase_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "The authorization code does not belong to the client";

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.newAuthCode(
                        "otherClientId",
                        new UserID(IdentifierUtils.generateNewULID()),
                        "http://localhost:8080/callback",
                        "N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928",
                        "S256"
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidNotMatchCodeVerifier_whenCallCreateAuthorizationTokenUseCase_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aCode = "code";
        final var aCodeVerifier = "codeVerifier";

        final var expectedErrorMessage = "The code verifier is invalid";

        final var aInput = new AuthorizationCodeGrantInput(
                aClientId,
                aCode,
                aCodeVerifier
        );

        Mockito.when(authorizationCodeRepository.authorizationCodeOfCode(aCode))
                .thenReturn(Optional.of(AuthorizationCode.newAuthCode(
                        aClientId,
                        new UserID(IdentifierUtils.generateNewULID()),
                        "http://localhost:8080/callback",
                        "otherCodeVerifier",
                        "S256"
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationCodeRepository, Mockito.times(1)).authorizationCodeOfCode(aCode);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAValidValuesWithRefreshInput_whenCallCreateAuthorizationTokenUseCase_thenReturnTokens() {
        final var aClientId = "clientId";
        final var aRefreshToken = "refreshToken";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aInput = new RefreshTokenGrantInput(
                aClientId,
                aRefreshToken
        );

        Mockito.when(authorizationTokenRepository.tokenOfJti(Mockito.any()))
                .thenReturn(Optional.of(AuthorizationToken.newAuthToken(
                        aRefreshToken,
                        AuthorizationTokenType.REFRESH_TOKEN,
                        InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        aClientId,
                        aSub.value().toString()
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(authorizationTokenRepository, Mockito.times(1)).tokenOfJti(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAValidValuesWithRefreshInputOnNonExistsOtherRefresh_whenCallCreateAuthorizationToken_thenReturnTokens() {
        final var aClientId = "clientId";
        final var aRefreshToken = "refreshToken";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aInput = new RefreshTokenGrantInput(
                aClientId,
                aRefreshToken
        );

        Assertions.assertEquals(aClientId, aInput.clientId());
        Assertions.assertEquals(aRefreshToken, aInput.refreshToken());
        Assertions.assertEquals("refresh_token", aInput.grantType());

        Mockito.when(authorizationTokenRepository.tokenOfJti(Mockito.any()))
                .thenReturn(Optional.of(AuthorizationToken.newAuthToken(
                        aRefreshToken,
                        AuthorizationTokenType.REFRESH_TOKEN,
                        InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        aClientId,
                        aSub.value().toString()
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aSub,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aSub.value().toString()
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(authorizationTokenRepository, Mockito.times(1)).tokenOfJti(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidExpiredRefreshToken_whenCallCreateAuthorizationTokenUseCase_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aRefreshToken = "refreshToken";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aExpectedErrorMessage = "The refresh token has expired";

        final var aInput = new RefreshTokenGrantInput(
                aClientId,
                aRefreshToken
        );

        Mockito.when(authorizationTokenRepository.tokenOfJti(Mockito.any()))
                .thenReturn(Optional.of(AuthorizationToken.newAuthToken(
                        aRefreshToken,
                        AuthorizationTokenType.REFRESH_TOKEN,
                        InstantUtils.now().minus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        aClientId,
                        aSub.value().toString()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationTokenRepository, Mockito.times(1)).tokenOfJti(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidRevokedRefreshToken_whenCallCreateAuthorizationTokenUseCase_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aRefreshToken = "refreshToken";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aExpectedErrorMessage = "The refresh token has been revoked";

        final var aInput = new RefreshTokenGrantInput(
                aClientId,
                aRefreshToken
        );

        Mockito.when(authorizationTokenRepository.tokenOfJti(Mockito.any()))
                .thenReturn(Optional.of(AuthorizationToken.with(
                        new AuthorizationTokenID(IdentifierUtils.generateNewULID()),
                        0L,
                        aRefreshToken,
                        AuthorizationTokenType.REFRESH_TOKEN,
                        InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        true,
                        aClientId,
                        aSub.value().toString()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationTokenRepository, Mockito.times(1)).tokenOfJti(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidClientIdAndNonMatchInRefreshToken_whenCallCreateAuthorizationTokenUseCase_thenThrowDomainException() {
        final var aClientId = "clientId";
        final var aRefreshToken = "refreshToken";
        final var aSub = new UserID(IdentifierUtils.generateNewULID());

        final var aExpectedErrorMessage = "The refresh token does not belong to the client";

        final var aInput = new RefreshTokenGrantInput(
                aClientId,
                aRefreshToken
        );

        Mockito.when(authorizationTokenRepository.tokenOfJti(Mockito.any()))
                .thenReturn(Optional.of(AuthorizationToken.newAuthToken(
                        aRefreshToken,
                        AuthorizationTokenType.REFRESH_TOKEN,
                        InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                        InstantUtils.now(),
                        "otherClientId",
                        aSub.value().toString()
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(aExpectedErrorMessage, aException.getMessage());

        Mockito.verify(authorizationTokenRepository, Mockito.times(1)).tokenOfJti(Mockito.any());
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).tokensOfSub(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(oAuthClientRepository, Mockito.never()).clientOfClientId(Mockito.any());
    }

    @Test
    void givenAnInvalidInputType_whenCallCreateAuthorizationTokenUseCase_thenThrowsDomainException() {
        final var aInput = new InputForDefaultTest("clientId", "clientSecret");

        Assertions.assertEquals("input_for_default_test", aInput.grantType());
        Assertions.assertEquals("clientId", aInput.clientId());
        Assertions.assertEquals("clientSecret", aInput.clientSecret());

        final var authorizationTokenUseCase = new DefaultCreateAuthorizationTokenUseCase(
                authorizationTokenRepository,
                authorizationCodeRepository,
                oAuthClientRepository,
                tokenGeneratorGateway
        );

        final var aException = Assertions.assertThrows(DomainException.class, () -> authorizationTokenUseCase.execute(aInput));
        Assertions.assertEquals("The grant type is not supported in this endpoint", aException.getMessage());
    }

    @Test
    void givenAValidValuesWithClientSecretInput_whenCallCreateAuthorizationTokenUseCase_thenReturnAccessTokens() {
        final var aClientId = "clientId";
        final var aClientSecret = "clientSecret";

        final var aInput = new ClientSecretGrantInput(
                aClientId,
                aClientSecret
        );

        Assertions.assertEquals(aClientId, aInput.clientId());
        Assertions.assertEquals(aClientSecret, aInput.clientSecret());
        Assertions.assertEquals("client_credentials", aInput.grantType());

        Mockito.when(oAuthClientRepository.clientOfClientId(aClientId))
                .thenReturn(Optional.of(new OAuthClient(
                        aClientId,
                        aClientSecret,
                        "http://localhost:8080/callback",
                        true,
                        5,
                        ChronoUnit.MINUTES.name(),
                        1,
                        ChronoUnit.DAYS.name(),
                        List.of("scope1", "scope2")
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aClientId,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aClientId
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aClientId,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aClientId
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(oAuthClientRepository, Mockito.times(1)).clientOfClientId(aClientId);
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void givenAValidValuesWithClientSecretInputAndNonExistsStored_whenCallCreateAuthorizationTokenUseCase_thenReturnAccessTokens() {
        final var aClientId = "clientId";
        final var aClientSecret = "clientSecret";

        final var aInput = new ClientSecretGrantInput(
                aClientId,
                aClientSecret
        );

        Assertions.assertEquals(aClientId, aInput.clientId());
        Assertions.assertEquals(aClientSecret, aInput.clientSecret());
        Assertions.assertEquals("client_credentials", aInput.grantType());

        Mockito.when(oAuthClientRepository.clientOfClientId(aClientId))
                .thenReturn(Optional.of(new OAuthClient(
                        aClientId,
                        aClientSecret,
                        "http://localhost:8080/callback",
                        true,
                        5,
                        ChronoUnit.MINUTES.name(),
                        1,
                        ChronoUnit.DAYS.name(),
                        List.of("scope1", "scope2")
                )));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aClientId,
                AuthorizationTokenType.ACCESS_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.ACCESS_TOKEN,
                aClientId,
                aClientId
        ));
        Mockito.when(tokenGeneratorGateway.generateToken(createTokenInput(
                aClientId,
                aClientId,
                AuthorizationTokenType.REFRESH_TOKEN
        ))).thenReturn(createToken(
                AuthorizationTokenType.REFRESH_TOKEN,
                aClientId,
                aClientId
        ));
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(authorizationTokenRepository.save(Mockito.any()))
                .thenAnswer(returnsFirstArg());

        final var aOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aInput));

        Assertions.assertNotNull(aOutput);
        Assertions.assertNotNull(aOutput.accessToken());
        Assertions.assertNotNull(aOutput.refreshToken());

        Mockito.verify(oAuthClientRepository, Mockito.times(1)).clientOfClientId(aClientId);
        Mockito.verify(tokenGeneratorGateway, Mockito.times(2)).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void givenAnNonExistsClientId_whenCallCreateAuthorizationTokenUseCase_thenThrowNotFoundException() {
        final var aClientId = "clientId";
        final var aClientSecret = "clientSecret";

        final var expectedErrorMessage = "Client not found";

        final var aInput = new ClientSecretGrantInput(
                aClientId,
                aClientSecret
        );

        Mockito.when(oAuthClientRepository.clientOfClientId(aClientId))
                .thenReturn(Optional.empty());

        final var aException = Assertions.assertThrows(NotFoundException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(oAuthClientRepository, Mockito.times(1)).clientOfClientId(aClientId);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenAValidClientIdButClientSecretThisDoesNotMatch_whenCallCreateAuthorizationTokenUseCase_thenThrowsDomainException() {
        final var aClientId = "clientId";
        final var aClientSecret = "clientSecret";

        final var expectedErrorMessage = "The client secret does not belong to the client";

        final var aInput = new ClientSecretGrantInput(
                aClientId,
                aClientSecret
        );

        Mockito.when(oAuthClientRepository.clientOfClientId(aClientId))
                .thenReturn(Optional.of(new OAuthClient(
                        aClientId,
                        "12345",
                        "http://localhost:8080/callback",
                        true,
                        5,
                        ChronoUnit.MINUTES.name(),
                        1,
                        ChronoUnit.DAYS.name(),
                        List.of("scope1", "scope2")
                )));

        final var aException = Assertions.assertThrows(DomainException.class,
                () -> this.useCase.execute(aInput));

        Assertions.assertEquals(expectedErrorMessage, aException.getMessage());

        Mockito.verify(oAuthClientRepository, Mockito.times(1)).clientOfClientId(aClientId);
        Mockito.verify(tokenGeneratorGateway, Mockito.never()).generateToken(Mockito.any());
        Mockito.verify(authorizationTokenRepository, Mockito.never()).save(Mockito.any());
    }

    private TokenGeneratorGateway.TokenInput createTokenInput(
            String aClientId,
            UserID aSub,
            AuthorizationTokenType aType
    ) {
        return new TokenGeneratorGateway.TokenInput(
                aClientId,
                aSub.value().toString(),
                aType
        );
    }

    private TokenGeneratorGateway.TokenInput createTokenInput(
            String aClientId,
            String aSub,
            AuthorizationTokenType aType
    ) {
        return new TokenGeneratorGateway.TokenInput(
                aClientId,
                aSub,
                aType
        );
    }

    private TokenGeneratorGateway.Token createToken(
            AuthorizationTokenType aType,
            String aClientId,
            String aSub
    ) {
        return new TokenGeneratorGateway.Token(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                aType,
                aClientId,
                aSub,
                InstantUtils.now().plus(5, ChronoUnit.MINUTES),
                InstantUtils.now()
        );
    }
}