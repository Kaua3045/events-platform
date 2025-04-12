package com.kaua.events.platform.application.usecases.auth.token.create;

public final class RefreshTokenGrantInput implements CreateAuthorizationTokenInput {

    public static final String GRANT_TYPE = "refresh_token";

    private final String clientId;
    private final String refreshToken;

    public RefreshTokenGrantInput(final String clientId, final String refreshToken) {
        this.clientId = clientId;
        this.refreshToken = refreshToken;
    }

    @Override
    public String grantType() {
        return GRANT_TYPE;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    public String refreshToken() {
        return refreshToken;
    }
}
