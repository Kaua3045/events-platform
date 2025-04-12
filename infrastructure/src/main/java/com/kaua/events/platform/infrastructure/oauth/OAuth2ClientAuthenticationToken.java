package com.kaua.events.platform.infrastructure.oauth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Collection;

public class OAuth2ClientAuthenticationToken extends AbstractAuthenticationToken {

    private final String clientId;
    private final String clientSecret;
    private final ClientAuthenticationMethod clientAuthenticationMethod;

    public OAuth2ClientAuthenticationToken(
            String clientId,
            ClientAuthenticationMethod clientAuthenticationMethod,
            String clientSecret,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.clientId = clientId;
        this.clientAuthenticationMethod = clientAuthenticationMethod;
        this.clientSecret = clientSecret;
    }

    @Override
    public Object getCredentials() {
        return this.clientSecret;
    }

    @Override
    public Object getPrincipal() {
        return this.clientId;
    }

    public ClientAuthenticationMethod getClientAuthenticationMethod() {
        return clientAuthenticationMethod;
    }
}
