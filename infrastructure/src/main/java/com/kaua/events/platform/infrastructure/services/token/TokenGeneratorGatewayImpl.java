package com.kaua.events.platform.infrastructure.services.token;

import com.kaua.events.platform.application.gateways.TokenGeneratorGateway;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.User;
import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.PKCEUtils;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import com.kaua.events.platform.infrastructure.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class TokenGeneratorGatewayImpl implements TokenGeneratorGateway {

    private static final Logger log = LoggerFactory.getLogger(TokenGeneratorGatewayImpl.class);

    private final JwtEncoder jwtEncoder;
    private final OAuthClients oAuthClients;
    private final UserRepository userRepository;

    public TokenGeneratorGatewayImpl(
            final JwtEncoder jwtEncoder,
            final OAuthClients oAuthClients,
            final UserRepository userRepository
    ) {
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder);
        this.oAuthClients = Objects.requireNonNull(oAuthClients);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Override
    public Token generateToken(final TokenInput input) {
        if (input == null) throw InternalErrorException.with("input token generator cannot be null");

        final var aOAuthClient = this.oAuthClients.getClients().get(input.clientId());

        if (input.type().equals(AuthorizationTokenType.ACCESS_TOKEN)) {
            log.debug("Generating access token for [clientId:{}] [sub:{}]", input.clientId(), input.sub());
            final var aNow = InstantUtils.now();
            final var aAccessTokenTTL = aOAuthClient.accessTokenTTL();
            final var aAccessTokenTTLUnit = ChronoUnit.valueOf(aOAuthClient.accessTokenTTLTimeUnit());
            final var aExpiresAt = InstantUtils.now().plus(aAccessTokenTTL, aAccessTokenTTLUnit);

            boolean isService = input.sub().equals(input.clientId());

            final var aClaims = JwtClaimsSet.builder()
                    .issuer(aOAuthClient.issuer())
                    .id(UUID.randomUUID().toString())
                    .issuedAt(aNow)
                    .expiresAt(aExpiresAt)
                    .subject(input.sub())
                    .claim("client_id", input.clientId())
                    .claim(Constants.JWT_CLAIM_TYPE, isService ?
                            Constants.SERVICE_TYPE : Constants.USER_TYPE)
                    .claim(Constants.JWT_AUTHORITIES, isService ?
                            aOAuthClient.authorities() : List.of(this.userRepository.userOfId(input.sub()).orElseThrow(
                            NotFoundException.with(User.class, input.sub())
                    ).getRole()))
                    .build();

            final var aJwt = this.jwtEncoder.encode(JwtEncoderParameters.from(aClaims));

            log.debug("Generated access token [tokenHash:{}] [expiresIn:{}]", aJwt.getId(), aExpiresAt); // TODO aqui deveria salvar o hash

            return new Token(
                    aJwt.getTokenValue(),
                    aJwt.getId(),
                    AuthorizationTokenType.ACCESS_TOKEN,
                    input.clientId(),
                    input.sub(),
                    aExpiresAt,
                    aNow
            );
        }

        // TODO assume refresh token
        log.debug("Generating refresh token for [clientId:{}] [sub:{}]", input.clientId(), input.sub());
        final var aNow = InstantUtils.now();
        final var aRefreshTokenTTL = aOAuthClient.refreshTokenTTL();
        final var aRefreshTokenTTLUnit = ChronoUnit.valueOf(aOAuthClient.refreshTokenTTLTimeUnit());
        final var aExpiresAt = InstantUtils.now().plus(aRefreshTokenTTL, aRefreshTokenTTLUnit);

        final var aToken = IdentifierUtils.generateNewIdWithoutHyphen();
        final var aTokenHash = PKCEUtils.generateCodeChallenge(aToken);

        log.debug("Generated refresh token [tokenHash:{}] [expiresIn:{}]", aTokenHash, aExpiresAt);

        return new Token(
                aToken,
                aTokenHash,
                AuthorizationTokenType.REFRESH_TOKEN,
                input.clientId(),
                input.sub(),
                aExpiresAt,
                aNow
        );
    }
}
