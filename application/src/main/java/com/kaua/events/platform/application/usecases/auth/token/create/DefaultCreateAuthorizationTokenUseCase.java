package com.kaua.events.platform.application.usecases.auth.token.create;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.AuthorizationCodeRepository;
import com.kaua.events.platform.application.repositories.AuthorizationTokenRepository;
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
    private final TokenGeneratorGateway tokenGeneratorGateway;

    public DefaultCreateAuthorizationTokenUseCase(
            final AuthorizationTokenRepository authorizationTokenRepository,
            final AuthorizationCodeRepository authorizationCodeRepository,
            final TokenGeneratorGateway tokenGeneratorGateway
    ) {
        this.authorizationTokenRepository = Objects.requireNonNull(authorizationTokenRepository);
        this.authorizationCodeRepository = Objects.requireNonNull(authorizationCodeRepository);
        this.tokenGeneratorGateway = Objects.requireNonNull(tokenGeneratorGateway);
    }

    @Override
    public CreateAuthorizationTokenOutput execute(final CreateAuthorizationTokenInput input) {
        if (input == null) throw new UseCaseInputCannotBeNullException(CreateAuthorizationTokenUseCase.class);

        if (input.grantType().equals(AuthorizationCodeGrantInput.GRANT_TYPE) && input instanceof AuthorizationCodeGrantInput codeGrantInput) {
            final var aAuthorizationCode = this.authorizationCodeRepository
                    .authorizationCodeOfCode(codeGrantInput.code())
                    .orElseThrow(NotFoundException.with(AuthorizationCode.class, "code", codeGrantInput.code()));

            if (aAuthorizationCode.isUsed()) {
                throw DomainException.with("The authorization code has already been used");
            }

            if (aAuthorizationCode.isExpired()) {
                throw DomainException.with("The authorization code has expired");
            }

            if (!aAuthorizationCode.getClientId().equalsIgnoreCase(codeGrantInput.clientId())) {
                throw DomainException.with("The authorization code does not belong to the client");
            }

            final var aReceivedCodeVerifier = PKCEUtils.generateCodeChallenge(codeGrantInput.codeVerifier());

            if (!aAuthorizationCode.getCodeChallenge().equals(aReceivedCodeVerifier)) {
                throw DomainException.with("The code verifier is invalid");
            }

            final var aAccessToken = this.tokenGeneratorGateway.generateToken(new TokenGeneratorGateway.TokenInput(
                    codeGrantInput.clientId(),
                    aAuthorizationCode.getUserId().value().toString(),
                    AuthorizationTokenType.ACCESS_TOKEN
            ));

            final var aRefreshToken = this.tokenGeneratorGateway.generateToken(new TokenGeneratorGateway.TokenInput(
                    codeGrantInput.clientId(),
                    aAuthorizationCode.getUserId().value().toString(),
                    AuthorizationTokenType.REFRESH_TOKEN
            ));

            final var aTokensStoredForSub = this.authorizationTokenRepository.tokensOfSub(aAuthorizationCode.getUserId().value().toString());

            if (aTokensStoredForSub.isEmpty()) {
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
            } else {
                aTokensStoredForSub.forEach(token -> {
                    if (token.getType().equals(AuthorizationTokenType.ACCESS_TOKEN)) {
                        this.authorizationTokenRepository.save(AuthorizationToken.with(
                                token.getId(),
                                token.getVersion(),
                                aAccessToken.tokenJTI(),
                                aAccessToken.type(),
                                aAccessToken.expiresIn(),
                                aAccessToken.issuedAt(),
                                token.isRevoked(),
                                token.getClientId(),
                                token.getUserId().orElse(token.getClientId()) // TODO verify if this is correct
                        ));
                    } else {
                        this.authorizationTokenRepository.save(AuthorizationToken.with(
                                token.getId(),
                                token.getVersion(),
                                aRefreshToken.tokenJTI(),
                                aRefreshToken.type(),
                                aRefreshToken.expiresIn(),
                                aRefreshToken.issuedAt(),
                                token.isRevoked(),
                                token.getClientId(),
                                token.getUserId().orElse(token.getClientId()) // TODO verify if this is correct
                        ));
                    }
                });
            }

            aAuthorizationCode.markAsUsed();
            this.authorizationCodeRepository.save(aAuthorizationCode);

            return CreateAuthorizationTokenOutput.with(
                    aAccessToken,
                    aRefreshToken
            );
        } else if (input.grantType().equals(RefreshTokenGrantInput.GRANT_TYPE) && input instanceof RefreshTokenGrantInput refreshTokenGrantInput) {
            final var aRefreshHashed = PKCEUtils.sha256Base64(refreshTokenGrantInput.refreshToken());
            final var aRefreshTokenStored = this.authorizationTokenRepository.tokenOfJti(aRefreshHashed)
                    .orElseThrow(NotFoundException.with(AuthorizationToken.class, "jti", aRefreshHashed));

            if (aRefreshTokenStored.isExpired()) {
                throw DomainException.with("The refresh token has expired");
            }

            if (aRefreshTokenStored.isRevoked()) {
                throw DomainException.with("The refresh token has been revoked");
            }

            if (!aRefreshTokenStored.getClientId().equalsIgnoreCase(refreshTokenGrantInput.clientId())) {
                throw DomainException.with("The refresh token does not belong to the client");
            }

            final var aAccessToken = this.tokenGeneratorGateway.generateToken(new TokenGeneratorGateway.TokenInput(
                    refreshTokenGrantInput.clientId(),
                    aRefreshTokenStored.getUserId().orElse(refreshTokenGrantInput.clientId()), // TODO ver se isso faz sentido
                    AuthorizationTokenType.ACCESS_TOKEN
            ));

            final var aRefreshToken = this.tokenGeneratorGateway.generateToken(new TokenGeneratorGateway.TokenInput(
                    refreshTokenGrantInput.clientId(),
                    aRefreshTokenStored.getUserId().orElse(refreshTokenGrantInput.clientId()), // TODO ver se isso faz sentido
                    AuthorizationTokenType.REFRESH_TOKEN
            ));

            // TODO verify this
            final var aTokensStoredForSub = this.authorizationTokenRepository.tokensOfSub(aRefreshTokenStored.getUserId().orElse(aRefreshTokenStored.getClientId()));

            if (aTokensStoredForSub.isEmpty()) {
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
            } else {
                aTokensStoredForSub.forEach(token -> {
                    if (token.getType().equals(AuthorizationTokenType.ACCESS_TOKEN)) {
                        this.authorizationTokenRepository.save(AuthorizationToken.with(
                                token.getId(),
                                token.getVersion(),
                                aAccessToken.tokenJTI(),
                                aAccessToken.type(),
                                aAccessToken.expiresIn(),
                                aAccessToken.issuedAt(),
                                token.isRevoked(),
                                token.getClientId(),
                                token.getUserId().orElse(token.getClientId()) // TODO verify if this is correct
                        ));
                    } else {
                        this.authorizationTokenRepository.save(AuthorizationToken.with(
                                token.getId(),
                                token.getVersion(),
                                aRefreshToken.tokenJTI(),
                                aRefreshToken.type(),
                                aRefreshToken.expiresIn(),
                                aRefreshToken.issuedAt(),
                                token.isRevoked(),
                                token.getClientId(),
                                token.getUserId().orElse(token.getClientId()) // TODO verify if this is correct
                        ));
                    }
                });
            }

            return CreateAuthorizationTokenOutput.with(
                    aAccessToken,
                    aRefreshToken
            );
        }

        throw DomainException.with("The grant type is not supported in this endpoint");
    }
}
