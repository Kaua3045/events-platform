package com.kaua.events.platform.application.usecases.auth.token.create;

public sealed interface CreateAuthorizationTokenInput permits AuthorizationCodeGrantInput, ClientSecretGrantInput, InputForDefaultTest, RefreshTokenGrantInput {

    String grantType();

    String clientId();
}
