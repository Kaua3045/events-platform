package com.kaua.events.platform.domain.auth;

import com.kaua.events.platform.domain.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class OAuthClientTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallNewOAuthClient_thenReturnOAuthClient() {
        final var clientId = "clientId";
        final var clientSecret = "clientSecret";
        final var redirectUri = "redirectUri";
        final var publicClient = true;
        final var accessTokenTTL = 3600;
        final var accessTokenTTLTimeUnit = "seconds";
        final var refreshTokenTTL = 7200;
        final var refreshTokenTTLTimeUnit = "seconds";
        final var authorities = List.of("authority1", "authority2");

        final var oAuthClient = new OAuthClient(
                clientId,
                clientSecret,
                redirectUri,
                publicClient,
                accessTokenTTL,
                accessTokenTTLTimeUnit,
                refreshTokenTTL,
                refreshTokenTTLTimeUnit,
                authorities
        );

        Assertions.assertNotNull(oAuthClient);
        Assertions.assertEquals(clientId, oAuthClient.clientId());
        Assertions.assertEquals(clientSecret, oAuthClient.clientSecret());
        Assertions.assertEquals(redirectUri, oAuthClient.redirectUri());
        Assertions.assertEquals(publicClient, oAuthClient.publicClient());
        Assertions.assertEquals(accessTokenTTL, oAuthClient.accessTokenTTL());
        Assertions.assertEquals(accessTokenTTLTimeUnit, oAuthClient.accessTokenTTLTimeUnit());
        Assertions.assertEquals(refreshTokenTTL, oAuthClient.refreshTokenTTL());
        Assertions.assertEquals(refreshTokenTTLTimeUnit, oAuthClient.refreshTokenTTLTimeUnit());
        Assertions.assertEquals(authorities, oAuthClient.authorities());
    }
}
