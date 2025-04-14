package com.kaua.events.platform.infrastructure.oauth;

import java.util.List;

public record OAuthClient(
        String clientId,
        String clientSecret,
        String redirectUri,
        boolean publicClient,
        int accessTokenTTL,
        String accessTokenTTLTimeUnit,
        int refreshTokenTTL,
        String refreshTokenTTLTimeUnit,
        List<String> authorities
) {
}
