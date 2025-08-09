package com.kaua.events.platform.application.usecases.auth.token.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
import com.kaua.events.platform.application.repositories.OAuthClientRepository;
import com.kaua.events.platform.application.wrapper.ObservationContext;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.auth.code.AuthorizationCode;
import com.kaua.events.platform.domain.auth.token.AuthorizationToken;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.utils.PKCEUtils;

import java.util.Objects;

public class DefaultCreateAuthorizationTokenUseCase extends CreateAuthorizationTokenUseCase {

    private final AuthorizationTokenRepository authorizationTokenRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final OAuthClientRepository oAuthClientRepository;
    private final TokenGeneratorGateway tokenGeneratorGateway;
    private final TracerWrapper tracerWrapper;

    public DefaultCreateAuthorizationTokenUseCase(
            final AuthorizationTokenRepository authorizationTokenRepository,
            final AuthorizationCodeRepository authorizationCodeRepository,
            final OAuthClientRepository oAuthClientRepository,
            final TokenGeneratorGateway tokenGeneratorGateway,
            final TracerWrapper tracerWrapper
    ) {
        this.authorizationTokenRepository = Objects.requireNonNull(authorizationTokenRepository);
        this.authorizationCodeRepository = Objects.requireNonNull(authorizationCodeRepository);
        this.oAuthClientRepository = Objects.requireNonNull(oAuthClientRepository);
        this.tokenGeneratorGateway = Objects.requireNonNull(tokenGeneratorGateway);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreateAuthorizationTokenOutput execute(final CreateAuthorizationTokenInput input) {
        return this.tracerWrapper.traceWithReturn(
                "createAuthorizationToken",
                span -> {
                    if (input == null)
                        throw new UseCaseInputCannotBeNullException(CreateAuthorizationTokenUseCase.class);

                    return switch (input) {
                        case AuthorizationCodeGrantInput codeGrantInput ->
                                handleAuthorizationCodeGrantType(codeGrantInput, span);
                        case RefreshTokenGrantInput refreshTokenGrantInput ->
                                handleRefreshTokenGrantType(refreshTokenGrantInput, span);
                        case ClientSecretGrantInput clientSecretGrantInput ->
                                handleClientSecretGrantType(clientSecretGrantInput, span);
                        default -> throw DomainException.with("The grant type is not supported in this endpoint");
                    };
                }
        );
    }

    private CreateAuthorizationTokenOutput handleAuthorizationCodeGrantType(final AuthorizationCodeGrantInput codeGrantInput, final ObservationContext trace) {
        final var aAuthorizationCode = trace.runInSpan(
                "authorization-code.retrieve",
                () -> this.authorizationCodeRepository
                        .authorizationCodeOfCode(codeGrantInput.code())
                        .orElseThrow(NotFoundException.with(AuthorizationCode.class, "code", codeGrantInput.code()))
        );

        trace.runInSpan(
                "authorization-code.validate",
                () -> validateAuthorizationCodeGrantType(codeGrantInput, aAuthorizationCode)
        );

        final var aReceivedCodeChallenge = PKCEUtils.generateCodeChallenge(codeGrantInput.codeVerifier());

        if (!aAuthorizationCode.getCodeChallenge().equals(aReceivedCodeChallenge)) {
            throw DomainException.with("The code verifier is invalid");
        }

        // TODO o access token jti devia ser hashed

        final var aAccessToken = trace.runInSpan(
                "token.generate.access-token",
                () -> generateToken(codeGrantInput.clientId(),
                        aAuthorizationCode.getUserId().value().toString(), AuthorizationTokenType.ACCESS_TOKEN)
        );
        final var aRefreshToken = trace.runInSpan(
                "token.generate.refresh-token",
                () -> generateToken(codeGrantInput.clientId(),
                        aAuthorizationCode.getUserId().value().toString(), AuthorizationTokenType.REFRESH_TOKEN)
        );

        trace.runInSpan(
                "token.save",
                () -> saveTokens(aAccessToken, aRefreshToken)
        );

        aAuthorizationCode.markAsUsed();
        trace.runInSpan(
                "authorization-code.mark-used",
                () -> this.authorizationCodeRepository.save(aAuthorizationCode)
        );

        return CreateAuthorizationTokenOutput.with(
                aAccessToken,
                aRefreshToken
        );
    }

    private CreateAuthorizationTokenOutput handleRefreshTokenGrantType(final RefreshTokenGrantInput refreshTokenGrantInput, final ObservationContext trace) {
        final var aRefreshHashed = PKCEUtils.generateCodeChallenge(refreshTokenGrantInput.refreshToken());
        final var aRefreshTokenStored = trace.runInSpan(
                "refresh-token.retrieve-by-jti",
                () -> this.authorizationTokenRepository.tokenOfJti(aRefreshHashed)
                        .orElseThrow(NotFoundException.with(AuthorizationToken.class, "jti", aRefreshHashed))
        );

        trace.runInSpan(
                "refresh-token.validate",
                () -> validateRefreshTokenGrantType(refreshTokenGrantInput, aRefreshTokenStored)
        );

        trace.runInSpan(
                "refresh-token.revoke",
                () -> this.authorizationTokenRepository.save(aRefreshTokenStored.revoke())
        ); // TODO check this

        final var aAccessToken = trace.runInSpan(
                "token.generate.access-token",
                () -> generateToken(refreshTokenGrantInput.clientId(),
                        aRefreshTokenStored.getUserId().orElse(refreshTokenGrantInput.clientId()), AuthorizationTokenType.ACCESS_TOKEN)
        );

        final var aRefreshToken = trace.runInSpan(
                "token.generate.refresh-token",
                () -> generateToken(refreshTokenGrantInput.clientId(),
                        aRefreshTokenStored.getUserId().orElse(refreshTokenGrantInput.clientId()), AuthorizationTokenType.REFRESH_TOKEN)
        );

        trace.runInSpan(
                "token.save",
                () -> saveTokens(aAccessToken, aRefreshToken)
        );

        return CreateAuthorizationTokenOutput.with(
                aAccessToken,
                aRefreshToken
        );
    }

    private CreateAuthorizationTokenOutput handleClientSecretGrantType(final ClientSecretGrantInput clientSecretGrantInput, final ObservationContext trace) {
        final var aClientId = clientSecretGrantInput.clientId();
        final var aClientSecret = clientSecretGrantInput.clientSecret();

        // TODO in future call the client repository to get client by clientId and check if clientId and secret match
        final var aOAuthClient = trace.runInSpan(
                "oauth-client.retrieve-by-client-id",
                () -> this.oAuthClientRepository
                        .clientOfClientId(aClientId)
                        .orElseThrow(() -> NotFoundException.with("Client not found"))
        );

        if (!aOAuthClient.clientSecret().equalsIgnoreCase(aClientSecret)) {
            throw DomainException.with("The client secret does not belong to the client");
        }

        final var aAccessToken = trace.runInSpan(
                "token.generate.access-token",
                () -> generateToken(aClientId, aClientId, AuthorizationTokenType.ACCESS_TOKEN)
        );
        final var aRefreshToken = trace.runInSpan(
                "token.generate.refresh-token",
                () -> generateToken(aClientId, aClientId, AuthorizationTokenType.REFRESH_TOKEN)
        );

        trace.runInSpan(
                "token.save",
                () -> saveTokens(aAccessToken, aRefreshToken)
        );

        return new CreateAuthorizationTokenOutput(
                aAccessToken,
                aRefreshToken
        );
    }

    private void validateAuthorizationCodeGrantType(
            final AuthorizationCodeGrantInput codeGrantInput,
            final AuthorizationCode aAuthorizationCode
    ) {
        if (aAuthorizationCode.isUsed()) {
            throw DomainException.with("The authorization code has already been used");
        }

        if (aAuthorizationCode.isExpired()) {
            throw DomainException.with("The authorization code has expired");
        }

        if (!aAuthorizationCode.getClientId().equalsIgnoreCase(codeGrantInput.clientId())) {
            throw DomainException.with("The authorization code does not belong to the client");
        }
    }

    private void validateRefreshTokenGrantType(
            final RefreshTokenGrantInput refreshTokenGrantInput,
            final AuthorizationToken aRefreshTokenStored
    ) {
        if (aRefreshTokenStored.isExpired()) {
            throw DomainException.with("The refresh token has expired");
        }

        if (aRefreshTokenStored.isRevoked()) {
            throw DomainException.with("The refresh token has been revoked");
        }

        if (!aRefreshTokenStored.getClientId().equalsIgnoreCase(refreshTokenGrantInput.clientId())) {
            throw DomainException.with("The refresh token does not belong to the client");
        }
    }

    private TokenGeneratorGateway.Token generateToken(
            final String clientId,
            final String sub,
            final AuthorizationTokenType type
    ) {
        return this.tokenGeneratorGateway.generateToken(new TokenGeneratorGateway.TokenInput(
                clientId,
                sub,
                type
        ));
    }

    private void saveTokens(
            final TokenGeneratorGateway.Token aAccessToken,
            final TokenGeneratorGateway.Token aRefreshToken
    ) {
        this.authorizationTokenRepository.save(AuthorizationToken.newAuthToken(
                aAccessToken.tokenJTI(),
                aAccessToken.type(),
                aAccessToken.expiresIn(),
                aAccessToken.issuedAt(),
                aAccessToken.clientId(),
                aAccessToken.sub()
        ));
        this.authorizationTokenRepository.save(AuthorizationToken.newAuthToken(
                aRefreshToken.tokenJTI(),
                aRefreshToken.type(),
                aRefreshToken.expiresIn(),
                aRefreshToken.issuedAt(),
                aRefreshToken.clientId(),
                aRefreshToken.sub()
        ));
    }
}
