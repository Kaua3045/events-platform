package com.kaua.events.platform.application.usecases.auth.code.create;

public record CreateAuthorizationCodeInput(
        String clientId,
        String redirectUri,
        String codeChallenge,
        String codeChallengeMethod,
        String email,
        String password
) {
}
