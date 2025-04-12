package com.kaua.events.platform.application.usecases.auth.token.create;

public final class AuthorizationCodeGrantInput implements CreateAuthorizationTokenInput {

    public static final String GRANT_TYPE = "authorization_code";

    private final String clientId;
    private final String code;
    private final String codeVerifier;

    public AuthorizationCodeGrantInput(
            final String clientId,
            final String aCode,
            final String aCodeVerifier
    ) {
        this.clientId = clientId;
        this.code = aCode;
        this.codeVerifier = aCodeVerifier;
    }

    @Override
    public String grantType() {
        return GRANT_TYPE;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    public String code() {
        return code;
    }

    public String codeVerifier() {
        return codeVerifier;
    }
}
