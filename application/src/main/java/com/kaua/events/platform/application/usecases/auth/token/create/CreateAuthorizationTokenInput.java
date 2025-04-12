package com.kaua.events.platform.application.usecases.auth.token.create;

public sealed interface CreateAuthorizationTokenInput permits
        AuthorizationCodeGrantInput, RefreshTokenGrantInput {

    String grantType();
    String clientId();
}
