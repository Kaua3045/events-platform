package com.kaua.events.platform.application.usecases.auth.token.create;

public final class ClientSecretGrantInput implements CreateAuthorizationTokenInput {

    public static final String GRANT_TYPE = "client_credentials";

    private final String clientId;
    private final String clientSecret;

    public ClientSecretGrantInput(final String clientId, final String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String grantType() {
        return GRANT_TYPE;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }
}
