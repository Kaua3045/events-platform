package com.kaua.events.platform.infrastructure.configurations.authentication.client;

public interface AuthenticationGateway {

    AuthenticationResult login(ClientCredentialsInput input);

    record AuthenticationResult(String accessToken) {}

    record ClientCredentialsInput(String clientId, String clientSecret) {}
}
