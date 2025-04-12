package com.kaua.events.platform.application.usecases.auth.code.create;

public record CreateAuthorizationCodeOutput(
        String code,
        String redirectUri
) {
}
