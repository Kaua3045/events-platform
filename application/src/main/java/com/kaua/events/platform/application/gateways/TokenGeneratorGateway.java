package com.kaua.events.platform.application.gateways;

import com.kaua.events.platform.domain.auth.token.AuthorizationTokenType;

import java.time.Instant;

public interface TokenGeneratorGateway {

    Token generateToken(TokenInput input);

    public record Token(
            String tokenValue,
            String tokenJTI,
            AuthorizationTokenType type,
            String clientId,
            String sub,
            Instant expiresIn,
            Instant issuedAt
    ) {
    }

    public record TokenInput(
            String clientId,
            String sub,
            AuthorizationTokenType type
    ) {
    }
}
